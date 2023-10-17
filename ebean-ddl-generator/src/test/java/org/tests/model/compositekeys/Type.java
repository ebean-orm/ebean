package org.tests.model.compositekeys;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.List;

@Entity
@Table(name="`type`") // needs to be quoted because it's a keyword on HANA
public class Type {
  @Id
  private TypeKey key;

  private String description;

  @Version
  private Long version;

  @OneToMany
  @JoinColumns({
    @JoinColumn(name = "customer", referencedColumnName = "customer", insertable = false, updatable = false),
    @JoinColumn(name = "type", referencedColumnName = "type", insertable = false, updatable = false)
  })
  private List<Item> items;

  @ManyToOne
  private SubType subType;

  public TypeKey getKey() {
    return key;
  }

  public void setKey(TypeKey key) {
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

  public SubType getSubType() {
    return subType;
  }

  public void setSubType(SubType subType) {
    this.subType = subType;
  }
}
