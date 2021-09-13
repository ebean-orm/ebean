package org.tests.model.onetoone;

import org.tests.model.basic.BasicDomain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class ORoadShowMsg extends BasicDomain {
  private static final long serialVersionUID = -1555123312818834212L;
  
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
