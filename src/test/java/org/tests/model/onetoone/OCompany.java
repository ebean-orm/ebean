package org.tests.model.onetoone;

import org.tests.model.basic.BasicDomain;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class OCompany extends BasicDomain {
  private static final long serialVersionUID = 6733591692223352686L;
  
  @Column(length = 50, unique = true)
  public String corpId;

  public String getCorpId() {
    return corpId;
  }

  public void setCorpId(String corpId) {
    this.corpId = corpId;
  }
}
