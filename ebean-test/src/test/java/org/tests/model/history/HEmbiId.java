package org.tests.model.history;

import io.ebean.annotation.Length;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class HEmbiId {

  private final long part;

  @Length(20)
  private final String brand;

  public HEmbiId(long part, String brand) {
    this.part = part;
    this.brand = brand;
  }

  public long getPart() {
    return part;
  }

  public String getBrand() {
    return brand;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HEmbiId hEmbiId = (HEmbiId) o;
    return part == hEmbiId.part && Objects.equals(brand, hEmbiId.brand);
  }

  @Override
  public int hashCode() {
    return Objects.hash(part, brand);
  }
}
