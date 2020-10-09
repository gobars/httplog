package com.github.gobars.httplog.snack.features;

import static com.google.common.truth.Truth.assertThat;

import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.httplog.snack.core.Cnf;
import org.junit.Test;

public class MaskTest {
  @Test
  public void test() {
    String s = "{\"name\":\"12345678901234567890\",\"key1\":\"123456\",\"key2\":\"123456\"}";
    String j = Onode.load(s).toJson();
    assertThat(j).isEqualTo(s);

    j = Onode.load(s, Cnf.def().maskKeys("key1", "key2-")).toJson();
    assertThat(j).isEqualTo("{\"name\":\"12345678901234567890\",\"key1\":\"...\"}");
  }
}
