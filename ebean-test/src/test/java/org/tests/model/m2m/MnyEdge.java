package org.tests.model.m2m;

import io.ebean.annotation.Index;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Index(unique = true, columnNames = { "from_id", "to_id" })
@Index(unique = true, columnNames = { "to_id", "from_id" })
public class MnyEdge {

  @Id
  private Integer id;

  @ManyToOne
  private MnyNode from;

  @ManyToOne
  private MnyNode to;

  private int flags;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public MnyNode getFrom() {
    return from;
  }

  public void setFrom(MnyNode from) {
    this.from = from;
  }

  public MnyNode getTo() {
    return to;
  }

  public void setTo(MnyNode to) {
    this.to = to;
  }

  public int getFlags() {
    return flags;
  }

  public void setFlags(int flags) {
    this.flags = flags;
  }

}
