package com.github.gobars.httplog.client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.http.*;
import org.apache.http.protocol.HttpContext;

/**
 * httpclient请求响应拦截器.
 *
 * <p>参考https://www.tutorialspoint.com/apache_httpclient/apache_httpclient_interceptors.htm
 */
public class Interceptor {
  public static final String HTTPLOG_KEY = "_httplog";
  public static final String HTTPLOG_FMT = "_httplog_fmt";

  public static class Req implements HttpRequestInterceptor {
    @Override
    public void process(HttpRequest r, HttpContext ctx) {
      long start = System.currentTimeMillis();
      Log log = new Log().start(start);
      ctx.setAttribute(HTTPLOG_KEY, log);

      SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      ctx.setAttribute(HTTPLOG_FMT, f);

      System.out.println("--> Come " + f.format(new Date(start)));
      System.out.println("--> URL " + r.getRequestLine());
      for (Header h : r.getAllHeaders()) {
        System.out.println("--> Header " + h.getName() + ":" + h.getValue());
      }
    }
  }

  public static class Rsp implements HttpResponseInterceptor {
    @Override
    public void process(HttpResponse r, HttpContext ctx) {
      long end = System.currentTimeMillis();
      Log log = (Log) ctx.getAttribute(HTTPLOG_KEY);
      log.end(end);
      log.cost(end - log.start());

      DateFormat f1 = (DateFormat) ctx.getAttribute(HTTPLOG_FMT);
      // refer https://stackoverflow.com/a/24327049 https://repl.it/repls/WindyMerryContent
      DateFormat f2 = new SimpleDateFormat("HH:mm:ss.SSS");
      f2.setTimeZone(TimeZone.getTimeZone("UTC"));

      System.out.println(
          "<-- Gone " + f1.format(new Date(end)) + " Cost " + f2.format(new Date(log.cost())));

      for (Header h : r.getAllHeaders()) {
        System.out.println("<-- Header " + h.getName() + ":" + h.getValue());
      }
    }
  }

  @Data
  @Accessors(fluent = true)
  public static class Log {
    long start;
    long end;
    long cost;
  }
}
