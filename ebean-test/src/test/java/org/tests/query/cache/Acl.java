package org.tests.query.cache;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Acl {
	@Id
	@GeneratedValue
	protected Long id;

	String name;

  public Acl(String name) {
    this.name = name;
  }

  public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}
}
