package com.avaje.tests.inheritance.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Configurations {
	@Id
	@Column(name="ID")
	private Integer id;
	
	@OneToMany
	private List<GroupConfiguration> groupConfigurations;


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public List<GroupConfiguration> getGroupConfigurations() {
		return groupConfigurations;
	}


	public void setGroupConfigurations(List<GroupConfiguration> groupConfigurations) {
		this.groupConfigurations = groupConfigurations;
	}
	
	public void add(GroupConfiguration groupConfiguration){
		groupConfiguration.setConfigurations(this);
		groupConfigurations.add(groupConfiguration);
	}
}
