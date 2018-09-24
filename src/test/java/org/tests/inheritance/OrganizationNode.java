package org.tests.inheritance;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import io.ebean.annotation.Index;

/**
 * Model class to reference an organization node.
 *
 * @author Christian Hartl, FOCONIS AG
 */
@Entity
@MappedSuperclass
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "kind")
@Index(unique = false, columnNames = "kind")
public abstract class OrganizationNode {

	@Id
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@OneToOne(cascade = {})
	@NotNull
	private OrganizationTreeNode parentTreeNode;

	public OrganizationTreeNode getParentTreeNode() {
		return parentTreeNode;
	}

	public void setParentTreeNode(OrganizationTreeNode parentTreeNode) {
		this.parentTreeNode = parentTreeNode;
	}

}
