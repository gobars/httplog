package com.github.gobars.httplog.snack.speed;

import com.github.gobars.httplog.snack.Onode;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class BaseSpeedTest2 {
  @Test
  public void test1() {
    // 1.加载json
    Onode n =
        Onode.load(
            "{code:1,msg:'Hello world',data:{list:[1,2,3,4,5], ary2:[{a:2},{a:3,b:{c:'ddd'}}]}}");

    Onode tmp = n.select("data.list[1,4]");

    long start = System.currentTimeMillis();
    for (int i = 0, len = 10000; i < len; i++) {
      n.select("data.list[1,4]");
    }

    long times = System.currentTimeMillis() - start;

    System.out.println(times);

    assert times > 0;
  }

  @Test
  public void test2() {
    // 1.加载json
    Onode n =
        Onode.load(
            "{code:1,msg:'Hello world',data:{list:[1,2,3,4,5], ary2:[{a:2},{a:3,b:{c:'ddd'}}]}}");

    Onode tmp = n.select("data.list[1,4]");

    long start = System.currentTimeMillis();
    for (int i = 0, len = 10000; i < len; i++) {
      Onode ary2 = new Onode().asArray();
      Onode tmp2 = n.get("data").get("list");
      ary2.addNode(tmp2.get(1));
      ary2.addNode(tmp2.get(4));
    }

    long times = System.currentTimeMillis() - start;

    System.out.println(times);

    assert times > 0;
  }

  @Test
  public void test3() {
    // 1.加载json
    Onode n =
        Onode.load(
            "{code:1,msg:'Hello world',data:{list:[1,2,3,4,5], ary2:[{a:2},{a:3,b:{c:'ddd'}}]}}");

    Onode tmp = n.select("data.list[1,4]");

    long start = System.currentTimeMillis();
    for (int i = 0, len = 10000; i < len; i++) {
      Onode ary2 = new Onode().asArray();
      Onode tmp2 = n.get("data").get("list");
      ary2.nodeData().array.add(tmp2.get(1));
      ary2.nodeData().array.add(tmp2.get(4));
    }

    long times = System.currentTimeMillis() - start;

    System.out.println(times);

    assert times > 0;
  }

  @Test
  public void test4() {
    // 1.加载json
    Onode n =
        Onode.load(
            "{code:1,msg:'Hello world',data:{list:[1,2,3,4,5], ary2:[{a:2},{a:3,b:{c:'ddd'}}]}}");

    Onode tmp = n.select("data.list[1,4]");

    long start = System.currentTimeMillis();
    for (int i = 0, len = 10000; i < len; i++) {
      List<Onode> ary2 = new ArrayList<>();
      Onode tmp2 = n.getOrNull("data").getOrNull("list");
      ary2.add(tmp2.get(1));
      ary2.add(tmp2.get(4));
    }

    long times = System.currentTimeMillis() - start;

    System.out.println(times);

    assert times > 0;
  }
}
