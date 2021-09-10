package org.tests.o2m.jointable;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="troop")
public class JtTroop {

  @Id
  long pid;

  String name;

  /**
   * No cascading over to Monkey but we do maintain the join table regardless.
   */
  @OneToMany
  @JoinTable(name = "troop_monkey")
  List<JtMonkey> monkeys;

  @Version
  long version;

  public JtTroop(String name) {
    this.name = name;
  }

  public long getPid() {
    return pid;
  }

  public void setPid(long pid) {
    this.pid = pid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<JtMonkey> getMonkeys() {
    return monkeys;
  }

  public void setMonkeys(List<JtMonkey> monkeys) {
    this.monkeys = monkeys;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
