package com.github.gobars.httplog;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;

@ToString
public class HttpLogCustom {
  private static final ThreadLocal<HttpLogCustom> LOCAL = new InheritableThreadLocal<>();
  @Getter private final Map<String, String> map = new HashMap<>(10);

  public static void set(HttpLogCustom custom) {
    LOCAL.set(custom);
  }

  public static void clear() {
    LOCAL.remove();
  }

  public static HttpLogCustom get() {
    return LOCAL.get();
  }

  public HttpLogCustom put(String name, String value) {
    map.put(name, value);
    return this;
  }
}
