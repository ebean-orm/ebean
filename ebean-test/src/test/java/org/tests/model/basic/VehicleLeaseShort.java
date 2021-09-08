package org.tests.model.basic;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import java.math.BigDecimal;

@Entity
@Inheritance
@DiscriminatorValue("SHORT")
public class VehicleLeaseShort extends VehicleLease {


  BigDecimal dayRate;

  Integer maxDays;

  public BigDecimal getDayRate() {
    return dayRate;
  }

  public void setDayRate(BigDecimal dayRate) {
    this.dayRate = dayRate;
  }

  public Integer getMaxDays() {
    return maxDays;
  }

  public void setMaxDays(Integer maxDays) {
    this.maxDays = maxDays;
  }
}
