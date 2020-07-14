package com.github.gobars.httplog.snack.to;

import com.github.gobars.httplog.snack.Odata;
import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.httplog.snack.Otype;
import com.github.gobars.httplog.snack.Ovalue;
import com.github.gobars.httplog.snack.core.Ctx;
import com.github.gobars.httplog.snack.core.exts.EnumWrap;
import com.github.gobars.httplog.snack.core.exts.FieldWrap;
import com.github.gobars.httplog.snack.core.utils.BeanUtil;
import com.github.gobars.httplog.snack.core.utils.StrUtil;
import com.github.gobars.httplog.snack.core.utils.TypeUtil;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 对象转换器
 *
 * <p>将 ONode 转为 java Object
 */
@SuppressWarnings("unchecked")
public class ObjectToer implements Toer {
  @Override
  public void handle(Ctx ctx) throws Exception {
    Onode o = (Onode) ctx.source;

    if (null != o) {
      ctx.target = analyse(ctx, o, ctx.targetClz, ctx.targetType);
    }
  }

  private Class<?> getTypeByNode(Ctx ctx, Onode o, Class<?> def) {
    //
    // 下面使用 .ary(), .oby(), .val() 可以减少检查；从而提高性能
    //
    if (ctx.targetType == null) {
      if (o.isObject()) {
        return HashMap.class;
      }

      if (o.isArray()) {
        return ArrayList.class;
      }
    }

    String typeStr = null;
    if (o.isArray() && o.ary().size() == 2) {
      Onode o1 = o.ary().get(0);
      if (o1.isObject() && o1.obj().size() == 1) { // 如果只有一个成员，则可能为list的类型节点
        //
        // 这段，不能与下面的 o.isObject() 复用
        //
        Onode n1 = o1.obj().get(ctx.config.typeKey);
        if (n1 != null) {
          typeStr = n1.val().getString();
        }
      }
    } else if (o.isObject()) {
      Onode n1 = o.obj().get(ctx.config.typeKey);
      if (n1 != null) {
        typeStr = n1.val().getString();
      }
    }

    if (!StrUtil.isEmpty(typeStr)) {
      return BeanUtil.loadClass(typeStr);
    }

    if (def == null || def == Object.class) {
      if (o.isObject()) {
        return HashMap.class;
      }

      if (o.isArray()) {
        return ArrayList.class;
      }
    }

    return def;
  }

  private Object analyse(Ctx ctx, Onode o, Class<?> clz, Type type) throws Exception {
    if (o == null || clz != null && Onode.class.isAssignableFrom(clz)) {
      return o;
    }

    switch (o.nodeType()) {
      case Value:
        return analyseVal(ctx, o.nodeData(), clz);
      case Object:
        clz = getTypeByNode(ctx, o, clz);
        o.remove(ctx.config.typeKey); // 尝试移除类型内容

        if (Map.class.isAssignableFrom(clz)) {
          return analyseMap(ctx, o, clz, type);
        } else if (StackTraceElement.class.isAssignableFrom(clz)) {
          return new StackTraceElement(
              o.get("declaringClass").getString(),
              o.get("methodName").getString(),
              o.get("fileName").getString(),
              o.get("lineNumber").getInt());
        } else {
          return analyseBean(ctx, o, clz);
        }
      case Array:
        clz = getTypeByNode(ctx, o, clz);

        if (clz != null && clz.isArray()) {
          return analyseArray(ctx, o.nodeData(), clz);
        } else {
          return analyseCollection(ctx, o, clz, type);
        }
      default:
        return null;
    }
  }

  private boolean is(Class<?> s, Class<?> t) {
    return s.isAssignableFrom(t);
  }

  public Object analyseVal(Ctx ctx, Odata d, Class<?> clz) {

    Ovalue v = d.value;

    if (v.type() == Otype.Null) {
      return null;
    }

    if (clz == null) {
      return v.getRaw();
    }

    if (is(Byte.class, clz) || clz == Byte.TYPE) {
      return (byte) v.getLong();
    } else if (is(Short.class, clz) || clz == Short.TYPE) {
      return v.getShort();
    } else if (is(Integer.class, clz) || clz == Integer.TYPE) {
      return v.getInt();
    } else if (is(Long.class, clz) || clz == Long.TYPE) {
      return v.getLong();
    } else if (is(Float.class, clz) || clz == Float.TYPE) {
      return v.getFloat();
    } else if (is(Double.class, clz) || clz == Double.TYPE) {
      return v.getDouble();
    } else if (is(Boolean.class, clz) || clz == Boolean.TYPE) {
      return v.getBoolean();
    } else if (is(Character.class, clz) || clz == Character.TYPE) {
      return v.getChar();
    } else if (is(String.class, clz)) {
      return v.getString();
    } else if (is(java.sql.Timestamp.class, clz)) {
      return new java.sql.Timestamp(v.getLong());
    } else if (is(java.sql.Date.class, clz)) {
      return new java.sql.Date(v.getLong());
    } else if (is(java.sql.Time.class, clz)) {
      return new java.sql.Time(v.getLong());
    } else if (is(Date.class, clz)) {
      return v.getDate();
    } else if (is(BigDecimal.class, clz)) {
      if (v.type() == Otype.BigNumber) {
        return v.getRawBignumber();
      } else {
        return new BigDecimal(v.getString());
      }
    } else if (is(BigInteger.class, clz)) {
      if (v.type() == Otype.BigNumber) {
        return v.getRawBignumber();
      } else {
        return new BigInteger(v.getString());
      }
    } else if (clz.isEnum()) {
      return analyseEnum(ctx, d, clz);
    } else if (is(Class.class, clz)) {
      return BeanUtil.loadClass(v.getString());
    } else if (is(Object.class, clz)) {
      return v.getRaw();
    } else {
      throw new RuntimeException("unsupported type " + clz.getName());
    }
  }

