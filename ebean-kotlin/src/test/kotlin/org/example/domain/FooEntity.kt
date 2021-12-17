package org.example.domain

import io.ebean.Model
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Version

@Entity
class FooEntity(name: String) : Model() {

  @Id
  var id: Long = 0

  var name: String = name

  @Version
  var version: Long = 0
}
