package misc.migration.v1_1;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.List;

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
