package org.tests.model.basic;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;

@Entity
@Inheritance()
@DiscriminatorColumn(name = "option_type", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("0")
public class Attribute extends BasicDomain {

  private static final long serialVersionUID = 1L;

  @ManyToOne
  private AttributeHolder attributeHolder;

  public AttributeHolder getAttributeHolder() {
    return attributeHolder;
  }

  public void setAttributeHolder(AttributeHolder attributeHolder) {
    this.attributeHolder = attributeHolder;
  }

}
