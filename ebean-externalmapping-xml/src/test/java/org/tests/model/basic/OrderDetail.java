package org.tests.model.basic;

import io.ebean.annotation.Cache;
import io.ebean.annotation.DocEmbedded;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "o_order_detail")
public class OrderDetail extends BasicDomain {

  @ManyToOne(optional = false)
  Order order;

  Integer orderQty;
  Integer shipQty;
  Double unitPrice;

  @ManyToOne
  Product product;

}
