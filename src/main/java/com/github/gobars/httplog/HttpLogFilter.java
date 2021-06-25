package com.github.gobars.httplog;

import static java.util.Collections.list;

import com.github.gobars.id.Id;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Log request and response for the http.
 *
 * <p>Original from
 *
 * <p>1. https://gitlab.com/Robin-Ln/httplogger
 *
 * <p>2. https://gist.github.com/int128/e47217bebdb4c402b2ffa7cc199307ba
 *
 * @author bingoo.
 */
@Slf4j
public class HttpLogFilter extends OncePerRequestFilter {

  @Autowired(required = false)
  private String[] httpLogWebIgnores;

  @SneakyThrows
  @Override
  protected void doFilterInternal(HttpServletRequest r, HttpServletResponse s, FilterChain c) {

    // 静态资源，直接跳过
    if (containIgnoreUris(r.getRequestURI())) {
      c.doFilter(r, s);
      return;
    }

    val rq = new ContentCachingRequestWrapper(r);
    val rp = new ContentCachingResponseWrapper(s);

    // Launch of the timing of the request
    long startNs = System.nanoTime();

    Req req = new Req();
    Rsp rsp = new Rsp();

    setup(rq, req, rsp);

    Exception e = null;
    try {
      c.doFilter(rq, rp);
    } catch (Exception ex) {
      e = ex;
    }

    val p = (HttpLogProcessor) rq.getAttribute(Const.PROCESSOR);
    if (p != null) {
      tearDown(rq, rp, startNs, req, rsp, e, p);
    }

    if (e != null) {
      throw e;
    }

    // Duplication of the response
    try {
      rp.copyBodyToResponse();
    } catch (Exception ex) {
      log.warn("copyBodyToResponse for req {} failed", req, ex);
    }
  }

  /**
   * 校验uri是否为静态资源的URI
   * @param uri 需要校验的uri
   * @return
   */
  private boolean containIgnoreUris(String uri) {
    AntPathMatcher pathMatcher = new AntPathMatcher();
    boolean flag = false;
    for (String ignore : httpLogWebIgnores) {
      if (pathMatcher.match(ignore, uri)) {
        flag = true;
      }
    }
    return flag;
  }

  private void tearDown(
      ContentCachingRequestWrapper rq,
      ContentCachingResponseWrapper rp,
      long startNs,
      Req req,
      Rsp rsp,
      Exception filterException,
      HttpLogProcessor p) {
    // Calculates the execution time of the request
    long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
    rsp.setEnd(new Timestamp(System.currentTimeMillis()));

    // Registration of the body of the request
    logBody(rq.getContentAsByteArray(), req);
    // Retrieving the status of the response
    rsp.setTookMs(tookMs);

    rsp.setStatus(rp.getStatus());
    rsp.setReasonPhrase(HttpStatus.valueOf(rsp.getStatus()).getReasonPhrase());
    rsp.setError(filterException);

    // Recovery of the body of the response
    logBody(rp.getContentAsByteArray(), rsp);

    // Retrieving response headers
    logRspHeaders(rp, rsp);

    p.complete(rq, rp, req, rsp);
    HttpLogCustom.clear();
  }

  private void setup(ContentCachingRequestWrapper rq, Req req, Rsp rsp) {
    req.setId(Id.next());
    rsp.setId(req.getId());

    // Registration of request status and headers
    logReqStatusAndHeaders(rq, req);

    rq.setAttribute(Const.REQ, req);
    rq.setAttribute(Const.RSP, rsp);

    HttpLogCustom custom = new HttpLogCustom();
    rq.setAttribute(Const.CUSTOM, custom);
    HttpLogCustom.set(custom);
  }

  private void logReqStatusAndHeaders(ContentCachingRequestWrapper r, Req req) {
    req.setMethod(r.getMethod());
    req.setRequestUri(r.getRequestURI());
    req.setQueries(Str.parseQuery(r.getQueryString()));
    req.setProtocol(r.getProtocol());

    Map<String, String> headers = new HashMap<>(10);
    list(r.getHeaderNames()).forEach(k -> headers.put(k, toStr(r.getHeaders(k))));
    req.setHeaders(headers);
  }

  private String toStr(Enumeration<String> iters) {
    val l = new ArrayList<String>(10);
    while (iters.hasMoreElements()) {
      l.add(iters.nextElement());
    }

    if (l.size() == 1) {
      return l.get(0);
    }

    return l.toString();
  }

  private void logRspHeaders(ContentCachingResponseWrapper r, Rsp rsp) {
    Map<String, String> headers = new HashMap<>(10);
    r.getHeaderNames().forEach(k -> headers.put(k, toStr(r.getHeaders(k))));
    rsp.setHeaders(headers);
  }

  private String toStr(Collection<String> strs) {
    if (strs.size() == 1) {
      return strs.iterator().next();
    }

    return strs.toString();
  }

  private void logBody(byte[] content, ReqRsp rr) {
    rr.setBodyBytes(content.length);
    rr.setBody(new String(content, StandardCharsets.UTF_8));
  }
}
