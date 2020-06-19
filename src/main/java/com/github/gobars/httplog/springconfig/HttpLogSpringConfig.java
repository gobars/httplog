package com.github.gobars.httplog.springconfig;

import com.github.gobars.httplog.HttpLogFilter;
import com.github.gobars.httplog.HttpLogInterceptor;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan
@Configuration
public class HttpLogSpringConfig {
  @Bean
  public HttpLogFilter httpLogFilter() {
    return new HttpLogFilter();
  }

  @Bean
  public HttpLogInterceptor httpLogInterceptor(DataSource dataSource) {
    return new HttpLogInterceptor(dataSource);
  }
}
