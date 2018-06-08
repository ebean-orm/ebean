package org.tests.model.aggregation;

import io.ebean.annotation.Aggregation;
import io.ebean.annotation.View;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@View(name = "d_machine_stats", dependentTables = "d_machine_stats")
public class DMachineStatsAgg {

  @ManyToOne
  DMachine machine;

  LocalDate date;

  /**
   * Matching with column underscore.
   */
  @Aggregation("sum(totalKms)")
  long totalKms;

  /**
   * Matching with no underscore.
   */
  @Aggregation("sum(hours)")
  long hours;

  @Aggregation("max(rate)")
  BigDecimal rate;

  /**
   * Not matching.
   */
  @Aggregation("sum(cost)")
  BigDecimal totalCost;

  /**
   * Not matching.
   */
  @Aggregation("max(totalKms)")
  BigDecimal maxKms;

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

  public long getTotalKms() {
    return totalKms;
  }

  public void setTotalKms(long totalKms) {
    this.totalKms = totalKms;
  }

  public long getHours() {
    return hours;
  }

  public void setHours(long hours) {
    this.hours = hours;
  }

  public BigDecimal getRate() {
    return rate;
  }

  public void setRate(BigDecimal rate) {
    this.rate = rate;
  }

  public BigDecimal getTotalCost() {
    return totalCost;
  }

  public void setTotalCost(BigDecimal totalCost) {
    this.totalCost = totalCost;
  }

  public BigDecimal getMaxKms() {
    return maxKms;
  }

  public void setMaxKms(BigDecimal maxKms) {
    this.maxKms = maxKms;
  }
}
