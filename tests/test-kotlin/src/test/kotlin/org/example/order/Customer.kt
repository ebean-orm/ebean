package org.example.order

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Version

@Entity
class Customer(
  name: String,
) {
  @Id
  var id: Int = 0
  val name: String = name

  @Version
  var version: Long = 0
}
