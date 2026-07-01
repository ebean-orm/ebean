package org.example.order

import io.ebean.annotation.DbMap
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

  @DbMap(length = 800)
  var params: Map<String, Any> = mapOf()
}
