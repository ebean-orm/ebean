package org.tests.cascade;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Version;
import java.util.List;

@Entity
public class OmOrderedMaster {

  @Id
  Long id;

  String name;

  /**
   * Cascade ALL set automatically as we set order values when cascading.
   */
  @OneToMany(mappedBy = "master") //, cascade = CascadeType.ALL)
  @OrderColumn(name="sort_order")
  List<OmOrderedDetail> details;

  @Version
  Long version;

  public OmOrderedMaster(String name) {
    this.name = name;
  }

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

  public List<OmOrderedDetail> getDetails() {
    return details;
  }

  public void setDetails(List<OmOrderedDetail> details) {
    this.details = details;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
