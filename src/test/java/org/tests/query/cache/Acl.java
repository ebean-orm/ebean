package org.tests.query.cache;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

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
