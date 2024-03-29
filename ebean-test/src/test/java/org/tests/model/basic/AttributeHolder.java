package org.tests.model.basic;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.Set;

@Entity
public class AttributeHolder extends BasicDomain {

  private static final long serialVersionUID = 1L;

  @OneToMany(mappedBy = "attributeHolder", cascade = {CascadeType.PERSIST})
  private Set<Attribute> attributes;// = new HashSet<Attribute>();

  public Set<Attribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(Set<Attribute> attributes) {
    this.attributes = attributes;
  }

  public void add(Attribute attribute) {
    getAttributes().add(attribute);
    attribute.setAttributeHolder(this);
  }
}
