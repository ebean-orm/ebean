package org.example.order

import java.math.BigDecimal
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class OrderItem(
  order: Order,
  name: String,
  price: BigDecimal,
  amount: Int = 1,
  description: String? = null,
) {
  @Id
  val id: Long = 0

  @ManyToOne
  val order: Order = order

  val name: String = name

  val description: String? = description

  val amount: Int = amount

  val price: BigDecimal = price
}
