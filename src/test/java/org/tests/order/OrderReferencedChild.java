package org.tests.order;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("D")
public class OrderReferencedChild extends OrderReferencedParent {

  String childName;

  @ManyToOne
  OrderMaster master;

  public OrderReferencedChild(final String name) {
    super(name);
  }

  public String getChildName() {
    return childName;
  }

  public void setChildName(final String childName) {
    this.childName = childName;
  }

  public OrderMaster getMaster() {
    return master;
  }

  public void setMaster(final OrderMaster master) {
    this.master = master;
  }
}
