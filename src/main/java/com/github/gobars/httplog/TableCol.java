package com.github.gobars.httplog;

import static com.github.gobars.httplog.Equals.eq;
import static com.github.gobars.httplog.Starts.starts;

import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.httplog.springconfig.HttpLogTagField;
import com.github.gobars.httplog.springconfig.HttpLogTagTable;
import com.github.gobars.httplog.springconfig.HttpLogTags;
import com.github.gobars.id.util.Pid;
import com.github.gobars.id.worker.WorkerIdHostname;
import com.github.gobars.id.worker.WorkerIdIp;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationContext;
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

  static Map<Matcher, ColValueGetterV> blts = new HashMap<>(10);
  static Map<Matcher, ColValueGetterV> rsps = new HashMap<>(5);
  static Map<Matcher, ColValueGetterV> reqs = new HashMap<>(13);

  public static <T> T wfork(ColValueGetterCtx c, HttpLogTag v, Supplier<T> fs, Supplier<T> os) {
    switch (v.forkMode()) {
      case Only:
        return c.fork() != null ? fs.get() : null;
      case Try:
        return c.fork() != null ? fs.get() : os.get();
      default:
        return os.get();
    }
  }

  public static String stackTrace(Throwable t) {
    if (t == null) {
      return null;
    }

    val sw = new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }

  static {
    blts.put(eq("id"), (c, v, col) -> wfork(c, v, () -> c.fork.getId(), c.req::getId));
    blts.put(eq("created"), (ctx, v, col) -> ctx.req().getStart());
    blts.put(eq("ip"), (ctx, v, col) -> WorkerIdIp.LOCAL_IP);
    blts.put(eq("hostname"), (ctx, v, col) -> WorkerIdHostname.HOSTNAME);
    blts.put(eq("pid"), (ctx, v, col) -> Pid.PROCESS_ID);
    blts.put(eq("started"), (c, v, col) -> wfork(c, v, () -> c.fork.getStart(), c.req::getStart));
    blts.put(eq("end"), (c, v, col) -> wfork(c, v, () -> c.fork.getEnd(), c.rsp::getEnd));
    blts.put(eq("cost"), (c, v, col) -> wfork(c, v, () -> c.fork.getTookMs(), c.rsp::getTookMs));
    blts.put(eq("biz"), (ctx, v, col) -> ctx.hl().biz());
    blts.put(
        eq("exception", "error"),
        (c, v, col) ->
            wfork(c, v, () -> stackTrace(c.fork.getError()), () -> stackTrace(c.rsp.getError())));

    rsps.put(starts("head_"), (ctx, v, col) -> ctx.rsp().getHeaders().get(v.subTagName(5)));
    rsps.put(eq("heads"), (ctx, v, col) -> ctx.rsp().getHeaders());

    rsps.put(
        eq("body"),
        (c, v, col) ->
            wfork(
                c, v, () -> c.fork.abbrRsp(col.maxLen, v), () -> c.rsp().abbrBody(col.maxLen)));
    rsps.put(
        eq("json"),
        (c, v, col) ->
            wfork(
                c, v, () -> c.fork.abbrRsp(col.maxLen, v), () -> getJsonBody(c, v, col, c.rsp())));
    rsps.put(
        starts("json_"),
        (c, v, col) ->
            wfork(
                c,
                v,
                () -> jp(v.subTagName(5), c.fork.getResponse()),
                () -> jsonpath(v.subTagName(5), c.rsp())));
    rsps.put(eq("status"), (ctx, v, col) -> ctx.rsp().getStatus());

    reqs.put(starts("head_"), (ctx, v, col) -> ctx.req().getHeaders().get(v.subTagName(5)));
    reqs.put(eq("heads"), (ctx, v, col) -> ctx.req().getHeaders());
    reqs.put(
        eq("body"),
        (c, v, col) ->
            wfork(
                c,
                v,
                () -> c.fork.getAbbrReq(col.maxLen, v),
                () -> c.req().abbrBody(col.maxLen)));

    reqs.put(
        eq("json"),
        (c, v, col) ->
            wfork(
                c, v, () -> c.fork.abbrReq(col.maxLen, v), () -> getJsonBody(c, v, col, c.req())));
    reqs.put(
        starts("json_"),
        (c, v, col) ->
            wfork(
                c,
                v,
                () -> jp(v.subTagName(5), c.fork.getRequest()),
                () -> jsonpath(v.subTagName(5), c.req())));

    reqs.put(
        eq("method"),
        (c, v, col) -> wfork(c, v, () -> c.fork.getMethod(), () -> c.req().getMethod()));
    reqs.put(eq("url"), (ctx, v, col) -> ctx.req().getRequestUri());
    reqs.put(starts("path_"), (ctx, v, col) -> getPathVar(ctx.r(), v.subTagName(5)));
    reqs.put(eq("paths"), (ctx, v, col) -> ctx.r().getAttribute(PATH_ATTR));
    reqs.put(starts("query_"), (ctx, v, col) -> ctx.req().getQueries().get(v.subTagName(6)));
    reqs.put(eq("queries"), (ctx, v, col) -> ctx.req().getQueries());
    reqs.put(starts("param_"), (ctx, v, col) -> getParams(ctx.r(), v.subTagName(6)));
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

  private ColValueGetter createValueGetter(HttpLogTag tag, Map<Matcher, ColValueGetterV> m) {
    ColValueGetterV getter = findGetterV(tag.tag(), m);
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

  private static String getJsonBody(ColValueGetterCtx ctx, HttpLogTag v, TableCol col, ReqRsp req) {
    val contentType = req.getHeaders().get("Content-Type");
    if (contentType == null || !contentType.contains("json")) {
      return null;
    }

    String b = req.abbrBody(col.maxLen);
    return b != null && (b.startsWith("{") || b.startsWith("[")) ? b : null;
  }

  private static String jp(String jsonpath, Object req) {
    Onode node = Onode.load(req);
    if (node == null) {
      return null;
    }

    String path = jsonpath;
    if (!path.startsWith("$.")) {
      path = "$." + path;
    }

    return node.select(path).getString();
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

  public void parseComment(String table, ApplicationContext appContext, Map<String, String> fixes) {
    String colComment = createComment(table, appContext);
    HttpLogTag tag = HttpLogTag.parse(name, colComment);

    this.valueGetter = parseValueGetter(fixes, tag);
    if (this.valueGetter != null) {
      this.valueGetter = wrap(this.valueGetter);
    }
  }

  public String createComment(String table, ApplicationContext appContext) {
    val httpLogTagsMap = appContext.getBeansOfType(HttpLogTags.class);
    if (httpLogTagsMap.isEmpty()) {
      return comment;
    }

    HttpLogTags tags = httpLogTagsMap.entrySet().iterator().next().getValue();
    HttpLogTagTable tagTable = tags.get(table);
    if (tagTable == null) {
      return comment;
    }

    HttpLogTagField tagField = tagTable.get(this.name);
    if (tagField == null) {
      return comment;
    }

    String tagFieldComment = tagField.getComment();
    if (tagFieldComment == null) {
      return comment;
    }

    tagFieldComment = tagFieldComment.trim();
    if (tagFieldComment.isEmpty()) {
      return comment;
    }

    return tagFieldComment;
  }

  public ColValueGetter parseValueGetter(Map<String, String> fixes, HttpLogTag tag) {
    if (tag.startsWith("req_")) {
      return createValueGetter(tag.subTag(4), reqs);
    }

    if (tag.startsWith("rsp_")) {
      return createValueGetter(tag.subTag(4), rsps);
    }

    if (tag.startsWith("ctx_")) {
      return createCtxValueGetter(tag.subTagName(4));
    }

    if (tag.startsWith("custom_")) {
      return createCustomValueGetter(tag, tag.subTagName(7));
    }

    if (tag.startsWith("fix_")) {
      return ctx -> fixes.get(tag.subTagName(4));
    }

    if (tag.startsWith("pre_")) {
      return ctx -> ctx.req().getPres().get(tag.subTagName(4));
    }

    if (tag.startsWith("post_")) {
      return ctx -> ctx.rsp().getPosts().get(tag.subTagName(5));
    }

    if (tag.equalsTo("-")) {
      // ignore, 此字段，由db自动处理(eg. auto increment / insert trigger)
      return null;
    }

    if (Str.containsIgnoreCase(extra, "auto_increment")) {
      return null;
    }

    // kingbase， oscar 和pg之类的bigseq 自增主键方式，支持
    if (Str.containsIgnoreCase(extra, "NEXTVAL") &&
            (Str.containsIgnoreCase(extra, "SEQ'::REGCLASS")
                    || Str.containsIgnoreCase(extra, "::text"))
    ){
      return null;
    }

    if (Str.containsIgnoreCase(extra, "1")
    ){
      return null;
    }
    return createValueGetter(tag, TableCol.blts);
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

  private ColValueGetter createCtxValueGetter(String tag) {
    return ctx -> {
      String path = tag;
      if (!path.startsWith("$.")) {
        path = "$." + path;
      }

      return Onode.load(ctx.r().getAttribute(tag)).select(path).toString();
    };
  }

  private ColValueGetter createCustomValueGetter(HttpLogTag tag, String tagName) {
    return c -> {
      val map = getCustom(tag, c);
      return map != null ? map.get(tagName) : null;
    };
  }

  @Nullable
  private HashMap<String, String> getCustom(HttpLogTag tag, ColValueGetterCtx c) {
    switch (tag.forkMode()) {
      case Only:
        return c.fork() != null ? c.fork().getCustomized() : null;
      case Try:
        return c.fork() != null
            ? c.fork().getCustomized()
            : ((HttpLogCustom) c.r().getAttribute(Const.CUSTOM)).getMap();
      default:
        return ((HttpLogCustom) c.r().getAttribute(Const.CUSTOM)).getMap();
    }
  }
}
