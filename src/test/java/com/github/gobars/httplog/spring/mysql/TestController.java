package com.github.gobars.httplog.spring.mysql;

import static com.google.common.truth.Truth.assertThat;

import com.github.gobars.httplog.HttpLog;
import com.github.gobars.httplog.HttpLogCustom;
import com.github.gobars.httplog.spring.TestDto;
import com.github.gobars.httplog.spring.TestException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/test")
public class TestController {

  /**
   * test get.
   *
   * @param id id
   * @return id
   */
  @MyHttpLog(fix = "desc:ID查找", biz = "测试查询")
  @GetMapping(value = "/{id}")
  public String get(@PathVariable Integer id) {
    return "test id : " + id;
  }

  /**
   * test get.
   *
   * @param id id
   * @return id
   */
  @MyHttpLog(fix = "desc:ID查找返回JSON", biz = "JSON测试查询")
  @GetMapping(value = "/json/{id}")
  public Map<String, String> json(@PathVariable String id) {
    Map<String, String> m = new HashMap<>();
    m.put("a1", id);
    m.put("b1", "毛主席在天安门城楼上宣布中华人民共和国中央人民政府今天成立了");
    m.put(
        "c1",
        "123456789012567890123458901256789012346789012345890123467890123456789012567890123458901256789012346789012345890123467890");
    return m;
  }

  /**
   * test post.
   *
   * @param testDto testDto
   * @return testDto
   */
  @HttpLog(tables = "biz_log_post", sync = true, abbrevMaxSize = 10)
  @PostMapping
  public TestDto post(@RequestBody TestDto testDto) {
    return testDto;
  }

  /**
   * test put error.
   *
   * @param testDto testDto
   */
  @HttpLog(tables = "biz_log_post")
  @PutMapping
  public void error(@RequestBody TestDto testDto) {
    log.warn("error TestException will be thrown");
    throw new TestException(testDto.toString());
  }

  @GetMapping("/custom")
  @HttpLog(tables = "biz_log_custom")
  String listContributors(HttpLogCustom httpLogCustom) {
    assertThat(httpLogCustom).isNotNull();

    httpLogCustom.put("name", "custom");
    return "custom OK";
  }

  @GetMapping("/customLocal")
  @HttpLog(tables = "biz_log_custom")
  String listContributors() {
    HttpLogCustom.get().put("name", "customLocal");
    return "custom OK";
  }
}
