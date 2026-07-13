package org.tests.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * ManyToMany relationship with an {@code @OrderColumn} - the order value is stored on the
 * intersection/join table (m2m_order_master_m2m_order_child) rather than on the target bean.
 */
@Entity
public class M2mOrderMaster {

  @Id
  long id;

  String name;

  @ManyToMany(cascade = CascadeType.ALL)
  @OrderColumn(name = "sort_order")
  List<M2mOrderChild> children = new ArrayList<>();

  public M2mOrderMaster() {
  }

  public M2mOrderMaster(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<M2mOrderChild> getChildren() {
    return children;
  }

  public void setChildren(List<M2mOrderChild> children) {
    this.children = children;
  }

  @Override
  public String toString() {
    return "M2mOrderMaster[" + id + "," + name + "]";
  }
}
