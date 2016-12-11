package org.tests.compositekeys.db;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.Version;
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
