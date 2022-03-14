package org.tests.model.join;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class HCustomer {

  @Id
  final String cid;

  final String name;

  String status = "A";

  public HCustomer(String cid, String name) {
    this.cid = cid;
    this.name = name;
  }
}
