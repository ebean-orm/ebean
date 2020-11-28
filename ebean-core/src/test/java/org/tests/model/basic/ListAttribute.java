package org.tests.model.basic;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.Set;

@Entity
@DiscriminatorValue("1")
public class ListAttribute extends Attribute {

  private static final long serialVersionUID = 1L;

  @ManyToMany(mappedBy = "listAttributes", cascade = {CascadeType.PERSIST})
  private Set<ListAttributeValue> values;// = new HashSet<ListAttributeValue>();
  // Do not define this as a HashSet so that it is left up to Ebean
  // to define it as a BeanSet ... and hence aware of M2M modify changes
  // Put this back to HashSet and Duplicate Key Exception will return

  public Set<ListAttributeValue> getValues() {
    return values;
  }

  public void setValues(Set<ListAttributeValue> values) {
    this.values = values;
  }

  public void add(ListAttributeValue value) {
    getValues().add(value);
    value.getListAttributes().add(this);
  }
}
