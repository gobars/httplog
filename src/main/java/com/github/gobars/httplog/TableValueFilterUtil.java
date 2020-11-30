package com.github.gobars.httplog;

import java.util.ServiceLoader;

/**
 * 表字段值过滤器辅助类.
 *
 * @author bingoobjca
 */
public class TableValueFilterUtil {
  static ServiceLoader<TableValueFilter> filters = ServiceLoader.load(TableValueFilter.class);

  public static Object filter(String table, ColValueGetterCtx ctx, Object val) {
    Object obj = val;
    for (TableValueFilter filter : filters) {
      obj = filter.filter(table, ctx, obj);
    }

    return obj;
  }
}
