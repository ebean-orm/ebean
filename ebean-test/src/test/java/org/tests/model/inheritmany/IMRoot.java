package org.tests.model.inheritmany;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class IMRoot {

  @Id
  Long id;

  @OneToMany(mappedBy = "owner")
  List<IMRelated> related;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public List<IMRelated> getRelated() {
    return related;
  }

  public void setRelated(List<IMRelated> related) {
    this.related = related;
  }

}
