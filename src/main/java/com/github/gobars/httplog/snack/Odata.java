package com.github.gobars.httplog.snack;

import com.github.gobars.httplog.snack.core.Feature;
import java.util.*;

/** 节点数据 */
public class Odata {
  /** 节点数据的 value */
  public Ovalue value = null;
  /** 节点数据的 object */
  public Map<String, Onode> object = null;
  /** 节点数据的 array */
  public List<Onode> array = null;

  /** 节点类型 */
  public OnodeType nodeType = OnodeType.Null;
  /** 节点的 特性 */
  public Map<String, String> attrs = null;

  protected Onode n;

  public Odata(Onode n) {
    this.n = n;
  }

  public Map<String, Onode> object() {
    tryInitObject();
    return object;
  }

  public List<Onode> array() {
    tryInitArray();
    return array;
  }

  public Ovalue value() {
    tryInitValue();
    return value;
  }

  /** 尝试初始化为 null */
  protected void tryInitNull() {
    if (nodeType != OnodeType.Null) {
      nodeType = OnodeType.Null;

      if (object != null) {
        object.clear();
        object = null;
      }

      if (array != null) {
        array.clear();
        array = null;
      }

      value = null;
    }
  }

  /** 尝试初始化为 value */
  protected void tryInitValue() {
    if (nodeType != OnodeType.Value) {
      nodeType = OnodeType.Value;

      if (value == null) {
        value = new Ovalue(n);
      }
    }
  }

  /** 尝试初始化为 object */
  protected void tryInitObject() {
    if (nodeType != OnodeType.Object) {
      nodeType = OnodeType.Object;

      if (object == null) {
        if (n.co.hasFeature(Feature.OrderedField)) {
          object = new OnodeLinkedObject();
        } else {
          object = new OnodeObject();
        }
      }
    }
  }

  /** 尝试初始化为 array */
  protected void tryInitArray() {
    if (nodeType != OnodeType.Array) {
      nodeType = OnodeType.Array;

      if (array == null) {
        array = new OnodeArray();
      }
    }
  }

  /** 尝试将 object 换为 array（一般用不到） */
  protected void shiftToArray() {
    tryInitArray();

    if (object != null) {
      array.addAll(object.values());
      object.clear();
      object = null;
    }
  }

  public String attrGet(String key) {
    if (attrs != null) {
      return attrs.get(key);
    }

    return null;
  }

  public void attrSet(String key, String val) {
    if (attrs == null) {
      attrs = new LinkedHashMap<>();
    }

    attrs.put(key, val);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null) {
      return false;
    }

    return this.hashCode() == o.hashCode();
  }

  @Override
  public int hashCode() {
    if (nodeType == OnodeType.Object) {
      return object.hashCode();
    }

    if (nodeType == OnodeType.Array) {
      return array.hashCode();
    }

    if (nodeType == OnodeType.Value) {
      return value.hashCode();
    }

    return 0;
  }

  static class OnodeArray extends ArrayList<Onode> {
    @Override
    public int indexOf(Object o) {
      for (int i = 0; i < size(); i++) {
        if (get(i).equals(o)) {
          return i;
        }
      }

      return -1;
    }
  }

  static class OnodeObject extends HashMap<String, Onode> {
    @Override
    public boolean containsValue(Object value) {
      for (Onode n : values()) {
        if (n.equals(value)) {
          return true;
        }
      }
      return false;
    }
  }

  static class OnodeLinkedObject extends LinkedHashMap<String, Onode> {
    @Override
    public boolean containsValue(Object value) {
      for (Onode n : values()) {
        if (n.equals(value)) {
          return true;
        }
      }
      return false;
    }
  }
}
