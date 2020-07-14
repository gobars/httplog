package com.github.gobars.httplog.snack.features;

import com.github.gobars.httplog.snack.Onode;
import java.util.List;
import org.junit.Test;

public class JsonPathTest {
  @Test
  public void demo1() {
    // 1.加载json
    Onode n =
        Onode.load(
            "{code:1,msg:'Hello world',data:{list:[1,2,3,4,5], ary2:[{a:2},{a:3,b:{c:'ddd'}}]}}");

    // 2.取一个属性的值
    String msg = n.get("msg").getString();

    // 3.取列表里的一项
    int li2 = n.get("data").get("list").get(2).getInt();

    // 4.获取一个数组
    // List<Integer> list = n.get("data").get("list").toObject(List.class);
    List<Integer> list = n.select("data.list").toObject(List.class);

    // int mi = n.get("data").get("list").get(0).getInt();
    int mi = n.select("data.list[-1]").getInt();

    List<Integer> list2 = n.select("data.list[2,4]").toObject(List.class);
    List<Integer> list3 = n.select("data.list[2:4]").toObject(List.class);

    Onode ary2_a = n.select("data.ary2[*].b.c");

    Onode ary2_b = n.select("..b");

    Onode ary2_c = n.select("data..b.c");

    assert list.size() == 5;
  }

  @Test
  public void demo2() {
    // 1.加载json
    Onode n =
        Onode.load(
            "{code:1,msg:'Hello world',data:{list:[1,2,3,4,5], ary2:[{a:2},{a:3,b:{c:'ddd'}}]}}");

    // 4.获取一个数组
    // List<Integer> list = n.get("data").get("list").toObject(List.class);
    List<Integer> list = n.select("$.data.list").toObject(List.class);

    // int mi = n.get("data").get("list").get(0).getInt();
    int mi = n.select("$.data.list[-1]").getInt();

    List<Integer> list2 = n.select("$.data.list[2,4]").toObject(List.class);
    List<Integer> list3 = n.select("$.data.list[1:4]").toObject(List.class);
    List<Integer> list4 = n.select("$.data.list[:4]").toObject(List.class);

    Onode ary2_a = n.select("$.data.ary2[*].b.c");

    Onode ary2_b = n.select("$..b");

    Onode ary2_c = n.select("$.data..b.c");

    assert list.size() == 5;
  }

  @Test
  public void demo3() {
    // 1.加载json
    Onode n =
        Onode.load(
            "{code:1,msg:'Hello world',data:{list:[1,2,3,4,5], ary2:[{a:2},{a:3,b:{c:'ddd'}}]}}");

    Onode ary2_a = n.select("$['code','msg']");
    assert ary2_a.count() == 2;

    Onode ary2_b = n.select("$.data[*]");
    assert ary2_b.count() == 2;

    Onode ary2_c = n.select("$['data']['list'][2]");
    assert ary2_c.getInt() == 3;
  }

  @Test
  public void demo4() {
    // 1.加载json
    Onode n =
        Onode.load(
            "{code:1,msg:'Hello world',data:{list:[1,2,3,4,5], ary2:[{a:2},{a:3,b:{c:'ddd'}}]}}");

    Onode ary2_a = n.select("$.*");
    assert ary2_a.count() == 3;

    Onode ary2_b = n.select("$..*");
    assert ary2_b.count() == 16;

    Onode ary2_c = n.select("$..*[1]");
    assert ary2_c.count() == 2;

    Onode ary2_d = n.select("$.*.list[0][0]");
    assert ary2_d.isValue();

    Onode ary2_e = n.select("$..list[0][0]");
    assert ary2_e.isValue();
  }

  @Test
  public void testx1() {
    // 1.加载json
    Onode n =
        Onode.load(
            "{code:1,msg:'Hello world',data:{list:[1,2,3,4,5],b:2,ary2:[{a:2,b:8},{a:3,b:{c:'ddd',b:23}}]}}");

    Onode t1 = n.select("$.data.ary2[*].a");
    assert t1.count() == 2;

    Onode t2 = n.select("$..a");
    assert t2.count() == 2;

    Onode t3 = n.select("$.data.*");
    assert t3.count() == 3;

    Onode t4 = n.select("$.data..a");
    assert t4.count() == 2;

    //        ONode t5 = n.select("$..a[0]");//a不是数组，不会支持了
    //        assert  t5.getInt()==2;

    Onode t60 = n.select("$..b");
    //    System.out.println(t60.toJson());

    Onode t6 = n.select("$..list[-1]");
    assert t6.get(0).getInt() == 5;

    Onode t7 = n.select("$..list[0,1]");
    assert t7.count() == 2;

    Onode t8 = n.select("$..list[:2]");
    assert t8.count() == 2;

    Onode t9 = n.select("$..list[1:2]");
    assert t9.count() == 1;

    Onode ta = n.select("$..list[-2:]");
    assert ta.count() == 2;

    Onode tb = n.select("$..list[2:]");
    assert tb.count() == 3;
  }

  @Test
  public void testx2() {
    // 1.加载json
    Onode n = Onode.load("[{b:{c:1}}, {b:{d:1}}, {b:{c:2}}, {b:{c:23}}]");

    Onode t0 = n.select("$..b");
    assert t0.count() == 4;

    Onode t1 = n.select("$..b[?(@.c)]");
    assert t1.count() == 3;

    Onode t2 = n.select("$..b[?(@.c == 1)]");
    assert t2.count() == 1;

    Onode t3 = n.select("$..b[?(@.c == 12)]");
    assert t3.count() == 0;

    Onode t4 = n.select("$..b[?(@.c > 1)]");
    assert t4.count() == 2;

    Onode t4_min = n.select("$..b[?(@.c > 1)].c.min()");
    assert t4_min.getInt() == 2;

    Onode t5 = n.select("$..b[?(@.c in [1,2])]");
    assert t5.count() == 2;

    Onode t6 = n.select("$..b[?(@.c nin [1,2])]");
    assert t6.count() == 1;

    Onode t7 = n.select("$..b[?(@.c =~ /\\d+/)]");
    assert t7.count() == 3;
  }

  @Test
  public void testx3() {
    // 1.加载json
    Onode n = Onode.load("[{c:'aaaa'}, {b:'cccc'}, {c:'cccaa'}]");

    Onode t1 = n.select("$[?(@.c =~ /a+/)]"); //
    assert t1.count() == 2;

    Onode t2 = n.select("$[?(@.c =~ /a{4}/)]"); //
    assert t2.count() == 1;

    Onode t3 = n.select("$..*"); //
    assert t3.count() == 6;

    Onode t4 = n.select("$..*[?(@ =~ /c+/)]"); //
    assert t4.count() == 2;
  }

  @Test
  public void testx4() {
    // 1.加载json
    Onode n =
        Onode.load(
            "{code:1,msg:'Hello world',data:{list:[1,2,3,4,5],b:2,ary2:[{a:2,b:8},{a:3,b:{c:'ddd',b:23}}]}}");

    Onode t1 = n.select("$..b");
    assert t1.count() == 4;

    Onode t2 = n.select("$..b.min()");
    assert t2.getInt() == 2;

    Onode t3 = n.select("$..b.max()");
    assert t3.getInt() == 23;

    Onode t4 = n.select("$..b.avg()");
    assert t4.getInt() == 11;
  }
}
