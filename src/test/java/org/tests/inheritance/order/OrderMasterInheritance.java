package org.tests.inheritance.order;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.List;

@Entity
public class OrderMasterInheritance {

  @Id
  Integer id;

  @OneToMany(cascade = CascadeType.ALL)
  @OrderColumn(name = "sort_order")
  List<OrderedParent> referenced = new ArrayList<>();

  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public List<OrderedParent> getReferenced() {
    return referenced;
  }

  public void setReferenced(final List<OrderedParent> referenced) {
    this.referenced = referenced;
  }
}
