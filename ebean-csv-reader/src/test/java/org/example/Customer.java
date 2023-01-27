package org.example;

import javax.persistence.*;
import java.time.LocalDate;

import static javax.persistence.CascadeType.PERSIST;

@Entity
public class Customer extends BaseEntity {

  enum Status {
    NEW,
    ACTIVE,
    INACTIVE
  }

  @Enumerated(EnumType.STRING)
  Status status;

  String name;

  LocalDate anniversary;

  @ManyToOne(optional = false, cascade = PERSIST)
  DAddress billingAddress;


  public Status status() {
    return status;
  }

  public Customer status(Status status) {
    this.status = status;
    return this;
  }

  public String name() {
    return name;
  }

  public Customer name(String name) {
    this.name = name;
    return this;
  }

  public LocalDate anniversary() {
    return anniversary;
  }

  public Customer anniversary(LocalDate anniversary) {
    this.anniversary = anniversary;
    return this;
  }

  public DAddress billingAddress() {
    return billingAddress;
  }

  public Customer billingAddress(DAddress billingAddress) {
    this.billingAddress = billingAddress;
    return this;
  }
}
