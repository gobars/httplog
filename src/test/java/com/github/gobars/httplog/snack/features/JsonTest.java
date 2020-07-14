package com.github.gobars.httplog.snack.features;

import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.httplog.snack.core.Cnf;
import com.github.gobars.httplog.snack.core.Ctx;
import com.github.gobars.httplog.snack.core.Defaults;
import com.github.gobars.httplog.snack.core.Feature;
import com.github.gobars.httplog.snack.from.JsonFromer;
import com.github.gobars.httplog.snack.to.JsonToer;
import java.io.IOException;
import java.text.SimpleDateFormat;
import org.junit.Test;

/**
 * 2019.01.28
 *
 * @author cjl
 */
public class JsonTest {

  /** 测试非对象，非数组数据 */
  @Test
  public void test11() throws IOException {
    Ctx c = new Ctx(Cnf.def(), "\"xxx\"");
    new JsonFromer().handle(c);
    assert "xxx".equals(((Onode) c.target).getString());

    c = new Ctx(Cnf.def(), "'xxx'");
    new JsonFromer().handle(c);
    assert "xxx".equals(((Onode) c.target).getString());

    c = new Ctx(Cnf.def(), "true");
    new JsonFromer().handle(c);
    assert ((Onode) c.target).getBoolean();

    c = new Ctx(Cnf.def(), "false");
    new JsonFromer().handle(c);
    assert ((Onode) c.target).getBoolean() == false;

    c = new Ctx(Cnf.def(), "123");
    new JsonFromer().handle(c);
    assert 123 == ((Onode) c.target).getInt();

    c = new Ctx(Cnf.def(), "null");
    new JsonFromer().handle(c);
    assert ((Onode) c.target).isNull();

    c = new Ctx(Cnf.def(), "NaN");
    new JsonFromer().handle(c);
    assert ((Onode) c.target).isNull();

    c = new Ctx(Cnf.def(), "undefined");
    new JsonFromer().handle(c);
    assert ((Onode) c.target).isNull();

    long times = System.currentTimeMillis();
    c = new Ctx(Cnf.def(), "new Date(" + times + ") ");
    new JsonFromer().handle(c);
    assert ((Onode) c.target).getDate().getTime() == times;
  }

  @Test
  public void test21() throws IOException {
    Ctx c =
        new Ctx(Cnf.def(), "{'a':'b','c':{'d':'e'},'f':{'g':\"h\"},'i':[{'j':'k','l':'m'},'n']}");

    new JsonFromer().handle(c);

    assert "m".equals(((Onode) c.target).get("i").get(0).get("l").getString());
    assert "n".equals(((Onode) c.target).get("i").get(1).getString());

    c.source = c.target;
    new JsonToer().handle(c);

    assert "{\"a\":\"b\",\"c\":{\"d\":\"e\"},\"f\":{\"g\":\"h\"},\"i\":[{\"j\":\"k\",\"l\":\"m\"},\"n\"]}"
        .equals(c.target);
  }

  @Test
  public void test22() throws IOException {
    Ctx c = new Ctx(Cnf.def(), "{a:\"b\"}");

    new JsonFromer().handle(c);

    assert "b".equals(((Onode) c.target).get("a").getString());

    c.source = c.target;
    new JsonToer().handle(c);

    assert "{\"a\":\"b\"}".equals(c.target);
  }

  @Test
  public void test23() throws IOException {
    Ctx c = new Ctx(Cnf.def(), "{a:{b:{c:{d:{e:'f'}}}}}");

    new JsonFromer().handle(c);

    assert "f".equals(((Onode) c.target).get("a").get("b").get("c").get("d").get("e").getString());

    c.source = c.target;
    new JsonToer().handle(c);

    assert "{\"a\":{\"b\":{\"c\":{\"d\":{\"e\":\"f\"}}}}}".equals(c.target);
  }

  @Test
  public void test24() throws IOException {
    String json = "[[[],[]],[[]],[],[{},{},null]]";

    Ctx c = new Ctx(Cnf.def(), json);

    new JsonFromer().handle(c);

    c.source = c.target;
    new JsonToer().handle(c);

    assert json.equals(c.target);
  }

