android-utils
=============

Helper classes to build android applications

http package
=============
Most useful thing there is request builder.
###exemple: 
```java
WebRequest.builder()
	.target("www.google.com")
	.handler(Handlers.STRING)
	.onAny(new Callback<String>()
		{ 
			@Override
			public void handle(String value, Throwable error)
			{
				Log.i(value);
			}
		})
	.async();
```
###reference:
```
	WebRequest.builder() - creation
	.scheme(String) - sets url scheme (default - http)
	.target(String) - sets middle part of url (domain, port)
	.path(String) - appends path part
	.query(String,String) - adds queriy name-value pair to url
	.method(String) - sets request method (default - GET)
	.header(String,String) - adds header property
	.charset(String) - adds Accept-Charset property value
	.accept(String) - adds Accept property value
	.basicAuth(String,String) - adds Authorization property with basic authorization
	.body(byte[]/String/File) - sets body for POST method
	.part(String,byte[]) - sets part for multi-part request
	.executor(ExecutorService) - sets executor service for async request
	.handler(Handler<T>) - sets handler to handle request response (default - Handles.OK)
	.onSuccess(Callback<T>) - sets listener that will called on success 
	.onFailure(Callback<T>) - sets listener that will called on failure 
	.onAny(Callback<T>) - sets instance for failure and success
	.sync() - send sync request and returns response
	.async() - send async request and Future task
	.build() - return request 
```

drawable package
=============
###exemple: 
```java
Drawable progressDrawable = getResources().getDrawable(R.drawable.bg_progress);
ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressbar);
progressBar.setProgressDrawable(new RadialClipDrawable(progressDrawable));
progressBar.setProgress(65);
```

License
=======

    Copyright 2013 Volodymyr Baydalka

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.