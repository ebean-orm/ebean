package org.tests.cache.embeddedid;

import io.ebean.annotation.Cache;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Version;
import java.util.List;

@Cache
@Entity
public class CEPProduct {

  @Id
  private Long id;

  private String name;

  @OrderColumn(name = "priority")
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "product")
  private List<CEPProductCategory> productCategories;

  @Version
  private Long version;

  public CEPProduct(String name) {
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

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public List<CEPProductCategory> getProductCategories() {
    return productCategories;
  }

  public void setProductCategories(List<CEPProductCategory> productCategories) {
    this.productCategories = productCategories;
  }
}
