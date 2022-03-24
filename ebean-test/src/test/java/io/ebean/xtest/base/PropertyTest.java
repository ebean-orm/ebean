package io.ebean.xtest.base;

import io.ebean.OrderBy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class PropertyTest {

  @Test
  public void equals() throws Exception {

    Assertions.assertEquals(prop("foo", true), prop("foo", true));
  }

  @Test
  public void diff_basic() throws Exception {

    Assertions.assertNotEquals(prop("foo", true), prop("bar", true));
    Assertions.assertNotEquals(prop("foo", true), prop("foo", false));
    Assertions.assertNotEquals(prop("foo", false), prop("foo", true));
  }

  @Test
  public void diff_nulls() throws Exception {

    Assertions.assertEquals(prop("foo", true, "nulls", "high"), prop("foo", true, "nulls", "high"));
    Assertions.assertEquals(prop("foo", true, "nulls", "low"), prop("foo", true, "nulls", "low"));

    Assertions.assertNotEquals(prop("foo", true), prop("foo", true, "nulls", "high"));
    Assertions.assertNotEquals(prop("foo", true, "nulls", "high"), prop("foo", true));
    Assertions.assertNotEquals(prop("foo", true, "nulls", "high"), prop("foo", true, "nulls", "low"));
    Assertions.assertNotEquals(prop("foo", true, "nulls", "low"), prop("foo", true, "nulls", "high"));
  }

  private OrderBy.Property prop(String name, boolean asc) {
    return new OrderBy.Property(name, asc, null, null);
  }

  private OrderBy.Property prop(String name, boolean asc, String nulls, String highLow) {
    return new OrderBy.Property(name, asc, nulls, highLow);
  }

}
