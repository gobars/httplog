package com.github.gobars.httplog;

import com.github.bingoohuang.logmask.LogMask;
import com.google.auto.service.AutoService;

@AutoService(TableValueFilter.class)
public class LogMaskValueFilter implements TableValueFilter {
  @Override
  public Object filter(String table, ColValueGetterCtx ctx, Object value) {
    if (value instanceof String) {
      return LogMask.mask(value);
    }

    return value;
  }
}
