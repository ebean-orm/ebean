package io.ebean.querybean.generator;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashSet;

public class SplitTest {

  @Test
  public void shortName() {

    assertEquals(Split.shortName("com.foo.domain.Customer"), "Customer");
    assertEquals(Split.shortName("Customer"), "Customer");
  }

  @Test
  public void split_normal() {

    String[] split = Split.split("com.foo.domain.Customer");

    assertEquals(split[0], "com.foo.domain");
    assertEquals(split[1], "Customer");
  }

  @Test
  public void split_noPackage() {

    String[] split = Split.split("Customer");

    assertNull(split[0]);
    assertEquals(split[1], "Customer");
  }

  @Test
  public void split_generics() {
    assertEquals("Foo<Bar, XFoo<XBar>>", Split.genericsSplit("com.Foo<com.Bar, org.XFoo<org.XBar>>").getKey());
    assertEquals(new HashSet<>(asList("com.Foo", "com.Bar", "org.XFoo", "org.XBar")), Split.genericsSplit("com.Foo<com.Bar, org.XFoo<org.XBar>>").getValue());
  }
}
