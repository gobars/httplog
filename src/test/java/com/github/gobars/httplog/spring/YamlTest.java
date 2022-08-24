package com.github.gobars.httplog.spring;

import com.github.gobars.httplog.springconfig.HttpLogYml;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class YamlTest {
  @Test
  @SneakyThrows
  public void test() {
    @Cleanup val is = new ClassPathResource("httplog.yml").getInputStream();
    HttpLogYml httpLogYml = HttpLogYml.loadYml(is);

    System.out.println(httpLogYml);
  }
}
