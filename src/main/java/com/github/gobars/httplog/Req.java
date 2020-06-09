package com.github.gobars.httplog;

import java.util.Map;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class Req extends ReqRsp {
  private String method;
  private String requestUri;
  private String protocol;
  private Map<String, String> queries;
}
