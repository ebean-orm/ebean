package com.avaje.ebeaninternal.server.persist.dml;

import java.sql.SQLException;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.ebeaninternal.server.persist.dmlbind.Bindable;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableDiscriminator;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableId;

/**
 * Meta data for insert handler. The meta data is for a particular bean type. It
 * is considered immutable and is thread safe.
 */
public final class InsertMeta {

	private final String sqlNullId;

	private final String sqlWithId;

	private final BindableId id;

	private final Bindable discriminator;

	private final Bindable all;

	private final boolean supportsGetGeneratedKeys;

	private final boolean concatinatedKey;

	private final String tableName;

	/**
	 * Used for DB that do not support getGeneratedKeys.
	 */
	private final String selectLastInsertedId;

	private final Bindable shadowFKey;
	
	private final String[] identityDbColumns;
	
	private final boolean emptyStringToNull;
	
	public InsertMeta(DatabasePlatform dbPlatform, BeanDescriptor<?> desc, Bindable shadowFKey, BindableId id, Bindable all) {

	    this.emptyStringToNull = dbPlatform.isTreatEmptyStringsAsNull();
		this.tableName = desc.getBaseTable();
		this.discriminator = getDiscriminator(desc);
		this.id = id;
		this.all = all;
		this.shadowFKey = shadowFKey;

		this.sqlWithId = genSql(false);

		// only available for single Id property
		if (id.isConcatenated()) {
			// concatenated key
			this.concatinatedKey = true;
			this.identityDbColumns = null;
			this.sqlNullId = null;
			this.supportsGetGeneratedKeys = false;
			this.selectLastInsertedId = null;

		} else {
			// insert sql for db identity or sequence insert
			this.concatinatedKey = false;
			this.identityDbColumns = new String[]{id.getIdentityColumn()};
			this.sqlNullId = genSql(true);
			this.supportsGetGeneratedKeys = dbPlatform.getDbIdentity().isSupportsGetGeneratedKeys();
			this.selectLastInsertedId = desc.getSelectLastInsertedId();
		}
	}

	private static Bindable getDiscriminator(BeanDescriptor<?> desc){
		InheritInfo inheritInfo = desc.getInheritInfo();
		if (inheritInfo != null){
			return new BindableDiscriminator(inheritInfo);
		} else {
			return null;
		}
	}
	
	/**
	 * Return true if empty strings should be treated as null.
	 */
	public boolean isEmptyStringToNull() {
        return emptyStringToNull;
    }

    /**
	 * Return true if this is a concatenated key.
	 */
	public boolean isConcatinatedKey() {
		return concatinatedKey;
	}

	public String[] getIdentityDbColumns() {
		return identityDbColumns;
	}

	/**
	 * Returns sql that is used to fetch back the last inserted id. This will
	 * return null if it should not be used.
	 * <p>
	 * This is only for DB's that do not support getGeneratedKeys. For MS
	 * SQLServer 2000 this could return "SELECT (at)(at)IDENTITY as id".
	 * </p>
	 */
	public String getSelectLastInsertedId() {
		return selectLastInsertedId;
	}

	/**
	 * Return true if getGeneratedKeys is supported by the underlying jdbc
	 * driver and database.
	 */
	public boolean supportsGetGeneratedKeys() {
		return supportsGetGeneratedKeys;
	}

	/**
	 * Return true if the Id can be derived from other property values.
	 */
	public boolean deriveConcatenatedId(PersistRequestBean<?> persist) {
		return id.deriveConcatenatedId(persist);
	}

	/**
	 * Bind the request based on whether the id value(s) are null.
	 */
	public void bind(DmlHandler request, EntityBean bean, boolean withId) throws SQLException {

		if (withId) {
			id.dmlBind(request, bean);
		}
		if (shadowFKey != null){
			shadowFKey.dmlBind(request, bean);
		}
		if (discriminator != null){
			discriminator.dmlBind(request, bean);			
		}
		all.dmlBind(request, bean);
	}

	/**
	 * get the sql based whether the id value(s) are null.
	 */
	public String getSql(boolean withId) {

		if (withId) {
			return sqlWithId;
		} else {
			return sqlNullId;
		}
	}

	private String genSql(boolean nullId) {

		GenerateDmlRequest request = new GenerateDmlRequest(emptyStringToNull, null, true);
		request.setInsertSetMode();
		
		request.append("insert into ").append(tableName);
		request.append(" (");

		if (!nullId) {
			id.dmlAppend(request);
		} 
		
		if (shadowFKey != null){
			shadowFKey.dmlAppend(request);
		}
		
		if (discriminator != null){
			discriminator.dmlAppend(request);			
		}
		
		all.dmlAppend(request);

		request.append(") values (");
		request.append(request.getInsertBindBuffer());
		request.append(")");

		return request.toString();
	}

}
