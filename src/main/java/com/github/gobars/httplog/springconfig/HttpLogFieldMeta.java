package com.github.gobars.httplog.springconfig;

import lombok.Data;

@Data
public class HttpLogFieldMeta {
  private String dataType;
  private String extra;
  private int maxLength;
  private boolean nullable;
  private String comment;
  private boolean manualSchema;
}
