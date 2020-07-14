package com.github.gobars.httplog.snack.speed.jsonpath;

import com.github.gobars.httplog.snack.Onode;
import org.junit.Test;

public class SpeedJsonPathTest2 {
  @Test
  public void test1() {
    String json =
        "{\"store\":{\"book\":[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95},{\"category\":\"fiction\",\"author\":\"Evelyn Waugh\",\"title\":\"Sword of Honour\",\"price\":12.99,\"isbn\":\"0-553-21311-3\"}],\"bicycle\":{\"color\":\"red\",\"price\":19.95}}}";
    Onode n = Onode.load(json);

    Onode t7 = n.select("$..book[*].author");
    System.out.println(t7);
    assert t7.isArray() && t7.count() == 2;

    Onode t8 = n.select("$..book.author");
    System.out.println(t8);
    assert t8.isArray() && t8.count() == 0;
  }
}
