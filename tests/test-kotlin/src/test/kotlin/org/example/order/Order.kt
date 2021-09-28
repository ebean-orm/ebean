package org.example.order

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "t_order")
class Order(
  customer: Customer,
) {
  @Id
  val id: Long = 0

  @ManyToOne
  val customer: Customer = customer

  @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
  val items: MutableList<OrderItem> = mutableListOf()

  @Version
  val version: Long = 0
}
