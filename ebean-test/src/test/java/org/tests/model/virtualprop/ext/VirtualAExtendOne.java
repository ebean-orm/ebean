package org.tests.model.virtualprop.ext;

import org.tests.model.virtualprop.VirtualBaseA;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class VirtualAExtendOne {

  @Id
  private int id;

  private String data;

  @ManyToOne
  private VirtualBaseA base;

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

  public VirtualBaseA getBase() {
    return base;
  }

  public void setBase(VirtualBaseA base) {
    this.base = base;
    this.id = base == null ? 0 : base.getId();
  }
}
