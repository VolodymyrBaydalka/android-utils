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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Volodymyr
 * Contains default response handlers
 */
public final class Handlers
{
	private Handlers()
	{

	}

	/**
	 * Returns true if status code is 200 
	 */
	public final static AbstractRequest.Handler<Boolean> OK = new AbstractRequest.Handler<Boolean>()
	{
		@Override
		public Boolean handle(int status, String reason, Map<String, List<String>> headers, InputStream stream)
		{
			return Boolean.valueOf(status == 200);
		}
	};

	/**
	 * Returns text from body 
	 */
	public final static AbstractRequest.Handler<String> STRING = new AbstractRequest.Handler<String>()
	{
		@Override
		public String handle(int status, String reason, Map<String, List<String>> headers, InputStream stream)
		{
			try
			{
				StringBuilder text = new StringBuilder();
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				String line;

				while ((line = reader.readLine()) != null)
				{
					text.append(line);
				}

				return text.toString();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	};

	/**
	 * Returns bytes from body 
	 */
	public final static AbstractRequest.Handler<byte[]> BYTES = new AbstractRequest.Handler<byte[]>()
	{
		@Override
		public byte[] handle(int status, String reason, Map<String, List<String>> headers, InputStream stream)
		{
			try
			{
				byte[] buf = new byte[4096];
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream(stream.available());
				int len = 0;

				while ((len = stream.read(buf)) != 0)
				{
					outputStream.write(buf, 0, len);
				}

				return outputStream.toByteArray();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	};

	/**
	 * Returns JSONObject from body 
	 */
	public final static AbstractRequest.Handler<JSONObject> JSON = new AbstractRequest.Handler<JSONObject>()
	{
		@Override
		public JSONObject handle(int status, String reason, Map<String, List<String>> headers, InputStream stream)
		{
			try
			{
				return new JSONObject(STRING.handle(status, reason, headers, stream));
			}
			catch (JSONException e)
			{
				throw new RuntimeException(e);
			}
		}
	};
}
