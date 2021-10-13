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
  private boolean manualSchema;

  public void put(String table, HttpLogTableMeta tag) {
    metas.put(table.toLowerCase(), tag);
  }

  public HttpLogTableMeta get(String table) {
    return metas.get(table.toLowerCase());
  }

  public static final String MANUAL_SCHEMA = "manual_schema";

  @SneakyThrows
  @SuppressWarnings("unchecked")
  public static HttpLogYml loadYml(InputStream is) {
    Yaml yaml = new Yaml();
    Map<String, Object> root = yaml.load(is);
    HttpLogYml httpLogYml = new HttpLogYml();
    httpLogYml.manualSchema = (boolean) root.get(MANUAL_SCHEMA);

    for (val entry : root.entrySet()) {
      String table = entry.getKey().toLowerCase();
      if (MANUAL_SCHEMA.equals(table)) {
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
        tagField.setDataType(getString(props, "data_type"));
        tagField.setMaxLength(getInt(props, "max_length"));
        tagField.setNullable(getBool(props, "nullable"));
        tagField.setExtra(getString(props, "extra"));
        tagField.setComment(getString(props, "comment"));
        tagField.setManualSchema(getBool(props, MANUAL_SCHEMA));

        tagTable.put(field.getKey(), tagField);
      }

      httpLogYml.put(table, tagTable);
    }

    return httpLogYml;
  }

  public static boolean getBool(Map<String, Object> props, String name) {
    Object v = props.get(name);
    if (v == null) {
      return false;
    }

    return (Boolean) v;
  }

  public static int getInt(Map<String, Object> props, String name) {
    Object v = props.get(name);
    if (v == null) {
      return 0;
    }

    return ((Number) v).intValue();
  }

  public static String getString(Map<String, Object> props, String name) {
    return (String) props.get(name);
  }

  public List<TableCol> loadTableColsSchema(String table, Map<String, String> fixes) {
    val tableCols = new ArrayList<TableCol>();
    val meta = get(table);
    if (meta == null) {
      return null;
    }

    for (val m : meta.getFieldTags().entrySet()) {
      val c = new TableCol();
      tableCols.add(c);

      HttpLogFieldMeta v = m.getValue();
      c.setName(m.getKey());
      c.setDataType(v.getDataType());
      c.setMaxLen(v.getMaxLength());
      c.setExtra(v.getExtra());
      c.setNullable(v.isNullable());
      c.setComment(v.getComment());

      c.parseComment(fixes, v.getComment());
    }

    return tableCols;
  }

  public boolean isTableManualSchema(String table) {
    if (isManualSchema()) {
      return true;
    }

    val meta = get(table);
    if (meta == null) {
      return false;
    }

    HttpLogFieldMeta f = meta.get(MANUAL_SCHEMA);
    return f != null && f.isManualSchema();
  }
}
