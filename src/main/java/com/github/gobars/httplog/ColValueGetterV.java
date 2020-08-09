package com.github.gobars.httplog;

public interface ColValueGetterV {
  Object get(ColValueGetterCtx ctx, HttpLogTag tag, TableCol col);
}
