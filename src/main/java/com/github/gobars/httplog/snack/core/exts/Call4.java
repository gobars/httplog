package com.github.gobars.httplog.snack.core.exts;

public interface Call4<R, T1, T2, T3, T4> {
  R run(T1 t1, T2 t2, T3 t3, T4 t4);
}
