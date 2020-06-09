package com.github.gobars.httplog.snack.core.exts;

import java.util.HashMap;
import java.util.Map;

/** Enum 包装器 */
public class EnumWrap {
  protected final Map<String, Enum> enumMap = new HashMap<>();
  protected final Enum[] enumOrdinal;

  public EnumWrap(Class<?> enumClass) {
    enumOrdinal = (Enum[]) enumClass.getEnumConstants();

    for (int i = 0; i < enumOrdinal.length; ++i) {
      Enum e = enumOrdinal[i];
      String name = e.name();

      enumMap.put(name, e);
    }
  }

  public Enum get(int ordinal) {
    return enumOrdinal[ordinal];
  }

  public Enum get(String name) {
    return enumMap.get(name);
  }
}
