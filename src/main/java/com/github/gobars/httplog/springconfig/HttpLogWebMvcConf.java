package com.github.gobars.httplog.springconfig;

import com.github.gobars.httplog.HttpLogCustomArgumentResolver;
import com.github.gobars.httplog.HttpLogFilter;
import com.github.gobars.httplog.HttpLogInterceptor;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Slf4j
@Configuration
@AllArgsConstructor
public class HttpLogWebMvcConf extends WebMvcConfigurationSupport {
  final HttpLogInterceptor httpLogInterceptor;
  final HttpLogFilter httpLogFilter;

  @Override
  protected void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(httpLogInterceptor).addPathPatterns("/**");
    log.info("Configure Interceptor.....");
    super.addInterceptors(registry);
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new HttpLogCustomArgumentResolver());
  }
}
