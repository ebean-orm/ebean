package org.tests.model.compositekeys;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import java.util.List;

@Entity
public class Region {
  @Id
  private RegionKey key;

  private String description;

  @Version
  private Long version;

  @OneToMany
  @JoinColumns({
    @JoinColumn(name = "customer", referencedColumnName = "customer", insertable = false, updatable = false),
    @JoinColumn(name = "region", referencedColumnName = "type", insertable = false, updatable = false)
  })
  private List<Item> items;

  public RegionKey getKey() {
    return key;
  }

  public void setKey(RegionKey key) {
    this.key = key;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public List<Item> getItems() {
    return items;
  }
}
