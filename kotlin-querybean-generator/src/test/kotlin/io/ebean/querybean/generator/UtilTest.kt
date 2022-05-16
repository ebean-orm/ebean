package io.ebean.querybean.generator

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class UtilTest {

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
