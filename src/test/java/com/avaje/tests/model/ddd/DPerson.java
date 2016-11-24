package com.avaje.tests.model.ddd;

import com.avaje.tests.model.ivo.CMoney;
import com.avaje.tests.model.ivo.Money;
import com.avaje.tests.model.ivo.Oid;
import org.joda.time.Interval;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DPerson {

  @Id
  Oid<DPerson> id;

  String firstName;

  String lastName;

  Money salary;

  CMoney cmoney;

  Interval interval;

  public String toString() {
    return id + " " + firstName + " " + lastName + " " + salary;
  }

  public Oid<DPerson> getId() {
    return id;
  }

  public void setId(Oid<DPerson> id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Money getSalary() {
    return salary;
  }

  public void setSalary(Money salary) {
    this.salary = salary;
  }

  public CMoney getCmoney() {
    return cmoney;
  }

  public void setCmoney(CMoney cmoney) {
    this.cmoney = cmoney;
  }

  public Interval getInterval() {
    return interval;
  }

  public void setInterval(Interval interval) {
    this.interval = interval;
  }

}
