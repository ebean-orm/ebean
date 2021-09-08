package org.tests.model.embedded;

import io.ebean.annotation.Cache;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import java.util.Date;

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

  @Embedded(prefix = "ship_")
  EAddress shipAddress;

  @Embedded(prefix = "bill_")
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
