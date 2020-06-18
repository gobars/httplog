package com.github.gobars.httplog;

import lombok.val;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class HttpLogCustomArgumentResolver implements HandlerMethodArgumentResolver {
  @Override
  public boolean supportsParameter(MethodParameter p) {
    return p.getParameter().getType() == HttpLogCustom.class;
  }

  @Override
  public Object resolveArgument(
      MethodParameter p, ModelAndViewContainer c, NativeWebRequest r, WebDataBinderFactory f) {
    val hr = (HttpServletRequest) r.getNativeRequest();

    HttpLogCustom custom = new HttpLogCustom();
    hr.setAttribute(Const.CUSTOM, custom);

    return custom;
  }
}
