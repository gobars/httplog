package com.github.gobars.httplog;

import com.github.gobars.httplog.snack.ONode;
import com.github.gobars.id.util.Pid;
import com.github.gobars.id.worker.WorkerIdHostname;
import com.github.gobars.id.worker.WorkerIdIp;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.servlet.HandlerMapping;

/**
 * 表定义
 *
 * @author bingoobjca
 */
@Data
@Slf4j
public class TableCol {
  static final Pattern TAG_PATTERN = Pattern.compile("httplog:\"(.*?)\"");
  /**
   * 字段名称
   *
   * <p>eg. created
   */
  private String name;
  /**
   * 字段注释
   *
   * <p>eg. httplog:"id"
   */
  private String comment;
  /**
   * 数据类型
   *
   * <p>eg. bigint
   */
  private String dataType;
  /**
   * 字段类型
   *
   * <p>eg. bigint(20)
   */
  private String type;
  /**
   * 字符最大长度
   *
   * <p>eg. 20
   */
  private int maxLen;
  /**
   * 字段顺序
   *
   * <p>从1开始
   */
  private int seq;
  /** 表字段类型 */
  private Type tagType;
  /** 字段取值器 */
  private ColValueGetter valueGetter;

  public boolean eagerSupport() {
    return tagType == Type.REQ
        || tagType == Type.CTX
        || tagType == Type.FIX
        || tagType == Type.BUILTIN;
  }

  public void parseComment(Map<String, String> fixes) {
    Matcher m = TAG_PATTERN.matcher(comment);
    String tag = name;
    if (m.find()) {
      tag = m.group(1);
    }

    if (tag.startsWith("req_")) {
      this.tagType = Type.REQ;
      this.valueGetter = createReqValueGetter(tag.substring(4));
    } else if (tag.startsWith("rsp_")) {
      this.tagType = Type.RSP;
      this.valueGetter = createRspValueGetter(tag.substring(4));
    } else if (tag.startsWith("ctx_")) {
      this.tagType = Type.CTX;
      this.valueGetter = createCtxValueGetter(tag.substring(4));
    } else if (tag.startsWith("fix_")) {
      this.tagType = Type.FIX;
      this.valueGetter = createFixValueGetter(tag.substring(4), fixes);
    } else if ("-".equals(tag)) {
      this.tagType = Type.IGNORE;
    } else {
      this.tagType = Type.BUILTIN;
      this.valueGetter = createBuiltinValueGetter(tag);
    }

    if (this.valueGetter != null) {
      this.valueGetter = wrapMaxLength(this.valueGetter);
    }
  }

  private ColValueGetter wrapMaxLength(final ColValueGetter vg) {
    return (req, rsp, r, p) -> {
      Object o = vg.get(req, rsp, r, p);
      if (maxLen <= 0) {
        return o;
      }

      if (o == null || o instanceof Timestamp || o instanceof Integer || o instanceof Long) {
        return o;
      }

      return Str.abbreviate(o.toString(), maxLen);
    };
  }

  private ColValueGetter createFixValueGetter(final String tag, final Map<String, String> fixes) {
    return (req, rsp, r, p) -> fixes.get(tag);
  }

  private ColValueGetter createBuiltinValueGetter(String tag) {
    if ("id".equals(tag)) {
      return (req, rsp, r, p) -> req.getId();
    }

    if ("created".equals(tag)) {
      return (req, rsp, r, p) -> req.getStartTime();
    }

    if ("ip".equals(tag)) {
      return (req, rsp, r, p) -> WorkerIdIp.localIP;
    }

    if ("hostname".equals(tag)) {
      return (req, rsp, r, p) -> WorkerIdHostname.HOSTNAME;
    }

    if ("pid".equals(tag)) {
      return (req, rsp, r, p) -> Pid.pid;
    }

    if ("start".equals(tag)) {
      return (req, rsp, r, p) -> req.getStartTime();
    }

    if ("end".equals(tag)) {
      return (req, rsp, r, p) -> rsp.getEndTime();
    }

    if ("cost".equals(tag)) {
      return (req, rsp, r, p) -> rsp.getTookMs();
    }

    if ("exception".equals(tag)) {
      return (req, rsp, r, p) -> rsp.getError();
    }

    return null;
  }

