package org.tests.order;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class OrderToy {

  @Id
  Integer id;

  String title;

  @ManyToOne
  OrderReferencedChild child;

  public OrderToy(final String title) {
    this.title = title;
  }

  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public OrderReferencedChild getChild() {
    return child;
  }

  public void setChild(final OrderReferencedChild child) {
    this.child = child;
  }
}
