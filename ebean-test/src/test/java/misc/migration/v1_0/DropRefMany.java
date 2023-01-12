package misc.migration.v1_0;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.List;

/**
 * @author Jonas P&ouml;hler, FOCONIS AG
 */
@Entity
@Table(name = "drop_ref_many")
public class DropRefMany {

  @Id
  Integer id;

  @ManyToMany(cascade = {}, mappedBy = "refsMany")
  List<DropMain> parents;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public List<DropMain> getParents() {
    return parents;
  }

  public void setParents(List<DropMain> parents) {
    this.parents = parents;
  }
}