package com.avaje.ebean.annotation;

/**
 * Optimistic concurrency mode used for updates and deletes.
 */
public enum ConcurrencyMode {

	/**
	 * No concurrency checking.
	 */
	NONE,
	
	/**
	 * Use a version column.
	 */
	VERSION
}
