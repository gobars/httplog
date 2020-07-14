package com.github.gobars.httplog.snack.features;

import static org.junit.Assert.assertEquals;

import com.github.gobars.httplog.snack.Onode;
import java.util.*;
import org.junit.Test;

public class JsonPathTest3 {
  @Test
  public void test1() {
    Entity entity = new Entity(123, new Object());
    Onode n = Onode.load(entity);

    assert n.select("$.id").getInt() == 123;
    assert n.select("$.*").count() == 2;
  }

  @Test
  public void test2() {
    List<Entity> entities = new ArrayList<Entity>();
    entities.add(new Entity("wenshao"));
    entities.add(new Entity("ljw2083"));
    Onode n = Onode.load(entities);

    List<String> names = n.select("$.name").toObject(List.class);
    assert names.size() == 2;
  }

  @Test
  public void test3() {
    List<Entity> entities = new ArrayList<Entity>();
    entities.add(new Entity("wenshao"));
    entities.add(new Entity("ljw2083"));
    entities.add(new Entity("Yako"));
    Onode n = Onode.load(entities);

    List<Entity> result = n.select("$[1,2]").toObject((new ArrayList<Entity>() {}).getClass());
    assert result.size() == 2;
  }

  @Test
  public void test4() {
    List<Entity> entities = new ArrayList<Entity>();
    entities.add(new Entity("wenshao"));
    entities.add(new Entity("ljw2083"));
    entities.add(new Entity("Yako"));
    Onode n = Onode.load(entities);

    List<Entity> result = n.select("$[0:2]").toObject((new ArrayList<Entity>() {}).getClass());
    assert result.size() == 2;
  }

  @Test
  public void test5() {
    List<Entity> entities = new ArrayList<Entity>();
    entities.add(new Entity(1001, "ljw2083"));
    entities.add(new Entity(1002, "wenshao"));
    entities.add(new Entity(1003, "yakolee"));
    entities.add(new Entity(1004, null));
    Onode n = Onode.load(entities);

    Onode rst = n.select("$[?($.id in [1001,1002])]");
    assert rst.count() == 2;
  }

  @Test
  public void test6() {
    Entity entity = new Entity(1001, "ljw2083");
    Onode n = Onode.load(entity);

    assert n.select("$[?(id == 1001)]").isObject();
    assert n.select("$[?(id == 1002)]").isNull();

    n.select("$").set("id", 123456);
    assert n.get("id").getInt() == 123456;

    n.get("value").add(1).add(2).add(3);
    assert n.get("value").count() == 3;
  }

  @Test
  public void test7() {
    Map root =
        Collections.singletonMap(
            "company",
            Collections.singletonMap(
                "departs",
                Arrays.asList(
                    Collections.singletonMap("id", 1001),
                    Collections.singletonMap("id", 1002),
                    Collections.singletonMap("id", 1003))));

    Onode n = Onode.load(root);

    List<Object> ids = n.select("$..id").toObject(List.class);
    assertEquals(3l, ids.size());
    assertEquals(1001l, ids.get(0));
    assertEquals(1002l, ids.get(1));
    assertEquals(1003l, ids.get(2));
  }

  @Test
  public void testx() {

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

  public static class Entity {
    public int id;
    public String name;
    public Object value;

    public Entity() {}

    public Entity(int id, Object value) {
      this.id = id;
      this.value = value;
    }

    public Entity(String name) {
      this.name = name;
    }
  }
}
