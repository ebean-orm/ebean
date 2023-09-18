package org.example.records;

import jakarta.persistence.Embeddable;

@Embeddable
public record CustomAccessors(String line1, String line2, String city) {

  @Override
  public String line1() {
    return "line1:" + line1;
  }

  @Override
  public String line2() {
    return line2 + "|" + city;
  }

  @Override
  public String city() {
    return city;
  }
}
