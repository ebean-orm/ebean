package org.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Entity that extends a two-level generic superclass chain:
 * {@code ProductWithGenericMiddle extends GenericMiddleModel<Long> extends GenericBaseModel<Long>}.
 *
 * <p>Regression for the NPE in DeployCreateProperties when a TypeVariable could not be resolved
 * because generic mappings were not composed across more than one superclass level.
 */
@Entity
@Table(name = "middle_product", schema = "foo")
public class ProductWithGenericMiddle extends GenericMiddleModel<Long> {

  String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
