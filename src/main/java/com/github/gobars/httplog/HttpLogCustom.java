package com.github.gobars.httplog;

import java.util.ArrayList;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

@ToString
public class HttpLogCustom {
  private static final ThreadLocal<HttpLogCustom> LOCAL = new InheritableThreadLocal<>();
  @Getter @Setter private HashMap<String, String> map = new HashMap<>(10);
  @Getter private final ArrayList<HttpLogFork> forks = new ArrayList<>(10);

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

  public static HttpLogFork fork(HttpLogAttr attr, Object request) {
    val f = new HttpLogFork(attr, request);
    get().forks.add(f);

    return f;
  }
}
