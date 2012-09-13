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
package com.avaje.ebeaninternal.server.persist.dmlbind;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;

/**
 * Adds support for id creation for concatenated ids on intersection tables.
 * <p>
 * Specifically if the concatenated id object is null on insert this can be
 * built from the matching ManyToOne associated beans. For example RoleUserId
 * embeddedId object could be built from the associated Role and User beans.
 * </p>
 * <p>
 * This is only attempted if the id is null when it gets to the insert.
 * </p>
 */
public interface BindableId extends Bindable {

  /**
   * Return true if there is no Id properties at all.
   */
  public boolean isEmpty();
  
	/**
	 * Return true if this is a concatenated key.
	 */
	public boolean isConcatenated();
	
	/**
	 * Return the DB Column to use with genGeneratedKeys.
	 */
	public String getIdentityColumn();
	
	/**
	 * Create the concatenated id for inserts with PFK relationships.
	 * <p>
	 * Really only where there are ManyToOne assoc beans that make up the
	 * primary key and the values can be got from those.
	 * </p>
	 */
	public boolean deriveConcatenatedId(PersistRequestBean<?> persist);

}
