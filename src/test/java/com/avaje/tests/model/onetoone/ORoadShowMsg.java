package com.avaje.tests.model.onetoone;

import com.avaje.tests.model.basic.BasicDomain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class ORoadShowMsg extends BasicDomain {

  @OneToOne(cascade = CascadeType.ALL, optional = false)
  @JoinColumn()//(name = "corp_id", nullable = false, referencedColumnName = "corp_id")
  public OCompany company;

  public OCompany getCompany() {
    return company;
  }

  public void setCompany(OCompany company) {
    this.company = company;
  }
}
