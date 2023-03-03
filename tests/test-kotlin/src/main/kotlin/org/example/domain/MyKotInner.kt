package org.example.domain

import javax.persistence.*

@Entity
@Table(name = "t_inner")
@IdClass(MyKotInner.Mid::class)
class MyKotInner {

  @Id
  var one: String = "0"
  @Id
  var two: String = "0"

  var description: String? = null

  @Embeddable
  data class Mid(val one: String, val two: String)

}
