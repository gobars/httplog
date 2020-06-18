package com.github.gobars.httplog.snack;

import com.github.gobars.httplog.snack.core.Constants;
import com.github.gobars.httplog.snack.core.Context;
import com.github.gobars.httplog.snack.core.DEFAULTS;
import com.github.gobars.httplog.snack.core.JsonPath;
import com.github.gobars.httplog.snack.core.exts.Act1;
import com.github.gobars.httplog.snack.core.exts.Act2;
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
public class ONode {
  /** 内部配置 */
  protected Constants co;
  /** 内部数据 */
  protected ONodeData da;

  public ONode() {
    co = Constants.def();
    da = new ONodeData(this);
  }

  public ONode(Constants cfg) {
    da = new ONodeData(this);

    if (cfg == null) {
      co = Constants.def();
    } else {
      co = cfg;
    }
  }

  public static ONode newValue() {
    return new ONode().asValue();
  }

  public static ONode newObject() {
    return new ONode().asObject();
  }

  public static ONode newArray() {
    return new ONode().asArray();
  }

  private static void renameDo(ONode n, String key, String newKey) {
    if (n.isObject()) {
      ONode tmp = n.da.object.get(key);
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
  public static ONode load(Object source) {
    return load(source, null);
  }

  public static ONode load(Object source, Constants cfg) {
    return load(source, cfg, null);
  }

  public static ONode load(Object source, Constants cfg, Fromer fromer) {
    return doLoad(source, source instanceof String, cfg, fromer);
  }

  /** 加载string并生成新节点 */
  public static ONode loadStr(String source) {
    return doLoad(source, true, null, null);
  }

  /** 加载java object并生成新节点 */
  public static ONode loadObj(Object source) {
    return loadObj(source, null);
  }

  /** loadStr 不需要 cfg */
  public static ONode loadObj(Object source, Constants cfg) {
    return doLoad(source, false, cfg, null);
  }

  private static ONode doLoad(Object source, boolean isString, Constants cfg, Fromer fromer) {
    if (fromer == null) {
      if (isString) {
        fromer = DEFAULTS.DEF_STRING_FROMER;
      } else {
        fromer = DEFAULTS.DEF_OBJECT_FROMER;
      }
    }

    if (cfg == null) {
      cfg = Constants.def();
    }

    return (ONode) new Context(cfg, source).handle(fromer).target;
  }

  /** 字会串化 （由序列化器决定格式） */
  public static String stringify(Object source) {
    return stringify(source, Constants.def());
  }

  /** 字会串化 （由序列化器决定格式） */
  public static String stringify(Object source, Constants cfg) {
    // 加载java object，须指定Fromer
    return load(source, cfg, DEFAULTS.DEF_OBJECT_FROMER).toString();
  }

  /** 序列化为 string（由序列化器决定格式） */
  public static String serialize(Object source) {
    // 加载java object，须指定Fromer
    return load(source, Constants.serialize(), DEFAULTS.DEF_OBJECT_FROMER).toJson();
  }

  /** 反序列化为 java object（由返序列化器决定格式） */
  public static <T> T deserialize(String source) {
    return deserialize(source, Object.class);
  }

  ////////////////////
  //
  // 值处理
  //
  ////////////////////

  /** 反序列化为 java object（由返序列化器决定格式） */
  public static <T> T deserialize(String source, Class<?> clz) {
    // 加载String，不需指定Fromer
    return load(source, Constants.serialize(), null).toObject(clz);
  }

  /**
   * Json path select
   *
   * @param jpath json path express
   * @param useStandard use standard mode(default: false)
   * @param cacheJpath cache json path parsing results
   * @return ONode
   */
  public ONode select(String jpath, boolean useStandard, boolean cacheJpath) {
    return JsonPath.eval(this, jpath, useStandard, cacheJpath);
  }

  public ONode select(String jpath, boolean useStandard) {
    return select(jpath, useStandard, true);
  }

  public ONode select(String jpath) {
    return select(jpath, false);
  }

  /**
   * 将节点切换为对象
   *
   * @return self:ONode
   */
  public ONode asObject() {
    da.tryInitObject();
    return this;
  }

  /**
   * 将节点切换为数组
   *
   * @return self:ONode
   */
  public ONode asArray() {
    da.tryInitArray();
    return this;
  }

  /**
   * 将节点切换为值
   *
   * @return self:ONode
   */
  public ONode asValue() {
    da.tryInitValue();
    return this;
  }

  /**
   * 将节点切换为null
   *
   * @return self:ONode
   */
  public ONode asNull() {
    da.tryInitNull();
    return this;
  }

  /**
   * 节点数据
   *
   * @return ONodeData
   * @see ONodeData
   */
  public ONodeData nodeData() {
    return da;
  }

  /**
   * 节点类型
   *
   * @return ONodeType
   * @see ONodeType
   */
  public ONodeType nodeType() {
    return da.nodeType;
  }

  /**
   * 切换配置
   *
   * @param cfg 常量配置
   * @return self:ONode
   */
  public ONode cfg(Constants cfg) {
    if (cfg != null) {
      co = cfg;
    }
    return this;
  }

  public Constants cfg() {
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
  public ONode build(Act1<ONode> fun) {
    fun.run(this);
    return this;
  }

  /**
   * 获取节点值数据结构体（如果不是值类型，会自动转换）
   *
   * @return OValue
   */
  public OValue val() {
    return asValue().da.value;
  }

  ////////////////////
  //
  // 对象处理
  //
  ////////////////////

  /**
   * 设置节点值
   *
   * @param val 为常规类型或ONode
   * @return self:ONode
   */
  public ONode val(Object val) {
    if (val == null) {
      da.tryInitNull();
    } else if (val instanceof ONode) { // 支持数据直接copy
      da.tryInitNull();
      da = ((ONode) val).da;
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
  public Map<String, ONode> obj() {
    return asObject().da.object;
  }

  /** 只读模式 get(key) 不会自动产生新节点 */
  public ONode readonly(boolean readonly) {
    co.readonly = readonly;
    return this;
  }

  ////////////////////
  //
  // 数组处理
  //
  ////////////////////

  public ONode readonly() {
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
  public ONode rename(String key, String newKey) {
    if (isObject()) {
      renameDo(this, key, newKey);
    } else if (isArray()) {
      for (ONode n : da.array) {
        renameDo(n, key, newKey);
      }
    }

    return this;
  }

  /** 获取对象子节点（不存在，生成新的子节点并返回） */
  public ONode get(String key) {
    da.tryInitObject();

    ONode tmp = da.object.get(key);
    if (tmp == null) {
      tmp = new ONode(co);

      if (!co.readonly) {
        da.object.put(key, tmp);
      }
    }

    return tmp;
  }

  /** 获取对象子节点（不存在，返回null） */
  public ONode getOrNull(String key) {
    if (isObject()) {
      return da.object.get(key);
    } else {
      return null;
    }
  }

  /** 生成新的对象子节点，会清除之前的数据 */
  public ONode getNew(String key) {
    ONode tmp = new ONode(co);
    da.object.put(key, tmp);

    return tmp;
  }

  /** 设置对象的子节点（会自动处理类型） */
  public ONode set(String key, Object val) {
    da.tryInitObject();

    if (val instanceof ONode) {
      da.object.put(key, ((ONode) val));
    } else {
      da.object.put(key, new ONode(co).val(val));
    }

    return this;
  }

  /** 设置对象的子节点，值为ONode类型 */
  public ONode setNode(String key, ONode val) {
    da.object.put(key, val);
    return this;
  }

  /**
   * 设置对象的子节点，将obj的子节点搬过来
   *
   * @param obj 对象类型的节点
   * @return self:ONode
   */
  public ONode setAll(ONode obj) {
    da.tryInitObject();

    if (obj != null && obj.isObject()) {
      da.object.putAll(obj.da.object);
    }

    return this;
  }

  /** 设置对象的子节点，将map的成员搬过来 */
  public <T> ONode setAll(Map<String, T> map) {
    da.tryInitObject();

    if (map != null) {
      map.forEach(this::set);
    }
    return this;
  }

  //////////////////////

  /** 设置对象的子节点，将map的成员搬过来，并交由代理处置 */
  public <T> ONode setAll(Map<String, T> map, Act2<ONode, T> handler) {
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
  public List<ONode> ary() {
    return asArray().da.array;
  }

  /** 获取数组子节点（超界，返回空节点） //支持倒数取 */
  public ONode get(int index) {
    da.tryInitArray();

    if (index >= 0 && da.array.size() > index) {
      return da.array.get(index);
    }

    return new ONode();
  }

  //////////////////////

  /** 获取数组子节点（超界，返回null） */
  public ONode getOrNull(int index) {
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

  ////////////////////
  //
  // 特性处理
  //
  ////////////////////

  /** 生成新的数组子节点 */
  public ONode addNew() {
    da.tryInitArray();
    ONode n = new ONode(co);
    da.array.add(n);
    return n;
  }

  /**
   * 添加数组子节点
   *
   * @param val 为常规类型或ONode
   * @return self:ONode
   */
  public ONode add(Object val) {
    da.tryInitArray();

    if (val instanceof ONode) {
      da.array.add((ONode) val);
    } else {
      da.array.add(new ONode(co).val(val));
    }

    return this;
  }

  /** 添加数组子节点，值为ONode类型 */
  public ONode addNode(ONode val) {
    da.array.add(val);
    return this;
  }

  ////////////////////
  //
  // 转换操作
  //
  ////////////////////

  /** 添加数组子节点，将ary的子节点搬过来 */
  public ONode addAll(ONode ary) {
    da.tryInitArray();

    if (ary != null && ary.isArray()) {
      da.array.addAll(ary.da.array);
    }

    return this;
  }

  /** 添加数组子节点，将ary的成员点搬过来 */
  public <T> ONode addAll(Collection<T> ary) {
    da.tryInitArray();

    if (ary != null) {
      ary.forEach(m -> add(m));
    }
    return this;
  }

  /** 添加数组子节点，将ary的成员点搬过来，并交由代理处置 */
  public <T> ONode addAll(Collection<T> ary, Act2<ONode, T> handler) {
    da.tryInitArray();

    if (ary != null) {
      ary.forEach(m -> handler.run(addNew(), m));
    }
    return this;
  }

  /** 检查节点是否为null */
  public boolean isNull() {
    return (da.nodeType == ONodeType.Null) || (isValue() && da.value.isNull());
  }

  /** 检查节点是否为值 */
  public boolean isValue() {
    return da.nodeType == ONodeType.Value;
  }

  /** 检查节点是否为对象 */
  public boolean isObject() {
    return da.nodeType == ONodeType.Object;
  }

  /** 检查节点是否为数组 */
  public boolean isArray() {
    return da.nodeType == ONodeType.Array;
  }

  /** 遍历对象的子节点 */
  public ONode forEach(BiConsumer<String, ONode> consumer) {
    if (isObject()) {
      da.object.forEach(consumer);
    }

    return this;
  }

  /** 遍历数组的子节点 */
  public ONode forEach(Consumer<ONode> consumer) {
    if (isArray()) {
      da.array.forEach(consumer);
    }

    return this;
  }

  /** 获取特性 */
  public String attrGet(String key) {
    return da.attrGet(key);
  }

  ////////////////////
  //
  // 来源加载
  //
  ////////////////////

  /** 设置特性 */
  public ONode attrSet(String key, String val) {
    da.attrSet(key, val);
    return this;
  }

  /** 遍历特性 */
  public ONode attrForeach(BiConsumer<String, String> consumer) {
    if (da.attrs != null) {
      da.attrs.forEach(consumer);
    }
    return this;
  }

  /** 将当前ONode 转为 string（由 stringToer 决定） */
  @Override
  public String toString() {
    return to(DEFAULTS.DEF_STRING_TOER);
  }

  /** 将当前ONode 转为 json string */
  public String toJson() {
    return to(DEFAULTS.DEF_JSON_TOER);
  }

  /** 将当前ONode 转为 数据结构体（Map or List or val） */
  public Object toData() {
    return to(DEFAULTS.DEF_OBJECT_TOER);
  }

  /** 将当前ONode 转为 java object */
  public <T> T toObject(Class<?> clz) {
    return to(DEFAULTS.DEF_OBJECT_TOER, clz);
  }

  public <T> List<T> toArray(Class<T> clz) {
    List<T> list = new ArrayList<>();

    for (ONode n : ary()) {
      list.add(n.toObject(clz));
    }

    return list;
  }

  ////////////////////
  //
  // 字符串化
  //
  ////////////////////

  /** 将当前ONode 通过 toer 进行转换 */
  @SuppressWarnings("unchecked")
  public <T> T to(Toer toer, Class<?> clz) {
    return (T) (new Context(co, this, clz).handle(toer).target);
  }

  public <T> T to(Toer toer) {
    return to(toer, null);
  }

  ////////////////////
  //
  // 序列化
  //
  ////////////////////

  /**
   * 填充数据（如有问题会跳过，不会出异常）
   *
   * @param source 可以是 String 或 java object 数据
   * @return self:ONode
   */
  public ONode fill(Object source) {
    val(doLoad(source, source instanceof String, co, null));
    return this;
  }

  /** @param fromer 来源处理器 */
  public ONode fill(Object source, Fromer fromer) {
    val(doLoad(source, source instanceof String, co, fromer));
    return this;
  }

  public ONode fillObj(Object source, Constants cfg) {
    val(doLoad(source, false, cfg, null));
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
      if (o instanceof ONode) {
        return Objects.equals(ary(), ((ONode) o).ary());
      } else {
        return Objects.equals(ary(), o);
      }
    }

    if (isObject()) {
      if (o instanceof ONode) {
        return Objects.equals(obj(), ((ONode) o).obj());
      } else {
        return Objects.equals(obj(), o);
      }
    }

    if (isValue()) {
      if (o instanceof ONode) {
        return Objects.equals(val(), ((ONode) o).val());
      } else {
        return Objects.equals(val(), o);
      }
    }

    // 最后是null type
    if (o instanceof ONode) {
      return ((ONode) o).isNull(); // 都是 null
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return da.hashCode();
  }
}
