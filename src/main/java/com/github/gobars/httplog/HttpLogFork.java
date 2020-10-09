package com.github.gobars.httplog;

import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.httplog.snack.core.Cnf;
import com.github.gobars.id.Id;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpLogFork {
  @Getter private final long id = Id.next();
  @Getter private final HttpLogAttr attr;
  @Getter private final Object request;
  @Getter private Object response;
  @Getter private boolean sealed;
  @Getter private final HashMap<String, String> customized = new HashMap<>();

  @Getter private final Timestamp start = new Timestamp(System.currentTimeMillis());
  @Getter private Timestamp end;
  @Getter private long tookMs;
  @Getter private Throwable error;

  @Getter @Setter private String method;

  public HttpLogFork(HttpLogAttr attr, Object request) {
    this.attr = attr;
    this.request = request;
  }

  public HttpLogFork custom(String key, String value) {
    checkSealed();

    customized.put(key, value);

    return this;
  }

  public void customAll(Map<String, String> map) {
    checkSealed();

    for (Map.Entry<String, String> entry : map.entrySet()) {
      customized.put(entry.getKey(), entry.getValue());
    }
  }

  public void submitError(Throwable error) {
    checkSealed();

    this.sealed = true;
    this.end = new Timestamp(System.currentTimeMillis());
    this.tookMs = this.end.getTime() - this.start.getTime();
    this.error = error;
  }

  public void submit(Object response) {
    checkSealed();

    this.sealed = true;
    this.end = new Timestamp(System.currentTimeMillis());
    this.tookMs = this.end.getTime() - this.start.getTime();
    this.response = response;
  }

  public void checkSealed() {
    if (sealed) {
      throw new HttpLogForkSealedException();
    }
  }

  public String abbrReq(int maxLen, HttpLogTag v) {
    return abbr(request, maxLen, v);
  }

  public String abbrRsp(int maxLen, HttpLogTag v) {
    return abbr(response, maxLen, v);
  }

  public String getAbbrReq(int maxLen, HttpLogTag v) {
    return abbr(request, maxLen, v);
  }

  public static String abbr(Object obj, int maxLen, HttpLogTag v) {
    Cnf cnf = Cnf.def().abbrevMaxSize(maxLen);
    cnf.maskKeys(v.maskKeys());

    try {
      return Onode.load(obj, cnf).toJson();
    } catch (Exception ex) {
      log.warn("failed toJSON", ex);
    }

    return Onode.load(obj).toJson();
  }

  private static class HttpLogForkSealedException extends RuntimeException {}
}
