package org.tests.model.join;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

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
