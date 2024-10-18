package org.example.records;

import org.jspecify.annotations.NullMarked;

import jakarta.persistence.Embeddable;

@NullMarked
@Embeddable
public record CustomToString(String line1, String line2, String city) {

  @Override
  public String toString() {
    return "[line1='" + line1 + '\'' +
      ", line2='" + line2 + '\'' +
      ", city='" + city + '\'' +
      ']';
  }
}
