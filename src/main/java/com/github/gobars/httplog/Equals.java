package com.github.gobars.httplog;

class Equals implements Matcher {
  private final String value;

  Equals(String value) {
    this.value = value;
  }

  static Matcher eq(String value) {
    return new Equals(value);
  }

  @Override
  public boolean matches(String tag) {
    return value.equals(tag);
  }
}
