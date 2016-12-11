package org.tests.model.m2m;

import io.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
public class DRol extends Model {

  @Id
  Long id;

  final String name;

  @ManyToMany(mappedBy = "droles", cascade = CascadeType.PERSIST)
  private List<DCredit> credits;

  @ManyToMany(mappedBy = "croles", cascade = CascadeType.PERSIST)
  private List<DRot> rots;

  public DRol(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public List<DCredit> getCredits() {
    return credits;
  }

  public void setCredits(List<DCredit> credits) {
    this.credits = credits;
  }

  public List<DRot> getRots() {
    return rots;
  }

  public void setRots(List<DRot> rots) {
    this.rots = rots;
  }
}
