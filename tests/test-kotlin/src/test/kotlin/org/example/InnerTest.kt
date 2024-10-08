package org.example

import io.ebean.DB
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.example.domain.MyKotInner
import org.junit.jupiter.api.Test

class InnerTest {

  @Test
  fun split_normal() {
    val my = MyKotInner()
    my.one = "a"
    my.two = "b"
    my.description = "desc"
    DB.save(my)

    val myId = MyKotInner.Mid("a", "b")

    val found = DB.find(MyKotInner::class.java, myId)
    assertThat(found).isNotNull

    val result = new QOrder()
      .customer.id.eq(42)
      .findList()

    assertThat(result).isEmpty
  }
}
