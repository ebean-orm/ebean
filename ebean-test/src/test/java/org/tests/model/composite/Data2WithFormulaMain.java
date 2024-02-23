package org.tests.model.composite;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.List;
import java.util.UUID;

@Entity
public class Data2WithFormulaMain {

  @Id
  private UUID id;

  private String title;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "main")
  private List<Data2WithFormula> metaData;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<Data2WithFormula> getMetaData() {
    return metaData;
  }

  public void setMetaData(List<Data2WithFormula> metaData) {
    this.metaData = metaData;
  }
}
