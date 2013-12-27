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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Volodymyr
 * <pre>
 * <code>
 *	 WebRequest.builder()
 *		.scheme("https")
 *		.target("api.github.com")
 *		.path("/repos/zVolodymyr")
 *		.path("/android-utils/commits")
 *		.handler(Handlers.STRING)
 *		.onAny(new WebRequest.Callback{@code <String>}() 
 *		{
 *			public void on(String value, Throwable error) 
 *			{
 *				Log.i(value);
 *			}
 *		})
 *		.async();   
 * </code>
 * </pre>
 */
public class WebRequest extends AbstractRequest
{	
	public static Builder<WebRequest> builder()
	{
		return new Builder<WebRequest>(new WebRequest());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T sync()
	{
		T response = null;

		try
		{
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

			for (String name : this.headers.keySet())
			{
				for (String value : this.headers.get(name))
				{
					conn.addRequestProperty(name, value);
				}
			}

			conn.setRequestMethod(this.method);

			if (METHOD_POST.equals(this.method))
			{
				conn.setDoOutput(true);

				if (bodyBytes != null)
				{
					OutputStream output = conn.getOutputStream();
					output.write(bodyBytes);
					output.close();
				}
				else if (bodyFile != null)
				{
					OutputStream output = conn.getOutputStream();
					FileInputStream fileStream = new FileInputStream(bodyFile);
					copy(fileStream, output, 2048);
					fileStream.close();
					output.close();
				}
				else if (bodyParts.size() != 0)
				{
					final String boundary = randomBoundary(30, 40);
					conn.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
					OutputStream output = conn.getOutputStream();
					writeMultiPart(output, bodyParts, boundary);
					output.close();
				}
			}

			conn.connect();
			
			InputStream stream = conn.getInputStream();
			response = (T) this.responseHandler.handle(conn.getResponseCode(), conn.getResponseMessage(), conn.getHeaderFields(), stream);
			stream.close();

			if (this.onSuccess != null)
			{
				((Callback<T>) this.onSuccess).on(response, null);
			}
		}
		catch (Exception e)
		{
			if (this.onFailure != null)
			{
				((Callback<T>) this.onSuccess).on(response, e);
			}
		}

		return response;
	}
}
