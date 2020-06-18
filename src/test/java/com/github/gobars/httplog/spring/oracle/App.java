package com.github.gobars.httplog.spring.oracle;

import com.github.gobars.httplog.HttpLogEnabled;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@HttpLogEnabled
@SpringBootApplication
public class App {

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
}
