package org.tests.model.basic;

import io.ebean.Model;
import jakarta.persistence.*;

@Entity
public class Building extends Model {
  public static final String CAFE = "cafe";
  public static final String HOUSE = "house";
  public static final String STORE = "store";

  @Id
  public int id;
  @Column(nullable = false)
  public String type;
  public int level;
  public final String name;
  @ManyToOne(optional = false)
  public final Clan clan;

  public Building(Clan clan, String type, String name) {
    this.clan = clan;
    this.type = type;
    this.name = name;
  }
}
