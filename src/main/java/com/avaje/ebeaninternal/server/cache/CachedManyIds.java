package com.avaje.ebeaninternal.server.cache;

import java.util.List;

public class CachedManyIds {

	private final List<Object> idList;
	
	public CachedManyIds(List<Object> idList) {
		this.idList = idList;
	}

	public List<Object> getIdList() {
    	return idList;
    }

}
