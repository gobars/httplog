package com.github.gobars.httplog.snack.core.exts;

import com.github.gobars.httplog.snack.core.utils.IOUtil;

/** 字符阅读器 */
public class CharReader {
  private final String chars;
  private final int length;
  private int next = 0;
  private char val;

  public CharReader(String s) {
    this.chars = s;
    this.length = s.length();
  }

  public boolean read() {
    if (next >= length) {
      return false;
    } else {
      val = chars.charAt(next++);
      return true;
    }
  }

  public char next() {
    if (read()) {
      return val;
    } else {
      return IOUtil.EOI;
    }
  }

  public int length() {
    return length;
  }

  public char value() {
    return val;
  }
}
