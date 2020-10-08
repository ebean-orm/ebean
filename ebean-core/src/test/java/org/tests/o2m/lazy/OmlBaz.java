package org.tests.o2m.lazy;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "oml_baz")
public class OmlBaz {
  @Id
  private Long id;

  @ManyToOne(optional = false)
  private OmlFoo foo;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public OmlFoo getFoo() {
    return foo;
  }

  public void setFoo(OmlFoo foo) {
    this.foo = foo;
  }

}
