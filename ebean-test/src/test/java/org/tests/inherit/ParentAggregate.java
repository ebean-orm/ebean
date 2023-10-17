package org.tests.inherit;

import io.ebean.annotation.Sql;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

@Entity
@Sql
public class ParentAggregate {

  @OneToOne
  public Parent parent;

}
