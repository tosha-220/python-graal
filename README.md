Spring Boot application based on graalvm-ce:20.1.0-java11.
Application has 3 endpoints:
1) `/test/python/{text} with not required boolean parameter `oneContext` (use oneContext = true only in case of mix endpoint).
2) `/test/js/{text}` with not required boolean parameter `oneContext` (use oneContext = true only in case of mix endpoint).
3) `/test/mix/{text}` with required boolean parameter `oneContext`. This endpoint reuse another ones and if 
'oneContext' = true application will use one context for js and python. 
In case of 'oneContext' = false application will use separate contexts for js and python.
{text} path variable is some text which will be injected into js/python scripts to make it unique.

So call `/test/js/{text}?oneContext=false` to test memory leaks in API with only js,

call `/test/python/{text}?oneContext=false` to test memory leaks in API with only python,

call `/test/mix/{text}?oneContext=false` to test memory leaks in API with python and js in separate contexts

call `/test/mix/{text}?oneContext=true` to test memory leaks in API with python and js in single contexts

Note that calling `/test/js/{text}?oneContext=true` or `/test/python/{text}?oneContext=true` will throw exception