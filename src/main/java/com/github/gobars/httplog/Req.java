package com.github.gobars.httplog;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class Req extends ReqRsp {
  private String method;
  private String requestUri;
  private String protocol;
}
