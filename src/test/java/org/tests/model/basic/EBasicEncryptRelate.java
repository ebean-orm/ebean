package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "e_basicenc_relate")
public class EBasicEncryptRelate {

  @Id
  private long id;

  private String name;

  @ManyToOne
  private EBasicEncrypt other;

  public EBasicEncryptRelate(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EBasicEncrypt getOther() {
    return other;
  }

  public void setOther(EBasicEncrypt other) {
    this.other = other;
  }
}
