package org.tests.ddl;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;

@Entity
public class DfkCascadeOne {

  @Id
  long id;

  String name;

  @OneToMany(mappedBy = "one", cascade = CascadeType.ALL)
  List<DfkCascade> details;

  public DfkCascadeOne(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<DfkCascade> getDetails() {
    return details;
  }

  public void setDetails(List<DfkCascade> details) {
    this.details = details;
  }
}
