package io.ebean.querybean.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilTest {

  @Test
  void stripForMethod() {
    assertEquals(Util.stripForMethod("-a"), "_a");
    assertEquals(Util.stripForMethod("-a-b-"), "_a_b_");
    assertEquals(Util.stripForMethod("c-"), "c_");
    assertEquals(Util.stripForMethod("my-foo"), "my_foo");
  }

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
