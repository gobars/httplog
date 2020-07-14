package com.github.gobars.httplog.snack.features;

import static com.google.common.truth.Truth.assertThat;

import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.httplog.snack.core.Cnf;
import org.junit.Test;

public class AbbreviateTest {
  @Test
  public void test0() {
    String s = "{\"name\":\"12345678901234567890\"}";
    String j = Onode.load(s).toJson();
    assertThat(j).isEqualTo(s);

    j = Onode.load(s, Cnf.def().abbrevMaxSize(10)).toJson();
    assertThat(j).isEqualTo("{\"name\":\"1234567...\"}");
  }
}
