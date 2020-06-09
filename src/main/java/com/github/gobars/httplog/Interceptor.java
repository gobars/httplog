package com.github.gobars.httplog;

import com.github.gobars.id.conf.ConnGetter;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Spring拦截器.
 *
 * @author bingoobjca
 */
@Slf4j
public class Interceptor extends HandlerInterceptorAdapter {
  public static final String HTTPLOG_PROCESSOR = "HTTPLOG_PROCESSOR";
  private final ConcurrentHashMap<HttpLog, HttpLogProcessor> cache = new ConcurrentHashMap<>(100);
  private final ConnGetter connGetter;

  public Interceptor(ConnGetter connGetter) {
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
  public boolean preHandle(HttpServletRequest r, HttpServletResponse p, Object h) {
    if (!(h instanceof HandlerMethod)) {
      log.warn("no permission....");
      return false;
    }

    val hm = (HandlerMethod) h;

    val httpLog = hm.getMethodAnnotation(HttpLog.class);
    if (httpLog == null) {
      return true;
    }

    val processor = cacheGet(httpLog);
    Req req = (Req) r.getAttribute(Filter.HTTPLOG_REQ);
    try {
      processor.logReq(r, req);
    } catch (Exception ex) {
      log.warn("failed to log req {}", req, ex);
    }

    r.setAttribute(HTTPLOG_PROCESSOR, processor);
    log.debug("preHandle method:{} URI:{} httpLog:{}", r.getMethod(), r.getRequestURI(), httpLog);

    return true;
  }

  private HttpLogProcessor cacheGet(HttpLog httpLog) {
    val processor = cache.get(httpLog);
    if (processor != null) {
      return processor;
    }

    synchronized (this) {
      val p = HttpLogProcessor.create(httpLog, connGetter);
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
}
