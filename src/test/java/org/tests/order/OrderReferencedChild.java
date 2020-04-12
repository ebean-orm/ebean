package org.tests.order;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.util.List;

@Entity
@DiscriminatorValue("D")
public class OrderReferencedChild extends OrderReferencedParent {

  String childName;

  @ManyToOne
  OrderMaster master;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "child", orphanRemoval = true)
  @OrderColumn(name = "sort_order")
  List<OrderToy> toys;

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

  public List<OrderToy> getToys() {
    return toys;
  }

  public void setToys(final List<OrderToy> toys) {
    this.toys = toys;
  }
}
