package com.avaje.tests.sp.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@MappedSuperclass
public abstract class IdEntity {

	@Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
	protected Long id;
	
	@Version
	private int version;
	
	public int getVersion() {
		return version;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	@Override
	public int hashCode() {
		return id == null ? 31 : 31 + 31 * id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		
		if (obj == null || !this.getClass().isInstance(obj))
			return false;
		
		IdEntity o = (IdEntity)obj;
		
		return id != null && o.id != null && id.equals(o.id);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + id + "]";
	}
}
