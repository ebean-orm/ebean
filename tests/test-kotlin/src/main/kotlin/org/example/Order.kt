package org.example.order

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Version

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
