package misc.migration.v1_1;

import io.ebean.annotation.Tablespace;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "migtest_mtm_m")
@Tablespace("db2;TSMASTER;")
public class MtmMaster {

  @Id
  Long id;

  String name;

  @ManyToMany
  List<MtmChild> children;

  @ElementCollection
  List<String> phoneNumbers;

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
