package com.github.gobars.httplog.snack;

import com.github.gobars.httplog.snack.core.Cnf;
import com.github.gobars.httplog.snack.core.Ctx;
import com.github.gobars.httplog.snack.core.Defaults;
import com.github.gobars.httplog.snack.core.JsonPath;
import com.github.gobars.httplog.snack.core.exts.Run1;
import com.github.gobars.httplog.snack.core.exts.Run2;
import com.github.gobars.httplog.snack.from.Fromer;
import com.github.gobars.httplog.snack.to.Toer;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 节点（One Node）
 *
 * @author noear
 */
public class Onode {
  /** 内部配置 */
  protected Cnf co;
  /** 内部数据 */
  protected Odata da;

  public Onode() {
    co = Cnf.def();
    da = new Odata(this);
  }

  public Onode(Cnf cnf) {
    da = new Odata(this);

    if (cnf == null) {
      co = Cnf.def();
    } else {
      co = cnf;
    }
  }

  public static Onode newValue() {
    return new Onode().asValue();
  }

  public static Onode newObject() {
    return new Onode().asObject();
  }

  public static Onode newArray() {
    return new Onode().asArray();
  }

  private static void renameDo(Onode n, String key, String newKey) {
    if (n.isObject()) {
      Onode tmp = n.da.object.get(key);
      if (tmp != null) {
        n.da.object.put(newKey, tmp);
        n.da.object.remove(key);
      }
    }
  }

  /**
   * 加载数据并生成新节点（如果异常，会生成空ONode）
   *
   * @param source 可以是 String 或 java object 数据
   * @return new:ONode
   */
  public static Onode load(Object source) {
    return load(source, null);
  }

  public static Onode load(Object source, Cnf cnf) {
    return load(source, cnf, null);
  }

  public static Onode load(Object source, Cnf cnf, Fromer fromer) {
    return doLoad(source, source instanceof String, cnf, fromer);
  }

  /** 加载string并生成新节点 */
  public static Onode loadStr(String source) {
    return doLoad(source, true, null, null);
  }

  /** 加载java object并生成新节点 */
  public static Onode loadObj(Object source) {
    return loadObj(source, null);
  }

  /** loadStr 不需要 cfg */
  public static Onode loadObj(Object source, Cnf cnf) {
    return doLoad(source, false, cnf, null);
  }

  private static Onode doLoad(Object source, boolean isString, Cnf cnf, Fromer fromer) {
    if (fromer == null) {
      fromer = isString ? Defaults.DEF_STRING_FROMER : Defaults.DEF_OBJECT_FROMER;
    }

    if (cnf == null) {
      cnf = Cnf.def();
    }

    return (Onode) new Ctx(cnf, source).handle(fromer).target;
  }

  /** 字会串化 （由序列化器决定格式） */
  public static String stringify(Object source) {
    return stringify(source, Cnf.def());
  }

  /** 字会串化 （由序列化器决定格式） */
  public static String stringify(Object source, Cnf cnf) {
    // 加载java object，须指定Fromer
    return load(source, cnf, Defaults.DEF_OBJECT_FROMER).toString();
  }

  /** 序列化为 string（由序列化器决定格式） */
  public static String serialize(Object source) {
    // 加载java object，须指定Fromer
    return load(source, Cnf.serialize(), Defaults.DEF_OBJECT_FROMER).toJson();
  }

  /** 反序列化为 java object（由返序列化器决定格式） */
  public static <T> T deserialize(String source) {
    return deserialize(source, Object.class);
  }

  // 值处理

  /** 反序列化为 java object（由返序列化器决定格式） */
  public static <T> T deserialize(String source, Class<?> clz) {
    // 加载String，不需指定Fromer
    return load(source, Cnf.serialize(), null).toObject(clz);
  }

  /**
   * Json path select
   *
   * @param jpath json path express
   * @param useStandard use standard mode(default: false)
   * @param cacheJpath cache json path parsing results
   * @return ONode
   */
  public Onode select(String jpath, boolean useStandard, boolean cacheJpath) {
    return JsonPath.eval(this, jpath, useStandard, cacheJpath);
  }

  public Onode select(String jpath, boolean useStandard) {
    return select(jpath, useStandard, true);
  }

  public Onode select(String jpath) {
    return select(jpath, false);
  }

  /**
   * 将节点切换为对象
   *
   * @return self:ONode
   */
  public Onode asObject() {
    da.tryInitObject();
    return this;
  }

