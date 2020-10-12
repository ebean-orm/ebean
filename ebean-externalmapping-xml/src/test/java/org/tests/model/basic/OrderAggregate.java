package org.tests.model.basic;

import io.ebean.annotation.Sql;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * An example of an Aggregate object.
 * <p>
 * Note the &#064;Sql indicates to Ebean that this bean is not based on a table but
 * instead uses RawSql.
 * </p>
 */
@Entity
@Sql
public class OrderAggregate {

  @OneToOne
  Order order;
  Double maxAmount;
  Double totalAmount;
  Long totalItems;

}
