package com.github.gobars.httplog;

import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.httplog.snack.core.Cnf;
import com.github.gobars.id.Id;
import java.sql.Timestamp;
import java.util.HashMap;
import lombok.Getter;

public class HttpLogFork {
  @Getter private final long id = Id.next();
  @Getter private final HttpLogAttr attr;
  @Getter private final Object request;
  @Getter private Object response;
  @Getter private boolean sealed;
  @Getter private HashMap<String, Object> customized = new HashMap<>();

  @Getter private Timestamp start = new Timestamp(System.currentTimeMillis());
  @Getter private Timestamp end;
  @Getter private long tookMs;
  @Getter private String error;

  public HttpLogFork(HttpLogAttr attr, Object request) {
    this.attr = attr;
    this.request = request;
  }

  public HttpLogFork custom(String key, Object value) {
    checkSealed();

    customized.put(key, value);

    return this;
  }

  public void submitError(String error) {
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

  public String abbrReq(int maxLen) {
    return abbr(request, maxLen);
  }

  public String abbrRsp(int maxLen) {
    return abbr(response, maxLen);
  }

  public String getAbbrReq(int maxLen) {
    return abbr(request, maxLen);
  }

  public static String abbr(Object obj, int maxLen) {
    String s = Onode.load(obj).toJson();
    if (maxLen > 0 && s.length() <= maxLen) {
      return s;
    }

    try {
      return Onode.load(s, Cnf.def().abbrevMaxSize(maxLen)).toJson();
    } catch (Exception ignore) {

    }

    return s;
  }

  private static class HttpLogForkSealedException extends RuntimeException {}
}
