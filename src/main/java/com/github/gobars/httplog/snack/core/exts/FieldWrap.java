package com.github.gobars.httplog.snack.core.exts;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import lombok.SneakyThrows;

/** 字段包装 */
public class FieldWrap {
  public Field field;
  public Class<?> clz;
  public Type type;

  public FieldWrap(Field f) {
    field = f;
    clz = f.getType();
    type = f.getGenericType();
  }

  public String name() {
    return field.getName();
  }

  @SneakyThrows
  public void set(Object obj, Object val) {
    field.set(obj, val);
  }

  @SneakyThrows
  public Object get(Object obj) {
    return field.get(obj);
  }
}
