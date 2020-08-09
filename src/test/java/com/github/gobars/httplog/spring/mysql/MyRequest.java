package com.github.gobars.httplog.spring.mysql;

import lombok.Data;

@Data
public class MyRequest {
  private final String name;
  private String forkName;

  public MyRequest(String name) {
    this.name = name;
  }
}
