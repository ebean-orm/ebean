package org.tests.order;

import io.ebean.annotation.Index;

import jakarta.persistence.*;

@Entity
@MappedSuperclass
// Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// DiscriminatorColumn(name = "type")
@Index(columnNames = "type")
public class OrderReferencedParent {

  @Id
  Long id;

  String type;

  String name;

  public OrderReferencedParent(final String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
