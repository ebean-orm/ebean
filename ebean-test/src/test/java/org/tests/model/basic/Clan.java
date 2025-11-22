package org.tests.model.basic;

import io.ebean.Model;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Clan extends Model {
  @Id
  public int id;
  @OneToMany(cascade = CascadeType.ALL)
  public List<Building> buildings = new ArrayList<>();
}
