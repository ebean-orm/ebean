package io.ebean.test.kotlin

import io.ebean.DB
import org.assertj.core.api.Assertions.assertThat
import org.example.order.Customer
import org.example.order.Order
import org.example.order.OrderItem
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertNotNull

class TransparentPersistenceTest {

  @Test
  fun `insert with cascade insert`() {
    // setup data

    // setup data
    val c0 = Customer("Customer 0")
    DB.save(c0)

    var orderId: Long
    DB.beginTransaction().use { transaction ->
      transaction.setAutoPersistUpdates(true) // EXPERIMENTAL feature
      val order = Order(c0)
      DB.insert(order)
      orderId = order.id
      // cascade persist will insert this OrderItem (even though it isn't in the persistence context)
      order.items.add(OrderItem(order, "item 0", BigDecimal.TEN))
      transaction.commit()
    }

    val checkOrder = DB.find(Order::class.java, orderId)

    assertNotNull(checkOrder)
    assertThat(checkOrder.items.size).isEqualTo(1)

    DB.delete(checkOrder)
    DB.delete(c0)
  }
}
