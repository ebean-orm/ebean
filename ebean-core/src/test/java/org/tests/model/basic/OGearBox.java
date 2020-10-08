package org.tests.model.basic;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import java.io.Serializable;
import java.util.UUID;

@Entity
public class OGearBox implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  private UUID id;

  private String boxDesc;

  @Column(name = "box_size")
  private Integer size;

  @Version
  private Integer version;

  @OneToOne
  private OCar car;

  public OGearBox() {
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getBoxDesc() {
    return boxDesc;
  }

  public void setBoxDesc(String boxDesc) {
    this.boxDesc = boxDesc;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public OCar getCar() {
    return car;
  }

  public void setCar(OCar car) {
    this.car = car;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

}
