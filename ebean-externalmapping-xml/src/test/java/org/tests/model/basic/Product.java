package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "o_product")
public class Product extends BasicDomain {
  String sku;
  String name;
}
