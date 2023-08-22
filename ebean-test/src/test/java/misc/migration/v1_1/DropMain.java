package misc.migration.v1_1;

import javax.persistence.*;
import java.util.List;

/**
 * Test-model referencing two other tables, which gets dropped for migration test in order to check for correct table drop
 * procedure (first drop foreign keys, then the tables).
 *
 * @author Jonas P&ouml;hler, FOCONIS AG
 */
@Entity
@Table(name = "drop_main")
public class DropMain {

  @Id
  Integer id;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
  List<DropRefOne> refsOne;

  @ManyToMany(cascade = CascadeType.ALL)
  List<DropRefMany> refsMany;

  @OneToOne(mappedBy = "parent")
  DropRefOneToOne refsOneToOne;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public List<DropRefOne> getRefsOne() {
    return refsOne;
  }

  public void setRefsOne(List<DropRefOne> refsOne) {
    this.refsOne = refsOne;
  }

  public List<DropRefMany> getRefsMany() {
    return refsMany;
  }

  public void setRefsMany(List<DropRefMany> refsMany) {
    this.refsMany = refsMany;
  }

  public DropRefOneToOne getRefsOneToOne() {
    return refsOneToOne;
  }

  public void setRefsOneToOne(DropRefOneToOne refsOneToOne) {
    this.refsOneToOne = refsOneToOne;
  }
}
