package com.github.gobars.httplog.spring.mysql;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.gobars.httplog.HttpLogFilter;
import com.github.gobars.httplog.HttpLogInterceptor;
import javax.sql.DataSource;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigMysql {
  @Bean
  public HttpLogFilter httpLogFilter() {
    return new HttpLogFilter();
  }

  @Bean
  public HttpLogInterceptor httpLogInterceptor(@Autowired DataSource dataSource) {
    return new HttpLogInterceptor(dataSource);
  }

  @Bean
  public DataSource getDataSource() {
    val dataSource = new DruidDataSource();
    dataSource.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource.setUrl(
        "jdbc:mysql://localhost:3306/id?useSSL=false&zeroDateTimeBehavior=convertToNull&useUnicode=yes&autoReconnect=true&characterEncoding=UTF-8&characterSetResults=UTF-8");
    dataSource.setUsername("root");
    dataSource.setPassword("root");

    return dataSource;
  }
}
