package org.tests.model.basic;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class EVanillaCollection {

  @Id
  Integer id;

  String name;

  @OneToMany(cascade = CascadeType.PERSIST)
  List<EVanillaCollectionDetail> details;

  public EVanillaCollection() {
    details = new ArrayList<>();
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<EVanillaCollectionDetail> getDetails() {
    return details;
  }

  public void setDetails(List<EVanillaCollectionDetail> details) {
    this.details = details;
  }

}
