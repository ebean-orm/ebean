package org.tests.model.ddd;

import org.tests.model.ivo.Money;
import org.tests.model.ivo.Oid;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DPerson {

  @Id
  Oid<DPerson> id;

  String firstName;

  String lastName;

  Money salary;

  @Override
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

}
