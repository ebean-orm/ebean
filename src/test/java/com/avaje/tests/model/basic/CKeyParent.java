/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.tests.model.basic;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

@Entity
public class CKeyParent {

	@EmbeddedId
	CKeyParentId id;
	
	String name;
	
	@Version
	int version;

	@ManyToOne(cascade=CascadeType.PERSIST)
	CKeyAssoc assoc;
	
	@OneToMany(cascade=CascadeType.PERSIST, mappedBy="parent")
	List<CKeyDetail> details;
	
	public CKeyParentId getId() {
		return id;
	}

	public void setId(CKeyParentId id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public CKeyAssoc getAssoc() {
		return assoc;
	}

	public void setAssoc(CKeyAssoc assoc) {
		this.assoc = assoc;
	}

	public List<CKeyDetail> getDetails() {
		return details;
	}

	public void setDetails(List<CKeyDetail> details) {
		this.details = details;
	}
	
	public void add(CKeyDetail detail) {
		if (details == null){
			details = new ArrayList<CKeyDetail>();
		}
		details.add(detail);
	}
	
}
