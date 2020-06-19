package com.github.gobars.httplog;

import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotationAttributes;

import com.github.gobars.id.conf.ConnGetter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Spring拦截器.
 *
 * @author bingoobjca
 */
@Slf4j
public class HttpLogInterceptor extends HandlerInterceptorAdapter
    implements ApplicationContextAware {
  private final ConcurrentMap<HttpLogAttr, HttpLogProcessor> cache = new ConcurrentHashMap<>(100);
  private final ConnGetter connGetter;
  private ApplicationContext appContext;

  public HttpLogInterceptor(DataSource dataSource) {
    this(new ConnGetter.DsConnGetter(dataSource));
  }

  public HttpLogInterceptor(ConnGetter connGetter) {
    this.connGetter = connGetter;
  }

  /**
   * 预处理回调方法，实现处理器的预处理（如检查登陆），第三个参数为响应的处理器，自定义Controller
   *
   * <p>返回值：
   *
   * <p>true表示继续流程（如调用下一个拦截器或处理器）
   *
   * <p>false表示流程中断（如登录检查失败），不会继续调用其他的拦截器或处理器，此时我们需要通过response来产生响应
   */
  @Override
  @SuppressWarnings("unchecked")
  public boolean preHandle(HttpServletRequest r, HttpServletResponse p, Object h) {
    if (!(h instanceof HandlerMethod)) {
      log.warn("no permission....");
      return false;
    }

    val hm = (HandlerMethod) h;

    if (!AnnotatedElementUtils.isAnnotated(hm.getMethod(), HttpLog.class)) {
      return true;
    }

    val attrs = getMergedAnnotationAttributes(hm.getMethod(), HttpLog.class);
    val hl = HttpLogAttr.create(attrs);
    log.info("HttpLog: {}", attrs);

    val ps = cacheGet(hl);
    val req = (Req) r.getAttribute(Const.REQ);
    try {
      ps.logReq(r, req);
    } catch (Exception ex) {
      log.warn("failed to log req {}", req, ex);
    }

    r.setAttribute(Const.PROCESSOR, ps);
    log.debug("preHandle method:{} URI:{} httpLog:{}", r.getMethod(), r.getRequestURI(), hl);

    return true;
  }

  private HttpLogProcessor cacheGet(HttpLogAttr httpLog) {
    val ps = cache.get(httpLog);
    if (ps != null) {
      return ps;
    }

    synchronized (this) {
      val p = HttpLogProcessor.create(httpLog, connGetter, appContext);
      cache.put(httpLog, p);
      return p;
    }
  }

  /**
   * 后处理回调方法，实现处理器的后处理（但在渲染视图之前）
   *
   * <p>此时我们可以通过modelAndView（模型和视图对象）对模型数据进行处理或对视图进行处理，modelAndView也可能为null
   *
   * <p>注意：如果被拦截方法抛出异常，本回调方法将不会被调用
   */
  @Override
  public void postHandle(HttpServletRequest r, HttpServletResponse p, Object h, ModelAndView m) {
    log.debug("postHandle method:{} URI:{}", r.getMethod(), r.getRequestURI());
  }

  /**
   * 整个请求处理完毕回调方法，即在视图渲染完毕时回调
   *
   * <p>如性能监控中我们可以在此记录结束时间并输出消耗时间，还可以进行一些资源清理
   *
   * <p>类似于try-catch-finally中的finally，但仅调用处理器执行链中
   */
  @Override
  public void afterCompletion(HttpServletRequest r, HttpServletResponse p, Object h, Exception e) {
    log.debug("afterCompletion method:{} URI:{} ex:{}", r.getMethod(), r.getRequestURI(), e);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.appContext = applicationContext;
  }
}
