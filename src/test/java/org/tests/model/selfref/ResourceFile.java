package org.tests.model.selfref;

import io.ebean.annotation.PrivateOwned;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resourcefile")
public class ResourceFile extends BaseResourceFile {

  private static final long serialVersionUID = 1L;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "parentResourceFileId", nullable = true)
  private ResourceFile parent;

  @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "parent")
  @PrivateOwned
  private Set<ResourceFile> alternatives = new HashSet<>();

  @Column(name = "name", length = 128, nullable = false)
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ResourceFile getParent() {
    return parent;
  }

  public void setParent(ResourceFile parent) {
    this.parent = parent;
  }

  public Set<ResourceFile> getAlternatives() {
    return alternatives;
  }

  public void setAlternatives(Set<ResourceFile> alternatives) {
    this.alternatives = alternatives;
  }
}
