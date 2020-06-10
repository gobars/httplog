package com.github.gobars.httplog.spring.mysql;

import com.github.gobars.httplog.HttpLog;
import com.github.gobars.httplog.HttpLogPost;
import com.github.gobars.httplog.HttpLogPre;
import java.lang.annotation.*;

@Documented
@HttpLog(tables = "biz_log", fix = "desc:ID查找", eager = true)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MyHttpLog {
  String fix() default "";

  /**
   * 方法进入前扩展属性获取器
   *
   * @return HttpLogPreExt
   */
  Class<? extends HttpLogPre>[] pre() default MyPre.class;

  /**
   * 方法运行后的扩展属性获取器
   *
   * @return HttpLogPostExt
   */
  Class<? extends HttpLogPost>[] post() default MyPost.class;
}
