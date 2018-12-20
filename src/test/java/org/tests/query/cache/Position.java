package org.tests.query.cache;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Position {
	@Id
	@GeneratedValue
	protected Long id;

	@ManyToOne(cascade = {}, optional = false)
	private Contract contract;

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public Contract getContract() {
		return contract;
	}

	public void setContract(final Contract contract) {
		this.contract = contract;
	}
}