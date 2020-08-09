package com.github.gobars.httplog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 字段值提取器上下文.
 *
 * @author bingoobjca
 */
@Data
@Accessors(fluent = true)
public class ColValueGetterCtx {
  Req req;
  Rsp rsp;
  HttpServletRequest r;
  HttpServletResponse p;
  HttpLogAttr hl;
  TableCol col;

  HttpLogFork fork;
}
