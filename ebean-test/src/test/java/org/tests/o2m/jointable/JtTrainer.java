package org.tests.o2m.jointable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import java.util.List;

@Entity
@Table(name="trainer")
public class JtTrainer {

  @Id
  long tid;

  String name;

  /**
   * Cascade so maintain join table and save any dirty Monkey beans.
   */
  @OneToMany(cascade = CascadeType.PERSIST)
  @JoinTable(name = "trainer_monkey")
  List<JtMonkey> monkeys;

  @Version
  long version;

  public JtTrainer(String name) {
    this.name = name;
  }

  public long getTid() {
    return tid;
  }

  public void setTid(long tid) {
    this.tid = tid;
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
