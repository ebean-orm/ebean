package org.tests.model.aggregation;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "d_machine_stats")
public class DMachineStats {

  @Id
  long id;

  @ManyToOne
  DMachine machine;

  LocalDate date;

  long totalKms;

  long hours;

  BigDecimal rate;

  BigDecimal cost;

  @Version
  long version;

  public DMachineStats(DMachine machine, LocalDate date) {
    this.machine = machine;
    this.date = date;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

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

  public BigDecimal getCost() {
    return cost;
  }

  public void setCost(BigDecimal cost) {
    this.cost = cost;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
