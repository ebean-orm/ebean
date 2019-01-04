package org.tests.delete;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import java.util.List;

@Entity
public class DcMaster {

  @Id
  long id;

  String name;

  @OneToMany(mappedBy = "master", cascade = CascadeType.ALL)
  List<DcDetail> details;

  @Version
  long version;

  public DcMaster(String name) {
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

  public void setName(String name) {
    this.name = name;
  }

  public List<DcDetail> getDetails() {
    return details;
  }

  public void setDetails(List<DcDetail> details) {
    this.details = details;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
