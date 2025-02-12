package org.example.domain

import jakarta.persistence.*

@Entity
@Table(name = "t_inner2")
class MyKInnerEmb {

  @Id
  var one: Long = 0
  var two: String = "0"
  var address: EmbAddre? = null

  @Embeddable
  data class EmbAddre(val line1: String, val line2: String)

}
