package org.tests.model.m2m;

import io.ebean.annotation.Index;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

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


  public MnyEdge() {
// default
  }

  public MnyEdge(Object from, Object to) {
    this.from = (MnyNode) from;
    this.to = (MnyNode) to;
    this.id = this.from.id * 10000 + this.to.id;
    this.flags = this.from.id + this.to.id;
  }

  public static MnyEdge createReverseRelation(Object to, MnyNode from) {
    return new MnyEdge(from, to);
  }

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
