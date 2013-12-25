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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class ApacheRequest extends AbstractRequest
{
	private DefaultHttpClient httpClient = new DefaultHttpClient();

	public static Builder<ApacheRequest> builder()
	{
		return new Builder<ApacheRequest>(new ApacheRequest());
	}

	@Override
	public <T> T sync()
	{
		T response = null;

		try
		{
			HttpUriRequest uriRequest = null;

			if (METHOD_POST.equals(this.method))
			{
				uriRequest = new HttpPost(this.url);
			}
			else
			{
				uriRequest = new HttpGet(this.url);
			}

			for (String name : this.headers.keySet())
			{
				for (String value : this.headers.get(name))
				{
					uriRequest.addHeader(name, value);
				}
			}
			
			if(uriRequest instanceof HttpPost)
			{
				HttpPost httpPost = (HttpPost)uriRequest;
				
				if(this.bodyBytes != null)
				{
					httpPost.setEntity(new ByteArrayEntity(bodyBytes));
				}
				else if(bodyFile != null)
				{
					httpPost.setEntity(new FileEntity(bodyFile, null));
				}
				else if(bodyParts.size() > 0)
				{
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					String boundary = writeMultiPart(outputStream, bodyParts);
					httpPost.addHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
					httpPost.setEntity(new ByteArrayEntity(outputStream.toByteArray()));
				}
			}
			
			HttpResponse httpResponse = httpClient.execute(uriRequest);

			InputStream stream = httpResponse.getEntity().getContent();
			response = (T) this.responseHandler.handle(httpResponse.getStatusLine().getStatusCode(), httpResponse
					.getStatusLine().getReasonPhrase(), null, stream);
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
