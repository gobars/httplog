package com.github.gobars.httplog.snack.core;

import com.github.gobars.httplog.snack.Onode;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/** 处理上下文对象 */
public class Ctx {
  /** 常量配置 */
  public final Cnf config;

  /** 来源 */
  public Object source;

  /** 目标 */
  public Object target;

  public Class<?> targetClz;
  public Type targetType;

  /** 用于来源处理的构造 */
  public Ctx(Cnf config, Object from) {
    this.config = config;
    this.source = from;
  }

  /** 用于去处的构造 */
  public Ctx(Cnf config, Onode node, Class<?> clz) {
    this.config = config;
    this.source = node;

    if (clz == null) {
      return;
    }

    if (TypeRef.class.isAssignableFrom(clz)) {
      Type superClass = clz.getGenericSuperclass();
      Type type = (((ParameterizedType) superClass).getActualTypeArguments()[0]);

      initType(type);
      return;
    }

    if (clz.getName().indexOf("$") > 0) {
      initType(clz.getGenericSuperclass());
    } else {
      initType(clz, clz);
    }
  }

  private void initType(Type type) {
    if (type instanceof ParameterizedType) {
      ParameterizedType pType = (ParameterizedType) type;

      initType(type, (Class<?>) pType.getRawType());
    } else {
      initType(type, (Class<?>) type);
    }
  }

  private void initType(Type type, Class<?> clz) {
    targetType = type;
    targetClz = clz;
  }

  /** 使用代理对当前上下文进行处理 */
  public Ctx handle(Handler handler) {
    try {
      handler.handle(this);
      return this;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
