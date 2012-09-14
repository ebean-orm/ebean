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
     * Create the pair.
     */
    public TableJoinColumn(DeployTableJoinColumn deploy) {
    	this.localDbColumn = InternString.intern(deploy.getLocalDbColumn());
    	this.foreignDbColumn = InternString.intern(deploy.getForeignDbColumn());
    	this.insertable = deploy.isInsertable();
    	this.updateable = deploy.isUpdateable();
    }
    
    public String toString() {
        return localDbColumn+" = "+foreignDbColumn;
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
