package com.avaje.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.annotation.UpdateMode;

@Entity
@Table(name="t_mapsuper1")
@UpdateMode(updateChangesOnly=true)
public class TMapSuperEntity extends TMappedSuper2 {

	private static final long serialVersionUID = 1L;
	
	@Id
	Integer id;
	
	String name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
