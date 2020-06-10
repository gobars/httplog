package com.github.gobars.httplog.spring.oracle;

import com.alibaba.druid.pool.DruidDataSource;
import javax.sql.DataSource;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigOracle {
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
