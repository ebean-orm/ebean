package org.tests.o2m.recurse;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class RMItemHolder extends Model {

  @Id
  long id;
  String name;
  String notes;
  @ManyToOne
  //@JoinColumn(name = "item_a_id")
  private RMItem itemA;
  @ManyToOne
  //@JoinColumn(name = "item_b_id")
  private RMItem itemB;
  @Version
  long version;

  public RMItemHolder(String name) {
    this.name = name;
  }

  public RMItemHolder() {
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

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public RMItem getItemA() {
    return itemA;
  }

  public void setItemA(RMItem itemA) {
    this.itemA = itemA;
  }

  public RMItem getItemB() {
    return itemB;
  }

  public void setItemB(RMItem itemB) {
    this.itemB = itemB;
  }
}
