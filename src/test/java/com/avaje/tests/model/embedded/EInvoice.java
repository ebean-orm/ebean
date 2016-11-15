package com.avaje.tests.model.embedded;

import com.avaje.ebean.annotation.Cache;

import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Cache
@Entity
public class EInvoice {

  public enum State {
    New, Processing, Approved
  }

  @Id
  Long id;

  @Version
  Long version;

  Date invoiceDate;

  State state;

  @ManyToOne
  EPerson person;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "street", column = @Column(name = "ship_street")),
    @AttributeOverride(name = "suburb", column = @Column(name = "ship_suburb")),
    @AttributeOverride(name = "city", column = @Column(name = "ship_city")),
    @AttributeOverride(name = "status", column = @Column(name = "ship_status"))
  })
  EAddress shipAddress;

  @Embedded
  EAddress billAddress;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public Date getInvoiceDate() {
    return invoiceDate;
  }

  public void setInvoiceDate(Date invoiceDate) {
    this.invoiceDate = invoiceDate;
  }

  public EPerson getPerson() {
    return person;
  }

  public void setPerson(EPerson person) {
    this.person = person;
  }

  public EAddress getShipAddress() {
    return shipAddress;
  }

  public void setShipAddress(EAddress shipAddress) {
    this.shipAddress = shipAddress;
  }

  public EAddress getBillAddress() {
    return billAddress;
  }

  public void setBillAddress(EAddress billAddress) {
    this.billAddress = billAddress;
  }

}
