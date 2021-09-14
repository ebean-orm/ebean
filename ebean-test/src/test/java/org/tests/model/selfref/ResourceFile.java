package org.tests.model.selfref;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resourcefile")
public class ResourceFile extends BaseResourceFile {

  private static final long serialVersionUID = 1L;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "parentresourcefileid", nullable = true)
  private ResourceFile parent;

  @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "parent", orphanRemoval = true)
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
