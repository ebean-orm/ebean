package com.avaje.tests.model.basic;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="or_order_ship")
public class OrderShipment extends BasicDomain {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	private Order order;
	
	private Timestamp shipTime = new Timestamp(System.currentTimeMillis());

	public Timestamp getShipTime() {
		return shipTime;
	}

	public void setShipTime(Timestamp shipTime) {
		this.shipTime = shipTime;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

}
