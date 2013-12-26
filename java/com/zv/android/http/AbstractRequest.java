/**
 * Copyright 2013 Volodymyr Baydalka.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
  */
 
 package com.zv.android.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.util.Base64;

public abstract class AbstractRequest
{
	public final static String SCHEME_HTTP = "http";
	public final static String SCHEME_HTTPS = "https";

	public final static String METHOD_GET = "GET";
	public final static String METHOD_POST = "POST";

	public final static String HEADER_AUTHORIZATION = "Authorization";
	public final static String HEADER_ACCEPT = "Accept";
	public final static String HEADER_ACCEPT_CHARSET = "Accept-Charset";

	protected final static ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);

	protected String method = METHOD_GET;
	protected String url;
	protected Map<String, List<String>> headers = new Hashtable<String, List<String>>();
	protected byte[] bodyBytes = null;
	protected File bodyFile = null;
	protected ArrayList<BodyPart> bodyParts = new ArrayList<BodyPart>();

	protected Callback<?> onSuccess = null;
	protected Callback<?> onFailure = null;
	protected Handler<?> responseHandler = Handlers.OK;

	protected ExecutorService executorService = DEFAULT_EXECUTOR_SERVICE;

	public abstract <T> T sync();
	
	public <T> Future<T> async()
	{
		return executorService.submit(new Callable<T>()
		{
			@Override
			public T call() throws Exception
			{
				return sync();
			}
		});
	}

	protected static void copy(InputStream input, OutputStream ouput, int buffer) throws IOException
	{
		byte[] buf = new byte[buffer];
		int len = 0;

		while ((len = input.read(buf)) != 0)
		{
			ouput.write(buf, 0, len);
		}
	}

	protected static String writeMultiPart(OutputStream output, List<BodyPart> bodyParts) throws IOException
	{
		final String boundary = randomBoundary();

		OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");

		for (int i = 0; i < bodyParts.size(); i++)
		{
			BodyPart part = bodyParts.get(i);

			writer.append("--").append(boundary).append("\r\n");
			writer.append("Content-Disposition: form-data; name=\"").append(part.name).append("\";");

			if (part.filename != null)
				writer.append(part.filename);

			writer.append("\r\n").append("Content-Type: ").append(part.type).append("\r\n").append("\r\n")
					.flush();

			if (part.bytes != null)
			{
				output.write(part.bytes);
			}
			else if (part.file != null)
			{
				FileInputStream fileStream = new FileInputStream(part.file);
				copy(fileStream, output, 2048);
				fileStream.close();
			}

			writer.append("\r\n").flush();
		}

		writer.append("--").append(boundary).append("--").append("\r\n").flush();
		
		return boundary;
	}
	
	protected static String randomBoundary()
	{
		Random rand = new Random();
		String chars = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuilder builder = new StringBuilder();

		for (int i = 0, l = 30 + rand.nextInt(10); i < l; i++)
		{
			builder.append(chars.charAt(rand.nextInt(chars.length())));
		}

		return builder.toString();
	}

	public interface Callback<T>
	{
		void on(T value, Throwable error);
	}

	public interface Handler<T>
	{
		T handle(int status, String reason, Map<String, List<String>> headers, InputStream stream);
	}

	public static class Builder<TRequest extends AbstractRequest>
	{
		private final TRequest request;

		private String scheme = SCHEME_HTTP;
		private String target;
		private StringBuilder path = new StringBuilder();
		private StringBuilder query = new StringBuilder();
		
		protected Builder(TRequest request)
		{
			this.request = request;
		}

		public Builder<TRequest> scheme(String scheme)
		{
			this.scheme = scheme;
			return this;
		}

		public Builder<TRequest> target(String target)
		{
			this.target = target;
			return this;
		}

		public Builder<TRequest> path(String path)
		{
			this.path.append(path);
			return this;
		}

		public Builder<TRequest> query(String... values)
		{
			for (int i = 0; i < values.length; i += 2)
			{
				query(values[i], values[i + 1]);
			}

			return this;
		}

		public Builder<TRequest> query(String name, Object value)
		{
			if (this.query.length() != 0)
				this.query.append("&");

			this.query.append(name).append("=").append(value);

			return this;
		}
		
		public Builder<TRequest> method(String method)
		{
			request.method = method;
			return this;
		}

		public Builder<TRequest> header(String name, String...values)
		{
			List<String> existsValues = this.request.headers.get(name);

			if(existsValues == null)
			{
				existsValues = new ArrayList<String>();
				this.request.headers.put(name, existsValues);
			}			
			
			for (String value : values)
			{
				existsValues.add(value);
			}
				
			return this;
		}

		public Builder<TRequest> accept(String value)
		{
			return header(HEADER_ACCEPT, value);
		}

		public Builder<TRequest> charset(String value)
		{
			return header(HEADER_ACCEPT_CHARSET, value);
		}

		public Builder<TRequest> basicAuth(String login, String password)
		{
			try
			{
				byte[] loginData = (login + ":" + password).getBytes("UTF-8");
				String token = "Basic " + Base64.encodeToString(loginData, Base64.NO_WRAP);
				return header(HEADER_AUTHORIZATION, token);
			}
			catch (UnsupportedEncodingException e)
			{
				throw new RuntimeException(e);
			}
		}

		public Builder<TRequest> body(String... params)
		{
			StringBuilder builder = new StringBuilder();

			for (int i = 0; i < params.length; i++)
			{
				if (builder.length() > 0)
					builder.append("&");

				builder.append(params[i]);
				builder.append("=");
				builder.append(params[i + 1]);
			}

			return this;
		}

		public Builder<TRequest> body(String text)
		{
			try
			{
				request.bodyBytes = text.getBytes("UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				throw new RuntimeException(e);
			}

			return this;
		}

		public Builder<TRequest> body(byte[] data)
		{
			request.bodyBytes = data;
			return this;
		}

		public Builder<TRequest> body(File file)
		{
			request.bodyFile = file;
			return this;
		}

		public Builder<TRequest> part(String name, File file, String type)
		{
			BodyPart bodyPart = new BodyPart();
			bodyPart.name = name;
			bodyPart.filename = file.getName();
			bodyPart.type = type;
			bodyPart.file = file;
			request.bodyParts.add(bodyPart);

			return this;
		}

		public Builder<TRequest> part(String name, byte[] data)
		{
			return part(name, data, "application/octet-stream");
		}

		public Builder<TRequest> part(String name, byte[] data, String type)
		{
			BodyPart bodyPart = new BodyPart();
			bodyPart.name = name;
			bodyPart.type = type;
			bodyPart.bytes = data;
			request.bodyParts.add(bodyPart);

			return this;
		}

		public Builder<TRequest> onSuccess(Callback<?> callback)
		{
			this.request.onSuccess = callback;
			return this;
		}

		public Builder<TRequest> onFailure(Callback<?> callback)
		{
			this.request.onFailure = callback;
			return this;
		}

		public Builder<TRequest> onAny(Callback<?> callback)
		{
			this.request.onSuccess = callback;
			this.request.onFailure = callback;
			return this;
		}

		public Builder<TRequest> executor(ExecutorService service)
		{
			this.request.executorService = service;
			return this;
		}

		public Builder<TRequest> handler(Handler<?> handler)
		{
			this.request.responseHandler = handler;
			return this;
		}

		public AbstractRequest build()
		{
			AbstractRequest result = this.request;
			
			StringBuilder urlBuilder = new StringBuilder(this.scheme).append("://").append(this.target);
			
			if(this.path.length() > 0)
				urlBuilder.append(this.path.toString());
			
			if(this.query.length() > 0)
			{
				urlBuilder.append("?");
				urlBuilder.append(this.query.toString());
			}

			result.url = urlBuilder.toString();

			return result;
		}

		public <T> T sync()
		{
			return build().sync();
		}

		public <T> Future<T> async()
		{
			return build().async();
		}
	}

	protected static class BodyPart
	{
		public String name;
		public String filename;
		public String type;
		public byte[] bytes;
		public File file;
	}
}
