package com.github.gobars.httplog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Control方法执行后扩展属性获取器.
 *
 * @author bingoobjca
 */
public interface HttpLogPost {
  /** 生成扩展属性. */
  Map<String, String> create(
      HttpServletRequest r,
      HttpServletResponse p,
      Req req,
      Rsp rsp,
      HttpLogAttr hl,
      Map<String, String> fixes);

  @Slf4j
  class HttpLogPostComposite implements HttpLogPost {
    private final List<HttpLogPost> composite;

    public HttpLogPostComposite(List<HttpLogPost> composite) {
      this.composite = composite;
    }

    static Map<String, String> create(
        HttpLogPost po,
        HttpServletRequest r,
        HttpServletResponse p,
        Req req,
        Rsp rsp,
        HttpLogAttr hl,
        Map<String, String> fixes) {
      val m = new HashMap<String, String>(10);

      try {
        return po.create(r, p, req, rsp, hl, fixes);
      } catch (Exception ex) {
        log.warn("pre {} create error", po, ex);
      }

      return m;
    }

    @Override
    public Map<String, String> create(
        HttpServletRequest r,
        HttpServletResponse p,
        Req req,
        Rsp rsp,
        HttpLogAttr hl,
        Map<String, String> fixes) {
      Map<String, String> m = new HashMap<>(10);

      for (val po : composite) {
        m.putAll(create(po, r, p, req, rsp, hl, fixes));
      }

      return m;
    }
  }
}
