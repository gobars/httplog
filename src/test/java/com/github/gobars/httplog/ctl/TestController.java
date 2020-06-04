package com.github.gobars.httplog.ctl;

import com.github.gobars.httplog.dto.TestDto;
import com.github.gobars.httplog.ex.TestException;
import org.springframework.web.bind.annotation.*;

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
    throw new TestException(testDto.toString());
  }
}
