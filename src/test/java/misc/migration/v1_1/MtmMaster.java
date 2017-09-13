package misc.migration.v1_1;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_mtm_m")
public class MtmMaster {

  @Id
  Long id;

  String name;

  @ManyToMany
  List<MtmChild> children;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<MtmChild> getChildren() {
    return children;
  }
  
  public void setChildren(List<MtmChild> children) {
    this.children = children;
  }

}
