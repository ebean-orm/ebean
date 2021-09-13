package org.example.records;

import io.ebean.annotation.Length;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "course")
public class Course extends BaseModel {

  @Length(200)
  final String name;

  @Length(400)
  String summary;

  public Course(String name) {
    this.name = name;
  }

  public String name() {
    return name;
  }

  public String summary() {
    return summary;
  }

  public void summary(String summary) {
    this.summary = summary;
  }
}
