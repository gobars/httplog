package com.github.gobars.httplog.snack.core;

import com.github.gobars.httplog.snack.core.exts.Act1;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** 参数配置 */
public class Constants {
  public static final int featuresDefault =
      Feature.of(
          Feature.OrderedField,
          Feature.WriteDateUseTicks,
          Feature.StringNullAsEmpty,
          Feature.QuoteFieldNames);

  public static final int featuresSerialize =
      Feature.of(
          Feature.OrderedField,
          Feature.BrowserCompatible,
          Feature.WriteClassName,
          Feature.QuoteFieldNames);

  /** 类型key */
  public String typeKey = Defaults.DEF_TYPE_KEY;

  /** 特性 */
  public int features = Defaults.DEF_FEATURES;
  /** n.get(key)时，只读处理; 即不自动添加新节点 */
  public boolean readonly = false;

  public String dateFormat = Defaults.DEF_DATE_FORMAT_STRING;

  public Constants() {}

  public Constants(int features) {
    this.features = features;
  }

  public static Constants def() {
    return new Constants(featuresDefault);
  }

  public static Constants serialize() {
    return new Constants(featuresSerialize);
  }

  public static Constants of(Feature... features) {
    return new Constants().add(features);
  }

  public Constants add(Feature... features) {
    for (Feature f : features) {
      this.features = Feature.config(this.features, f, true);
    }
    return this;
  }

  public Constants sub(Feature... features) {
    for (Feature f : features) {
      this.features = Feature.config(this.features, f, false);
    }
    return this;
  }

  public Constants build(Act1<Constants> builder) {
    builder.run(this);
    return this;
  }

  // =================

  public final String dateToString(Date date) {
    SimpleDateFormat dateFormat =
        new SimpleDateFormat(Defaults.DEF_DATE_FORMAT_STRING, Locale.getDefault());
    return dateFormat.format(date);
  }

  public final boolean hasFeature(Feature feature) {
    return Feature.isEnabled(features, feature);
  }

  /** null string 默认值 */
  public final String nullString() {
    if (hasFeature(Feature.StringNullAsEmpty)) {
      return "";
    }

    return null;
  }
}
