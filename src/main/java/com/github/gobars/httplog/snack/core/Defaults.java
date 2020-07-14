package com.github.gobars.httplog.snack.core;

import com.github.gobars.httplog.snack.from.Fromer;
import com.github.gobars.httplog.snack.from.JsonFromer;
import com.github.gobars.httplog.snack.from.ObjectFromer;
import com.github.gobars.httplog.snack.to.JsonToer;
import com.github.gobars.httplog.snack.to.ObjectToer;
import com.github.gobars.httplog.snack.to.Toer;

/** 默认值 */
public interface Defaults {
  /** 默认特性 */
  int DEF_FEATURES = Feature.QuoteFieldNames.code;
  /** 默认类型的key */
  String DEF_TYPE_KEY = "@type";

  /** 默认时间格式字符串 */
  String DEF_DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss";
  /** 默认对象来源器 */
  Fromer DEF_OBJECT_FROMER = new ObjectFromer();
  /** 默认对象去处器 */
  Toer DEF_OBJECT_TOER = new ObjectToer();
  /** 默认STRING来源器 */
  Fromer DEF_STRING_FROMER = new JsonFromer();
  /** 默认STRING去处器 */
  Toer DEF_STRING_TOER = new JsonToer();
  /** 默认JSON去处器 */
  Toer DEF_JSON_TOER = new JsonToer();

  int featuresDefault =
      Feature.of(
          Feature.OrderedField,
          Feature.WriteDateUseTicks,
          Feature.StringNullAsEmpty,
          Feature.QuoteFieldNames);

  int featuresSerialize =
      Feature.of(
          Feature.OrderedField,
          Feature.BrowserCompatible,
          Feature.WriteClassName,
          Feature.QuoteFieldNames);
}
