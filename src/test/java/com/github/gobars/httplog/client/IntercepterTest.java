package com.github.gobars.httplog.client;

import java.io.IOException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jooq.lambda.Unchecked;
import org.junit.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class IntercepterTest {
  @Test
  public void apache() throws IOException {
    // Creating a CloseableHttpClient object
    CloseableHttpClient client =
        HttpClients.custom()
            .addInterceptorFirst(new Interceptor.Rsp())
            .addInterceptorFirst(new Interceptor.Req())
            .build();

    // Creating a request object
    HttpGet g = new HttpGet("https://www.baidu.com/");

    // Executing the request
    client.execute(g);
  }

  // refer https://www.stubbornjava.com/posts/okhttpclient-logging-configuration-with-interceptors
  @Test
  public void okhttp() {
    // {{start:noLogging}}
    log.info("noLogging");
    OkHttpClient client = new OkHttpClient.Builder().build();
    request(client, "https://www.baidu.com/");
    // {{end:noLogging}}

    // {{start:interceptor}}
    log.info("interceptor");
    OkHttpClient interceptorClient =
        new OkHttpClient.Builder().addInterceptor(getLoggingInterceptor()).build();
    request(interceptorClient, "https://www.baidu.com/");
    // {{end:interceptor}}

    // {{start:networkInterceptor}}
    log.info("networkInterceptor");
    OkHttpClient networkInterceptorClient =
        new OkHttpClient.Builder().addNetworkInterceptor(getLoggingInterceptor()).build();
    request(networkInterceptorClient, "https://www.baidu.com/");
    // {{end:networkInterceptor}}
  }

  private static void request(OkHttpClient client, String url) {
    Request request = new Request.Builder().url(url).get().build();
    Unchecked.supplier(
            () -> {
              Response response = client.newCall(request).execute();
              log.info(
                  "{} - {}", response.code(), Objects.requireNonNull(response.body()).string());
              return null;
            })
        .get();
  }

  // {{start:logging}}
  private static final HttpLoggingInterceptor loggingInterceptor =
      new HttpLoggingInterceptor(log::info);

  static {
    if (log.isDebugEnabled()) {
      loggingInterceptor.setLevel(Level.BASIC);
    } else if (log.isTraceEnabled()) {
      loggingInterceptor.setLevel(Level.BODY);
    }
  }

  public static HttpLoggingInterceptor getLoggingInterceptor() {
    return loggingInterceptor;
  }
  // {{end:logging}}

  @Test
  public void restTemplate() {
    val client =
        HttpClients.custom()
            .addInterceptorFirst(new Interceptor.Rsp())
            .addInterceptorFirst(new Interceptor.Req())
            .build();
    val factory = new HttpComponentsClientHttpRequestFactory();
    factory.setHttpClient(client);

    val restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(factory);

    restTemplate.getForObject("https://www.baidu.com/", String.class);
  }
}
