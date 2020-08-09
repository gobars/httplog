package com.github.gobars.httplog.springconfig;

import com.github.gobars.httplog.HttpLog;
import com.github.gobars.httplog.HttpLogAttr;
import com.github.gobars.httplog.HttpLogCustom;
import com.github.gobars.httplog.HttpLogFork;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
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
  @Around("@annotation(httpLog)") // the pointcut expression
  public Object pointcut(ProceedingJoinPoint jp, HttpLog httpLog) throws Throwable {
    log.debug("pointcut httpLog got:{}", httpLog);

    if (jp.getTarget().getClass().getSimpleName().contains("Controller")) {
      log.debug("pointcut httpLog ignored:{} Controller", httpLog);
      return jp.proceed();
    }

    String prefix = "org.springframework.web.bind.annotation";
    if (anyAnnStarts(jp.getTarget().getClass().getAnnotations(), prefix)) {
      log.debug("pointcut httpLog ignored:{} Class", httpLog);

      return jp.proceed();
    }

    Method method = ((MethodSignature) jp.getSignature()).getMethod();
    if (anyAnnStarts(method.getAnnotations(), prefix)) {
      log.debug("pointcut httpLog ignored:{} Method", httpLog);

      return jp.proceed();
    }

    log.debug("pointcut httpLog ok:{}", httpLog);

    val attr =
        HttpLogAttr.create(
            new AnnotationAttributes(AnnotationUtils.getAnnotationAttributes(httpLog)));
    Object req = null;
    if (jp.getArgs().length > 0) {
      req = jp.getArgs()[0];
    }

    HttpLogFork f = HttpLogCustom.fork(attr, req);
    f.setMethod(method.getName());
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

  public static boolean anyAnnStarts(Annotation[] annotations, String webBind) {
    for (Annotation a : annotations) {
      if (a.annotationType().getName().startsWith(webBind)) {
        return true;
      }
    }

    return false;
  }
}
