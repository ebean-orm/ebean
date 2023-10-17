package org.tests.model.basic;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import java.math.BigDecimal;

@Entity
@Inheritance
@DiscriminatorValue("LONG")
public class VehicleLeaseLong extends VehicleLease {

  BigDecimal bond;

  int minDuration;

  public BigDecimal getBond() {
    return bond;
  }

  public void setBond(BigDecimal bond) {
    this.bond = bond;
  }

  public int getMinDuration() {
    return minDuration;
  }

  public void setMinDuration(int minDuration) {
    this.minDuration = minDuration;
  }
}
