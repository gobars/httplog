package com.github.gobars.httplog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Control方法执行前扩展属性获取器.
 *
 * @author bingoobjca
 */
public interface HttpLogPre {
  /**
   * 获得扩展属性.
   *
   * @return Map<String, String>
   */
  Map<String, String> create(
      HttpServletRequest r, Req req, HttpLogAttr httpLog, Map<String, String> fixes);

  @Slf4j
  class HttpLogPreComposite implements HttpLogPre {
    private final List<HttpLogPre> composite;

    public HttpLogPreComposite(List<HttpLogPre> composite) {
      this.composite = composite;
    }

    private static Map<String, String> create(
        HttpLogPre pre, HttpServletRequest r, Req req, HttpLogAttr hl, Map<String, String> fixes) {
      val m = new HashMap<String, String>(10);

      try {
        return pre.create(r, req, hl, fixes);
      } catch (Exception ex) {
        log.warn("pre {} create error", pre, ex);
      }

      return m;
    }

    @Override
    public Map<String, String> create(
        HttpServletRequest r, Req req, HttpLogAttr httpLog, Map<String, String> fixes) {
      Map<String, String> m = new HashMap<>(10);

      for (val p : composite) {
        m.putAll(create(p, r, req, httpLog, fixes));
      }

      return m;
    }
  }
}
