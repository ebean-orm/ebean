package org.tests.model.tevent;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class TEventMany {

  @Id
  Long id;

  String description;

  @ManyToOne
  TEventOne event;

  int myUnits;

  double amount;

  @Version
  Long version;

  public TEventMany(String description, int myUnits, double amount) {
    this.description = description;
    this.myUnits = myUnits;
    this.amount = amount;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public TEventOne getEvent() {
    return event;
  }

  public void setEvent(TEventOne event) {
    this.event = event;
  }

  public int getMyUnits() {
    return myUnits;
  }

  public void setMyUnits(int myUnits) {
    this.myUnits = myUnits;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
