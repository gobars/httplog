package com.github.gobars.httplog;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class Rsp extends ReqRsp {
  private int status;
  private String reasonPhrase;
}
