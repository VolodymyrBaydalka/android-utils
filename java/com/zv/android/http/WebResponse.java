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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class WebResponse<T>
{
	public static class DefaultHandler<T> implements AbstractRequest.Handler<WebResponse<T>>
	{
		private final AbstractRequest.Handler<T> _contentHandler;
		
		public DefaultHandler(AbstractRequest.Handler<T> handler)
		{
			_contentHandler = handler;
		}

		@Override
		public WebResponse<T> handle(int status, String reason, Map<String, List<String>> headers, InputStream stream)
		{
			WebResponse<T> response = new WebResponse<T>();

			response.headers = headers;
			response.status = status;
			response.reason = reason;
			response.data = _contentHandler.handle(status, reason, headers, stream);

			return response;
		}		
	}
	
	public int status;
	public String reason;
	public Map<String, List<String>> headers;
	public T data;
}
