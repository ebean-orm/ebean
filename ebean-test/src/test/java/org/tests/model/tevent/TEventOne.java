package org.tests.model.tevent;

import io.ebean.annotation.Aggregation;

import jakarta.persistence.*;
import java.math.BigDecimal;
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

  @Aggregation("sum(logs.myUnits)")
  BigDecimal totalUnits;

  @Aggregation("sum(logs.myUnits * logs.amount)")
  BigDecimal totalAmount;

  @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
  List<TEventMany> logs;

  @CustomFormulaAnnotationParser.Count("logs")
  //@Formula(select = "f1.child_count",
  //join = "left join (select event_id, count(*) as child_count from tevent_many GROUP BY event_id ) as f1 on f1.event_id = ${ta}.id")
  Long customFormula;

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

  public Long getCustomFormula() {
    return customFormula;
  }

  public BigDecimal getTotalUnits() {
    return totalUnits;
  }

  public BigDecimal getTotalAmount() {
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
