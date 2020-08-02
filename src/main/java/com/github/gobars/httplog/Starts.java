package com.github.gobars.httplog;

class Starts implements Matcher {
  private final String value;

  Starts(String value) {
    this.value = value;
  }

  public static Matcher starts(String value) {
    return new Starts(value);
  }

  @Override
  public boolean matches(String tag) {
    return tag != null && tag.startsWith(value);
  }
}
