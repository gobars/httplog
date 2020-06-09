package com.github.gobars.httplog.snack.core;

/** 处理者 */
public interface Handler {
  void handle(Context context) throws Exception;
}
