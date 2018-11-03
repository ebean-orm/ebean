package org.tests.model.nofk;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

import io.ebean.annotation.DbForeignKey;
import io.ebean.annotation.Formula;
import io.ebean.annotation.Index;

@Entity
public class EFile2NoFk {

  @Id
  @Size(max = 64) // Note: mysql supports only 767 bytes for index
  String fileName;

  @Index
  int ownerId;

  // owner without softdelete property - will throw bean has been deleted
  @ManyToOne
  @Formula(select = "${ta}.Owner_Id")
  EUserNoFk owner;

  // owner with softdelete property - will set the property to true
  @ManyToOne
  @Formula(select = "${ta}.Owner_id")
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
