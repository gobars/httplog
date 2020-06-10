package com.github.gobars.httplog.spring.mysql;

import com.github.gobars.httplog.HttpLogAttr;
import com.github.gobars.httplog.HttpLogPost;
import com.github.gobars.httplog.Req;
import com.github.gobars.httplog.Rsp;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.val;

public class MyPost implements HttpLogPost {
  @Override
  public Map<String, String> create(
      HttpServletRequest r,
      HttpServletResponse p,
      Req req,
      Rsp rsp,
      HttpLogAttr hl,
      Map<String, String> fixes) {
    val m = new HashMap<String, String>(1);
    m.put("bye", "huang");

    return m;
  }
}