  /**
   * 将节点切换为数组
   *
   * @return self:ONode
   */
  public Onode asArray() {
    da.tryInitArray();
    return this;
  }

  /**
   * 将节点切换为值
   *
   * @return self:ONode
   */
  public Onode asValue() {
    da.tryInitValue();
    return this;
  }

  /**
   * 将节点切换为null
   *
   * @return self:ONode
   */
  public Onode asNull() {
    da.tryInitNull();
    return this;
  }

  /**
   * 节点数据
   *
   * @return ONodeData
   * @see Odata
   */
  public Odata nodeData() {
    return da;
  }

  /**
   * 节点类型
   *
   * @return ONodeType
   * @see OnodeType
   */
  public OnodeType nodeType() {
    return da.nodeType;
  }

  /**
   * 切换配置
   *
   * @param cnf 常量配置
   * @return self:ONode
   */
  public Onode cfg(Cnf cnf) {
    if (cnf != null) {
      co = cnf;
    }
    return this;
  }

  public Cnf cfg() {
    return co;
  }

  ////////////////////
  //
  // 对象与数组公共处理
  //
  ////////////////////

  /**
   * 构建表达式
   *
   * @param fun lambda表达式
   * @return self:ONode
   */
  public Onode build(Run1<Onode> fun) {
    fun.run(this);
    return this;
  }

  /**
   * 获取节点值数据结构体（如果不是值类型，会自动转换）
   *
   * @return OValue
   */
  public Ovalue val() {
    return asValue().da.value;
  }

  // 对象处理

  /**
   * 设置节点值
   *
   * @param val 为常规类型或ONode
   * @return self:ONode
   */
  public Onode val(Object val) {
    if (val == null) {
      da.tryInitNull();
    } else if (val instanceof Onode) { // 支持数据直接copy
      da.tryInitNull();
      da = ((Onode) val).da;
    } else {
      da.tryInitValue();
      da.value.set(val);
    }

    return this;
  }

  /** 获取节点值并以 String 输出 如果节点为对象或数组类型，则输出json */
  public String getString() {
    if (isValue()) {
      return da.value.getString();
    } else {
      if (isArray()) {
        return toJson();
      }

      if (isObject()) {
        return toJson();
      }

      return co.nullString();
    }
  }

  /** 获取节点值并以 short 输出 */
  public short getShort() {
    if (isValue()) {
      return da.value.getShort();
    }

    return 0;
  }

  /** 获取节点值并以 int 输出 */
  public int getInt() {
    if (isValue()) {
      return da.value.getInt();
    }

    return 0;
  }

  /** 获取节点值并以 boolean 输出 */
  public boolean getBoolean() {
    if (isValue()) {
      return da.value.getBoolean();
    }

    return false;
  }

  /** 获取节点值并以 long 输出 */
  public long getLong() {
    if (isValue()) {
      return da.value.getLong();
    }
    return 0;
  }

  /** 获取节点值并以 Date 输出 */
  public Date getDate() {
    if (isValue()) {
      return da.value.getDate();
    }

    return null;
  }

  /** 获取节点值并以 float 输出 */
  public float getFloat() {
    if (isValue()) {
      return da.value.getFloat();
    }

    return 0;
  }

  /** 获取节点值并以 double 输出 */
  public double getDouble() {
    if (isValue()) {
      return da.value.getDouble();
    }

    return 0;
  }

  /** 获取节点值并以 double 输出 */
  public double getDouble(int scale) {
    double temp = getDouble();

    if (temp == 0) {
      return 0;
    }

    return BigDecimal.valueOf(temp).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
  }

  /** 获取节点值并以 char 输出 */
  public char getChar() {
    if (isValue()) {
      return da.value.getChar();
    }

    return 0;
  }

  /** 清空子节点（对象或数组有效） */
  public void clear() {
    if (isObject()) {
      da.object.clear();
    } else if (isArray()) {
      da.array.clear();
    }
  }

  /** 子节点数量（对象或数组有效） */
  public int count() {
    if (isObject()) {
      return da.object.size();
    }

    if (isArray()) {
      return da.array.size();
    }

    return 0;
  }

  /** 获取节点对象数据结构体（如果不是对象类型，会自动转换） */
  public Map<String, Onode> obj() {
    return asObject().da.object;
  }

  /** 只读模式 get(key) 不会自动产生新节点 */
  public Onode readonly(boolean readonly) {
    co.readonly = readonly;
    return this;
  }

  ////////////////////
  //
  // 数组处理
  //
  ////////////////////

