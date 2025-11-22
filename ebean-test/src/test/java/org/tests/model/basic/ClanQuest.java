package org.tests.model.basic;

import io.ebean.Model;
import jakarta.persistence.*;

@Entity
public class ClanQuest extends Model {
  @Id
  public int id;
  @Basic(fetch = FetchType.LAZY)
  @ManyToOne(optional = false)
  public Clan clan;
}
