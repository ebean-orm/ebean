package org.tests.model.aggregation;

import io.ebean.annotation.Sum;
import io.ebean.annotation.View;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@View(name = "d_machine_aux_use")
public class DMachineAuxUseAgg {

  @ManyToOne
  private DMachine machine;

  private String name;

  private LocalDate date;

  @Sum
  private long useSecs;

  @Sum
  private BigDecimal fuel;

  public DMachine getMachine() {
    return machine;
  }

  public void setMachine(DMachine machine) {
    this.machine = machine;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public long getUseSecs() {
    return useSecs;
  }

  public void setUseSecs(long useSecs) {
    this.useSecs = useSecs;
  }

  public BigDecimal getFuel() {
    return fuel;
  }

  public void setFuel(BigDecimal fuel) {
    this.fuel = fuel;
  }

}
