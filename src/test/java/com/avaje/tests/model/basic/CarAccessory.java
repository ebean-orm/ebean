package com.avaje.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class CarAccessory extends BasicDomain{
	
    private static final long serialVersionUID = 1L;

    private String name;
	
	@ManyToOne
	private Car car;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}
}
