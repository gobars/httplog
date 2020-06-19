# httplog

[![Build Status](https://travis-ci.org/gobars/httplog.svg?branch=master)](https://travis-ci.org/gobars/httplog)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.github.gobars%3Ahttplog&metric=alert_status)](https://sonarcloud.io/dashboard/index/com.github.gobars%3Ahttplog)
[![Coverage Status](https://coveralls.io/repos/github/gobars/httplog/badge.svg?branch=master)](https://coveralls.io/github/gobars/httplog?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.gobars/httplog/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.github.gobars/httplog/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

log request and response for http

## Usage

HttpLog会根据`@HttpLog`中定义的日志表的注解及字段名，自动记录相关HTTP信息.

### Prepare log tables

业务日志表定义，根据具体业务需要，必须字段为主键`id`（名字固定）, 示例: [mysql](scripts/mysql-ddl.sql), [oracle](scripts/oracle-ddl.sql)

<details>
  <summary>
    <p>日志表建表规范</p>
  </summary>

字段注释包含| 或者字段名 | 说明
---|---|---
内置类:||
`httplog:"id"`|id| 日志记录ID
`httplog:"created"`|created| 创建时间
`httplog:"ip"` |ip|当前机器IP
`httplog:"hostname"` |hostname|当前机器名称
`httplog:"pid"` |pid|应用程序PID
`httplog:"started"` |start|开始时间(yyyy-MM-dd HH:mm:ss.SSS)
`httplog:"end"` |end|结束时间(yyyy-MM-dd HH:mm:ss.SSS)
`httplog:"cost"` |cost|花费时间（ms)
`httplog:"biz"` |biz|业务名称，对应到HttpLog注解的biz
`httplog:"exception"` |exception|异常信息
请求类:||
`httplog:"req_head_xxx"` |req_head_xxx|请求中的xxx头
`httplog:"req_heads"` |req_heads|请求中的所有头
`httplog:"req_method"` |req_method|请求method
`httplog:"req_url"` |req_url|请求URL
`httplog:"req_path_xxx"` |req_path_xxx|请求URL中的xxx路径参数
`httplog:"req_paths"` |req_paths|请求URL中的所有路径参数
`httplog:"req_query_xxx"` |req_query_xxx|请求URl中的xxx查询参数
`httplog:"req_queries"` |req_queries|请求URl中的所有查询参数
`httplog:"req_param_xxx"` |req_param_xxx|请求中query/form的xxx参数
`httplog:"req_params"` |req_params|请求中query/form的所有参数
`httplog:"req_body"` |req_body|请求体
`httplog:"req_json"` |req_json|请求体（当Content-Type为JSON时)
`httplog:"req_json_xxx"` |req_json_xxx|请求体JSON中的xxx属性
响应类:||
`httplog:"rsp_head_xxx"` |rsp_head_xxx|响应中的xxx头
`httplog:"rsp_heads"` |rsp_heads|响应中的所有头
`httplog:"rsp_body"` |rsp_body|响应体
`httplog:"rsp_json"` |rsp_json|响应体JSON（当Content-Type为JSON时)
`httplog:"rsp_json_xxx"`|rsp_json_xxx| 请求体JSON中的xxx属性
`httplog:"rsp_status"`|rsp_status| 响应编码
上下文:||
`httplog:"ctx_xxx"` |ctx_xxx|上下文对象xxx的值
固定值:||
`httplog:"fix_xxx"`|fix_xxx| 由fix参数指定的固定值 
扩展类:||
`httplog:"pre_xxx"`|pre_xxx| 由自定义扩展器pre给出属性值，见[示例](src/test/java/com/github/gobars/httplog/spring/mysql/MyHttpLog.java)
`httplog:"post_xxx"`|post_xxx| 由自定义扩展器post给出属性值
自定义:||自助引入HttpLogCustom实例
`httplog:"custom_xxx"`|custom_xxx| 由HttpLogCustom提供的自定义xxx值 

</details>

### Setup interceptors and filters

<details>
  <summary>
    <p>Spring配置示例</p>
  </summary>
  
```java
@HttpLogEnabled
@SpringBootApplication
public class App {
    
}
```
</details>

<details>
  <summary>
    <p>SpringMVC Controller使用示例</p>
  </summary>
  
```java
import com.github.gobars.httplog.HttpLog;
import com.github.gobars.httplog.spring.TestDto;
import com.github.gobars.httplog.spring.TestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/test")
public class TestController {

  /**
   * test get.
   *
   * @param id id
   * @return id
   */
  @HttpLog(tables = "biz_log", fix = "desc:ID查找", eager = true)
  @GetMapping(value = "/{id}")
  public String get(@PathVariable Integer id) {
    return "test id : " + id;
  }

  /**
   * test post.
   *
   * @param testDto testDto
   * @return testDto
   */
  @HttpLog(tables = "biz_log_post")
  @PostMapping
  public TestDto post(@RequestBody TestDto testDto) {
    return testDto;
  }

  /**
   * test put error.
   *
   * @param testDto testDto
   */
  @PutMapping
  public void error(@RequestBody TestDto testDto) {
    log.warn("error TestException will be thrown");
    throw new TestException(testDto.toString());
  }
}
```
</details>

<details>
  <summary>
    <p>日志表记录日志示例</p>
  </summary>

sample biz_log records:

```json
[
  {
    "id": 928019202048,
    "created": "2020-06-09 16:41:51",
    "start": "2020-06-09 16:41:51",
    "end": "2020-06-09 16:41:52",
    "cost": 592,
    "ip": "192.168.224.20",
    "hostname": "bingoobjcadeMacBook-Pro.local",
    "pid": 73362,
    "biz": null,
    "req_path_id": "10",
    "req_url": "/test/10",
    "req_heads": "{host=localhost:53865, connection=keep-alive, accept=text/plain, application/json, application/*+json, */*, user-agent=Java/11.0.7}",
    "req_method": "GET",
    "rsp_body": "",
    "bizdesc": "ID查找"
  }
]
```

sample biz_log_post records:

```json
[
  {
    "id": 931332702208,
    "created": "2020-06-09 16:41:52",
    "start": "2020-06-09 16:41:52",
    "end": "2020-06-09 16:41:52",
    "cost": 31,
    "ip": "192.168.224.20",
    "hostname": "bingoobjcadeMacBook-Pro.local",
    "pid": 73362,
    "biz": null,
    "req_path_id": null,
    "req_url": "/test",
    "req_heads": "{content-length=9, host=localhost:53865, content-type=application/json, connection=keep-alive, accept=application/json, application/*+json, user-agent=Java/11.0.7}",
    "req_method": "POST",
    "rsp": "{\"id\":10}",
    "dtoid": "10"
  }
]
```

</details>

## 单纯HttpLogFilter打印日志示例

```
@org.springframework.context.annotation.Configuration
public class HttpLogFilterBean extends com.github.gobars.httplog.HttpLogFilter {}
```

<details>
  <summary>
    <p>请求响应日志</p>
  </summary>

GET：

```log
2020-06-04 20:43:31.665  INFO 81103 --- [o-auto-1-exec-1] c.github.gobars.httplog.HttpLogFilter  : req: Req(super=ReqRsp(headers={host=localhost:57413, connection=keep-alive, accept=text/plain, application/json, application/*+json, */*, user-agent=Java/11.0.7}, startNs=13514944034314, tookMs=0, bodyBytes=0, body=, error=null), method=GET, requestUri=/test/10, protocol=HTTP/1.1)
2020-06-04 20:43:31.669  INFO 81103 --- [o-auto-1-exec-1] c.github.gobars.httplog.HttpLogFilter  : rsp: Rsp(super=ReqRsp(headers={Keep-Alive=timeout=60, Connection=keep-alive, Content-Length=12, Date=Thu, 04 Jun 2020 12:43:31 GMT, Content-Type=text/plain;charset=UTF-8}, startNs=13514944034314, tookMs=51, bodyBytes=12, body=test id : 10, error=null), status=200, reasonPhrase=OK)
```

POST：

```log
2020-06-04 20:43:31.900  INFO 81103 --- [o-auto-1-exec-2] c.github.gobars.httplog.HttpLogFilter  : req: Req(super=ReqRsp(headers={content-length=9, host=localhost:57413, content-type=application/json, connection=keep-alive, accept=application/json, application/*+json, user-agent=Java/11.0.7}, startNs=13515206496154, tookMs=0, bodyBytes=9, body={"id":10}, error=null), method=POST, requestUri=/test, protocol=HTTP/1.1)
2020-06-04 20:43:31.900  INFO 81103 --- [o-auto-1-exec-2] c.github.gobars.httplog.HttpLogFilter  : rsp: Rsp(super=ReqRsp(headers={Keep-Alive=timeout=60, Connection=keep-alive, Content-Length=9, Date=Thu, 04 Jun 2020 12:43:31 GMT, Content-Type=application/json}, startNs=13515206496154, tookMs=29, bodyBytes=9, body={"id":10}, error=null), status=200, reasonPhrase=OK)
```

PUT exception：

```log
2020-06-04 20:43:31.917  INFO 81103 --- [o-auto-1-exec-3] c.github.gobars.httplog.HttpLogFilter  : req: Req(super=ReqRsp(headers={content-length=9, host=localhost:57413, content-type=application/json, connection=keep-alive, accept=application/json, application/*+json, user-agent=Java/11.0.7}, startNs=13515248933063, tookMs=0, bodyBytes=9, body={"id":10}, error=null), method=PUT, requestUri=/test, protocol=HTTP/1.1)
2020-06-04 20:43:31.917  INFO 81103 --- [o-auto-1-exec-3] c.github.gobars.httplog.HttpLogFilter  : rsp: Rsp(super=ReqRsp(headers=null, startNs=13515248933063, tookMs=4, bodyBytes=0, body=null, error=org.springframework.web.util.NestedServletException: Request processing failed; nested exception is com.github.gobars.httplog.spring.TestException: TestDto(id=10)
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
	at com.github.gobars.httplog.HttpLogFilter.doFilterInternal(HttpLogFilter.java:56)
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
Caused by: com.github.gobars.httplog.spring.TestException: TestDto(id=10)
	at com.github.gobars.httplog.spring.mysql.TestController.error(TestController.java:40)
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

</details>


## Rationale

[![image](https://user-images.githubusercontent.com/1940588/84857321-56bf0480-b09b-11ea-8fb1-b89212c9e857.png)](doc/rationale.drawio)

## Release 

1. `export GPG_TTY=$(tty)`
1. `mvn clean install -Prelease -Dgpg.passphrase=thephrase`
