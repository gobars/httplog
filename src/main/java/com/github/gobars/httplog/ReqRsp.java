package com.github.gobars.httplog;

import java.util.Map;
import lombok.Data;

@Data
public class ReqRsp {
  private Map<String, String> headers;
  private long startNs;
  private long tookMs;
  private int bodyBytes;
  private String body;
  private String error;
}
