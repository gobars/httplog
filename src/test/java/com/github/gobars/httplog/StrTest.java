package com.github.gobars.httplog;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class StrTest {
  @Test
  public void substring() {
    assertThat(Str.truncate(null, 4)).isNull();
    assertThat(Str.truncate("", 4)).isEmpty();
    assertThat(Str.truncate("123", 4)).isEqualTo("123");
    assertThat(Str.truncate("兵123", 4)).isEqualTo("兵1");
    assertThat(Str.truncate("黄进兵", 4)).isEqualTo("黄");
    assertThat(Str.truncate("黄😝兵", 1)).isEqualTo("");
    assertThat(Str.truncate("黄😝兵", 2)).isEqualTo("");
    assertThat(Str.truncate("黄😝兵", 3)).isEqualTo("黄");
    assertThat(Str.truncate("黄😝兵", 4)).isEqualTo("黄");
    assertThat(Str.truncate("黄😝兵", 5)).isEqualTo("黄");
    assertThat(Str.truncate("黄😝兵", 6)).isEqualTo("黄");
    assertThat(Str.truncate("黄😄兵", 7)).isEqualTo("黄😄");
    assertThat(Str.truncate("黄😄兵", 8)).isEqualTo("黄😄");
    assertThat(Str.truncate("黄😄兵", 9)).isEqualTo("黄😄");
    assertThat(Str.truncate("黄😄兵", 10)).isEqualTo("黄😄兵");
    assertThat(Str.truncate("黄😄兵", 11)).isEqualTo("黄😄兵");
  }
}
