package org.domain;


import io.ebean.annotation.Cache;
import io.ebean.annotation.Index;

import javax.persistence.Entity;
import java.time.LocalDate;

@Cache(enableQueryCache = true, nearCache = true, naturalKey = "name")
@Entity
public class Person extends EBase {

  public enum Status {
    NEW,
    ACTIVE,
    INACTIVE
  }

  @Index(unique = true)
  String name;

  Status status;

  LocalDate localDate;

  String notes;

  public Person(String name) {
    this.name = name;
    this.status = Status.NEW;
  }

  public String toString() {
    return "[id:" + id + " name:" + name + "date:" + localDate + ']';
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public LocalDate getLocalDate() {
    return localDate;
  }

  public void setLocalDate(LocalDate localDate) {
    this.localDate = localDate;
  }
}
