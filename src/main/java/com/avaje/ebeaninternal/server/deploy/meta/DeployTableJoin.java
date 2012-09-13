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
package com.avaje.ebeaninternal.server.deploy.meta;

import java.util.ArrayList;

import javax.persistence.JoinColumn;

import com.avaje.ebeaninternal.server.core.Message;
import com.avaje.ebeaninternal.server.deploy.BeanCascadeInfo;
import com.avaje.ebeaninternal.server.deploy.BeanTable;
import com.avaje.ebeaninternal.server.deploy.TableJoin;

/**
 * Represents a join to another table during deployment phase.
 * <p>
 * This gets converted into a immutable TableJoin when complete.
 * </p>
 */
public class DeployTableJoin {

    /**
     * Flag set when the imported key maps to the primary key.
     * This occurs for intersection tables (ManyToMany).
     */
    private boolean importedPrimaryKey;
    
    /**
     * The joined table.
     */
    private String table;
    
    /**
     * The type of join. LEFT OUTER etc.
     */
    private String type = TableJoin.JOIN;

    /**
     * The list of properties mapped to this joined table.
     */
    private ArrayList<DeployBeanProperty> properties = new ArrayList<DeployBeanProperty>();

    /**
     * The list of join column pairs. Used to generate the on clause.
     */
    private ArrayList<DeployTableJoinColumn> columns = new ArrayList<DeployTableJoinColumn>();

    /**
     * The persist cascade info.
     */
    private BeanCascadeInfo cascadeInfo = new BeanCascadeInfo();
    

    /**
     * Create a DeployTableJoin.
     */
    public DeployTableJoin() {
    }
    
    public String toString() {
        return type + " " + table + " " + columns;
    }

    /**
     * Return true if the imported foreign key maps to the primary key.
     */
    public boolean isImportedPrimaryKey() {
		return importedPrimaryKey;
	}

    /**
     * Flag set when the imported key maps to the primary key.
     * This occurs for intersection tables (ManyToMany).
     */
	public void setImportedPrimaryKey(boolean importedPrimaryKey) {
		this.importedPrimaryKey = importedPrimaryKey;
	}

	/**
     * Return true if the JoinOnPair have been set.
     */
    public boolean hasJoinColumns() {
        return columns.size() > 0;
    }

    /**
     * Return the persist info.
     */
    public BeanCascadeInfo getCascadeInfo() {
        return cascadeInfo;
    }
    

    /**
     * Copy all the columns to this join potentially reversing the columns.
     */
	public void setColumns(DeployTableJoinColumn[] cols, boolean reverse) {
		columns = new ArrayList<DeployTableJoinColumn>();
		for (int i = 0; i < cols.length; i++) {
			addJoinColumn(cols[i].copy(reverse));
		}
	}
	
    /**
     * Add a join pair
     */
    public void addJoinColumn(DeployTableJoinColumn pair) {
        columns.add(pair);
    }

    /**
     * Add a JoinColumn
     * <p>
     * The order is generally true for OneToMany and false for ManyToOne relationships.
     * </p>
     */
    public void addJoinColumn(boolean order, JoinColumn jc, BeanTable beanTable) {
		if (!"".equals(jc.table())) {
			setTable(jc.table());
		}
    	addJoinColumn(new DeployTableJoinColumn(order, jc, beanTable));
    }
    
    /**
     * Add a JoinColumn array.
     */
    public void addJoinColumn(boolean order, JoinColumn[] jcArray, BeanTable beanTable) {
    	for (int i = 0; i < jcArray.length; i++) {
    		addJoinColumn(order, jcArray[i], beanTable);
		}
    }
    
    /**
     * Return the join columns.
     */
    public DeployTableJoinColumn[] columns() {
    	return (DeployTableJoinColumn[])columns.toArray(new DeployTableJoinColumn[columns.size()]);
    }

    
    /**
     * For secondary table joins returns the properties mapped to that table.
     */
    public DeployBeanProperty[] properties() {
    	return (DeployBeanProperty[])properties.toArray(new DeployBeanProperty[properties.size()]);
    }

    /**
     * Return the joined table name.
     */
    public String getTable() {
        return table;
    }

    /**
     * set the joined table name.
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * Return the type of join. LEFT OUTER JOIN etc.
     */
    public String getType() {
        return type;
    }

    /**
     * Return true if this join is a left outer join.
     */
    public boolean isOuterJoin() {
        return type.equals(TableJoin.LEFT_OUTER);
    }
    
    /**
     * Set the type of join.
     */
    public void setType(String joinType) {
        joinType = joinType.toUpperCase();
        if (joinType.equalsIgnoreCase(TableJoin.JOIN)) {
            type = TableJoin.JOIN;
        } else if (joinType.indexOf("LEFT") > -1) {
            type = TableJoin.LEFT_OUTER;
        } else if (joinType.indexOf("OUTER") > -1) {
            type = TableJoin.LEFT_OUTER;
        } else if (joinType.indexOf("INNER") > -1) {
            type = TableJoin.JOIN;
        } else {
            throw new RuntimeException(Message.msg("join.type.unknown", joinType));
        }
    }
    
    public DeployTableJoin createInverse(String tableName) {
    	
    	DeployTableJoin inverse = new DeployTableJoin();

        return copyTo(inverse, true, tableName);

    }
    
    public DeployTableJoin copyTo(DeployTableJoin destJoin, boolean reverse, String tableName) {
    	
    	destJoin.setTable(tableName);
    	destJoin.setType(type);
    	destJoin.setColumns(columns(), reverse);
    	
    	return destJoin;
    }
}