  public Object analyseEnum(Ctx ctx, Odata d, Class<?> target) {
    EnumWrap ew = TypeUtil.createEnum(target);
    if (d.value.type() == Otype.String) {
      return ew.get(d.value.getString());
    } else {
      return ew.get(d.value.getInt());
    }
  }

  public Object analyseArray(Ctx ctx, Odata d, Class<?> target) throws Exception {
    int len = d.array.size();

    if (is(byte[].class, target)) {
      byte[] val = new byte[len];
      for (int i = 0; i < len; i++) {
        val[i] = (byte) d.array.get(i).getLong();
      }
      return val;
    } else if (is(short[].class, target)) {
      short[] val = new short[len];
      for (int i = 0; i < len; i++) {
        val[i] = d.array.get(i).getShort();
      }
      return val;
    } else if (is(int[].class, target)) {
      int[] val = new int[len];
      for (int i = 0; i < len; i++) {
        val[i] = d.array.get(i).getInt();
      }
      return val;
    } else if (is(long[].class, target)) {
      long[] val = new long[len];
      for (int i = 0; i < len; i++) {
        val[i] = d.array.get(i).getLong();
      }
      return val;
    } else if (is(float[].class, target)) {
      float[] val = new float[len];
      for (int i = 0; i < len; i++) {
        val[i] = d.array.get(i).getFloat();
      }
      return val;
    } else if (is(double[].class, target)) {
      double[] val = new double[len];
      for (int i = 0; i < len; i++) {
        val[i] = d.array.get(i).getDouble();
      }
      return val;
    } else if (is(boolean[].class, target)) {
      boolean[] val = new boolean[len];
      for (int i = 0; i < len; i++) {
        val[i] = d.array.get(i).getBoolean();
      }
      return val;
    } else if (is(char[].class, target)) {
      char[] val = new char[len];
      for (int i = 0; i < len; i++) {
        val[i] = d.array.get(i).getChar();
      }
      return val;
    } else if (is(String[].class, target)) {
      String[] val = new String[len];
      for (int i = 0; i < len; i++) {
        val[i] = d.array.get(i).getString();
      }
      return val;
    } else if (is(Object[].class, target)) {
      Class<?> c = target.getComponentType();
      Object[] val = (Object[]) Array.newInstance(c, len);
      for (int i = 0; i < len; i++) {
        val[i] = analyse(ctx, d.array.get(i), c, c);
      }
      return val;
    } else {
      throw new RuntimeException("unsupport type " + target.getName());
    }
  }

  @SuppressWarnings("unchecked")
  public Object analyseCollection(Ctx ctx, Onode o, Class<?> clz, Type type) throws Exception {
    Collection list = TypeUtil.createCollection(clz, false);

    if (list == null) {
      return null;
    }

    if (o.count() == 2) {
      Onode o1 = o.get(0);
      // 说明，是有类型的集合
      if (o1.count() == 1 && o1.contains(ctx.config.typeKey)) {
        // 取第二个节点，做为数据节点（第1个为类型节点）
        o = o.get(1);
      }
    }

    Type itemType = null;
    if (ctx.targetType != null) {
      itemType = TypeUtil.getCollectionItemType(type);
    }

    for (Onode o1 : o.nodeData().array) {
      list.add(analyse(ctx, o1, (Class<?>) itemType, itemType));
    }

    return list;
  }

  public Object analyseMap(Ctx ctx, Onode o, Class<?> clz, Type type) throws Exception {
    Map<Object, Object> map = TypeUtil.createMap(clz);

    if (type instanceof ParameterizedType) { // 这里还要再研究下
      ParameterizedType ptt = ((ParameterizedType) type);
      Type kType = ptt.getActualTypeArguments()[0];
      Type vType = ptt.getActualTypeArguments()[1];

      if (kType instanceof ParameterizedType) {
        kType = ((ParameterizedType) kType).getRawType();
      }

      if (vType instanceof ParameterizedType) {
        vType = ((ParameterizedType) vType).getRawType();
      }

      if (kType == String.class) {
        for (Map.Entry<String, Onode> kv : o.nodeData().object.entrySet()) {
          map.put(kv.getKey(), analyse(ctx, kv.getValue(), (Class<?>) vType, vType));
        }
      } else {
        for (Map.Entry<String, Onode> kv : o.nodeData().object.entrySet()) {
          map.put(
              TypeUtil.strTo(kv.getKey(), (Class<?>) kType),
              analyse(ctx, kv.getValue(), (Class<?>) vType, vType));
        }
      }
    } else {
      for (Map.Entry<String, Onode> kv : o.nodeData().object.entrySet()) {
        map.put(kv.getKey(), analyse(ctx, kv.getValue(), null, null));
      }
    }

    return map;
  }

  public Object analyseBean(Ctx ctx, Onode o, Class<?> target) throws Exception {
    if (is(SimpleDateFormat.class, target)) {
      return new SimpleDateFormat(o.get("val").getString());
    }

    if (is(InetSocketAddress.class, target)) {
      return new InetSocketAddress(o.get("address").getString(), o.get("port").getInt());
    }

    Object rst;
    try {
      rst = target.newInstance();
    } catch (Exception ex) {
      throw new Exception("create instance error, class " + target.getName());
    }

    // 遍历每个字段
    for (FieldWrap f : BeanUtil.getAllFields(target)) {
      String key = f.name();

      if (o.contains(key)) {
        f.set(rst, analyse(ctx, o.get(key), f.clz, f.type));
      }
    }
    return rst;
  }
}
