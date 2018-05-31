package org.tests.ddl;

import io.ebean.annotation.DbForeignKey;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class DfkNoneViaMtoM {

  @Id
  long id;

  String name;

  /**
   * Have no Foreign key constraints on the intersection table.
   */
  @ManyToMany(cascade = CascadeType.ALL)
  @DbForeignKey(noConstraint = true)
  List<DfkOne> ones = new ArrayList<>();

  public DfkNoneViaMtoM(String name) {
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

  public List<DfkOne> getOnes() {
    return ones;
  }

  public void setOnes(List<DfkOne> ones) {
    this.ones = ones;
  }
}
