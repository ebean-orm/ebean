/**
 * Copyright (C) 2009 Authors
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
package com.avaje.ebeaninternal.server.persist.dml;

import java.sql.SQLException;
import java.util.Set;

import com.avaje.ebeaninternal.api.SpiUpdatePlan;
import com.avaje.ebeaninternal.server.core.ConcurrencyMode;
import com.avaje.ebeaninternal.server.persist.dmlbind.Bindable;

/**
 * Cachable plan for executing bean updates for a given set of changed
 * properties.
 * 
 * @author rbygrave
 */
public class UpdatePlan implements SpiUpdatePlan {

    /**
     * Special plan used when there is nothing in the set clause and the update
     * should in fact be skipped. Occurs when the updated properties have
     * updatable=false in their deployment.
     */
    public static final UpdatePlan EMPTY_SET_CLAUSE = new UpdatePlan();
    
	private final Integer key;

	private final ConcurrencyMode mode;

	private final String sql;

	private final Bindable set;

	private final Set<String> properties;

	private final boolean checkIncludes;

	private final long timeCreated;

	private final boolean emptySetClause;
	
	private Long timeLastUsed;

	/**
	 * Create a non cachable UpdatePlan.
	 */
	public UpdatePlan(ConcurrencyMode mode, String sql, Bindable set) {

		this(null, mode, sql, set, null);
	}

	/**
	 * Create a cachable UpdatePlan with a given key.
	 */
	public UpdatePlan(Integer key, ConcurrencyMode mode, String sql,
			Bindable set, Set<String> properties) {

	    this.emptySetClause = false;
		this.key = key;
		this.mode = mode;
		this.sql = sql;
		this.set = set;
		this.properties = properties;
		this.checkIncludes = properties != null;
		this.timeCreated = System.currentTimeMillis();
	}

	/**
	 * Special constructor for emptySetClause=true instance.
	 */
	private UpdatePlan(){
	    this.emptySetClause = true;
	    this.key = Integer.valueOf(0);
	    this.mode = ConcurrencyMode.NONE;
	    this.sql = null;
	    this.set = null;
	    this.properties = null;
	    this.checkIncludes = false;
	    this.timeCreated = 0;
	}
	
	
	public boolean isEmptySetClause() {
        return emptySetClause;
    }

    /**
	 * Run the prepared statement binding for the 'update set' properties.
	 */
	public void bindSet(DmlHandler bind, Object bean) throws SQLException {

		set.dmlBind(bind, checkIncludes, bean);

		// not strictly 'thread safe' but object assignment is atomic
		Long touched = Long.valueOf(System.currentTimeMillis());
		this.timeLastUsed = touched;
	}

	/**
	 * Return the time this plan was created.
	 */
	public long getTimeCreated() {
		return timeCreated;
	}

	/**
	 * Return the time this plan was last used.
	 */
	public Long getTimeLastUsed() {

		// not thread safe but atomic
		return timeLastUsed;
	}

	/**
	 * Return the hash key.
	 */
	public Integer getKey() {
		return key;
	}

	/**
	 * Return the concurrency mode for this plan.
	 */
	public ConcurrencyMode getMode() {
		return mode;
	}

	/**
	 * Return the DML statement.
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * Return the Bindable properties for the update set.
	 */
	public Bindable getSet() {
		return set;
	}

	/**
	 * Return the set of changed properties.
	 * <p>
	 * This can return null when all properties in the set are being bound in
	 * the update statement.
	 * </p>
	 */
	public Set<String> getProperties() {
		return properties;
	}

}
