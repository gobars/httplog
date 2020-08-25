package com.github.gobars.httplog.spring;

import com.github.gobars.httplog.springconfig.HttpLogTags;
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
    HttpLogTags httpLogTags = HttpLogTags.parseYml(is);

    System.out.println(httpLogTags);
  }
}
