package org.tests.compositekeys.db;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class CaoBean {

  @Id
  @AttributeOverride(name = "customer", column = @Column(name = "x_cust_id"))
  @AttributeOverride(name = "type", column = @Column(name = "x_type_id"))
  private CaoKey key;

  private String description;

  @Version
  private Long version;

  public CaoKey getKey() {
    return key;
  }

  public void setKey(CaoKey key) {
    this.key = key;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

}