  public Onode readonly() {
    return readonly(true);
  }

  /** 是否存在对象子节点 */
  public boolean contains(String key) {
    if (isObject()) {
      return da.object.containsKey(key);
    } else {
      return false;
    }
  }

  /** 重命名一个子节点（如果不存在则跳过） */
  public Onode rename(String key, String newKey) {
    if (isObject()) {
      renameDo(this, key, newKey);
    } else if (isArray()) {
      for (Onode n : da.array) {
        renameDo(n, key, newKey);
      }
    }

    return this;
  }

  /** 获取对象子节点（不存在，生成新的子节点并返回） */
  public Onode get(String key) {
    da.tryInitObject();

    Onode tmp = da.object.get(key);
    if (tmp == null) {
      tmp = new Onode(co);

      if (!co.readonly) {
        da.object.put(key, tmp);
      }
    }

    return tmp;
  }

  /** 获取对象子节点（不存在，返回null） */
  public Onode getOrNull(String key) {
    if (isObject()) {
      return da.object.get(key);
    } else {
      return null;
    }
  }

  /** 生成新的对象子节点，会清除之前的数据 */
  public Onode getNew(String key) {
    Onode tmp = new Onode(co);
    da.object.put(key, tmp);

    return tmp;
  }

  /** 设置对象的子节点（会自动处理类型） */
  public Onode set(String key, Object val) {
    da.tryInitObject();

    if (val instanceof Onode) {
      da.object.put(key, ((Onode) val));
    } else {
      da.object.put(key, new Onode(co).val(val));
    }

    return this;
  }

  /** 设置对象的子节点，值为ONode类型 */
  public Onode setNode(String key, Onode val) {
    da.object.put(key, val);
    return this;
  }

  /**
   * 设置对象的子节点，将obj的子节点搬过来
   *
   * @param obj 对象类型的节点
   * @return self:ONode
   */
  public Onode setAll(Onode obj) {
    da.tryInitObject();

    if (obj != null && obj.isObject()) {
      da.object.putAll(obj.da.object);
    }

    return this;
  }

  /** 设置对象的子节点，将map的成员搬过来 */
  public <T> Onode setAll(Map<String, T> map) {
    da.tryInitObject();

    if (map != null) {
      map.forEach(this::set);
    }
    return this;
  }

  /** 设置对象的子节点，将map的成员搬过来，并交由代理处置 */
  public <T> Onode setAll(Map<String, T> map, Run2<Onode, T> handler) {
    da.tryInitObject();

    if (map != null) {
      map.forEach((k, v) -> handler.run(this.get(k), v));
    }
    return this;
  }

  /** 移除对象的子节点 (搞不清楚是自身还是被移除的，所以不返回) */
  public void remove(String key) {
    if (isObject()) {
      da.object.remove(key);
    }
  }

  /** 获取节点数组数据结构体（如果不是数组，会自动转换） */
  public List<Onode> ary() {
    return asArray().da.array;
  }

  /** 获取数组子节点（超界，返回空节点） //支持倒数取 */
  public Onode get(int index) {
    da.tryInitArray();

    if (index >= 0 && da.array.size() > index) {
      return da.array.get(index);
    }

    return new Onode();
  }

  /** 获取数组子节点（超界，返回null） */
  public Onode getOrNull(int index) {
    if (isArray()) {
      if (index >= 0 && da.array.size() > index) {
        return da.array.get(index);
      }
    }

    return null;
  }

  /** 移除数组的子节点(搞不清楚是自身还是被移除的，所以不返回) */
  public void removeAt(int index) {
    if (isArray()) {
      da.array.remove(index);
    }
  }

  // 特性处理

  /** 生成新的数组子节点 */
  public Onode addNew() {
    da.tryInitArray();
    Onode n = new Onode(co);
    da.array.add(n);
    return n;
  }

  /**
   * 添加数组子节点
   *
   * @param val 为常规类型或ONode
   * @return self:ONode
   */
  public Onode add(Object val) {
    da.tryInitArray();

    if (val instanceof Onode) {
      da.array.add((Onode) val);
    } else {
      da.array.add(new Onode(co).val(val));
    }

    return this;
  }

  /** 添加数组子节点，值为ONode类型 */
  public Onode addNode(Onode val) {
    da.array.add(val);
    return this;
  }

  // 转换操作

  /** 添加数组子节点，将ary的子节点搬过来 */
  public Onode addAll(Onode ary) {
    da.tryInitArray();

    if (ary != null && ary.isArray()) {
      da.array.addAll(ary.da.array);
    }

    return this;
  }

