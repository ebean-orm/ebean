package org.example.order

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Version
import org.example.api.ICustomer

@Entity
class Customer(
  name: String,
) : ICustomer {
  @Id
  var id: Int = 0
  val name: String = name

  @Version
  var version: Long = 0
}
