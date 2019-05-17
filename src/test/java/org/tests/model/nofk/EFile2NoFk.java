package org.tests.model.nofk;

import io.ebean.annotation.Formula;
import io.ebean.annotation.Index;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

@Entity
public class EFile2NoFk {

  @Id
  @Size(max = 64) // Note: mysql supports only 767 bytes for index
  String fileName;

  @Index
  int ownerId;

  // owner without softdelete property - will throw bean has been deleted
  @ManyToOne
  @Formula(select = "${ta}.owner_id")
  EUserNoFk owner;

  // owner with softdelete property - will set the property to true
  @ManyToOne
  @Formula(select = "${ta}.owner_id")
  EUserNoFkSoftDel ownerSoftDel;


  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public int getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(int ownerId) {
    this.ownerId = ownerId;
  }

  public EUserNoFk getOwner() {
    return owner;
  }

  public EUserNoFkSoftDel getOwnerSoftDel() {
    return ownerSoftDel;
  }

}
