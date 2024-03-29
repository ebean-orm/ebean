package org.tests.model.basic;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.sql.Date;

@Entity
@DiscriminatorValue("DOG")
public class Dog extends Animal {

  String registrationNumber;

  Date dateOfBirth;

  public String getRegistrationNumber() {
    return registrationNumber;
  }

  public void setRegistrationNumber(String registrationNumber) {
    this.registrationNumber = registrationNumber;
  }

  public Date getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(Date dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

}
