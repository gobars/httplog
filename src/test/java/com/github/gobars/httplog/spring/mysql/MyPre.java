package com.github.gobars.httplog.spring.mysql;

import com.github.gobars.httplog.HttpLogAttr;
import com.github.gobars.httplog.HttpLogPre;
import com.github.gobars.httplog.Req;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyPre implements HttpLogPre {
  @Autowired DataSource dataSource;

  @Override
  @SneakyThrows
  public Map<String, String> create(
      HttpServletRequest r, Req req, HttpLogAttr httpLog, Map<String, String> fixes) {
    val m = new HashMap<String, String>(1);

    @Cleanup val c = dataSource.getConnection();
    m.put("hi", c.getMetaData().getDriverName());

    return m;
  }
}
