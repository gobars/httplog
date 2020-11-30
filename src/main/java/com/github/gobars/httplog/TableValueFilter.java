package com.github.gobars.httplog;

/**
 * 表字段值过滤器.
 *
 * <p>应用可以定义过滤器，对字段值进行过滤，比如脱敏处理等
 *
 * @author bingoobjca
 */
public interface TableValueFilter {
  /**
   * 过滤值.
   *
   * @param table 表名
   * @param ctx 字段上下文
   * @param value 字段取值
   * @return 过滤后的值
   */
  Object filter(String table, ColValueGetterCtx ctx, Object value);
}
