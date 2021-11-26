package io.ebean.test.kotlin

import io.ebean.common.BeanList
import org.assertj.core.api.Assertions.assertThat
import org.example.order.Customer
import org.example.order.Order
import org.junit.jupiter.api.Test

class OneToManyEnhancementTest {

  @Test
  fun `o2m is BeanCollection`() {
    val customer = Customer("foo")
    val order = Order(customer)

    assertThat(order.items).isInstanceOf(BeanList::class.java)
  }
}
