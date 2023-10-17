package org.tests.order;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
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
