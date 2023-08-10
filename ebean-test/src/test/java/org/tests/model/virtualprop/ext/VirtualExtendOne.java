package org.tests.model.virtualprop.ext;

import org.tests.model.virtualprop.VirtualBase;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity
public class VirtualExtendOne {

  @Id
  private int id;

  private String data;

  @PrimaryKeyJoinColumn
  @OneToOne(optional = false)
  private VirtualBase base;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public VirtualBase getBase() {
    return base;
  }

  public void setBase(VirtualBase base) {
    this.base = base;
    this.id = base == null ? 0 : base.getId();
  }
}
