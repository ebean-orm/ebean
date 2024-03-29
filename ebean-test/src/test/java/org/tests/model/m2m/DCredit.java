package org.tests.model.m2m;

import io.ebean.Model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.List;

@Entity
public class DCredit extends Model {

  @Id
  Long id;

  String credit;

  @ManyToMany(cascade = CascadeType.PERSIST)
  List<DRol> droles;

  public DCredit(String credit) {
    this.credit = credit;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCredit() {
    return credit;
  }

  public void setCredit(String credit) {
    this.credit = credit;
  }

  public List<DRol> getDroles() {
    return droles;
  }

  public void setDroles(List<DRol> droles) {
    this.droles = droles;
  }
}
