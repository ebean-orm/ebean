package io.ebean.querybean.generator;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class SplitTest {

  @Test
  public void trimType() {
    assertEquals(Split.trimType("com.foo.domain.Customer"), "com.foo.domain.Customer");
    assertEquals(Split.trimType("? extends com.foo.domain.Customer"), "com.foo.domain.Customer");
  }

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

}