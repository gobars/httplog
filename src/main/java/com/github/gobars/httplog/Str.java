package com.github.gobars.httplog;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

  /**
   * 按字节数进行缩略.
   *
   * @param s 字符串
   * @param maxBytesLen 最大字节数
   * @return 缩略字符串
   */
  public String abbreviate(String s, int maxBytesLen) {
    if (s.length() <= maxBytesLen || maxBytesLen <= 0) {
      return s;
    }

    if (maxBytesLen > 3) {
      return truncate(s, maxBytesLen - 3) + "...";
    }

    return truncate(s, maxBytesLen);
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

  /**
   * Truncating Strings by Bytes.
   *
   * <p>https://stackoverflow.com/a/17893381
   *
   * @param s string
   * @param maxBytes max bytes
   * @return truncated string
   */
  public String truncate(String s, int maxBytes) {
    if (s == null) {
      return null;
    }

    byte[] utf8 = s.getBytes(StandardCharsets.UTF_8);
    if (utf8.length <= maxBytes) {
      return s;
    }

    int n16 = 0;
    boolean extraLong;
    int i = 0;
    while (i < maxBytes) {
      // Unicode characters above U+FFFF need 2 words in utf16
      extraLong = ((utf8[i] & 0xF0) == 0xF0);
      if ((utf8[i] & 0x80) == 0) {
        ++i;
      } else {
        for (int b = utf8[i]; (b & 0x80) > 0; b <<= 1) {
          ++i;
        }
      }
      if (i <= maxBytes) {
        n16 += (extraLong) ? 2 : 1;
      }
    }

    return s.substring(0, n16);
  }
}
