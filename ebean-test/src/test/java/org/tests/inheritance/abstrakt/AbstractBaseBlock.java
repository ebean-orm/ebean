package org.tests.inheritance.abstrakt;

import javax.persistence.*;

@Entity
@Table(name = "blockt")
@Inheritance
@DiscriminatorColumn(name = "case_type", discriminatorType = DiscriminatorType.INTEGER)
public abstract class AbstractBaseBlock {

  @Id
  long id;

  String name;

  @Version
  long version;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
