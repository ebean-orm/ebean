package org.tests.model.m2m;

import io.ebean.Model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.List;


@Entity
public class DRot extends Model {

  @Id
  Long id;

  final String name;

  @ManyToMany(cascade = CascadeType.ALL)
  List<DRol> croles;

  public DRot(String name) {
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

  public List<DRol> getCroles() {
    return croles;
  }

  public void setCroles(List<DRol> croles) {
    this.croles = croles;
  }
}
