package org.tests.model.embedded;

import jakarta.persistence.Embeddable;

@Embeddable
public class Eembeddable {

  String description;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
