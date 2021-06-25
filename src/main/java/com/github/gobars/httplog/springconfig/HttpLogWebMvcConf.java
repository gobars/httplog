package com.github.gobars.httplog.springconfig;

import com.github.gobars.httplog.Const;
import com.github.gobars.httplog.HttpLogFilter;
import com.github.gobars.httplog.HttpLogInterceptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Slf4j
@Configuration
@AllArgsConstructor
public class HttpLogWebMvcConf extends WebMvcConfigurerAdapter {

  final HttpLogInterceptor httpLogInterceptor;
  final HttpLogFilter httpLogFilter;

  @Autowired(required = false)
  final String[] httpLogWebIgnores;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    InterceptorRegistration i = registry.addInterceptor(httpLogInterceptor).addPathPatterns("/**");
    if (httpLogWebIgnores.length > 0) {
      i.excludePathPatterns(httpLogWebIgnores);
    }

    log.info("Configure Interceptor.....");
    super.addInterceptors(registry);
  }
}
