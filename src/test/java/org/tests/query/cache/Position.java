package org.tests.query.cache;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "e_position")
public class Position {
	@Id
	@GeneratedValue
	protected Long id;

	private String name;

	@ManyToOne(cascade = {}, optional = false)
	private Contract contract;

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

  public Contract getContract() {
		return contract;
	}

	public void setContract(final Contract contract) {
		this.contract = contract;
	}
}
