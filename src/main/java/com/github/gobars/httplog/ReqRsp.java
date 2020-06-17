package com.github.gobars.httplog;

import com.github.gobars.httplog.snack.ONode;
import java.sql.Timestamp;
import java.util.Map;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = {"bodyONode", "bodyONodeInitialized"})
public class ReqRsp {
  private long id;
  private Timestamp startTime = new Timestamp(System.currentTimeMillis());
  private Timestamp endTime;
  private Map<String, String> headers;
  private long startNs;
  private long tookMs;
  private int bodyBytes;
  private String body;
  private String error;
  private ONode bodyONode;
  private boolean bodyONodeInitialized;
}
