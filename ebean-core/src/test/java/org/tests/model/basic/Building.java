package org.tests.model.basic;

import io.ebean.Model;
import jakarta.persistence.*;

@Entity
public class Building extends Model {
  public static final String CAFE = "cafe";

  @Id
  private int id;
  @Column(nullable = false)
  public String type;
  public int level;
}
