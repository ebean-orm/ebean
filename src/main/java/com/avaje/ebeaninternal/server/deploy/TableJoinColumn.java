package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebeaninternal.server.core.InternString;
import com.avaje.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;

/**
 * A join pair of local and foreign properties.
 */
public class TableJoinColumn {

    /**
     * The local database column name.
     */
    private final String localDbColumn;

    /**
     * The foreign database column name.
     */
    private final String foreignDbColumn;

    private final boolean insertable;
    
    private final boolean updateable;

    /**
     * Hash for including in a query plan
     */
    private final int queryHash;

    /**
     * Create the pair.
     */
    public TableJoinColumn(DeployTableJoinColumn deploy) {
    	this.localDbColumn = InternString.intern(deploy.getLocalDbColumn());
    	this.foreignDbColumn = InternString.intern(deploy.getForeignDbColumn());
    	this.insertable = deploy.isInsertable();
    	this.updateable = deploy.isUpdateable();
      this.queryHash = hashOf(localDbColumn) * 31 + hashOf(foreignDbColumn);
    }

    private int hashOf(String value) {
        return (value == null) ? 0 : value.hashCode();
    }

    public String toString() {
        return localDbColumn+" = "+foreignDbColumn;
    }

    /**
     * Return a hash for including in a query plan.
     */
    public int queryHash() {
      return queryHash;
    }

    /**
     * Return the foreign database column name.
     */
    public String getForeignDbColumn() {
        return foreignDbColumn;
    }

    /**
     * Return the local database column name.
     */
    public String getLocalDbColumn() {
        return localDbColumn;
    }

	/**
	 * Return true if this column should be insertable.
	 */
	public boolean isInsertable() {
		return insertable;
	}

	/**
	 * Return true if this column should be updateable.
	 */
	public boolean isUpdateable() {
		return updateable;
	}
}
