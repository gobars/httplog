package com.github.gobars.httplog.spring.mysql;

import lombok.Data;

@Data
public class MyResponse {
  private final String message;
  private int tran;

  public MyResponse(String message) {
    this.message = message;
  }
}
