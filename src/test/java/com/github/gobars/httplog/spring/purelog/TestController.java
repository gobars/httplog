package com.github.gobars.httplog.spring.purelog;

import com.github.gobars.httplog.spring.TestDto;
import com.github.gobars.httplog.spring.TestException;
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
  @GetMapping(value = "/{id}")
  public String get(@PathVariable Integer id) {
    return "test id : " + id;
  }

  /**
   * test post.
   *
   * @param testDto testDto
   * @return testDto
   */
  @PostMapping
  public TestDto post(@RequestBody TestDto testDto) {
    return testDto;
  }

  /**
   * test put error.
   *
   * @param testDto testDto
   */
  @PutMapping
  public void error(@RequestBody TestDto testDto) {
    log.warn("error TestException will be thrown");
    throw new TestException(testDto.toString());
  }
}
