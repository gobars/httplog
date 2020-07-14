package com.github.gobars.httplog.snack.from;

import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.httplog.snack.core.Cnf;
import com.github.gobars.httplog.snack.core.Ctx;
import com.github.gobars.httplog.snack.core.Feature;
import com.github.gobars.httplog.snack.core.exts.FieldWrap;
import com.github.gobars.httplog.snack.core.utils.BeanUtil;
import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.Clob;
import java.text.SimpleDateFormat;
import java.util.*;

/** 对象转换器（将 java Object 转为 ONode） */
public class ObjectFromer implements Fromer {
  @Override
  public void handle(Ctx ctx) {
    ctx.target = analyse(ctx.config, ctx.source); // 如果是null,会返回 ONode.Null
  }

  @SuppressWarnings("unchecked")
  private Onode analyse(Cnf cnf, Object source) {

    Onode rst = new Onode(cnf);

    if (source == null) {
      return rst;
    }

    Class<?> clz = source.getClass();

    if (source instanceof Onode) {
      rst.val(source);
    } else if (source instanceof String) {
      rst.val().setString((String) source);
    } else if (source instanceof Date) {
      rst.val().setDate((Date) source);
    } else if (source instanceof Integer) {
      rst.val().setInteger((Integer) source);
    } else if (source instanceof Long) {
      rst.val().setInteger((Long) source);
    } else if (source instanceof Float) {
      rst.val().setDecimal((Float) source);
    } else if (source instanceof Double) {
      rst.val().setDecimal((Double) source);
    } else if (source instanceof Short) { // 新补充的类型
      rst.val().setInteger((Short) source);
    } else if (source instanceof Character) { // 新补充的类型
      rst.val().setInteger((Character) source);
    } else if (source instanceof Byte) { // 新补充的类型
      rst.val().setInteger((Byte) source);
    } else if (source instanceof Boolean) {
      rst.val().setBool((boolean) source);
    } else if (source instanceof Number) {
      rst.val().setBigNumber((Number) source);
    } else if (source instanceof Throwable) { // 新补充的类型
      analyseBean(cnf, rst, clz, source);
    } else if (analyseArray(cnf, rst, clz, source)) { // 新补充的类型::可适用任何数组

    } else if (clz.isEnum()) { // 新补充的类型
      Enum em = (Enum) source;

      if (cnf.hasFeature(Feature.EnumUsingName)) {
        rst.val().setString(em.name());
      } else {
        rst.val().setInteger(em.ordinal());
      }
    } else if (source instanceof Map) {
      // 为序列化添加特性支持
      if (cnf.hasFeature(Feature.WriteClassName)) {
        typeSet(cnf, rst, clz);
      }

      rst.asObject();
      Map map = ((Map) source);
      for (Object k : map.keySet()) {
        if (k != null) {
          rst.setNode(k.toString(), analyse(cnf, map.get(k)));
        }
      }
    } else if (source instanceof Iterable) {
      rst.asArray();
      Onode ary = rst;
      // 为序列化添加特性支持
      if (cnf.hasFeature(Feature.WriteArrayClassName)) {
        rst.add(typeSet(cnf, new Onode(cnf), clz));
        ary = rst.addNew().asArray();
      }

      for (Object o : ((Iterable) source)) {
        ary.addNode(analyse(cnf, o));
      }
    } else if (source instanceof Enumeration) { // 新补充的类型
      rst.asArray();
      Enumeration o = (Enumeration) source;
      while (o.hasMoreElements()) {
        rst.addNode(analyse(cnf, o.nextElement()));
      }
    } else {
      String clzName = clz.getName();

      if (clzName.endsWith(".Undefined")) {
        rst.val().setNull();
      } else {
        if (analyseOther(cnf, rst, clz, source) == false) {
          if (clzName.startsWith("jdk.") == false) {
            analyseBean(cnf, rst, clz, source);
          }
        }
      }
    }

    return rst;
  }

  private Onode typeSet(Cnf cnf, Onode o, Class<?> clz) {
    return o.set(cnf.typeKey, clz.getName());
  }

