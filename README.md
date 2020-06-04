# httplog

log request and response for http

## Usage 

```
@Configuration
public class ReqRspLogConfig extends com.github.gobars.httplog.ReqRspLogFilter {}
```

## Examples

### Method GET

```log
2020-06-04 20:43:31.665  INFO 81103 --- [o-auto-1-exec-1] c.github.gobars.httplog.ReqRspLogFilter  : req: Req(super=ReqRsp(headers={host=localhost:57413, connection=keep-alive, accept=text/plain, application/json, application/*+json, */*, user-agent=Java/11.0.7}, startNs=13514944034314, tookMs=0, bodyBytes=0, body=, error=null), method=GET, requestUri=/test/10, protocol=HTTP/1.1)
2020-06-04 20:43:31.669  INFO 81103 --- [o-auto-1-exec-1] c.github.gobars.httplog.ReqRspLogFilter  : rsp: Rsp(super=ReqRsp(headers={Keep-Alive=timeout=60, Connection=keep-alive, Content-Length=12, Date=Thu, 04 Jun 2020 12:43:31 GMT, Content-Type=text/plain;charset=UTF-8}, startNs=13514944034314, tookMs=51, bodyBytes=12, body=test id : 10, error=null), status=200, reasonPhrase=OK)
```

### Method POST

```log
2020-06-04 20:43:31.900  INFO 81103 --- [o-auto-1-exec-2] c.github.gobars.httplog.ReqRspLogFilter  : req: Req(super=ReqRsp(headers={content-length=9, host=localhost:57413, content-type=application/json, connection=keep-alive, accept=application/json, application/*+json, user-agent=Java/11.0.7}, startNs=13515206496154, tookMs=0, bodyBytes=9, body={"id":10}, error=null), method=POST, requestUri=/test, protocol=HTTP/1.1)
2020-06-04 20:43:31.900  INFO 81103 --- [o-auto-1-exec-2] c.github.gobars.httplog.ReqRspLogFilter  : rsp: Rsp(super=ReqRsp(headers={Keep-Alive=timeout=60, Connection=keep-alive, Content-Length=9, Date=Thu, 04 Jun 2020 12:43:31 GMT, Content-Type=application/json}, startNs=13515206496154, tookMs=29, bodyBytes=9, body={"id":10}, error=null), status=200, reasonPhrase=OK)
```

### Method PUT exception

```log
2020-06-04 20:43:31.917  INFO 81103 --- [o-auto-1-exec-3] c.github.gobars.httplog.ReqRspLogFilter  : req: Req(super=ReqRsp(headers={content-length=9, host=localhost:57413, content-type=application/json, connection=keep-alive, accept=application/json, application/*+json, user-agent=Java/11.0.7}, startNs=13515248933063, tookMs=0, bodyBytes=9, body={"id":10}, error=null), method=PUT, requestUri=/test, protocol=HTTP/1.1)
2020-06-04 20:43:31.917  INFO 81103 --- [o-auto-1-exec-3] c.github.gobars.httplog.ReqRspLogFilter  : rsp: Rsp(super=ReqRsp(headers=null, startNs=13515248933063, tookMs=4, bodyBytes=0, body=null, error=org.springframework.web.util.NestedServletException: Request processing failed; nested exception is com.github.gobars.httplog.ex.TestException: TestDto(id=10)
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014)
	at org.springframework.web.servlet.FrameworkServlet.doPut(FrameworkServlet.java:920)
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:663)
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:883)
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:741)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:231)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at com.github.gobars.httplog.ReqRspLogFilter.doFilterInternal(ReqRspLogFilter.java:56)
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:202)
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:96)
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:541)
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:139)
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92)
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:343)
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:373)
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:65)
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:868)
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1590)
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
	at java.base/java.lang.Thread.run(Thread.java:834)
Caused by: com.github.gobars.httplog.ex.TestException: TestDto(id=10)
	at com.github.gobars.httplog.ctl.TestController.error(TestController.java:40)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:190)
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:138)
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:105)
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:879)
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:793)
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1040)
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:943)
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1006)
	... 41 more
), status=500, reasonPhrase=Internal Server Error)
```

