package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "la_attr_value")
public class ListAttributeValue extends BasicDomain {
  private static final long serialVersionUID = 1L;

  private String name;

  @ManyToMany
  private Set<ListAttribute> listAttributes;

  public Set<ListAttribute> getListAttributes() {
    return listAttributes;
  }

  public void setListAttribute(Set<ListAttribute> listAttributes) {
    this.listAttributes = listAttributes;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
