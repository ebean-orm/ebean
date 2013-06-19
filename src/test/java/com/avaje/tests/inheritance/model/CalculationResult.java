package com.avaje.tests.inheritance.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({
	@NamedQuery(name="loadResult",
			query="find CalculationResult " +
			"fetch productConfiguration "+
			"fetch groupConfiguration "+
			"where charge = :charge")
})
public class CalculationResult {
	
	@Id
	@Column(name="ID")
	private Integer id;
	
	private double charge;
	
	@ManyToOne(cascade=CascadeType.PERSIST)
	private ProductConfiguration productConfiguration;

	@ManyToOne(cascade=CascadeType.PERSIST)
	private GroupConfiguration groupConfiguration;
	
	public double getCharge() {
		return charge;
	}

	public void setCharge(double charge) {
		this.charge = charge;
	}

	public ProductConfiguration getProductConfiguration() {
		return productConfiguration;
	}

	public void setProductConfiguration(ProductConfiguration productConfiguration) {
		this.productConfiguration = productConfiguration;
	}

	public GroupConfiguration getGroupConfiguration() {
		return groupConfiguration;
	}

	public void setGroupConfiguration(GroupConfiguration groupConfiguration) {
		this.groupConfiguration = groupConfiguration;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
