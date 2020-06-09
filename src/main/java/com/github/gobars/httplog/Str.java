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
  public String abbreviate(String s, int maxLen) {
    if (s.length() <= maxLen) {
      return s;
    }

    if (maxLen > 3) {
      return s.substring(0, maxLen - 3) + "...";
    }

    return s.substring(0, maxLen);
  }

  /**
   * Parses a query string passed from the client to the server and builds a <code>
   * Map<String, String></code> object with key-value pairs. The query string should be in the form
   * of a string packaged by the GET or POST method, that is, it should have key-value pairs in the
   * form <i>key=value</i>, with each pair separated from the next by a &amp; character.
   *
   * <p>A key can appear more than once in the query string with different values. However, the key
   * appears only once in the hashtable, with its value being an array of strings containing the
   * multiple values sent by the query string.
   *
   * <p>The keys and values in the hashtable are stored in their decoded form, so any + characters
   * are converted to spaces, and characters sent in hexadecimal notation (like <i>%xx</i>) are
   * converted to ASCII characters.
   *
   * @param s a string containing the query to be parsed
   * @return a <code>Map<String, String></code> object built from the parsed key-value pairs
   * @exception IllegalArgumentException if the query string is invalid
   */
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
}
