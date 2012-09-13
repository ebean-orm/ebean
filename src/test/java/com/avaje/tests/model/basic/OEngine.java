package com.avaje.tests.model.basic;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class OEngine {

	private static final long serialVersionUID = 1L;

	@Id
	private UUID engineId;

	private String shortDesc;
	
	@Version
	private Integer version;
	
	@OneToOne
	private OCar car;
	
	public OEngine() {
	}

    public UUID getEngineId() {
        return engineId;
    }

    public void setEngineId(UUID engineId) {
        this.engineId = engineId;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public OCar getCar() {
        return car;
    }

    public void setCar(OCar car) {
        this.car = car;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    
}
