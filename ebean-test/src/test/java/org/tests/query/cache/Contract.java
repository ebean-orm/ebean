package org.tests.query.cache;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Contract {
	@Id
	@GeneratedValue
	protected Long id;

	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "contract")
	private List<Position> positions;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "container", fetch = FetchType.LAZY)
	private List<AclContainerRelation> aclEntries = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Position> getPositions() {
		return positions;
	}

	public void setPositions(final List<Position> positions) {
		this.positions = positions;
	}

	public List<AclContainerRelation> getAclEntries() {
		return aclEntries;
	}

	public void setAclEntries(final List<AclContainerRelation> aclEntries) {
		this.aclEntries = aclEntries;
	}
}
