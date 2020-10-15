package io.ebean.querybean.generator;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.HashSet;

import org.testng.annotations.Test;

public class SplitTest {

  @Test
  public void shortName() throws Exception {

    assertEquals(Split.shortName("com.foo.domain.Customer"), "Customer");
    assertEquals(Split.shortName("Customer"), "Customer");
  }

  @Test
  public void split_normal() throws Exception {

    String[] split = Split.split("com.foo.domain.Customer");

    assertEquals(split[0], "com.foo.domain");
    assertEquals(split[1], "Customer");
  }

  @Test
  public void split_noPackage() throws Exception {

    String[] split = Split.split("Customer");

    assertNull(split[0]);
    assertEquals(split[1], "Customer");
  }

  @Test
  public void split_generics() throws Exception {
    assertEquals("Foo<Bar, XFoo<XBar>>", Split.genericsSplit("com.Foo<com.Bar, org.XFoo<org.XBar>>").getKey());
    assertEquals(new HashSet<>(asList("com.Foo", "com.Bar", "org.XFoo", "org.XBar")), Split.genericsSplit("com.Foo<com.Bar, org.XFoo<org.XBar>>").getValue());
  }
}
