package com.github.gobars.httplog.spring.mysql;

import com.github.gobars.httplog.spring.TestDto;
import com.github.gobars.id.Id;
import com.github.gobars.id.db.SqlRunner;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
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
public class MysqlAppTest {
  private static final TestDto TEST_DTO =
      TestDto.builder().id(10).data("12345678901234567890").build();
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
    long id = Id.next();
    runner.insert("insert into biz_log(id) values(?)", id);

    try {
      runner.insert("insert into biz_log(id) values(?)", id);
      Assert.fail();
    } catch (SQLException se) {
      System.out.println(se.getSQLState().equals("23000"));
      System.out.println(se.getSQLState());
      System.out.println(se);
    }
  }

  @Test
  @SneakyThrows
  public void getTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpEntity<String> entity = new HttpEntity<>(null, EMPTY_HEADER);

    ResponseEntity<String> response =
        restTemplate.exchange(createURLWithPort("/test/10"), HttpMethod.GET, entity, String.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo("test id : 10");

    response =
        restTemplate.exchange(createURLWithPort("/test/10"), HttpMethod.GET, entity, String.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo("test id : 10");

    TimeUnit.SECONDS.sleep(3);
  }

  @Test
  @SneakyThrows
  public void json() {
    TestRestTemplate rt = new TestRestTemplate();
    HttpEntity<String> entity = new HttpEntity<>(null, EMPTY_HEADER);

    String addr = createURLWithPort("/test/json/10");
    ResponseEntity<String> r = rt.exchange(addr, HttpMethod.GET, entity, String.class);
    Assertions.assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);

    r = rt.exchange(addr, HttpMethod.GET, entity, String.class);
    Assertions.assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(r.getBody())
        .isEqualTo(
            "{\"a1\":\"10\",\"c1\":\"123456789012567890123458901256789012346789012345890123467890123456789012567890123458901256789012346789012345890123467890\",\"b1\":\"毛主席在天安门城楼上宣布中华人民共和国中央人民政府今天成立了\"}");

    TimeUnit.SECONDS.sleep(3);
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

  @Test
  public void custom() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpEntity<String> entity = new HttpEntity<>(null, EMPTY_HEADER);

    ResponseEntity<String> response =
        restTemplate.exchange(
            createURLWithPort("/test/custom"), HttpMethod.GET, entity, String.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo("custom OK");
  }

  @Test
  public void customLocal() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpEntity<String> entity = new HttpEntity<>(null, EMPTY_HEADER);

    ResponseEntity<String> response =
        restTemplate.exchange(
            createURLWithPort("/test/customLocal"), HttpMethod.GET, entity, String.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isEqualTo("custom OK");
  }

  private String createURLWithPort(String uri) {
    return String.format("http://localhost:%s%s", port, uri);
  }
}
