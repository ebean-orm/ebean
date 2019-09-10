package org.tests.inheritance;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import io.ebean.annotation.PrivateOwned;

/**
 * Model class to reference an organization tree node.
 *
 * @author Christian Hartl, FOCONIS AG
 */
@Entity
public class OrganizationTreeNode {

	@Id
	private Long id;

	private String name;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "parentTreeNode")
	@NotNull
	@PrivateOwned
	private OrganizationNode organizationNode;

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

  public OrganizationNode getOrganizationNode() {
		return organizationNode;
	}

	public void setOrganizationNode(OrganizationNode organizationNode) {
		this.organizationNode = organizationNode;
	}


}
