package com.avaje.ebeaninternal.server.idgen;

import java.util.UUID;

import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.dbplatform.IdGenerator;

/**
 * IdGenerator for java util UUID.
 */
public class UuidIdGenerator implements IdGenerator {

	/**
	 * Return UUID from UUID.randomUUID();
	 */
	public Object nextId(Transaction t) {
		return UUID.randomUUID();
	}

	/**
	 * Returns "uuid".
	 */
	public String getName() {
		return "uuid";
	}

	/**
	 * Returns false.
	 */
	public boolean isDbSequence() {
		return false;
	}

	/**
	 * Ignored for UUID as not required as a performance optimisation.
	 */
	public void preAllocateIds(int allocateSize) {
		// ignored
	}

	
	
}
