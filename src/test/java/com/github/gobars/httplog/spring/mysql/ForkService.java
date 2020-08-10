package com.github.gobars.httplog.spring.mysql;

import com.github.gobars.httplog.HttpLog;
import com.github.gobars.httplog.HttpLogCustom;
import com.github.gobars.httplog.TestUtil;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class ForkService {
  @HttpLog(tables = "biz_log_fork", fix = "channel:一所")
  public MyResponse rpc1(MyRequest req) {
    Random random = new Random();
    random.setSeed(System.currentTimeMillis());

    MyResponse response = new MyResponse("MyResponse1");
    response.setTran(808);

    TestUtil.sleep(1000 + random.nextInt(100));

    HttpLogCustom.get().put("name", "一所流水123");

    return response;
  }

  @HttpLog(tables = "biz_log_fork", fix = "channel:二所")
  public MyResponse rpc2(MyRequest req) {
    Random random = new Random();
    random.setSeed(System.currentTimeMillis());

    // rpc2 ...
    TestUtil.sleep(1000 + random.nextInt(100));
    HttpLogCustom.get().put("name", "二所流水123");

    throw new RuntimeException("timeout error");
  }
}
