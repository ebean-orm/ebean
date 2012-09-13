package com.avaje.tests.basic.one2one;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "drel_booking")
public class Booking {

	@Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;
	
	@Version
	private int version;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "agent_invoice")	
	private Invoice agentInvoice;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "client_invoice")	
	private Invoice clientInvoice;
	
	@OneToMany(mappedBy = "booking")//, cascade = CascadeType.ALL)
	private List<Invoice> invoices;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public int getVersion() {
		return version;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
		
	public Invoice getAgentInvoice() {
		return agentInvoice;
	}
	
	public void setAgentInvoice(Invoice agentInvoice) {
		this.agentInvoice = agentInvoice;
	}
	
	public Invoice getClientInvoice() {
		return clientInvoice;
	}
	
	public void setClientInvoice(Invoice clientInvoice) {
		this.clientInvoice = clientInvoice;
	}
	
	public List<Invoice> getInvoices() {
		return invoices;
	}
	
	public void setInvoices(List<Invoice> invoices) {
		this.invoices= invoices;
	}
}
