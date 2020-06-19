package com.github.gobars.httplog.spring.oracle;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.gobars.httplog.springconfig.HttpLogEnabled;
import javax.sql.DataSource;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@HttpLogEnabled
@SpringBootApplication
public class App {
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

  @Bean
  public DataSource getDataSource() {
    val dataSource = new DruidDataSource();
    dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
    dataSource.setUrl("jdbc:oracle:thin:@127.0.0.1:1521:xe");
    dataSource.setUsername("system");
    dataSource.setPassword("oracle");

    return dataSource;
  }
}
