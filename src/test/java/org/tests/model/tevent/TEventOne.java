package org.tests.model.tevent;

import io.ebean.annotation.Aggregation;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;
import java.util.List;

@Entity
public class TEventOne {

  public enum Status {
    AA,
    BB
  }

  @Id
  Long id;

  String name;

  Status status;

  @Version
  Long version;

  @OneToOne
  TEvent event;

  @Aggregation("max(version)")
  Long maxVersion;

  @Aggregation("count(logs.id)")
  Long count;

  @Aggregation("sum(logs.units)")
  Double totalUnits;

  @Aggregation("sum(logs.units * logs.amount)")
  Double totalAmount;

  @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
  List<TEventMany> logs;

  public TEventOne(String name, Status status) {
    this.name = name;
    this.status = status;
  }

  @Override
  public String toString() {
    return "id:" + id + " name:" + name + " status:" + status + " mv:" + maxVersion + " ct:" + count;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getCount() {
    return count;
  }

  public Double getTotalUnits() {
    return totalUnits;
  }

  public Double getTotalAmount() {
    return totalAmount;
  }

  public Long getMaxVersion() {
    return maxVersion;
  }

  public String getName() {
    return name;
  }

  public Status getStatus() {
    return status;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public TEvent getEvent() {
    return event;
  }

  public void setEvent(TEvent event) {
    this.event = event;
  }

  public List<TEventMany> getLogs() {
    return logs;
  }

  public void setLogs(List<TEventMany> logs) {
    this.logs = logs;
  }
}
