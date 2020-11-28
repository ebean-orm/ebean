package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "o_address")
public class Address extends BasicDomain {
  String line1;
  String line2;
  String city;
//  @ManyToOne
//  Country country;
}
