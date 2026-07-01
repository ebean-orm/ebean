package org.tests.aggregateformula;

import io.ebean.annotation.Aggregation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class EWithLobAndAgg {

  @Id
  long id;

  @Column
  String name;

   @Lob
  @Column
  String description;

  @Aggregation("count(*)")
  int count;

  public long id() {
    return id;
  }

  public EWithLobAndAgg setId(long id) {
    this.id = id;
    return this;
  }

  public String name() {
    return name;
  }

  public EWithLobAndAgg setName(String name) {
    this.name = name;
    return this;
  }

  public String description() {
    return description;
  }

  public EWithLobAndAgg setDescription(String description) {
    this.description = description;
    return this;
  }

  public int count() {
    return count;
  }

  public EWithLobAndAgg setCount(int count) {
    this.count = count;
    return this;
  }
}
