package io.ebean.test.kotlin

import io.ebean.DB
import org.assertj.core.api.Assertions.assertThat
import org.example.order.Customer
import org.junit.jupiter.api.Test

class BasicInsertQueryTest {

  @Test
  fun `insert find`() {

    val customer = Customer("HelloThere")
    DB.save(customer)

    val found = DB.find(Customer::class.java)
      .setId(customer.id)
      .findOne()

    val name = found?.name
    assertThat(name).isEqualTo("HelloThere")
    assertThat(found).isNotNull

    val list = DB.find(Customer::class.java)
      .findList()

    list.size
    assertThat(list).isNotEmpty

    DB.delete(customer)
  }
}
