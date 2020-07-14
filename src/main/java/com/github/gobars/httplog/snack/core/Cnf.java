package com.github.gobars.httplog.snack.core;

import com.github.gobars.httplog.snack.core.exts.Run1;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import lombok.val;

/** 参数配置 */
public class Cnf {
  /**
   * 字符串大小限制，超过大小将自动截取并且补充...
   *
   * <p>默认不限制
   */
  public int abbrevMaxSize;
  /** 类型key */
  public String typeKey = Defaults.DEF_TYPE_KEY;

  /** 特性 */
  public int features = Defaults.DEF_FEATURES;
  /** n.get(key)时，只读处理; 即不自动添加新节点 */
  public boolean readonly = false;

  public String dateFormat = Defaults.DEF_DATE_FORMAT_STRING;

  public Cnf() {}

  public Cnf(int features) {
    this.features = features;
  }

  public static Cnf def() {
    return new Cnf(Defaults.featuresDefault);
  }

  public static Cnf serialize() {
    return new Cnf(Defaults.featuresSerialize);
  }

  public static Cnf of(Feature... features) {
    return new Cnf().add(features);
  }

  public Cnf add(Feature... features) {
    for (Feature f : features) {
      this.features = Feature.config(this.features, f, true);
    }
    return this;
  }

  public Cnf sub(Feature... features) {
    for (Feature f : features) {
      this.features = Feature.config(this.features, f, false);
    }
    return this;
  }

  public Cnf build(Run1<Cnf> builder) {
    builder.run(this);
    return this;
  }

  public final String dateToString(Date date) {
    val f = new SimpleDateFormat(dateFormat, Locale.getDefault());
    return f.format(date);
  }

  public final boolean hasFeature(Feature feature) {
    return Feature.isEnabled(features, feature);
  }

  /** null string 默认值 */
  public final String nullString() {
    return hasFeature(Feature.StringNullAsEmpty) ? "" : null;
  }

  public Cnf abbrevMaxSize(int maxSize) {
    this.abbrevMaxSize = maxSize;
    return this;
  }
}
