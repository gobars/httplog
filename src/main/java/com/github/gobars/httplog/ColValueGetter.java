package com.github.gobars.httplog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 日志表字段值获取器
 *
 * @author bingoobjca
 */
public interface ColValueGetter {
  Object get(Req req, Rsp rsp, HttpServletRequest r, HttpServletResponse p, HttpLog httpLog);
}
