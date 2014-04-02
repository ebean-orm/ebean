package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.DerivedRelationshipData;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Request object passed to bindables.
 */
public interface BindableRequest {

	/**
	 * Set the id for use with summary level logging.
	 */
	public void setIdValue(Object idValue);

	/**
	 * Bind the value to a PreparedStatement.
	 * <p>
	 * Takes into account logicalType to dbType conversion if required.
	 * </p>
	 * <p>
	 * Returns the value that was bound (and was potentially converted from
	 * logicalType to dbType.
	 * </p>
	 * 
	 * @param value
	 *            the value of a property
	 * @param bindNull
	 *            if true bind null values, if false use IS NULL.
	 */
	public Object bind(Object value, BeanProperty prop, String propName) throws SQLException;

	/**
	 * Bind a raw value. Used to bind the discriminator column.
	 */
	public Object bind(String propName, Object value, int sqlType) throws SQLException;

	/**
	 * Bind a raw value with a placeHolder to put into the transaction log.
	 */
    public Object bindNoLog(Object value, int sqlType, String logPlaceHolder) throws SQLException;
    
    /**
     * Bind the value to the preparedStatement without logging.
     */
    public Object bindNoLog(Object value, BeanProperty prop, String propName) throws SQLException;


	/**
	 * Register the value from a update GeneratedValue. This can only be set to
	 * the bean property after the where clause has bean built.
	 */
	public void registerUpdateGenValue(BeanProperty prop, EntityBean bean, Object value);
	
	/**
	 * Return the original PersistRequest.
	 */
	public PersistRequestBean<?> getPersistRequest();

	public void registerDerivedRelationship(DerivedRelationshipData assocBean);
}
