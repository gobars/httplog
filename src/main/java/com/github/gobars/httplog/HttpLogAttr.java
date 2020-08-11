package com.github.gobars.httplog;

import java.util.Map;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

@Data
public class HttpLogAttr {
  private String biz;
  private String[] tables;
  private String fix;
  private Class<? extends HttpLogPre>[] pre;
  private Class<? extends HttpLogPost>[] post;
  private boolean sync;
  private int abbrevMaxSize;

  public HttpLogAttr tables(String... tables) {
    this.tables = tables;
    return this;
  }

  public HttpLogAttr biz(String biz) {
    this.biz = biz;
    return this;
  }

  public HttpLogAttr fix(String fix) {
    this.fix = fix;
    return this;
  }

  public HttpLogAttr pre(Class<? extends HttpLogPre>[] pre) {
    this.pre = pre;
    return this;
  }

  public HttpLogAttr post(Class<? extends HttpLogPost>[] post) {
    this.post = post;
    return this;
  }

  public HttpLogAttr sync(boolean sync) {
    this.sync = sync;
    return this;
  }

  public HttpLogAttr abbrevMaxSize(int abbrevMaxSize) {
    this.abbrevMaxSize = abbrevMaxSize;
    return this;
  }

  public String biz() {
    return biz;
  }

  public String[] tables() {
    return tables;
  }

  public String fix() {
    return fix;
  }

  public Class<? extends HttpLogPre>[] pre() {
    return pre;
  }

  public Class<? extends HttpLogPost>[] post() {
    return post;
  }

  public boolean sync() {
    return sync;
  }

  public int abbrevMaxSize() {
    return abbrevMaxSize;
  }

  public static HttpLogAttr create(Map<String, Object> annotationAttributes) {
    return create(new AnnotationAttributes(annotationAttributes));
  }

  @SuppressWarnings("unchecked")
  public static HttpLogAttr create(AnnotationAttributes attrs) {
    return new HttpLogAttr()
        .biz(attrs.getString("biz"))
        .tables(attrs.getStringArray("tables"))
        .fix(attrs.getString("fix"))
        .pre((Class<? extends HttpLogPre>[]) attrs.get("pre"))
        .post((Class<? extends HttpLogPost>[]) attrs.get("post"))
        .sync(attrs.getBoolean("sync"))
        .abbrevMaxSize(attrs.getNumber("abbrevMaxSize"));
  }
}
