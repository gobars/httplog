package com.github.gobars.httplog;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.core.annotation.AnnotationAttributes;

@Data
@Accessors(fluent = true)
public class HttpLogAttr {
  private boolean eager;
  private String biz;
  private String[] tables;
  private String fix;
  private Class<? extends HttpLogPre>[] pre;
  private Class<? extends HttpLogPost>[] post;

  @SuppressWarnings("unchecked")
  public static HttpLogAttr create(AnnotationAttributes attrs) {
    return new HttpLogAttr()
        .eager(attrs.getBoolean("eager"))
        .biz(attrs.getString("biz"))
        .tables(attrs.getStringArray("tables"))
        .fix(attrs.getString("fix"))
        .pre((Class<? extends HttpLogPre>[]) attrs.get("pre"))
        .post((Class<? extends HttpLogPost>[]) attrs.get("post"));
  }
}
