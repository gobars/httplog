package com.github.gobars.httplog;

import static com.github.gobars.httplog.TableCol.Equals.eq;
import static com.github.gobars.httplog.TableCol.Starts.starts;

import com.github.gobars.httplog.snack.Onode;
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
import org.jetbrains.annotations.Nullable;
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

  static Map<Matcher, ColValueGetter> blts = new HashMap<>(10);
  static Map<Matcher, ColValueGetterV> rsps = new HashMap<>(5);
  static Map<Matcher, ColValueGetterV> reqs = new HashMap<>(13);

  static {
    blts.put(eq("id"), (req, rsp, r, p, hl) -> req.getId());
    blts.put(eq("created"), (req, rsp, r, p, hl) -> req.getStartTime());
    blts.put(eq("ip"), (req, rsp, r, p, hl) -> WorkerIdIp.LOCAL_IP);
    blts.put(eq("hostname"), (req, rsp, r, p, hl) -> WorkerIdHostname.HOSTNAME);
    blts.put(eq("pid"), (req, rsp, r, p, hl) -> Pid.PROCESS_ID);
    blts.put(eq("started"), (req, rsp, r, p, hl) -> req.getStartTime());
    blts.put(eq("end"), (req, rsp, r, p, hl) -> rsp == null ? null : rsp.getEndTime());
    blts.put(eq("cost"), (req, rsp, r, p, hl) -> rsp == null ? null : rsp.getTookMs());
    blts.put(eq("biz"), (req, rsp, r, p, hl) -> hl.biz());
    blts.put(eq("exception"), (req, rsp, r, p, hl) -> rsp == null ? null : rsp.getError());
  }

  static {
    rsps.put(starts("head_"), (req, rsp, r, p, hl, v) -> rsp.getHeaders().get(v.substring(5)));
    rsps.put(eq("heads"), (req, rsp, r, p, hl, v) -> rsp.getHeaders());
    rsps.put(eq("body"), (req, rsp, r, p, hl, v) -> rsp.getAbbreviateBody());
    rsps.put(eq("json"), (req, rsp, r, p, hl, v) -> getJsonBody(rsp));
    rsps.put(starts("json_"), (req, rsp, r, hl, p, v) -> jsonpath(v.substring(5), rsp));
    rsps.put(eq("status"), (req, rsp, r, p, hl, v) -> p.getStatus());
  }

  static {
    reqs.put(starts("head_"), (req, rsp, r, p, hl, v) -> req.getHeaders().get(v.substring(5)));
    reqs.put(eq("heads"), (req, rsp, r, p, hl, v) -> req.getHeaders());
    reqs.put(eq("body"), (req, rsp, r, p, hl, v) -> req.getAbbreviateBody());
    reqs.put(eq("json"), (req, rsp, r, p, hl, v) -> getJsonBody(req));
    reqs.put(starts("json_"), (req, rsp, r, p, hl, v) -> jsonpath(v.substring(5), req));
    reqs.put(eq("method"), (req, rsp, r, p, hl, v) -> r.getMethod());
    reqs.put(eq("url"), (req, rsp, r, p, hl, v) -> req.getRequestUri());
    reqs.put(starts("path_"), (req, rsp, r, p, hl, v) -> getPathVar(r, v.substring(5)));
    reqs.put(eq("paths"), (req, rsp, r, p, hl, v) -> r.getAttribute(PATH_ATTR));
    reqs.put(starts("query_"), (req, rsp, r, p, hl, v) -> req.getQueries().get(v.substring(6)));
    reqs.put(eq("queries"), (req, rsp, r, p, hl, v) -> req.getQueries());
    reqs.put(starts("param_"), (req, rsp, r, p, hl, v) -> getParams(r, v.substring(6)));
    reqs.put(eq("params"), (req, rsp, r, p, hl, v) -> convert(r.getParameterMap()));
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
   * 额外信息
   *
   * <p>eg. auto_increment in MySQL.
   */
  private String extra;

  /** 是否可以为空. */
  private boolean nullable;

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

  /** 字段取值器 */
  private ColValueGetter valueGetter;

  private static ColValueGetter findGetter(String tag, Map<Matcher, ColValueGetter> m) {
    for (val entry : m.entrySet()) {
      if (entry.getKey().matches(tag)) {
        return entry.getValue();
      }
    }

    return null;
  }

  private static ColValueGetter createValueGetter(String tag, Map<Matcher, ColValueGetterV> m) {
    ColValueGetterV getter = findGetterV(tag, m);
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
    Map<String, String> attr = (Map<String, String>) r.getAttribute(PATH_ATTR);
    return attr == null ? null : attr.get(v);
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

  private static ColValueGetterV findGetterV(String tag, Map<Matcher, ColValueGetterV> m) {
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
    Onode node = req.getBodyOnode();
    if (node == null) {
      if (req.isBodyOnodeInitialized()) {
        return null;
      }

      req.setBodyOnodeInitialized(true);
      try {
        node = Onode.loadStr(req.getBody());
        req.setBodyOnode(node);
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

  public void parseComment(Map<String, String> fixes) {
    String tag = name.toLowerCase();
    if (comment != null) {
      val m = TAG_PATTERN.matcher(comment);
      if (m.find()) {
        tag = m.group(1);
      }
    }

    if (tag.startsWith("req_")) {
      this.valueGetter = createValueGetter(tag.substring(4), reqs);
    } else if (tag.startsWith("rsp_")) {
      this.valueGetter = createValueGetter(tag.substring(4), rsps);
    } else if (tag.startsWith("ctx_")) {
      this.valueGetter = createCtxValueGetter(tag.substring(4));
    } else if (tag.startsWith("custom_")) {
      this.valueGetter = createCustomValueGetter(tag.substring(7));
    } else if (tag.startsWith("fix_")) {
      this.valueGetter = createFixValueGetter(tag.substring(4), fixes);
    } else if (tag.startsWith("pre_")) {
      this.valueGetter = createPreValueGetter(tag.substring(4));
    } else if (tag.startsWith("post_")) {
      this.valueGetter = createPostValueGetter(tag.substring(5));
    } else if ("-".equals(tag)) {
      // ignore, 此字段，由db自动处理(eg. auto increment / insert trigger)
      this.valueGetter = null;
    } else {
      if (Str.containsIgnoreCase(extra, "auto_increment")) {
        // auto increment
        this.valueGetter = null;
      } else {
        this.valueGetter = createBuiltinValueGetter(tag);
      }
    }

    if (this.valueGetter != null) {
      this.valueGetter = wrap(this.valueGetter);
    }
  }

  private ColValueGetter createPostValueGetter(String tag) {
    return (req, rsp, r, p, hl) -> rsp.getPosts().get(tag);
  }

  private ColValueGetter createPreValueGetter(String tag) {
    return (req, rsp, r, p, hl) -> req.getPres().get(tag);
  }

  private ColValueGetter wrap(final ColValueGetter vg) {
    return (req, rsp, r, p, hl) -> {
      Object o = vg.get(req, rsp, r, p, hl);
      o = truncateToMaxLength(o);

      if (o == null && !nullable) {
        o = 0;
      }

      return o;
    };
  }

  @Nullable
  private Object truncateToMaxLength(Object o) {
    if (maxLen <= 0) {
      return o;
    }

    if (o == null || o instanceof Timestamp || o instanceof Integer || o instanceof Long) {
      return o;
    }

    return Str.abbreviate(o.toString(), maxLen);
  }

  private ColValueGetter createFixValueGetter(final String tag, final Map<String, String> fixes) {
    return (req, rsp, r, p, hl) -> fixes.get(tag);
  }

  private ColValueGetter createBuiltinValueGetter(String tag) {
    return findGetter(tag, TableCol.blts);
  }

  private ColValueGetter createCtxValueGetter(String tag) {
    return (req, rsp, r, p, hl) -> {
      String path = tag;
      if (!path.startsWith("$.")) {
        path = "$." + path;
      }

      return Onode.load(r.getAttribute(tag)).select(path).toString();
    };
  }

  private ColValueGetter createCustomValueGetter(String tag) {
    return (req, rsp, r, p, hl) -> {
      val custom = (HttpLogCustom) r.getAttribute(Const.CUSTOM);
      if (custom != null) {
        return custom.getMap().get(tag);
      }

      return null;
    };
  }

  public interface Matcher {
    boolean matches(String tag);
  }

  public interface ColValueGetterV {
    Object get(
        Req req, Rsp rsp, HttpServletRequest r, HttpServletResponse p, HttpLogAttr hl, String v);
  }

  static class Equals implements Matcher {
    private final String value;

    Equals(String value) {
      this.value = value;
    }

    static Matcher eq(String value) {
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

    public static Matcher starts(String value) {
      return new Starts(value);
    }

    @Override
    public boolean matches(String tag) {
      return tag != null && tag.startsWith(value);
    }
  }
}
