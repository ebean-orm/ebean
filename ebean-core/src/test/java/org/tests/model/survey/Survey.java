package org.tests.model.survey;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.List;

@Entity
public class Survey {
  @Id
  public Long id;

  String name;

  public Survey(String name) {
    this.name = name;
  }

  @OneToMany(mappedBy = "survey", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OrderBy("sequenceNumber")
  private List<Category> categories;

  public List<Category> getCategories() {
    return categories;
  }

  public void setCategories(List<Category> categories) {
    this.categories = categories;
  }
}
