package org.example.domain

import io.ebean.DB
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FooEntityTest {

  @Test
  fun iud() {

    val foo = FooEntity("hello")
    foo.save()

    val found = DB.find(FooEntity::class.java, foo.id)
    assertThat(found).isNotNull

    found?.delete()

  }
}
