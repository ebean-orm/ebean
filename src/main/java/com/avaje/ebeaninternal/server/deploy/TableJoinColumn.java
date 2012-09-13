/**
 * Copyright (C) 2006  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
