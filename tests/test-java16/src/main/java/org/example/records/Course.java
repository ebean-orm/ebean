package org.example.records;

import io.ebean.annotation.Length;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "course")
public class Course extends BaseModel {

  @Length(200)
  final String name;

  @Length(400)
  String summary;

  @Transient
  private final Map someCache = new HashMap();

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
