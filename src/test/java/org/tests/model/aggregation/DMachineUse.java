package org.tests.model.aggregation;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "d_machine_use")
public class DMachineUse extends Model {

  @Id
  private long id;

  @ManyToOne(optional = false)
  private DMachine machine;

  private LocalDate date;

  private long distanceKms;

  private long timeSecs;

  private BigDecimal fuel;

  @Version
  private long version;

  public DMachineUse(DMachine machine, LocalDate date) {
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

  public long getDistanceKms() {
    return distanceKms;
  }

  public void setDistanceKms(long distanceKms) {
    this.distanceKms = distanceKms;
  }

  public long getTimeSecs() {
    return timeSecs;
  }

  public void setTimeSecs(long timeSecs) {
    this.timeSecs = timeSecs;
  }

  public BigDecimal getFuel() {
    return fuel;
  }

  public void setFuel(BigDecimal fuel) {
    this.fuel = fuel;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
