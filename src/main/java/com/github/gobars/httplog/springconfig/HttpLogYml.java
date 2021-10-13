package com.github.gobars.httplog.springconfig;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gobars.httplog.TableCol;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.yaml.snakeyaml.Yaml;

@Data
public class HttpLogYml {
  private Map<String, HttpLogTableMeta> metas = new HashMap<>();
  private boolean autoSchema;

  public void put(String table, HttpLogTableMeta tag) {
    metas.put(table.toLowerCase(), tag);
  }

  public HttpLogTableMeta get(String table) {
    return metas.get(table.toLowerCase());
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  public static HttpLogYml loadYml(InputStream is) {
    Yaml yaml = new Yaml();
    Map<String, Object> root = yaml.load(is);
    HttpLogYml httpLogYml = new HttpLogYml();
    httpLogYml.autoSchema = (boolean) root.get("auto_schema");

    for (val entry : root.entrySet()) {
      String table = entry.getKey().toLowerCase();
      if ("auto_schema".equals(table)) {
        continue;
      }

      HttpLogTableMeta tagTable = new HttpLogTableMeta(table);
      if (!(entry.getValue() instanceof Map)) {
        continue;
      }

      val fields = (Map<String, Object>) entry.getValue();
      for (val field : fields.entrySet()) {
        if (!(field.getValue() instanceof Map)) {
          continue;
        }

        val props = (Map<String, Object>) field.getValue();
        val tagField = new HttpLogFieldMeta();
        tagField.setDataType((String) props.get("data_type"));
        tagField.setMaxLength(((Number) props.get("max_length")).intValue());
        tagField.setNullable((Boolean) props.get("nullable"));
        tagField.setExtra((String) props.get("extra"));
        tagField.setComment((String) props.get("comment"));

        tagTable.put(field.getKey(), tagField);
      }

      httpLogYml.put(table, tagTable);
    }

    return httpLogYml;
  }

  public List<TableCol> loadTableColsSchema(String table, Map<String, String> fixes) {
    val tableCols = new ArrayList<TableCol>();
    val meta = get(table);
    for (val m : meta.getFieldTags().entrySet()) {
      val c = new TableCol();
      tableCols.add(c);

      HttpLogFieldMeta v = m.getValue();
      c.setDataType(v.getDataType());
      c.setMaxLen(v.getMaxLength());
      c.setExtra(v.getExtra());
      c.setNullable(v.isNullable());
      c.setComment(v.getComment());

      c.parseComment(fixes, c.getComment());
    }

    return tableCols;
  }
}
