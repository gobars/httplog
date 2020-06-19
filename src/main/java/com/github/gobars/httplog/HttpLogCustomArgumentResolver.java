package com.github.gobars.httplog;

import javax.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class HttpLogCustomArgumentResolver implements HandlerMethodArgumentResolver {
  @Override
  public boolean supportsParameter(MethodParameter p) {
    return p.getParameter().getType() == HttpLogCustom.class;
  }

  @Override
  public Object resolveArgument(
      MethodParameter p, ModelAndViewContainer c, NativeWebRequest r, WebDataBinderFactory f) {
    return ((HttpServletRequest) r.getNativeRequest()).getAttribute(Const.CUSTOM);
  }
}