  @Test
  public void test25() throws IOException {
    Ctx c = new Ctx(Cnf.def(), "[{a:'b'},{c:'d'},[{e:'f'}]]");

    new JsonFromer().handle(c);

    assert "f".equals(((Onode) c.target).get(2).get(0).get("e").getString());

    c.source = c.target;
    new JsonToer().handle(c);

    assert "[{\"a\":\"b\"},{\"c\":\"d\"},[{\"e\":\"f\"}]]".equals(c.target);
  }

  @Test
  public void test26() throws IOException {
    Ctx c = new Ctx(Cnf.def(), "[123,123.45,'123.45','2019-01-02T03:04:05',true,false]");

    new JsonFromer().handle(c);

    assert 123 == ((Onode) c.target).get(0).getInt();
    assert 123.45 == ((Onode) c.target).get(1).getDouble();
    assert "123.45".equals(((Onode) c.target).get(2).getString());
    assert "2019-01-02T03:04:05"
        .equals(
            new SimpleDateFormat(Defaults.DEF_DATE_FORMAT_STRING)
                .format(((Onode) c.target).get(3).getDate()));
    assert ((Onode) c.target).get(4).getBoolean();
    assert !((Onode) c.target).get(5).getBoolean();

    c.source = c.target;
    new JsonToer().handle(c);

    assert "[123,123.45,\"123.45\",\"2019-01-02T03:04:05\",true,false]".equals(c.target);
  }

  /** 测试：换行符之类的 转码 */
  @Test
  public void test27() throws IOException {

    Ctx c = new Ctx(Cnf.def(), "{\"a\":\"\\t\"}");

    new JsonFromer().handle(c);

    assert "\t".equals(((Onode) c.target).get("a").getString());

    c.source = c.target;
    new JsonToer().handle(c);

    assert "{\"a\":\"\\t\"}".equals(c.target);
  }

  /** 测试：unicode 转码 */
  @Test
  public void test28() throws IOException {

    Ctx c = new Ctx(Cnf.def(), "{\"a\":\"'\\u7684\\t\\n\"}");

    new JsonFromer().handle(c);

    assert "'的\t\n".equals(((Onode) c.target).get("a").getString());

    c.source = c.target;
    new JsonToer().handle(c);

    assert "{\"a\":\"'的\\t\\n\"}".equals(c.target);
  }

  /** 测试：emoji unicode 转码 */
  @Test
  public void test29() throws IOException {

    Ctx c = new Ctx(Cnf.of(Feature.BrowserCompatible), "{\"a\":\"'\\ud83d\\udc4c\\t\\n\"}");

    new JsonFromer().handle(c);

    assert "'👌\t\n".equals(((Onode) c.target).get("a").getString());

    c.source = c.target;
    new JsonToer().handle(c);

    assert "{\"a\":\"'\\ud83d\\udc4c\\t\\n\"}".equalsIgnoreCase((String) c.target);
  }

  @Test
  public void test30() throws IOException {

    Ctx c = new Ctx(Cnf.def(), "{\"a\":\" \\0\\1\\2\\3\\4\\5\\6\\7\"}");

    new JsonFromer().handle(c);

    assert " \0\1\2\3\4\5\6\7".equals(((Onode) c.target).get("a").getString());

    c.source = c.target;
    new JsonToer().handle(c);

    assert "{\"a\":\" \\0\\1\\2\\3\\4\\5\\6\\7\"}".equals(c.target);
  }

  @Test
  public void test31() throws IOException {

    Ctx c = new Ctx(Cnf.of(Feature.BrowserCompatible), "{\"a\":\" \\u000f\\u0012\"}");

    new JsonFromer().handle(c);

    assert " \u000f\u0012".equals(((Onode) c.target).get("a").getString());

    c.source = c.target;
    new JsonToer().handle(c);

    assert "{\"a\":\" \\u000f\\u0012\"}".equalsIgnoreCase((String) c.target);
  }
}
