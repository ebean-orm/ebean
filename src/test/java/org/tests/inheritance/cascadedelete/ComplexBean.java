package org.tests.inheritance.cascadedelete;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
public class ComplexBean extends RootBean {

  @OneToMany(cascade = ALL)
  private List<ElementBean> elements;

  public ComplexBean(List<ElementBean> elements) {
    this.elements = elements;
  }

  public List<ElementBean> getElements() {
    return elements;
  }

  public void setElements(List<ElementBean> elements) {
    this.elements = elements;
  }
}

