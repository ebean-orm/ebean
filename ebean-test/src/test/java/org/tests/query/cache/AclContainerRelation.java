package org.tests.query.cache;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class AclContainerRelation {
	@Id
	@GeneratedValue
	protected Long id;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@NotNull
	private Contract container;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@NotNull
	private Acl aclEntry;

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public Contract getContainer() {
		return container;
	}

	public void setContainer(final Contract container) {
		this.container = container;
	}

	public Acl getAclEntry() {
		return aclEntry;
	}

	public void setAclEntry(final Acl aclEntry) {
		this.aclEntry = aclEntry;
	}

}
