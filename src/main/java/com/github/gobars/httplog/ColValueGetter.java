package com.github.gobars.httplog;

/**
 * 日志表字段值获取器
 *
 * @author bingoobjca
 */
public interface ColValueGetter {
  Object get(ColValueGetterCtx ctx);
}
