package org.tests.model.rawsql;

import io.ebean.annotation.Sql;

import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
@Sql
public class ERawSqlAggBean {

  LocalDate day;
  long total;
  int scheduled;
  int completed;

  public LocalDate getDay() {
    return day;
  }

  public void setDay(LocalDate day) {
    this.day = day;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public int getScheduled() {
    return scheduled;
  }

  public void setScheduled(int scheduled) {
    this.scheduled = scheduled;
  }

  public int getCompleted() {
    return completed;
  }

  public void setCompleted(int completed) {
    this.completed = completed;
  }
}
