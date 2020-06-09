package com.github.gobars.httplog.spring;

import com.github.gobars.httplog.Interceptor;
import com.github.gobars.id.conf.ConnGetter;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Slf4j
@Configuration
public class WebConf extends WebMvcConfigurationSupport {
  @Autowired DataSource dataSource;

  @Override
  protected void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(new Interceptor(new ConnGetter.DsConnGetter(dataSource)))
        .addPathPatterns("/**");
    log.info("Configure Interceptor.....");
    super.addInterceptors(registry);
  }
}
