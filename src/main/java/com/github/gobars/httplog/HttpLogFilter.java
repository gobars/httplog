package com.github.gobars.httplog;

import com.github.gobars.id.Id;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.list;

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
@Component
public class HttpLogFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest r, HttpServletResponse s, FilterChain c)
      throws ServletException, IOException {

    val rq = new ContentCachingRequestWrapper(r);
    val rp = new ContentCachingResponseWrapper(s);

    Req req = new Req();
    req.setId(Id.next());
    Rsp rsp = new Rsp();
    rsp.setId(req.getId());

    // Launch of the timing of the request
    long startNs = System.nanoTime();
    req.setStartNs(startNs);
    rsp.setStartNs(startNs);

    // Registration of request status and headers
    logReqStatusAndHeaders(rq, req);

    try {
      rq.setAttribute(Const.REQ, req);
      rq.setAttribute(Const.RSP, rsp);
      c.doFilter(rq, rp);
      logStart(rq, req, rsp, startNs, rp.getStatus());
      rsp.setEndTime(new Timestamp(System.currentTimeMillis()));
      logRsp(rp, req, rsp);
    } catch (Exception e) {
      // Calculates the execution time of the request
      rsp.setEndTime(new Timestamp(System.currentTimeMillis()));
      logStart(rq, req, rsp, startNs, 500);
      logEx(rsp, e);

      // Throw the exception to not continue the treatment
      throw e;
    } finally {
      val p = rq.getAttribute(Const.PROCESSOR);
      if (p != null) {
        try {
          ((HttpLogProcessor) p).complete(rq, rp, req, rsp);
        } catch (Exception ex) {
          log.warn("failed to complete req:{} rsp:{}", req, rsp, ex);
        }
      }

      log.info("req: {}", req);
      log.info("rsp: {}", rsp);
    }
  }

  private void logRsp(ContentCachingResponseWrapper rp, Req req, Rsp rsp) {
    // Recovery of the body of the response
    logBody(rp.getContentAsByteArray(), rp.getCharacterEncoding(), rsp);

    // Duplication of the response
    try {
      rp.copyBodyToResponse();
    } catch (Exception e) {
      log.warn("copyBodyToResponse for req {} failed", req, e);
    }

    // Retrieving response headers
    logRspHeaders(rp, rsp);
  }

  private void logEx(Rsp rsp, Exception e) {
    val sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    rsp.setError(sw.toString());
  }

  private void logStart(
      ContentCachingRequestWrapper rq, Req req, Rsp rsp, long startNs, int status) {
    // Calculates the execution time of the request
    long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
    // Registration of the body of the request
    logBody(rq.getContentAsByteArray(), rq.getCharacterEncoding(), req);
    // Retrieving the status of the response
    logRsp(status, tookMs, rsp);
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

  private void logRsp(int status, long tookMs, Rsp rsp) {
    rsp.setTookMs(tookMs);
    rsp.setStatus(status);
    rsp.setReasonPhrase(HttpStatus.valueOf(status).getReasonPhrase());
  }

  private void logBody(byte[] content, String contentEncoding, ReqRsp rr) {
    try {
      rr.setBodyBytes(content.length);
      rr.setBody(new String(content, contentEncoding));
    } catch (UnsupportedEncodingException e) {
      rr.setError(String.format("%s unsupported encoding content", contentEncoding));
    }
  }
}
