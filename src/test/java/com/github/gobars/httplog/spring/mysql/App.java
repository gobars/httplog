package com.github.gobars.httplog.spring.mysql;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.gobars.httplog.springconfig.HttpLogEnabled;
import com.github.gobars.httplog.springconfig.HttpLogYml;
import javax.sql.DataSource;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@HttpLogEnabled
@SpringBootApplication
public class App {
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

  @Bean
  public DataSource getDataSource() {
    val dataSource = new DruidDataSource();
    dataSource.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource.setUrl(
        "jdbc:mysql://localhost:3306/id?serverTimezone=UTC&useSSL=false&zeroDateTimeBehavior=convertToNull&useUnicode=yes&autoReconnect=true&characterEncoding=UTF-8&characterSetResults=UTF-8");
    dataSource.setUsername("root");
    dataSource.setPassword("root");

    return dataSource;
  }

  @Bean
  @SneakyThrows
  public HttpLogYml httpLogYml() {
    @Cleanup val is = new ClassPathResource("httplog.yml").getInputStream();

    return HttpLogYml.loadYml(is);
  }

  @Bean
  public String[] httpLogWebIgnores() {
    return new String[] {};
  }
}
