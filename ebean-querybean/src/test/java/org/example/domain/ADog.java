package org.example.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;

@Inheritance
@DiscriminatorValue("DOG")
@Entity
public class ADog extends Animal {

  private String registration;

  public ADog(String name, String registration) {
    super(name);
    this.registration = registration;
  }

  public String getRegistration() {
    return registration;
  }

  public void setRegistration(String registration) {
    this.registration = registration;
  }
}
