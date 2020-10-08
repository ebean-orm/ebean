package io.ebean;

import io.ebean.OrderBy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PropertyTest {

  @Test
  public void equals() throws Exception {

    assertEquals(prop("foo", true), prop("foo", true));
  }

  @Test
  public void diff_basic() throws Exception {

    assertNotEquals(prop("foo", true), prop("bar", true));
    assertNotEquals(prop("foo", true), prop("foo", false));
    assertNotEquals(prop("foo", false), prop("foo", true));
  }

  @Test
  public void diff_nulls() throws Exception {

    assertEquals(prop("foo", true, "nulls", "high"), prop("foo", true, "nulls", "high"));
    assertEquals(prop("foo", true, "nulls", "low"), prop("foo", true, "nulls", "low"));

    assertNotEquals(prop("foo", true), prop("foo", true, "nulls", "high"));
    assertNotEquals(prop("foo", true, "nulls", "high"), prop("foo", true));
    assertNotEquals(prop("foo", true, "nulls", "high"), prop("foo", true, "nulls", "low"));
    assertNotEquals(prop("foo", true, "nulls", "low"), prop("foo", true, "nulls", "high"));
  }

  private OrderBy.Property prop(String name, boolean asc) {
    return new OrderBy.Property(name, asc, null, null);
  }

  private OrderBy.Property prop(String name, boolean asc, String nulls, String highLow) {
    return new OrderBy.Property(name, asc, nulls, highLow);
  }

}
