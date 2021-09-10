package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import java.util.List;

import static javax.persistence.CascadeType.PERSIST;

@Entity
public class AnimalShelter {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @OneToMany(cascade = PERSIST, mappedBy = "shelter", orphanRemoval = true)
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
