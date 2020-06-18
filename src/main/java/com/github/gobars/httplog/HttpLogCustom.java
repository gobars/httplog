package com.github.gobars.httplog;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class HttpLogCustom {
  private final Map<String, String> map = new HashMap<>(10);

  public HttpLogCustom put(String name, String value) {
    map.put(name, value);
    return this;
  }
}
