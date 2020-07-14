package com.github.gobars.httplog.snack.features;

import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.httplog.snack._models.CModel;
import com.github.gobars.httplog.snack._models.OrderModel;
import com.github.gobars.httplog.snack._models.UserGroupModel;
import com.github.gobars.httplog.snack._models.UserModel;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import org.junit.Test;

public class SerializationTest {

  @Test
  public void test0() {
    String temp = Onode.serialize("aaa");
    assert "\"aaa\"".equals(temp);

    temp = Onode.serialize(12);
    assert "12".equals(temp);

    temp = Onode.serialize(true);
    assert "true".equals(temp);

    temp = Onode.serialize(null);
    assert "null".equals(temp);

    temp = Onode.serialize(new Date());
    assert "null".equals(temp) == false;

    String tm2 = "{a:'http:\\/\\/raas.dev.zmapi.cn'}";

    Onode tm3 = Onode.load(tm2);

    assert tm3.toJson().equals("{\"a\":\"http://raas.dev.zmapi.cn\"}");
  }

  @Test
  public void test1() {
    try {
      String val = null;
      val.equals("");
    } catch (Exception ex) {
      //      ex.printStackTrace();

      String json = Onode.serialize(ex);

      //      System.out.println(json);

      NullPointerException ex2 = Onode.deserialize(json, NullPointerException.class);

      Object ex22 = Onode.deserialize(json, Object.class);
      assert ex22 instanceof NullPointerException;

      Object ex23 = Onode.deserialize(json, null);
      assert ex23 instanceof Map;

      ex2.printStackTrace();

      assert json != null;
    }
  }

  @Test
  public void test2() {

    UserGroupModel group = new UserGroupModel();
    group.id = 9999;
    group.users = new ArrayList<>();
    group.users2 = new LinkedHashMap<>();
    group.users3 = new TreeSet<>();
    group.names = new String[5];
    group.ids = new short[5];
    group.iids = new Integer[5];
    group.dd = new BigDecimal(12);
    group.tt1 = new Timestamp(new Date().getTime());
    group.tt2 = new Date();

    for (short i = 0; i < 5; i++) {
      UserModel user = new UserModel();
      user.id = i;
      user.name = "张三" + i;
      user.note = null;
      group.users.add(user);
      group.users2.put(Integer.valueOf(i), user);
      group.names[i] = "李四" + i;
      group.ids[i] = i;
    }

    String json = Onode.serialize(group);
    //    System.out.println(json);
    UserGroupModel group2 = Onode.deserialize(json, UserGroupModel.class);

    Object group22 = Onode.deserialize(json, Object.class);
    assert group22 instanceof UserGroupModel;

    Object group23 = Onode.deserialize(json, null);
    assert group23 instanceof Map;

    assert group2.id == 9999;
  }

  @Test
  public void test2_2() {

    UserGroupModel group = new UserGroupModel();
    group.id = 9999;
    group.users = new ArrayList<>();
    group.users2 = new LinkedHashMap<>();
    group.users3 = new TreeSet<>();
    group.names = new String[5];
    group.ids = new short[5];
    group.iids = new Integer[5];
    group.dd = new BigDecimal(12);
    group.tt1 = new Timestamp(new Date().getTime());
    group.tt2 = new Date();

    for (short i = 0; i < 5; i++) {
      UserModel user = new UserModel();
      user.id = i;
      user.name = "张三" + i;
      user.note = null;
      group.users.add(user);
      group.users2.put(Integer.valueOf(i), user);
      group.names[i] = "李四" + i;
      group.ids[i] = i;
    }

    String json = Onode.stringify(group); // 产生的json，没有@type
    //    System.out.println(json);
    UserGroupModel group2 = Onode.deserialize(json, UserGroupModel.class);

    Object group22 = Onode.deserialize(json, (new UserGroupModel() {}).getClass());
    assert group22 instanceof UserGroupModel;

    Object group23 = Onode.deserialize(json, LinkedHashMap.class);
    assert group23 instanceof Map;

    Object group24 = Onode.deserialize(json, null);
    assert group24 instanceof Map;

    assert group2.id == 9999;
  }

  @Test
  public void test3() {

    Map<String, Object> obj = new LinkedHashMap<String, Object>();

    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    Map<String, Object> m = new LinkedHashMap<String, Object>();
    m.put("a", 1);
    m.put("b", true);
    m.put("c", 1.2);
    m.put("d", new Date());

    list.add(m);

    obj.put("list", list);

    String json = Onode.serialize(obj);
    //    System.out.println(json);
    Map<String, Object> obj2 = Onode.deserialize(json, LinkedHashMap.class);
    assert obj2 instanceof LinkedHashMap;

    Map<String, Object> obj22 = Onode.deserialize(json, Object.class);
    assert obj22 instanceof HashMap;

    Map<String, Object> obj23 = Onode.deserialize(json, null);
    assert obj23 instanceof Map;

    assert obj2.size() == 1;
  }

  @Test
  public void test4() {
    UserModel user = new UserModel();
    user.id = 1111;
    user.name = "张三";
    user.note = null;

    OrderModel order = new OrderModel();
    order.user = user;
    order.order_id = 2222;
    order.order_num = "ddddd";

    String json = Onode.serialize(order);
    //    System.out.println(json);
    OrderModel order2 = Onode.deserialize(json, OrderModel.class);
    Object order22 = Onode.deserialize(json, Object.class);
    Map order23 = Onode.deserialize(json, null);

    assert 1111 == order2.user.id;
  }

  @Test
  public void test5() {
    CModel obj = new CModel();

    String json = Onode.serialize(obj);
    //    System.out.println(json);

    CModel obj2 = Onode.deserialize(json, CModel.class);

    assert obj2.list == null;
  }

  @Test
  public void test52() {
    CModel obj = new CModel();
    obj.init();

    String json = Onode.serialize(obj);
    //    System.out.println(json);

    CModel obj2 = Onode.deserialize(json, CModel.class);

    assert obj2.list.size() == obj.list.size();
  }

  @Test
  public void test53() {
    CModel obj = new CModel();
    obj.build();

    String json = Onode.serialize(obj);
    //    System.out.println(json);

    CModel obj2 = Onode.deserialize(json, CModel.class);

    assert obj2.list.size() == obj.list.size();
  }

  @Test
  public void test6() {
    String tmp =
        "{code:1,msg:'Hello world',data:{list:[1,2,3,4,5], ary2:[{a:2},{a:3,b:{c:'ddd'}}]}}";
    // 1.加载json
    Object n = Onode.deserialize(tmp);

    assert n instanceof Map;
    assert ((Map) n).size() == 3;
  }
}
