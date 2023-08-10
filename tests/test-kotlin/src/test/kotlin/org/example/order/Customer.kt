package org.example.order

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Version
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
