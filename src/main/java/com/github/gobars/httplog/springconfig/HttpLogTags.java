package com.github.gobars.httplog.springconfig;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.yaml.snakeyaml.Yaml;

@Data
public class HttpLogTags {
  private Map<String, HttpLogTagTable> tags = new HashMap<>();

  public void put(String table, HttpLogTagTable tag) {
    tags.put(table.toLowerCase(), tag);
  }

  public HttpLogTagTable get(String table) {
    return tags.get(table.toLowerCase());
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  public static HttpLogTags parseYml(InputStream is) {
    Yaml yaml = new Yaml();
    Map<String, Object> object = yaml.load(is);

    HttpLogTags httpLogTags = new HttpLogTags();
    for (val entry : object.entrySet()) {
      String table = entry.getKey().toLowerCase();
      HttpLogTagTable tagTable = new HttpLogTagTable(table);
      if (!(entry.getValue() instanceof Map)) {
        continue;
      }

      val fields = (Map<String, Object>) entry.getValue();
      for (val field : fields.entrySet()) {
        if (!(field.getValue() instanceof Map)) {
          continue;
        }

        val props = (Map<String, String>) field.getValue();
        val tagField = new HttpLogTagField();
        tagField.setComment(props.get("comment"));
        tagTable.put(field.getKey(), tagField);
      }

      httpLogTags.put(table, tagTable);
    }

    return httpLogTags;
  }
}
