package io.ebean.querybean.generator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.lang.Exception
import java.util.Arrays
import java.util.HashSet

class SplitTest {
  @Test
  fun trimType() {
    assertEquals(Split.trimType("com.foo.domain.Customer"), "com.foo.domain.Customer")
    assertEquals(Split.trimType("? extends com.foo.domain.Customer"), "com.foo.domain.Customer")
  }

  @Test
  fun shortName() {
    assertEquals(Split.shortName("com.foo.domain.Customer"), "Customer")
    assertEquals(Split.shortName("Customer"), "Customer")
  }

  @Test
  fun split_normal() {
    val split = Split.split("com.foo.domain.Customer")
    assertEquals(split[0], "com.foo.domain")
    assertEquals(split[1], "Customer")
  }

  @Test
  fun split_noPackage() {
    val split = Split.split("Customer")
    assertNull(split[0])
    assertEquals(split[1], "Customer")
  }

  @Test
  @Throws(Exception::class)
  fun split_generics() {
    assertEquals("Foo<Bar, XFoo<XBar>>", Split.genericsSplit("com.Foo<com.Bar, org.XFoo<org.XBar>>").key)
    assertEquals(
      HashSet(Arrays.asList("com.Foo", "com.Bar", "org.XFoo", "org.XBar")),
      Split.genericsSplit("com.Foo<com.Bar, org.XFoo<org.XBar>>").value
    )
    assertEquals("Foo", Split.genericsSplit("com.bar.Foo").key)
  }
}
