package com.github.gobars.httplog.springconfig;

import com.github.gobars.httplog.Const;
import com.github.gobars.httplog.HttpLogFilter;
import com.github.gobars.httplog.HttpLogInterceptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Slf4j
@Configuration
@AllArgsConstructor
public class HttpLogWebMvcConf extends WebMvcConfigurerAdapter {

  final HttpLogInterceptor httpLogInterceptor;
  final HttpLogFilter httpLogFilter;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(httpLogInterceptor).addPathPatterns("/**")
    .excludePathPatterns(Const.WEB_IGNORES);
    log.info("Configure Interceptor.....");
    super.addInterceptors(registry);
  }
}
