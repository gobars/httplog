package com.github.gobars.httplog;

import java.util.Set;

/** Tag中的Fork选项模式. */
public enum ForkMode {
  /**
   * 只从fork对象中取值.
   *
   * <p>在tag中标示为 httplog:"name,fork"
   */
  Only("fork"),
  /**
   * 先从fork对象中取值，取不到，则到http请求响应对象中取值.
   *
   * <p>在tag中标示为 httplog:"name,fork.."
   */
  Try("fork.."),
  /** 不从fork对象中取值. */
  None(""),
  ;

  private final String option;

  ForkMode(String option) {
    this.option = option;
  }

  public static ForkMode parse(Set<String> options) {
    for (ForkMode value : values()) {
      if (options.contains(value.option)) {
        return value;
      }
    }

    return None;
  }
}
