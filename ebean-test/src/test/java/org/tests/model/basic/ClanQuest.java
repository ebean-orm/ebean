package org.tests.model.basic;

import io.ebean.Model;
import jakarta.persistence.*;

@Entity
public class ClanQuest extends Model {

  @Id
  public int id;

  @ManyToOne(optional = false)
  public final Clan clan;

  public ClanQuest(Clan clan) {
    this.clan = clan;
  }
}
