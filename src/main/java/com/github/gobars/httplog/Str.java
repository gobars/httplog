package com.github.gobars.httplog;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * 字符串小函数
 *
 * @author bingoobjca
 */
@UtilityClass
public class Str {
  public boolean containsIgnoreCase(String s, String a) {
    return s != null && a != null && s.toLowerCase().contains(a.toLowerCase());
  }

  public String abbreviate(String s, int maxLen) {
    if (s.length() <= maxLen || maxLen <= 0) {
      return s;
    }

    if (maxLen > 3) {
      return s.substring(0, maxLen - 3) + "...";
    }

    return s.substring(0, maxLen);
  }

  public Map<String, String> parseQuery(String s) {
    Map<String, String> m = parseMap(s, "&", "=");
    Map<String, String> r = new HashMap<>(m.size());

    for (val e : m.entrySet()) {
      r.put(decode(e.getKey()), decode(e.getValue()));
    }

    return r;
  }

  public Map<String, String> parseMap(String s, String sep1, String sep2) {
    Map<String, String> ht = new HashMap<>(10);

    if (s == null) {
      return ht;
    }

    val st = new StringTokenizer(s, sep1);
    while (st.hasMoreTokens()) {
      String pair = st.nextToken();
      int pos = pair.indexOf(sep2);
      if (pos == -1) {
        continue;
      }

      String key = pair.substring(0, pos).trim();
      String val = pair.substring(pos + 1).trim();
      if (ht.containsKey(key)) {
        String oldVal = ht.get(key);
        val = oldVal + "," + val;
      }

      ht.put(key, val);
    }

    return ht;
  }

  private String decode(final String content) {
    try {
      return URLDecoder.decode(content, "UTF-8");
    } catch (UnsupportedEncodingException problem) {
      throw new IllegalArgumentException(problem);
    }
  }

  public String join(String[] values) {
    return join(",", values);
  }

  public String join(String sep, String[] values) {
    if (values == null) {
      return null;
    }

    return String.join(sep, values);
  }
}
