package com.github.gobars.httplog;

import java.lang.annotation.*;

/**
 * 业务日志记录
 *
 * @author bingoobjca
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface HttpLog {
  /**
   * 定义业务名称
   *
   * @return 业务名称
   */
  String biz() default "";

  /**
   * 在哪些表中记录
   *
   * <p>注意，此处使用的表，需要各个字段的comment中包含如下指示符，或者直接字段名符合以下命名规范.
   *
   * <ul>
   *   <li>内置类:
   *   <li>httplog:"id" 日志记录ID
   *   <li>httplog:"created" 创建时间
   *   <li>httplog:"ip" 当前机器IP
   *   <li>httplog:"hostname" 当前机器名称
   *   <li>httplog:"pid" 应用程序PID
   *   <li>httplog:"start" 开始时间(yyyy-MM-dd HH:mm:ss.SSS)
   *   <li>httplog:"end" 结束时间(yyyy-MM-dd HH:mm:ss.SSS)
   *   <li>httplog:"cost" 花费时间（ms)
   *   <li>httplog:"exception" 异常信息
   *   <li>请求类:
   *   <li>httplog:"req_head_xxx" 请求中的xxx头
   *   <li>httplog:"req_heads" 请求中的所有头
   *   <li>httplog:"req_method" 请求method
   *   <li>httplog:"req_url" 请求URL
   *   <li>httplog:"req_path_xxx" 请求URL中的xxx路径参数
   *   <li>httplog:"req_paths" 请求URL中的所有路径参数
   *   <li>httplog:"req_query_xxx" 请求URl中的xxx查询参数
   *   <li>httplog:"req_queries" 请求URl中的所有查询参数
   *   <li>httplog:"req_param_xxx" 请求中query/form的xxx参数
   *   <li>httplog:"req_params" 请求中query/form的所有参数
   *   <li>httplog:"req_body" 请求体
   *   <li>httplog:"req_json" 请求体（当Content-Type为JSON时)
   *   <li>httplog:"req_json_xxx" 请求体JSON中的xxx属性
   *   <li>响应类:
   *   <li>httplog:"rsp_head_xxx" 响应中的xxx头
   *   <li>httplog:"rsp_heads" 响应中的所有头
   *   <li>httplog:"rsp_body" 响应体
   *   <li>httplog:"rsp_json" 响应体JSON（当Content-Type为JSON时)
   *   <li>httplog:"rsp_json_xxx" 请求体JSON中的xxx属性
   *   <li>httplog:"rsp_status" 响应编码
   *   <li>上下文:
   *   <li>httplog:"ctx_xxx" 上下文对象xxx的值
   *   <li>固定值:
   *   <li>httplog:"fix_xxx" 由fix参数指定的固定值
   *   <li>更多详见Readme.md#Prepare-log-tables的说明
   * </ul>
   *
   * @return 表名列表
   */
  String[] tables() default {};

  /**
   * 指定日志表字段固定值.
   *
   * <p>格式: xxx:xvalue, yyy:yvalue
   *
   * @return 固定值
   */
  String fix() default "";

  /**
   * 方法进入前扩展属性获取器
   *
   * @return HttpLogPreExt
   */
  Class<? extends HttpLogPre>[] pre() default {};

  /**
   * 方法运行后的扩展属性获取器
   *
   * @return HttpLogPostExt
   */
  Class<? extends HttpLogPost>[] post() default {};
}
