package org.tests.order;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.util.List;

@Entity
public class OrderMaster {

  @Id
  Long id;

  @OneToMany(mappedBy = "master")
  @OrderColumn(name = "sort_order")
  List<OrderReferencedChild> children;

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public List<OrderReferencedChild> getChildren() {
    return children;
  }

  public void setChildren(final List<OrderReferencedChild> children) {
    this.children = children;
  }
}
