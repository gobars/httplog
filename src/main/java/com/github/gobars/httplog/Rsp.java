package com.github.gobars.httplog;

import java.util.Map;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class Rsp extends ReqRsp {
  private int status;
  private String reasonPhrase;
  private Map<String, String> posts;
}
