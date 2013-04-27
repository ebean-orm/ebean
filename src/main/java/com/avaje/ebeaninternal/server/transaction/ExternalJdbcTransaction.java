package com.avaje.ebeaninternal.server.transaction;

import java.sql.Connection;

import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;

/**
 * Transaction based on a java.sql.Connection supplied by an external
 * transaction manager such as Spring.
 * <p>
 * This means that the transaction demarcation [commit(), rollback(), end()]
 * must be controlled externally (by Spring etc) and so these methods [commit(),
 * rollback(), end()] can not be called on this ExternalJdbcTransaction.
 * </p>
 * <p>
 * That is, a transaction started externally (by Spring etc) must be committed
 * or rolled back externally as well.
 * </p>
 */
public class ExternalJdbcTransaction extends JdbcTransaction {

	/**
	 * Create a Transaction that will have no transaction logging support.
	 * <p>
	 * You need to create with a TransactionManager to have transaction logging.
	 * </p>
	 */
    public ExternalJdbcTransaction(Connection connection) {
        super(null, true, connection, null);
    }

    /**
     * Construct will all explicit parameters.
     */
	public ExternalJdbcTransaction(String id, boolean explicit, Connection connection, TransactionManager manager) {
		super(id, explicit, connection, manager);
	}

	/**
	 * This will always throw a PersistenceException.
	 * <p>
	 * Externally created connections should be committed or rolled back externally.
	 * </p>
	 */
	@Override
	public void commit() throws RollbackException {
		throw new PersistenceException("This is an external transaction so must be committed externally");
	}

	/**
	 * This will always throw a PersistenceException.
	 * <p>
	 * Externally created connections should be committed or rolled back externally.
	 * </p>
	 */
	@Override
	public void end() throws PersistenceException {
		throw new PersistenceException("This is an external transaction so must be committed externally");
	}

	/**
	 * This will always throw a PersistenceException.
	 * <p>
	 * Externally created connections should be committed or rolled back externally.
	 * </p>
	 */
	@Override
	public void rollback() throws PersistenceException {
		throw new PersistenceException("This is an external transaction so must be rolled back externally");
	}

	/**
	 * This will always throw a PersistenceException.
	 * <p>
	 * Externally created connections should be committed or rolled back externally.
	 * </p>
	 */
	@Override
	public void rollback(Throwable e) throws PersistenceException {
		throw new PersistenceException("This is an external transaction so must be rolled back externally");
	}

}
