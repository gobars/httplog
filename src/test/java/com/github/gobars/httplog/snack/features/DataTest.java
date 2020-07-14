package com.github.gobars.httplog.snack.features;

import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.httplog.snack._models.*;
import com.github.gobars.httplog.snack.core.Cnf;
import com.github.gobars.httplog.snack.core.Ctx;
import com.github.gobars.httplog.snack.from.ObjectFromer;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * 2019.01.30
 *
 * @author cjl
 */
public class DataTest {

  /** 无限递归 */
  @Test
  public void test000() {

    AModel a = new AModel();
    BModel b = new BModel();

    a.b = b;
    b.a = a;

    Ctx c = new Ctx(Cnf.def(), a);

    new ObjectFromer().handle(c);

    //    System.out.println(((ONode) c.target).toJson());

    Object data = ((Onode) c.target).toObject(null);

    assert (data instanceof Map);
  }

  @Test
  public void test1() {

    UserModel user = new UserModel();
    user.id = 1111;
    user.name = "张三";
    user.note = null;
    OrderModel order = new OrderModel();
    order.user = user;
    order.order_id = 2222;
    order.order_num = "ddddd";

    Ctx c = new Ctx(Cnf.def(), order);

    new ObjectFromer().handle(c);

    //    System.out.println(((ONode) c.target).toJson());

    Object data = ((Onode) c.target).toObject(null);

    assert (data instanceof Map);
  }

  @Test
  public void test2() {

    UserGroupModel group = new UserGroupModel();
    group.id = 9999;
    group.users = new ArrayList<>();
    group.names = new String[5];
    group.ids = new short[5];
    group.iids = new Integer[5];

    for (short i = 0; i < 5; i++) {
      UserModel user = new UserModel();
      user.id = i;
      user.name = "张三" + i;
      user.note = null;
      group.users.add(user);
      group.names[i] = "李四" + i;
      group.ids[i] = i;
      group.iids[i] = (int) i;
    }

    //        Context context = new Context(Constants.serialize, group);
    //
    //        new ObjectFromer().handle(context);

    String json = Onode.serialize(group);

    //    System.out.println(json); // context.node.toJson());

    Object g2 = Onode.deserialize(json);

    Onode node = Onode.load(json); // context.node.toData();

    Object tmp = node.toObject(null);

    assert (tmp instanceof Map);
  }

  @Test
  public void test3() {
    List<Object> d = new ArrayList<>();
    final TypeVariable<? extends Class<? extends List>>[] typeParameters =
        d.getClass().getTypeParameters();
    for (TypeVariable<? extends Class<? extends List>> t : typeParameters) {
      //      System.out.println(t.getName());
    }
  }
}
