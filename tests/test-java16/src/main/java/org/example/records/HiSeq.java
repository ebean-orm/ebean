package org.example.records;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.util.SequencedSet;

@Entity
public class HiSeq {

  @Id
  private long id;

  @ManyToOne
  private HiBasic parent;

  private final String name;

  private SequencedSet<Course> courses;

  public HiSeq(String name) {
    this.name = name;
  }

  public long id() {
    return id;
  }

  public HiSeq setId(long id) {
    this.id = id;
    return this;
  }

  public HiBasic parent() {
    return parent;
  }

  public HiSeq setParent(HiBasic parent) {
    this.parent = parent;
    return this;
  }

  public String name() {
    return name;
  }

  public SequencedSet<Course> courses() {
    return courses;
  }

  public HiSeq setCourses(SequencedSet<Course> courses) {
    this.courses = courses;
    return this;
  }
}
