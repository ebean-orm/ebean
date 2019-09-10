package org.tests.model.nofk;

import io.ebean.annotation.DbForeignKey;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
public class EFileNoFk {

  @Id
  @Size(max = 64) // Note: mysql supports only 767 bytes for index
  String fileName;

  // owner without softdelete property - will throw bean has been deleted
  @ManyToOne
  @DbForeignKey(noConstraint = true)
  EUserNoFk owner;

  // owner with softdelete property - will set the property to true
  @ManyToOne
  @DbForeignKey(noConstraint = true)
  EUserNoFkSoftDel ownerSoftDel;

  // hold also a many-2-many relation for tests.
  @ManyToMany
  @DbForeignKey(noConstraint = true)
  List<EUserNoFk> editors;

  @ManyToMany
  @DbForeignKey(noConstraint = true)
  List<EUserNoFkSoftDel> editorsSoftDel;

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public EUserNoFk getOwner() {
    return owner;
  }

  public void setOwner(EUserNoFk owner) {
    this.owner = owner;
  }

  public EUserNoFkSoftDel getOwnerSoftDel() {
    return ownerSoftDel;
  }

  public void setOwnerSoftDel(EUserNoFkSoftDel ownerSoftDel) {
    this.ownerSoftDel = ownerSoftDel;
  }

  public List<EUserNoFk> getEditors() {
    return editors;
  }

  public void setEditors(List<EUserNoFk> editors) {
    this.editors = editors;
  }

  public List<EUserNoFkSoftDel> getEditorsSoftDel() {
    return editorsSoftDel;
  }

  public void setEditorsSoftDel(List<EUserNoFkSoftDel> editorsSoftDel) {
    this.editorsSoftDel = editorsSoftDel;
  }

}
