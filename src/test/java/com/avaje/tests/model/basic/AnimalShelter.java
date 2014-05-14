package com.avaje.tests.model.basic;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import com.avaje.ebean.annotation.PrivateOwned;

@Entity
public class AnimalShelter {

  @Id
  Long id;
  
  @Version
  Long version;
  
  String name;
  
  @OneToMany(cascade=CascadeType.PERSIST, mappedBy="shelter")
  @PrivateOwned
  List<Animal> animals;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Animal> getAnimals() {
    return animals;
  }

  public void setAnimals(List<Animal> animals) {
    this.animals = animals;
  }
  
}
