package org.tests.model.basic;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class ENullCollection {

  @Id
  Integer id;

  String name;

  @OneToMany(cascade = CascadeType.PERSIST)
  List<ENullCollectionDetail> details;

  public ENullCollection() {
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

  public List<ENullCollectionDetail> getDetails() {
    return details;
  }

  public void setDetails(List<ENullCollectionDetail> details) {
    this.details = details;
  }
}
