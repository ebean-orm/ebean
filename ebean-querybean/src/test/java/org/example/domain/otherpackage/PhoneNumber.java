package org.example.domain.otherpackage;

public class PhoneNumber {
  private final String msisdn;

  public PhoneNumber(final String msisdn) {
    this.msisdn = msisdn;
  }

  public String getMsisdn() {
    return msisdn;
  }
}
