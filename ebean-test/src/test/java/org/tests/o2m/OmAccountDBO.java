package org.tests.o2m;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
public class OmAccountDBO extends Model {

  @Id
  private long id;

  private final String name;

  @OneToMany(cascade = ALL, orphanRemoval = true)//, mappedBy = "bananaRama")
  private List<OmAccountChildDBO> child676;

  public OmAccountDBO(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public List<OmAccountChildDBO> getChild676() {
    return child676;
  }

  public void setChild676(List<OmAccountChildDBO> child676) {
    this.child676 = child676;
  }
}
