package com.github.gobars.httplog.springconfig;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class HttpLogTagTable {
  private final String table;
  private Map<String, HttpLogTagField> fieldTags = new HashMap<>();

  public HttpLogTagTable(String table) {
    this.table = table.toLowerCase();
  }

  public void put(String field, HttpLogTagField tableFieldTags) {
    fieldTags.put(field.toLowerCase(), tableFieldTags);
  }

  public HttpLogTagField get(String field) {
    return fieldTags.get(field.toLowerCase());
  }
}
