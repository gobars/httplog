package com.github.gobars.httplog;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.val;

/**
 * HttpLog在数据库字段comment中的注解信息.
 *
 * @author bingoobjca
 */
@Data
@Accessors(fluent = true)
public class HttpLogTag {
  public static final String MASK_KEYS = "maskKeys=";
  private String tag;
  private ForkMode forkMode = ForkMode.None;
  private Set<String> maskKeys = new HashSet<>();

  {
    maskKeys.add("password");
  }

  public boolean startsWith(String s) {
    return tag.startsWith(s);
  }

  public HttpLogTag subTag(int index) {
    return new HttpLogTag().tag(tag.substring(index)).forkMode(forkMode).maskKeys(maskKeys);
  }

  public String subTagName(int index) {
    return tag.substring(index);
  }

  static final Pattern TAG_PATTERN = Pattern.compile("httplog:\"(.*?)\"");

  public static HttpLogTag parse(String name, String comment) {
    String tag = name.toLowerCase();
    HttpLogTag httpLogTag = new HttpLogTag().tag(tag);
    if (comment == null) {
      return httpLogTag;
    }

    val m = TAG_PATTERN.matcher(comment);
    if (!m.find()) {
      return httpLogTag;
    }

    String tagValue = m.group(1);
    int optionStart = tagValue.indexOf(",");
    if (optionStart < 0) {
      httpLogTag.tag(tagValue);
      return httpLogTag;
    }

    String tagName = tagValue.substring(0, optionStart);
    if (tagName.length() > 0) {
      httpLogTag.tag(tagName);
    }

    Set<String> options = parseTagOptions(tagValue.substring(optionStart + 1));
    httpLogTag.forkMode(ForkMode.parse(options));

    String[] maskKeys = parseMaskKeys(options);
    if (maskKeys.length > 0) {
      Collections.addAll(httpLogTag.maskKeys, maskKeys);
    }

    return httpLogTag;
  }

  private static String[] parseMaskKeys(Set<String> options) {
    for (String key : options) {
      if (key.startsWith(MASK_KEYS)) {
        String keys = key.substring(MASK_KEYS.length());
        return keys.split("/");
      }
    }

    return new String[0];
  }

  public static Set<String> parseTagOptions(String option) {
    Set<String> options = new HashSet<>();

    for (String v : option.split(",")) {
      if (v.length() > 0) {
        options.add(v);
      }
    }

    return options;
  }

  public boolean equalsTo(String s) {
    return tag.equals(s);
  }
}
