package com.github.gobars.httplog.spring.oracle;

import com.github.gobars.httplog.SqlRunner;
import com.github.gobars.httplog.spring.TestDto;
import java.sql.SQLException;
import javax.sql.DataSource;

import com.github.ksuid.Ksuid;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = App.class)
public class OracleAppTest {
  private static final TestDto TEST_DTO = TestDto.builder().id(10).build();
  private static final TestDto TEST_DTO_EMPTY = TestDto.builder().build();
  private static final MultiValueMap<String, String> EMPTY_HEADER = new LinkedMultiValueMap<>();
  private static final MultiValueMap<String, String> JSON_HEADER = new LinkedMultiValueMap<>();

  static {
    JSON_HEADER.add("Content-Type", "application/json");
  }

  @LocalServerPort private int port;
  @Autowired DataSource dataSource;

  @SneakyThrows
  @Test
  public void Duplicate() {
    SqlRunner runner = new SqlRunner(dataSource.getConnection());
    int id = Ksuid.newKsuid().hashCode();
    runner.insert("insert into biz_log(id) values(?)", id);

    try {
      runner.insert("insert into biz_log(id) values(?)", id);
      Assert.fail();
    } catch (SQLException se) {
      System.out.println(se.getSQLState());
      System.out.println(se.getErrorCode());
      System.out.println(se);
    }

    try {
      runner.insert("insert into biz_log(id) values(null)");
      Assert.fail();
    } catch (SQLException se) {
      System.out.println(se.getSQLState());
      System.out.println(se.getErrorCode());
      System.out.println(se);
    }
  }

  @Test
  public void getTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpEntity<String> entity = new HttpEntity<>(null, EMPTY_HEADER);

    ResponseEntity<String> response =
        restTemplate.exchange(createURLWithPort("/test/10"), HttpMethod.GET, entity, String.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo("test id : 10");
  }

  @Test
  public void postTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpEntity<TestDto> entity = new HttpEntity<>(TEST_DTO, JSON_HEADER);

    ResponseEntity<TestDto> response =
        restTemplate.exchange(createURLWithPort("/test"), HttpMethod.POST, entity, TestDto.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo(TEST_DTO);
  }

  @Test
  public void putTestError() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpEntity<TestDto> entity = new HttpEntity<>(TEST_DTO, JSON_HEADER);

    ResponseEntity<TestDto> response =
        restTemplate.exchange(createURLWithPort("/test"), HttpMethod.PUT, entity, TestDto.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    Assertions.assertThat(response.getBody()).isEqualTo(TEST_DTO_EMPTY);
  }

  private String createURLWithPort(String uri) {
    return String.format("http://localhost:%s%s", port, uri);
  }
}
