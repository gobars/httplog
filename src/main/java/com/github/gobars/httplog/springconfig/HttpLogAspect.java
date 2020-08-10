package com.github.gobars.httplog.springconfig;

import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;

import com.github.gobars.httplog.HttpLog;
import com.github.gobars.httplog.HttpLogAttr;
import com.github.gobars.httplog.HttpLogCustom;
import com.github.gobars.httplog.HttpLogFork;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class HttpLogAspect {
  /**
   * 拦截@HttpLog注解的Service的方法.
   *
   * @param jp 拦截点
   * @param httpLog 注解
   */
  @Around("@annotation(httpLog)")
  public Object pointcut(ProceedingJoinPoint jp, HttpLog httpLog) throws Throwable {
    if (shouldBypass(jp, httpLog)) {
      return jp.proceed();
    }

    val attr = HttpLogAttr.create(new AnnotationAttributes(getAnnotationAttributes(httpLog)));
    Object req = null;
    if (jp.getArgs().length > 0) {
      req = jp.getArgs()[0];
    }

    HttpLogFork f = HttpLogCustom.fork(attr, req);
    f.setMethod(((MethodSignature) jp.getSignature()).getMethod().getName());

    HashMap<String, String> last = HttpLogCustom.get().getMap();
    HashMap<String, String> thisMap = new HashMap<>();
    HttpLogCustom.get().setMap(thisMap);

    try {
      Object result = jp.proceed();

      f.customAll(thisMap);
      HttpLogCustom.get().setMap(last);

      f.submit(result);
      return result;
    } catch (Throwable t) {
      f.customAll(thisMap);
      HttpLogCustom.get().setMap(last);

      f.submitError(t);
      throw t;
    }
  }

  private boolean shouldBypass(ProceedingJoinPoint jp, HttpLog httpLog) {
    log.debug("pointcut httpLog got:{}", httpLog);

    Class<?> targetClass = jp.getTarget().getClass();
    if (targetClass.getSimpleName().contains("Controller")) {
      log.debug("pointcut httpLog ignored:{} Controller", httpLog);
      return true;
    }

    // since all @Controller @RestController  @RequestMapping @XyzMapping are all in
    // this package, we will bypass it to skip @HttpLog in Controller's methods.
    String prefix = "org.springframework.web.bind.annotation";
    if (anyAnnStarts(targetClass.getAnnotations(), prefix)) {
      log.debug("pointcut httpLog ignored:{} Class", httpLog);

      return true;
    }

    if (anyAnnStarts(((MethodSignature) jp.getSignature()).getMethod().getAnnotations(), prefix)) {
      log.debug("pointcut httpLog ignored:{} Method", httpLog);

      return true;
    }

    log.debug("pointcut httpLog ok:{}", httpLog);
    return false;
  }

  public static boolean anyAnnStarts(Annotation[] annotations, String webBind) {
    for (Annotation a : annotations) {
      if (a.annotationType().getName().startsWith(webBind)) {
        return true;
      }
    }

    return false;
  }
}
