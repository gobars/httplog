package com.github.gobars.httplog.snack.demo;

import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.httplog.snack.core.Cnf;
import com.github.gobars.httplog.snack.core.Feature;
import com.github.gobars.httplog.snack.core.TypeRef;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class Demo3 {
  @Test
  public void demo1() {

    int i = Onode.load("100").getInt(); // 100
    double d = Onode.load("\"99.99\"").getDouble(); // 99.99
    boolean b = Onode.load("true").getBoolean(); // true
    String str = Onode.load("String").getString(); // String

    Onode tmp = new Onode();

    tmp.cfg().add(Feature.QuoteFieldNames);

    assert i == 100;
    assert d == 99.99;
    assert b == true;
    assert str == "String";
  }

  public void foeach_demo() {
    Onode tmp = new Onode();

    if (tmp.isArray()) {
      tmp.forEach((v) -> {});
    }

    if (tmp.isObject()) {
      tmp.forEach((k, v) -> {});
    }
  }

  @Test
  public void demo2() {
    String jsonNumber = Onode.load(100).toJson(); // 100
    String jsonBoolean = Onode.load(false).toJson(); // false
    String jsonString = Onode.load("String").toString(); // "String"

    Assert.assertEquals(jsonNumber, "100");
    Assert.assertEquals(jsonBoolean, "false");
    Assert.assertEquals(jsonString, "\"String\"");
  }

  @Test
  public void demo3() {
    User user = new User("张三", 24);
    String json = Onode.stringify(user); // {"name":"张三","age":24}

    String json2 = Onode.serialize(user); // {"@type":"demo.User","name":"\u5F20\u4E09","age":24}

    System.out.println(json);
    System.out.println(json2);
  }

  @Test
  public void demo4() {
    String json = "{name:'张三',age:24}";
    User user = Onode.deserialize(json, User.class);

    assert user.age == 24;
  }

  @Test
  public void demo5() {
    String jsonArray = "[\"Android\",\"Java\",\"PHP\"]";
    String[] strings = Onode.deserialize(jsonArray, String[].class);

    assert strings.length == 3;
  }

  @Test
  public void demo6() {
    String jsonArray = "[\"Android\",\"Java\",\"PHP\"]";

    Onode ary0 = Onode.load(jsonArray);
    List<String> ary1 = Onode.deserialize(jsonArray, (new ArrayList<String>() {}).getClass());
    List<String> ary2 = Onode.deserialize(jsonArray, (new TypeRef<List<String>>() {}).getClass());

    assert ary1.size() == ary2.size();
  }

  @Test
  public void demo7() {
    String json = "{\"name\":\"张三\",\"age\":\"24\"}";

    // 反序列化
    User user = Onode.load(json).toObject(User.class);

    // 序列化
    Onode.load(user).toJson();
  }

  @Test
  public void demo8() {
    User user = new User("张三", 24);
    System.out.println(Onode.stringify(user)); // {"name":"张三","age":24}

    Cnf cnf = Cnf.of(Feature.SerializeNulls);
    System.out.println(
        Onode.load(user, cnf).toJson()); // {"name":"张三","age":24,"emailAddress":null}
  }

  @Test
  public void demo9() {
    Date date = new Date();

    Cnf cnf = Cnf.of(Feature.WriteDateUseFormat).build(c -> c.dateFormat = "yyyy-MM-dd");

    System.out.println(Onode.load(date, cnf).toJson()); // 2019-12-06
  }

  @Test
  public void demo10() {
    User user = new User("name", 12, "xxx@mail.cn");
    String json =
        Onode.load(user)
            .rename("emailAddress", "email")
            .toJson(); // {"name":"name","age":12,"email":"xxx@mail.cn"}

    System.out.println(json);
  }

  @Test
  public void demo11() {
    String jsonStr =
        "{\n"
            + "    \"store\": {\n"
            + "        \"bicycle\": {\n"
            + "            \"color\": \"red\",\n"
            + "            \"price\": 19.95\n"
            + "        },\n"
            + "        \"book\": [\n"
            + "            {\n"
            + "                \"author\": \"刘慈欣\",\n"
            + "                \"price\": 8.95,\n"
            + "                \"category\": \"科幻\",\n"
            + "                \"title\": \"三体\"\n"
            + "            },\n"
            + "            {\n"
            + "                \"author\": \"itguang\",\n"
            + "                \"price\": 12.99,\n"
            + "                \"category\": \"编程语言\",\n"
            + "                \"title\": \"go语言实战\"\n"
            + "            }\n"
            + "        ]\n"
            + "    }\n"
            + "}";

    System.out.println(jsonStr);

    Onode o = Onode.load(jsonStr);

    // 得到所有的书
    Onode books = o.select("$.store.book");
    System.out.println("books=::" + books);

    // 得到所有的书名
    Onode titles = o.select("$.store.book.title");
    System.out.println("titles=::" + titles);

    // 第一本书title
    Onode title = o.select("$.store.book[0].title");
    System.out.println("title=::" + title);

    // price大于10元的book
    Onode list = o.select("$.store.book[?(price > 10)]");
    System.out.println("price大于10元的book=::" + list);

    // price大于10元的title
    Onode list2 = o.select("$.store.book[?(price > 10)].title");
    System.out.println("price大于10元的title=::" + list2);

    // category(类别)为科幻的book
    Onode list3 = o.select("$.store.book[?(category == '科幻')]");
    System.out.println("category(类别)为科幻的book=::" + list3);

    // bicycle的所有属性值
    Onode values = o.select("$.store.bicycle.*");
    System.out.println("bicycle的所有属性值=::" + values);

    // bicycle的color和price属性值
    Onode read = o.select("$.store.bicycle['color','price']");
    System.out.println("bicycle的color和price属性值=::" + read);
  }
}
