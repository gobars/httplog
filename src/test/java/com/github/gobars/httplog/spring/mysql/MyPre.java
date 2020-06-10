package com.github.gobars.httplog.spring.mysql;

import com.github.gobars.httplog.HttpLogAttr;
import com.github.gobars.httplog.HttpLogPre;
import com.github.gobars.httplog.Req;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.val;

public class MyPre implements HttpLogPre {
  @Override
  public Map<String, String> create(
      HttpServletRequest r, Req req, HttpLogAttr httpLog, Map<String, String> fixes) {
    val m = new HashMap<String, String>(1);

    m.put("hi", "bingoo");

    return m;
  }
}
