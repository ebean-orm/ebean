package io.ebean.querybean.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilTest {

  @Test
  void packageOf() {
    assertEquals(Util.packageOf(true, "com.example.Foo.Bar"), "com.example");
    assertEquals(Util.packageOf(false, "com.example.other.foo.Bar"), "com.example.other.foo");
  }

  @Test
  void shortName() {
    assertEquals(Util.shortName(true, "com.example.Foo.Bar"), "Foo.Bar");
    assertEquals(Util.shortName(false, "com.example.foo.Bar"), "Bar");
    assertEquals(Util.shortName(false, "Bar"), "Bar");
  }

}
