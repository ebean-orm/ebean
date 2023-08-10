package org.example.otherpackage

class ValidEmail(
  val emailAddress: String
) : Email<ValidEmail> {
  override fun compareTo(other: ValidEmail): Int {
    return emailAddress.compareTo(other.emailAddress)
  }
}
