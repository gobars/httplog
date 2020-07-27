package com.github.gobars.httplog;

import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.httplog.snack.core.Cnf;
import java.sql.Timestamp;
import java.util.Map;
import lombok.Data;

@Data
public class ReqRsp {
  private long id;
  private Timestamp startTime = new Timestamp(System.currentTimeMillis());
  private Timestamp endTime;
  private Map<String, String> headers;
  private long tookMs;
  private int bodyBytes;
  private String body;
  private String error;
  private Onode bodyOnode;
  private boolean bodyOnodeInitialized;

  @Override
  public String toString() {
    return "ReqRsp{"
        + "id="
        + id
        + ", startTime="
        + startTime
        + ", endTime="
        + endTime
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

  public String getAbbreviateBody() {
    try {
      return Onode.load(this.body, Cnf.def().abbrevMaxSize(abbrevMaxSize)).toJson();
    } catch (Exception e) {
      return this.body;
    }
  }

  private String tryAbbreviate(String s) {
    try {
      return Onode.load(s, Cnf.def().abbrevMaxSize(abbrevMaxSize)).toJson();
    } catch (Exception e) {
      return Str.abbreviate(s, abbrevMaxSize);
    }
  }
}
