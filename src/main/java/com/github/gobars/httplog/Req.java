package com.github.gobars.httplog;

import java.util.Map;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true, exclude = "pres")
public class Req extends ReqRsp {
  private String method;
  private String requestUri;
  private String protocol;
  private Map<String, String> queries;
  private Map<String, String> pres;
}
