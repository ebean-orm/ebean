package misc.migration.v1_0;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
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
}
