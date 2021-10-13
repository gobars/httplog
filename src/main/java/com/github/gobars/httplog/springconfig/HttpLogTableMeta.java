package com.github.gobars.httplog.springconfig;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class HttpLogTableMeta {
  private final String table;
  private Map<String, HttpLogFieldMeta> fieldTags = new HashMap<>();

  public HttpLogTableMeta(String table) {
    this.table = table.toLowerCase();
  }

  public void put(String field, HttpLogFieldMeta httpLogFieldMeta) {
    fieldTags.put(field.toLowerCase(), httpLogFieldMeta);
  }

  public HttpLogFieldMeta get(String field) {
    return fieldTags.get(field.toLowerCase());
  }
}
