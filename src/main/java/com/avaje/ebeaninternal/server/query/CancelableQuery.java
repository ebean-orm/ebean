package com.avaje.ebeaninternal.server.query;

/**
 * Defines a cancelable query.
 * <p>
 * Typically holds a representation of the PreparedStatement to perform the
 * actual cancel.
 * </p>
 */
public interface CancelableQuery {

	/**
	 * Cancel the query.
	 * <p>
	 * For JDBC this translates to calling cancel on the PreparedStatement.
	 * </p>
	 */
	public void cancel();
}
