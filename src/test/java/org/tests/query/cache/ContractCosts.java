package org.tests.query.cache;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class ContractCosts {
	@Id
	@GeneratedValue
	protected Long id;

	String status;

	@NotNull
	@ManyToOne(cascade = {}, optional = false)
	private Position position;

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Position getPosition() {
		return position;
	}

	public void setPosition(final Position position) {
		this.position = position;
	}
}
