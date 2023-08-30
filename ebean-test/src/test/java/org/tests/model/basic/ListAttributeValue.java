package org.tests.model.basic;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "la_attr_value")
public class ListAttributeValue extends BasicDomain {
  private static final long serialVersionUID = 1L;

  private String name;

  @ManyToMany
  private Set<Attribute> listAttributes;

  public Set<Attribute> getListAttributes() {
    return listAttributes;
  }

  public void setListAttribute(Set<Attribute> listAttributes) {
    this.listAttributes = listAttributes;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
