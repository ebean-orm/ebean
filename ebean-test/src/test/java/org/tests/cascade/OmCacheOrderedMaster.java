package org.tests.cascade;

import io.ebean.annotation.Cache;

import javax.persistence.*;
import java.util.List;

@Entity
@Cache
public class OmCacheOrderedMaster {

  @Id
  Long id;

  String name;

  /**
   * Cascade ALL set automatically as we set order values when cascading.
   */
  @OneToMany(mappedBy = "master") //, cascade = CascadeType.ALL)
  @OrderColumn(name = "sort_order")
  List<OmCacheOrderedDetail> details;

  @Version
  Long version;

  public OmCacheOrderedMaster(String name) {
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

  public List<OmCacheOrderedDetail> getDetails() {
    return details;
  }

  public void setDetails(List<OmCacheOrderedDetail> details) {
    this.details = details;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
