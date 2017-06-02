package org.tests.model.basic;


import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class Zoo {

  @Id
  Long id;

  @Version
  Long version;
  
  @ManyToOne(cascade = CascadeType.PERSIST)
  Animal anyAnimal;
  
  @ManyToOne(cascade = CascadeType.PERSIST)
  Dog dog;
  
  @ManyToOne(cascade = CascadeType.PERSIST)
  BigDog bigDog;
  
  @ManyToOne(cascade = CascadeType.PERSIST)
  Cat cat;
  
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

  public Animal getAnyAnimal() {
    return anyAnimal;
  }

  public void setAnyAnimal(Animal anyAnimal) {
    this.anyAnimal = anyAnimal;
  }

  public Dog getDog() {
    return dog;
  }

  public void setDog(Dog dog) {
    this.dog = dog;
  }

  public BigDog getBigDog() {
    return bigDog;
  }

  public void setBigDog(BigDog bigDog) {
    this.bigDog = bigDog;
  }

  public Cat getCat() {
    return cat;
  }

  public void setCat(Cat cat) {
    this.cat = cat;
  }
  
}
