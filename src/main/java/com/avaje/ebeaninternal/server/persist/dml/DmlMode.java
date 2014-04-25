package com.avaje.ebeaninternal.server.persist.dml;

/**
 * Used to indicate the part of DML being processed.
 */
public enum DmlMode {

	/**
	 * The Insert SET.
	 */
	INSERT,
	
	/**
	 * The Update SET.
	 */
	UPDATE,
	
}
