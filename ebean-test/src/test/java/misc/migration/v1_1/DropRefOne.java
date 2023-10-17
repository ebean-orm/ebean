package misc.migration.v1_1;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "drop_ref_one")
public class DropRefOne {

  @Id
  Integer id;

  @ManyToOne(cascade = {})
  DropMain parent;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public DropMain getParent() {
    return parent;
  }

  public void setParent(DropMain parent) {
    this.parent = parent;
  }
}
