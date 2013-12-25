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

/**
 * @author Volodymyr
 * Package contains helper method to make web request. Example:
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