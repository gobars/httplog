package com.github.gobars.httplog.snack.core.exts;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/** 泛式类型（引用自：Fastjson） */
public class PtypeImpl implements ParameterizedType {
  private final Type[] actualTypeArguments;
  private final Type ownerType;
  private final Type rawType;

  public PtypeImpl(Type[] actualTypeArguments, Type ownerType, Type rawType) {
    this.actualTypeArguments = actualTypeArguments;
    this.ownerType = ownerType;
    this.rawType = rawType;
  }

  @Override
  public Type[] getActualTypeArguments() {
    return actualTypeArguments;
  }

  @Override
  public Type getOwnerType() {
    return ownerType;
  }

  @Override
  public Type getRawType() {
    return rawType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PtypeImpl that = (PtypeImpl) o;

    if (!Arrays.equals(actualTypeArguments, that.actualTypeArguments)) {
      return false;
    }

    if (!Objects.equals(ownerType, that.ownerType)) {
      return false;
    }

    return Objects.equals(rawType, that.rawType);
  }

  @Override
  public int hashCode() {
    int result = actualTypeArguments != null ? Arrays.hashCode(actualTypeArguments) : 0;
    result = 31 * result + (ownerType != null ? ownerType.hashCode() : 0);
    return 31 * result + (rawType != null ? rawType.hashCode() : 0);
  }
}
