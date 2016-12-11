package org.tests.model.basic;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "species")
public abstract class Animal {

  @Id
  Long id;

  @Version
  Long version;

  @Column(name = "species", insertable = false, updatable = false, nullable = false)
  String species;

  @ManyToOne
  AnimalShelter shelter;

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

  public String getSpecies() {
    return species;
  }

  public void setSpecies(String species) {
    this.species = species;
  }

  public AnimalShelter getShelter() {
    return shelter;
  }

  public void setShelter(AnimalShelter shelter) {
    this.shelter = shelter;
  }

}
