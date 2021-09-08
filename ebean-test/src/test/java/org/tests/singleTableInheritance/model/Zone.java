package org.tests.singleTableInheritance.model;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "zones")
public class Zone {
  @Id
  @Column(name = "id")
  private Integer id;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public int hashCode() {
    if (getId() != null) return getId().hashCode();
    else return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Zone) {
      return ((Zone) obj).getId().equals(getId());
    } else {
      return false;
    }
  }
}
