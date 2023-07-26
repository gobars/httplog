package com.github.gobars.httplog;

import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.httplog.snack.core.Cnf;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Map;

@Data
public class ReqRsp {
  private String id;
  private Timestamp start = new Timestamp(System.currentTimeMillis());
  private Timestamp end;
  private Map<String, String> headers;
  private long tookMs;
  private int bodyBytes;
  private String body;
  private Throwable error;
  private Onode bodyOnode;
  private boolean bodyOnodeInitialized;

  @Override
  public String toString() {
    return "ReqRsp{"
        + "id="
        + id
        + ", start="
        + start
        + ", end="
        + end
        + ", headers="
        + headers
        + ", tookMs="
        + tookMs
        + ", bodyBytes="
        + bodyBytes
        + ", body="
        + tryAbbreviate(body)
        + ", error="
        + error
        + '}';
  }

  private int abbrevMaxSize;

  public String abbrBody(int maxLen) {
    if (maxLen > 0 && this.body.length() <= maxLen) {
      return this.body;
    }

    Cnf cnf = Cnf.def().abbrevMaxSize(abbrevMaxSize);
    try {
      return Onode.load(this.body, cnf).toJson();
    } catch (Exception e) {
      return this.body;
    }
  }

  private String tryAbbreviate(String s) {
    Cnf cnf = Cnf.def().abbrevMaxSize(abbrevMaxSize);

    try {
      return Onode.load(s, cnf).toJson();
    } catch (Exception e) {
      return Str.abbreviate(s, abbrevMaxSize);
    }
  }
}