  private ColValueGetter createCtxValueGetter(String tag) {
    return (req, rsp, r, p) -> {
      String path = tag;
      if (!path.startsWith("$.")) {
        path = "$." + path;
      }

      return ONode.load(r.getAttribute(tag)).select(path).toString();
    };
  }

  private ColValueGetter createRspValueGetter(String tag) {
    if (tag.startsWith("head_")) {
      val name = tag.substring(5);
      return (req, rsp, r, p) -> rsp.getHeaders().get(name);
    }

    if ("heads".equals(tag)) {
      return (req, rsp, r, p) -> rsp.getHeaders();
    }

    if ("body".equals(tag)) {
      return (req, rsp, r, p) -> rsp.getBody();
    }

    if ("json".equals(tag)) {
      return (req, rsp, r, p) -> getJsonBody(rsp);
    }

    if (tag.startsWith("json_")) {
      val jsonpath = tag.substring(5);
      return (req, rsp, r, p) -> jsonpath(jsonpath, rsp);
    }

    return null;
  }

  private ColValueGetter createReqValueGetter(String tag) {
    if (tag.startsWith("head_")) {
      val name = tag.substring(5);
      return (req, rsp, r, p) -> req.getHeaders().get(name);
    }

    if ("heads".equals(tag)) {
      return (req, rsp, r, p) -> req.getHeaders();
    }

    if ("method".equals(tag)) {
      return (req, rsp, r, p) -> r.getMethod();
    }

    if ("url".equals(tag)) {
      return (req, rsp, r, p) -> req.getRequestUri();
    }

    if (tag.startsWith("path_")) {
      val v = tag.substring(5);
      return (req, rsp, r, p) -> {
        val vars = r.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return ((Map<String, String>) vars).get(v);
      };
    }

    if ("paths".equals(tag)) {
      return (req, rsp, r, p) -> r.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }

    if (tag.startsWith("query_")) {
      val v = tag.substring(6);
      return (req, rsp, r, p) -> req.getQueries().get(v);
    }

    if ("queries".equals(tag)) {
      return (req, rsp, r, p) -> req.getQueries();
    }

    if (tag.startsWith("param_")) {
      val v = tag.substring(6);
      return (req, rsp, r, p) -> {
        String[] values = r.getParameterValues(v);
        return values == null ? null : String.join(",", values);
      };
    }

    if ("params".equals(tag)) {
      return (req, rsp, r, p) -> {
        val m = r.getParameterMap();
        val params = new HashMap<String, String>(m.size());
        for (val e : m.entrySet()) {
          params.put(e.getKey(), String.join(",", e.getValue()));
        }
        return params;
      };
    }

    if ("body".equals(tag)) {
      return (req, rsp, r, p) -> req.getBody();
    }

    if ("json".equals(tag)) {
      return (req, rsp, r, p) -> getJsonBody(req);
    }

    if (tag.startsWith("json_")) {
      val v = tag.substring(5);
      return (req, rsp, r, p) -> jsonpath(v, req);
    }

    return null;
  }

  private Object getJsonBody(ReqRsp req) {
    String b = req.getBody();
    val contentType = req.getHeaders().get("Content-Type");

    return contentType != null
            && contentType.contains("json")
            && b != null
            && (b.startsWith("{") || b.startsWith("["))
        ? b
        : null;
  }

  private String jsonpath(String jsonpath, ReqRsp req) {
    ONode node = req.getBodyONode();
    if (node == null) {
      if (req.isBodyONodeInitialized()) {
        return null;
      }

      req.setBodyONodeInitialized(true);
      try {
        node = ONode.loadStr(req.getBody());
        req.setBodyONode(node);
      } catch (Exception ex) {
        log.error("failed to load json {}", req.getBody(), ex);
      }
    }

    if (node == null) {
      return null;
    }

    String path = jsonpath;
    if (!path.startsWith("$.")) {
      path = "$." + path;
    }

    return node.select(path).getString();
  }

  public enum Type {
    /** 请求 */
    REQ,
    /** 响应 */
    RSP,
    /** 上下文 */
    CTX,
    /** 固定值 */
    FIX,
    /** 内建 */
    BUILTIN,
    /** 忽略，由数据库insert时自动创建 */
    IGNORE,
  }
}
