package io.ebean.querybean.generator

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class UtilTest {

  @Test
  fun stripForMethod() {
    Assertions.assertEquals(io.ebean.querybean.generator.Util.stripForMethod("-a"), "_a")
    Assertions.assertEquals(io.ebean.querybean.generator.Util.stripForMethod("-a-b-"), "_a_b_")
    Assertions.assertEquals(io.ebean.querybean.generator.Util.stripForMethod("c-"), "c_")
    Assertions.assertEquals(io.ebean.querybean.generator.Util.stripForMethod("my-foo"), "my_foo")
  }

  @Test
  fun packageOf() {
    Assertions.assertEquals(Util.packageOf(true, "com.example.Foo.Bar"), "com.example")
    Assertions.assertEquals(Util.packageOf(false, "com.example.other.foo.Bar"), "com.example.other.foo")
  }

  @Test
  fun shortName() {
    Assertions.assertEquals(Util.shortName(true, "com.example.Foo.Bar"), "Foo.Bar")
    Assertions.assertEquals(Util.shortName(false, "com.example.foo.Bar"), "Bar")
    Assertions.assertEquals(Util.shortName(false, "Bar"), "Bar")
  }
}
