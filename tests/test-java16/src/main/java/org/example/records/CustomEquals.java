package org.example.records;

import io.avaje.lang.NonNullApi;

import javax.persistence.Embeddable;
import java.util.Objects;

@NonNullApi
@Embeddable
public record CustomEquals(String line1, String line2, String city) {

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CustomEquals that = (CustomEquals) o;
    return line1.equals(that.line1) && line2.equals(that.line2) && city.equals(that.city);
  }

  @Override
  public int hashCode() {
    return Objects.hash(line1, line2, city);
  }
}
