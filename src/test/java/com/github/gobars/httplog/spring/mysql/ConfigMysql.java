package com.github.gobars.httplog.spring.mysql;

import com.alibaba.druid.pool.DruidDataSource;
import javax.sql.DataSource;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigMysql {
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
