package io.ebean.querybean.generator;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

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

}