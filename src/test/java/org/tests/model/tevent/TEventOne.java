package org.tests.model.tevent;

import io.ebean.annotation.Aggregation;
import io.ebean.annotation.Formula;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;
import java.util.List;

@Entity
public class TEventOne {

  @Id
  Long id;

  String name;

  @Version
  Long version;

  @OneToOne
  TEvent event;

  @Aggregation("count(logs.id)")
  Long count;

  @Aggregation("sum(logs.units)")
  Double totalUnits;

  @Aggregation("sum(logs.units * logs.amount)")
  Double totalAmount;

  // this is an example how to count child entities correctly
  // current @Aggregation makes a full join, so if no log exists, you'll get no bean at all
  // and if @Aggregation on different tables is used, it would not work
  @Formula(select = "coalesce(f1.child_count, 0)", 
  join = "left join (select event_id, count(*) as child_count from tevent_many GROUP BY event_id ) as f1 on f1.event_id = ${ta}.id")
  Long customFormula;
  
  @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
  List<TEventMany> logs;

  public TEventOne(String name) {
    this.name = name;
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
  
  public Double getTotalUnits() {
    return totalUnits;
  }

  public Double getTotalAmount() {
    return totalAmount;
  }

  public String getName() {
    return name;
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
