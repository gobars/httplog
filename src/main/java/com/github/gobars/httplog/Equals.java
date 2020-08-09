package com.github.gobars.httplog;

class Equals implements Matcher {
  private final String[] value;

  Equals(String... value) {
    this.value = value;
  }

  static Matcher eq(String... value) {
    return new Equals(value);
  }

  @Override
  public boolean matches(String tag) {
    for (String v : value) {
      if (v.equals(tag)) {
        return true;
      }
    }

    return false;
  }
}
