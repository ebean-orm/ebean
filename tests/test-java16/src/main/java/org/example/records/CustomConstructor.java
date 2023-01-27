package org.example.records;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public record CustomConstructor(String line1, String line2, String city) {

  public CustomConstructor(String line1, String line2, String city) {
    this.line1 = Objects.requireNonNull(line1);
    this.line2 = Objects.requireNonNull(line2, "no line2");
    this.city = city;
  }

  public CustomConstructor(String line1, String line2) {
    this(line1, line2, "Auckland");
  }
}
