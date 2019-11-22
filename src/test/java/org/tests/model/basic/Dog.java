package org.tests.model.basic;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@DiscriminatorValue("DOG")
public class Dog extends Animal {

  String registrationNumber;

  Date dateOfBirth;

  @Transient
  @JsonIgnore
  private Map<String, Object> otherProps = new HashMap<>();

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

  public Map<String, Object> getOtherProps() {
    return otherProps;
  }

  public void setOtherProps(Map<String, Object> otherProps) {
    this.otherProps = otherProps;
  }
}
