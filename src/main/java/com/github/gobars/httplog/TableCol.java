package com.github.gobars.httplog;

import static com.github.gobars.httplog.Equals.eq;
import static com.github.gobars.httplog.Starts.starts;

import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.id.util.Pid;
import com.github.gobars.id.worker.WorkerIdHostname;
import com.github.gobars.id.worker.WorkerIdIp;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
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
    blts.put(eq("id"), ctx -> ctx.req().getId());
    blts.put(eq("created"), ctx -> ctx.req().getStartTime());
    blts.put(eq("ip"), ctx -> WorkerIdIp.LOCAL_IP);
    blts.put(eq("hostname"), ctx -> WorkerIdHostname.HOSTNAME);
    blts.put(eq("pid"), ctx -> Pid.PROCESS_ID);
    blts.put(eq("started"), ctx -> ctx.req().getStartTime());
    blts.put(eq("end"), ctx -> ctx.rsp() == null ? null : ctx.rsp().getEndTime());
    blts.put(eq("cost"), ctx -> ctx.rsp() == null ? null : ctx.rsp().getTookMs());
    blts.put(eq("biz"), ctx -> ctx.hl().biz());
    blts.put(eq("exception"), ctx -> ctx.rsp() == null ? null : ctx.rsp().getError());
  }

  static {
    rsps.put(starts("head_"), (ctx, v, col) -> ctx.rsp().getHeaders().get(v.substring(5)));
    rsps.put(eq("heads"), (ctx, v, col) -> ctx.rsp().getHeaders());
    rsps.put(eq("body"), (ctx, v, col) -> ctx.rsp().getAbbreviateBody(col.maxLen));
    rsps.put(eq("json"), (ctx, v, col) -> getJsonBody(ctx.rsp()));
    rsps.put(starts("json_"), (ctx, v, col) -> jsonpath(v.substring(5), ctx.rsp()));
    rsps.put(eq("status"), (ctx, v, col) -> ctx.rsp().getStatus());
  }

  static {
    reqs.put(starts("head_"), (ctx, v, col) -> ctx.req().getHeaders().get(v.substring(5)));
    reqs.put(eq("heads"), (ctx, v, col) -> ctx.req().getHeaders());
    reqs.put(eq("body"), (ctx, v, col) -> ctx.req().getAbbreviateBody(col.maxLen));
    reqs.put(eq("json"), (ctx, v, col) -> getJsonBody(ctx.req()));
    reqs.put(starts("json_"), (ctx, v, col) -> jsonpath(v.substring(5), ctx.req()));
    reqs.put(eq("method"), (ctx, v, col) -> ctx.req().getMethod());
    reqs.put(eq("url"), (ctx, v, col) -> ctx.req().getRequestUri());
    reqs.put(starts("path_"), (ctx, v, col) -> getPathVar(ctx.r(), v.substring(5)));
    reqs.put(eq("paths"), (ctx, v, col) -> ctx.r().getAttribute(PATH_ATTR));
    reqs.put(starts("query_"), (ctx, v, col) -> ctx.req().getQueries().get(v.substring(6)));
    reqs.put(eq("queries"), (ctx, v, col) -> ctx.req().getQueries());
    reqs.put(starts("param_"), (ctx, v, col) -> getParams(ctx.r(), v.substring(6)));
    reqs.put(eq("params"), (ctx, v, col) -> convert(ctx.r().getParameterMap()));
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

  private ColValueGetter createValueGetter(String tag, Map<Matcher, ColValueGetterV> m) {
    ColValueGetterV getter = findGetterV(tag, m);
    if (getter == null) {
      return null;
    }

    return ctx -> getter.get(ctx, tag, this);
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
    String tag = parseTag();

    this.valueGetter = parseValueGetter(fixes, tag);
    if (this.valueGetter != null) {
      this.valueGetter = wrap(this.valueGetter);
    }
  }

  public ColValueGetter parseValueGetter(Map<String, String> fixes, String tag) {
    if (tag.startsWith("req_")) {
      return createValueGetter(tag.substring(4), reqs);
    }

    if (tag.startsWith("rsp_")) {
      return createValueGetter(tag.substring(4), rsps);
    }

    if (tag.startsWith("ctx_")) {
      return createCtxValueGetter(tag.substring(4));
    }

    if (tag.startsWith("custom_")) {
      return createCustomValueGetter(tag.substring(7));
    }

    if (tag.startsWith("fix_")) {
      return ctx -> fixes.get(tag.substring(4));
    }

    if (tag.startsWith("pre_")) {
      return ctx -> ctx.req().getPres().get(tag.substring(4));
    }

    if (tag.startsWith("post_")) {
      return ctx -> ctx.rsp().getPosts().get(tag.substring(5));
    }

    if ("-".equals(tag)) {
      // ignore, 此字段，由db自动处理(eg. auto increment / insert trigger)
      return null;
    }

    if (Str.containsIgnoreCase(extra, "auto_increment")) {
      return null;
    }

    return createBuiltinValueGetter(tag);
  }

  public String parseTag() {
    String tag = name.toLowerCase();
    if (comment == null) {
      return tag;
    }

    val m = TAG_PATTERN.matcher(comment);
    return m.find() ? m.group(1) : tag;
  }

  private ColValueGetter wrap(final ColValueGetter vg) {
    return ctx -> {
      Object o = vg.get(ctx);
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

  private ColValueGetter createBuiltinValueGetter(String tag) {
    return findGetter(tag, TableCol.blts);
  }

  private ColValueGetter createCtxValueGetter(String tag) {
    return ctx -> {
      String path = tag;
      if (!path.startsWith("$.")) {
        path = "$." + path;
      }

      return Onode.load(ctx.r().getAttribute(tag)).select(path).toString();
    };
  }

  private ColValueGetter createCustomValueGetter(String tag) {
    return ctx -> {
      val custom = (HttpLogCustom) ctx.r().getAttribute(Const.CUSTOM);
      if (custom != null) {
        return custom.getMap().get(tag);
      }

      return null;
    };
  }
}
