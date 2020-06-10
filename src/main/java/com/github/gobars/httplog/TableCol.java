package com.github.gobars.httplog;

import static com.github.gobars.httplog.TableCol.Equals.eqOf;
import static com.github.gobars.httplog.TableCol.Factory.of;
import static com.github.gobars.httplog.TableCol.Starts.startsOf;

import com.github.gobars.httplog.snack.ONode;
import com.github.gobars.id.util.Pid;
import com.github.gobars.id.worker.WorkerIdHostname;
import com.github.gobars.id.worker.WorkerIdIp;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
  public static final String PATH_ATTR = HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
  static final Pattern TAG_PATTERN = Pattern.compile("httplog:\"(.*?)\"");
  static Map<Matcher, Factory> builtins = new HashMap<>(10);
  static Map<Matcher, ColValueGetterV> rsps = new HashMap<>(5);
  static Map<Matcher, ColValueGetterV> reqs = new HashMap<>(13);

  static {
    builtins.put(eqOf("id"), of((req, rsp, r, p, hl) -> req.getId()));
    builtins.put(eqOf("created"), of((req, rsp, r, p, hl) -> req.getStartTime()));
    builtins.put(eqOf("ip"), of((req, rsp, r, p, hl) -> WorkerIdIp.LOCAL_IP));
    builtins.put(eqOf("hostname"), of((req, rsp, r, p, hl) -> WorkerIdHostname.HOSTNAME));
    builtins.put(eqOf("pid"), of((req, rsp, r, p, hl) -> Pid.PROCESS_ID));
    builtins.put(eqOf("started"), of((req, rsp, r, p, hl) -> req.getStartTime()));
    builtins.put(eqOf("end"), of((req, rsp, r, p, hl) -> rsp == null ? null : rsp.getEndTime()));
    builtins.put(eqOf("cost"), of((req, rsp, r, p, hl) -> rsp == null ? null : rsp.getTookMs()));
    builtins.put(eqOf("biz"), of((req, rsp, r, p, hl) -> hl.biz()));
    builtins.put(
        eqOf("exception"), of((req, rsp, r, p, hl) -> rsp == null ? null : rsp.getError()));
  }

  static {
    rsps.put(startsOf("head_"), (req, rsp, r, p, hl, v) -> rsp.getHeaders().get(v.substring(5)));
    rsps.put(eqOf("heads"), (req, rsp, r, p, hl, v) -> rsp.getHeaders());
    rsps.put(eqOf("body"), (req, rsp, r, p, hl, v) -> rsp.getBody());
    rsps.put(eqOf("json"), (req, rsp, r, p, hl, v) -> getJsonBody(rsp));
    rsps.put(startsOf("json_"), (req, rsp, r, hl, p, v) -> jsonpath(v.substring(5), rsp));
    rsps.put(eqOf("status"), (req, rsp, r, p, hl, v) -> p.getStatus());
  }

  static {
    reqs.put(startsOf("head_"), (req, rsp, r, p, hl, v) -> req.getHeaders().get(v.substring(5)));
    reqs.put(eqOf("heads"), (req, rsp, r, p, hl, v) -> req.getHeaders());
    reqs.put(eqOf("body"), (req, rsp, r, p, hl, v) -> req.getBody());
    reqs.put(eqOf("json"), (req, rsp, r, p, hl, v) -> getJsonBody(req));
    reqs.put(startsOf("json_"), (req, rsp, r, p, hl, v) -> jsonpath(v.substring(5), req));
    reqs.put(eqOf("method"), (req, rsp, r, p, hl, v) -> r.getMethod());
    reqs.put(eqOf("url"), (req, rsp, r, p, hl, v) -> req.getRequestUri());
    reqs.put(startsOf("path_"), (req, rsp, r, p, hl, v) -> getPathVar(r, v.substring(5)));
    reqs.put(eqOf("paths"), (req, rsp, r, p, hl, v) -> r.getAttribute(PATH_ATTR));
    reqs.put(startsOf("query_"), (req, rsp, r, p, hl, v) -> req.getQueries().get(v.substring(6)));
    reqs.put(eqOf("queries"), (req, rsp, r, p, hl, v) -> req.getQueries());
    reqs.put(startsOf("param_"), (req, rsp, r, p, hl, v) -> getParams(r, v.substring(6)));
    reqs.put(eqOf("params"), (req, rsp, r, p, hl, v) -> convert(r.getParameterMap()));
  }

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

  private static ColValueGetter findGetter(String tag, Map<Matcher, Factory> m) {
    for (val entry : m.entrySet()) {
      if (entry.getKey().matches(tag)) {
        return entry.getValue().create();
      }
    }

    return null;
  }

  private static ColValueGetter createValueGetter(
      final String tag, Map<Matcher, ColValueGetterV> m, final HttpLog httpLog) {
    ColValueGetterV getter = findGetter2(tag, m);
    if (getter == null) {
      return null;
    }

    return (req, rsp, r, p, hl) -> getter.get(req, rsp, r, p, hl, tag);
  }

  private static Object getParams(HttpServletRequest r, String v) {
    return Str.join(",", r.getParameterValues(v));
  }

  @SuppressWarnings("unchecked")
  private static Object getPathVar(HttpServletRequest r, String v) {
    return ((Map<String, String>) r.getAttribute(PATH_ATTR)).get(v);
  }

  private static Map<String, String> convert(Map<String, String[]> src) {
    if (src == null) {
      return null;
    }

    val params = new HashMap<String, String>(src.size());
    for (val e : src.entrySet()) {
      params.put(e.getKey(), String.join(",", e.getValue()));
    }

    return params;
  }

  private static ColValueGetterV findGetter2(String tag, Map<Matcher, ColValueGetterV> m) {
    for (val entry : m.entrySet()) {
      if (entry.getKey().matches(tag)) {
        return entry.getValue();
      }
    }

    return null;
  }

  private static Object getJsonBody(ReqRsp req) {
    val contentType = req.getHeaders().get("Content-Type");
    if (contentType == null || !contentType.contains("json")) {
      return null;
    }

    String b = req.getBody();
    return b != null && (b.startsWith("{") || b.startsWith("[")) ? b : null;
  }

  private static String jsonpath(String jsonpath, ReqRsp req) {
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

  public boolean eagerSupport() {
    return tagType == Type.REQ
        || tagType == Type.CTX
        || tagType == Type.FIX
        || tagType == Type.BUILTIN;
  }

  public void parseComment(Map<String, String> fixes, HttpLog httpLog) {
    String tag = name.toLowerCase();
    if (comment != null) {
      val m = TAG_PATTERN.matcher(comment);
      if (m.find()) {
        tag = m.group(1);
      }
    }

    if (tag.startsWith("req_")) {
      this.tagType = Type.REQ;
      this.valueGetter = createValueGetter(tag.substring(4), reqs, httpLog);
    } else if (tag.startsWith("rsp_")) {
      this.tagType = Type.RSP;
      this.valueGetter = createValueGetter(tag.substring(4), rsps, httpLog);
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
    return (req, rsp, r, p, hl) -> {
      Object o = vg.get(req, rsp, r, p, hl);
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
    return (req, rsp, r, p, hl) -> fixes.get(tag);
  }

  private ColValueGetter createBuiltinValueGetter(String tag) {
    return findGetter(tag, TableCol.builtins);
  }

  private ColValueGetter createCtxValueGetter(String tag) {
    return (req, rsp, r, p, hl) -> {
      String path = tag;
      if (!path.startsWith("$.")) {
        path = "$." + path;
      }

      return ONode.load(r.getAttribute(tag)).select(path).toString();
    };
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

  public interface Matcher {
    boolean matches(String tag);
  }

  public interface ColValueGetterV {
    Object get(Req req, Rsp rsp, HttpServletRequest r, HttpServletResponse p, HttpLog hl, String v);
  }

  static class Equals implements Matcher {
    private final String value;

    Equals(String value) {
      this.value = value;
    }

    static Matcher eqOf(String value) {
      return new Equals(value);
    }

    @Override
    public boolean matches(String tag) {
      return value.equals(tag);
    }
  }

  static class Starts implements Matcher {
    private final String value;

    Starts(String value) {
      this.value = value;
    }

    public static Matcher startsOf(String value) {
      return new Starts(value);
    }

    @Override
    public boolean matches(String tag) {
      return tag != null && tag.startsWith(value);
    }
  }

  static class Factory {
    private final ColValueGetter colValueGetter;

    Factory(ColValueGetter colValueGetter) {
      this.colValueGetter = colValueGetter;
    }

    static Factory of(ColValueGetter colValueGetter) {
      return new Factory(colValueGetter);
    }

    public ColValueGetter create() {
      return colValueGetter;
    }
  }
}
