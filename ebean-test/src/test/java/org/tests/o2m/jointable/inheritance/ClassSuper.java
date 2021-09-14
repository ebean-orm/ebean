package org.tests.o2m.jointable.inheritance;

import org.tests.o2m.jointable.JtMonkey;

import javax.persistence.*;
import java.util.List;

@Inheritance
@Entity
public abstract class ClassSuper {
  @Id
  long sid;

  @JoinTable
  @OneToMany(cascade = CascadeType.ALL)
  private List<JtMonkey> monkeys;

  public List<JtMonkey> getMonkeys() {
    return monkeys;
  }
}
