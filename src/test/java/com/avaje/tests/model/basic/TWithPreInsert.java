package com.avaje.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.validation.NotEmpty;

@Entity
public class TWithPreInsert {

	@Id
	private Integer id;
	
	@NotEmpty
	private String name;
	
	private String title;

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

	public String getTitle() {
    	return title;
    }

	public void setTitle(String title) {
    	this.title = title;
    }
	
}
