package com.github.gobars.httplog.snack.features;

import com.github.gobars.httplog.snack._models.UserModel;
import java.lang.reflect.Type;
import java.util.ArrayList;
import org.junit.Test;

public class _type {

  @Test
  public void test2() {
    Type type1 = UserModel.class;
    Type type2 = (new ArrayList<UserModel>() {}).getClass().getGenericSuperclass();

    return;
  }
}
