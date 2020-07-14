package com.github.gobars.httplog.snack.demo;

import com.github.gobars.httplog.snack.Onode;
import org.junit.Test;

public class JsonPath2 {
  @Test
  public void demo1() {
    final String json =
        "{\"store\":{\"book\":[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95},{\"category\":\"fiction\",\"author\":\"Evelyn Waugh\",\"title\":\"Sword of Honour\",\"price\":12.99},{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99},{\"category\":\"fiction\",\"author\":\"J. R. R. Tolkien\",\"title\":\"The Lord of the Rings\",\"isbn\":\"0-395-19395-8\",\"price\":22.99}],\"bicycle\":{\"color\":\"red\",\"price\":19.95}},\"expensive\":10}";

    Onode n = Onode.load(json);

    Onode t1 = n.select("$.store.book [0].title");
    System.out.println("\nt1:" + t1);

    Onode t2 = n.select("$['store']['book'][0]['title']");
    System.out.println("\nt2:" + t2);

    Onode t3 = n.select("$.store.book[*].author");
    System.out.println("\nt3:" + t3);

    Onode t4 = n.select("$..author");
    System.out.println("\nt4:" + t4);

    Onode t5 = n.select("$.store.*");
    System.out.println("\nt5:" + t5);

    Onode t6 = n.select("$.store..price");
    System.out.println("\nt6:" + t6);

    Onode t7 = n.select("$..book[2]");
    System.out.println("\nt7:" + t7);

    Onode t8 = n.select("$..book[-2]");
    System.out.println("\nt8:" + t8);

    Onode t9 = n.select("$..book[0,1]");
    System.out.println("\nt9:" + t9);

    Onode ta = n.select("$..book[:2]");
    System.out.println("\nta:" + ta);

    Onode tb = n.select("$..book[1:2]");
    System.out.println("\ntb:" + tb);

    Onode tc = n.select("$..book[-2:]");
    System.out.println("\ntc:" + tc);

    Onode td = n.select("$..book[2:]");
    System.out.println("\ntd:" + td);

    Onode te = n.select("$..book[?(@.isbn)]");
    System.out.println("\nte:" + te);

    Onode tf = n.select("$.store.book[?(@.price < 10)]");
    System.out.println("\ntf:" + tf);

    Onode tg = n.select("$..book[?(@.author =~ /.*REES/i)]");
    System.out.println("\ntg:" + tg);

    Onode th = n.select("$..*");
    System.out.println("\nth:" + th);

    Onode ti = n.select("$..book[?(@.price <= $.expensive)]");
    System.out.println("\nti:" + ti);
  }
}