  private boolean analyseArray(Cnf cnf, Onode rst, Class<?> clz, Object obj) {
    if (obj instanceof Object[]) {
      rst.asArray();
      for (Object o : ((Object[]) obj)) {
        rst.addNode(analyse(cnf, o));
      }
    } else if (obj instanceof byte[]) {
      rst.asArray();
      for (byte o : ((byte[]) obj)) {
        rst.addNode(analyse(cnf, o));
      }
    } else if (obj instanceof short[]) {
      rst.asArray();
      for (short o : ((short[]) obj)) {
        rst.addNode(analyse(cnf, o));
      }
    } else if (obj instanceof int[]) {
      rst.asArray();
      for (int o : ((int[]) obj)) {
        rst.addNode(analyse(cnf, o));
      }
    } else if (obj instanceof long[]) {
      rst.asArray();
      for (long o : ((long[]) obj)) {
        rst.addNode(analyse(cnf, o));
      }
    } else if (obj instanceof float[]) {
      rst.asArray();
      for (float o : ((float[]) obj)) {
        rst.addNode(analyse(cnf, o));
      }
    } else if (obj instanceof double[]) {
      rst.asArray();
      for (double o : ((double[]) obj)) {
        rst.addNode(analyse(cnf, o));
      }
    } else if (obj instanceof boolean[]) {
      rst.asArray();
      for (boolean o : ((boolean[]) obj)) {
        rst.addNode(analyse(cnf, o));
      }
    } else if (obj instanceof char[]) {
      rst.asArray();
      for (char o : ((char[]) obj)) {
        rst.addNode(analyse(cnf, o));
      }
    } else {
      return false;
    }

    return true;
  }

  private boolean analyseBean(Cnf cnf, Onode rst, Class<?> clz, Object obj) {
    rst.asObject();

    // 为序列化添加特性支持
    if (cnf.hasFeature(Feature.WriteClassName)) {
      rst.set(cnf.typeKey, clz.getName());
    }

    Collection<FieldWrap> list = BeanUtil.getAllFields(clz);
    for (FieldWrap f : list) {
      Object val = f.get(obj);

      if (val == null) {
        // null string 是否以 空字符处理
        if (cnf.hasFeature(Feature.StringFieldInitEmpty) && f.type == String.class) {
          rst.setNode(f.name(), analyse(cnf, ""));
          continue;
        }

        // null是否输出
        if (cnf.hasFeature(Feature.SerializeNulls)) {
          rst.setNode(f.name(), analyse(cnf, null));
        }
        continue;
      }

      if (!val.equals(obj)) { // null 和 自引用 不需要处理
        rst.setNode(f.name(), analyse(cnf, val));
      }
    }

    return true;
  }

  @SuppressWarnings("unchecked")
  private boolean analyseOther(Cnf cnf, Onode rst, Class<?> clz, Object obj) {
    if (obj instanceof SimpleDateFormat) {
      rst.set(cnf.typeKey, clz.getName());
      rst.set("val", ((SimpleDateFormat) obj).toPattern());
    } else if (clz == Class.class) {
      rst.val().setString(clz.getName());
    } else if (obj instanceof InetSocketAddress) {
      InetSocketAddress address = (InetSocketAddress) obj;
      InetAddress inetAddress = address.getAddress();
      rst.set("address", inetAddress.getHostAddress());
      rst.set("port", address.getPort());
    } else if (obj instanceof File) {
      rst.val().setString(((File) obj).getPath());
    } else if (obj instanceof InetAddress) {
      rst.val().setString(((InetAddress) obj).getHostAddress());
    } else if (obj instanceof TimeZone) {
      rst.val().setString(((TimeZone) obj).getID());
    } else if (obj instanceof Currency) {
      rst.val().setString(((Currency) obj).getCurrencyCode());
    } else if (obj instanceof Iterator) {
      rst.asArray();
      ((Iterator) obj)
          .forEachRemaining(
              v -> {
                rst.add(analyse(cnf, v));
              });
    } else if (obj instanceof Map.Entry) {
      Map.Entry kv = (Map.Entry) obj;
      Object k = kv.getKey();
      Object v = kv.getValue();
      rst.asObject();
      if (k != null) {
        rst.set(k.toString(), analyse(cnf, v));
      }
    } else if (obj instanceof Calendar) {
      rst.val().setDate(((Calendar) obj).getTime());
    } else if (obj instanceof Clob) {
      rst.val().setString(BeanUtil.clobToString((Clob) obj));
    } else if (obj instanceof Appendable) {
      rst.val().setString(obj.toString());
    } else {
      return false;
    }

    return true;
  }
}
