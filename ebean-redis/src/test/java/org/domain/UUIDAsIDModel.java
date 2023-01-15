package org.domain;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import io.ebean.annotation.CacheBeanTuning;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Cache(enableQueryCache = true, nearCache = true, naturalKey = "uuidModel")
@CacheBeanTuning(maxSecsToLive = 1)
@Entity
public class UUIDAsIDModel extends Model {
	
	@Id
	UUID uuid;
	
	String name;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public UUID getUuid() {
		return uuid;
	}
	
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
}