  /** 添加数组子节点，将ary的成员点搬过来 */
  public <T> Onode addAll(Collection<T> ary) {
    da.tryInitArray();

    if (ary != null) {
      ary.forEach(m -> add(m));
    }
    return this;
  }

  /** 添加数组子节点，将ary的成员点搬过来，并交由代理处置 */
  public <T> Onode addAll(Collection<T> ary, Run2<Onode, T> handler) {
    da.tryInitArray();

    if (ary != null) {
      ary.forEach(m -> handler.run(addNew(), m));
    }
    return this;
  }

  /** 检查节点是否为null */
  public boolean isNull() {
    return (da.nodeType == OnodeType.Null) || (isValue() && da.value.isNull());
  }

  /** 检查节点是否为值 */
  public boolean isValue() {
    return da.nodeType == OnodeType.Value;
  }

  /** 检查节点是否为对象 */
  public boolean isObject() {
    return da.nodeType == OnodeType.Object;
  }

  /** 检查节点是否为数组 */
  public boolean isArray() {
    return da.nodeType == OnodeType.Array;
  }

  /** 遍历对象的子节点 */
  public Onode forEach(BiConsumer<String, Onode> consumer) {
    if (isObject()) {
      da.object.forEach(consumer);
    }

    return this;
  }

  /** 遍历数组的子节点 */
  public Onode forEach(Consumer<Onode> consumer) {
    if (isArray()) {
      da.array.forEach(consumer);
    }

    return this;
  }

  /** 获取特性 */
  public String attrGet(String key) {
    return da.attrGet(key);
  }

  // 来源加载

  /** 设置特性 */
  public Onode attrSet(String key, String val) {
    da.attrSet(key, val);
    return this;
  }

  /** 遍历特性 */
  public Onode attrForeach(BiConsumer<String, String> consumer) {
    if (da.attrs != null) {
      da.attrs.forEach(consumer);
    }
    return this;
  }

  /** 将当前ONode 转为 string（由 stringToer 决定） */
  @Override
  public String toString() {
    return to(Defaults.DEF_STRING_TOER);
  }

  /** 将当前ONode 转为 json string */
  public String toJson() {
    return to(Defaults.DEF_JSON_TOER);
  }

  /** 将当前ONode 转为 数据结构体（Map or List or val） */
  public Object toData() {
    return to(Defaults.DEF_OBJECT_TOER);
  }

  /** 将当前ONode 转为 java object */
  public <T> T toObject(Class<?> clz) {
    return to(Defaults.DEF_OBJECT_TOER, clz);
  }

  public <T> List<T> toArray(Class<T> clz) {
    List<T> list = new ArrayList<>();

    for (Onode n : ary()) {
      list.add(n.toObject(clz));
    }

    return list;
  }

  // 字符串化

  /** 将当前ONode 通过 toer 进行转换 */
  @SuppressWarnings("unchecked")
  public <T> T to(Toer toer, Class<?> clz) {
    return (T) (new Ctx(co, this, clz).handle(toer).target);
  }

  public <T> T to(Toer toer) {
    return to(toer, null);
  }

  // 序列化

  /**
   * 填充数据（如有问题会跳过，不会出异常）
   *
   * @param source 可以是 String 或 java object 数据
   * @return self:ONode
   */
  public Onode fill(Object source) {
    val(doLoad(source, source instanceof String, co, null));
    return this;
  }

  /** @param fromer 来源处理器 */
  public Onode fill(Object source, Fromer fromer) {
    val(doLoad(source, source instanceof String, co, fromer));
    return this;
  }

  public Onode fillObj(Object source, Cnf cnf) {
    val(doLoad(source, false, cnf, null));
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null) {
      return isNull();
    }

    if (isArray()) {
      if (o instanceof Onode) {
        return Objects.equals(ary(), ((Onode) o).ary());
      }

      return Objects.equals(ary(), o);
    }

    if (isObject()) {
      if (o instanceof Onode) {
        return Objects.equals(obj(), ((Onode) o).obj());
      }

      return Objects.equals(obj(), o);
    }

    if (isValue()) {
      if (o instanceof Onode) {
        return Objects.equals(val(), ((Onode) o).val());
      }

      return Objects.equals(val(), o);
    }

    // 最后是null type
    if (o instanceof Onode) {
      return ((Onode) o).isNull(); // 都是 null
    }

    return false;
  }

  @Override
  public int hashCode() {
    return da.hashCode();
  }
}
