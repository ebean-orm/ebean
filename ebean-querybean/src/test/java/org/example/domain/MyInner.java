package org.example.domain;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.util.Objects;

@Entity
@IdClass(MyInner.ID.class)
public class MyInner {

  @Embeddable
  public static class ID {
    final long id;
    final String one;

    public ID(long id, String one) {
      this.id = id;
      this.one = one;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ID id1 = (ID) o;
      return id == id1.id && one.equals(id1.one);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, one);
    }
  }

  @Id
  long id;
  @Id
  String one;
  String description;

  public long id() {
    return id;
  }

  public MyInner id(long id) {
    this.id = id;
    return this;
  }

  public String one() {
    return one;
  }

  public MyInner one(String beanOne) {
    this.one = beanOne;
    return this;
  }

  public String description() {
    return description;
  }

  public MyInner description(String description) {
    this.description = description;
    return this;
  }
}
