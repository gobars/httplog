package com.github.gobars.httplog;

public interface ColValueGetterV {
  Object get(ColValueGetterContext ctx, String v, TableCol col);
}
