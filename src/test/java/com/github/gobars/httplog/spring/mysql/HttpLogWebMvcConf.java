package com.github.gobars.httplog.spring.mysql;

import com.github.gobars.httplog.HttpLogFilter;
import com.github.gobars.httplog.HttpLogInterceptor;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Slf4j
@Configuration
public class HttpLogWebMvcConf extends WebMvcConfigurationSupport {
  @Autowired DataSource dataSource;

  @Bean
  public HttpLogFilter httpLogFilter() {
    return new HttpLogFilter();
  }

  @Override
  protected void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new HttpLogInterceptor(dataSource)).addPathPatterns("/**");
    log.info("Configure Interceptor.....");
    super.addInterceptors(registry);
  }
}
