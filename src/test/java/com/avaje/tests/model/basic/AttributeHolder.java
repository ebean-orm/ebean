package com.avaje.tests.model.basic;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class AttributeHolder extends BasicDomain{
	
	private static final long serialVersionUID = 1L;

	@OneToMany(mappedBy="attributeHolder", cascade={CascadeType.PERSIST})
	private Set<Attribute> attributes;// = new HashSet<Attribute>();

	public Set<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Set<Attribute> attributes) {
		this.attributes = attributes;
	}
	
	public void add(Attribute attribute){
		getAttributes().add(attribute);
		attribute.setAttributeHolder(this);
	}
}
