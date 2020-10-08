package org.tests.model.aggregation;

import io.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "d_machine_aux_use")
public class DMachineAuxUse extends Model {

  @Id
  private long id;

  @ManyToOne(optional = false)
  private DMachine machine;

  private String name;

  @Column(name="edate")
  private LocalDate date;

  private long useSecs;

  private BigDecimal fuel;

  @Version
  private long version;

  public DMachineAuxUse(DMachine machine, LocalDate date, String name) {
    this.machine = machine;
    this.date = date;
    this.name = name;
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

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
