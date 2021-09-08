package misc.migration.v1_1;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "migtest_ckey_parent")
public class CKeyParent {

  @EmbeddedId
  CKeyParentId id;

  String name;

  @Version
  int version;

  @ManyToOne(cascade = CascadeType.PERSIST)
  CKeyAssoc assoc;

  @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "parent")
  List<CKeyDetail> details;

  public CKeyParentId getId() {
    return id;
  }

  public void setId(CKeyParentId id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public CKeyAssoc getAssoc() {
    return assoc;
  }

  public void setAssoc(CKeyAssoc assoc) {
    this.assoc = assoc;
  }

  public List<CKeyDetail> getDetails() {
    return details;
  }

  public void setDetails(List<CKeyDetail> details) {
    this.details = details;
  }

  public void add(CKeyDetail detail) {
    if (details == null) {
      details = new ArrayList<>();
    }
    details.add(detail);
  }

}
