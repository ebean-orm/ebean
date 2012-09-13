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
package com.avaje.ebeaninternal.server.deploy.id;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;

/**
 * Creates the appropriate IdConvertSet depending on the type of Id property(s).
 */
public class IdBinderFactory {

	private static final IdBinderEmpty EMPTY = new IdBinderEmpty();
	
	private final boolean idInExpandedForm;
	
	public IdBinderFactory(boolean idInExpandedForm) {
	    this.idInExpandedForm = idInExpandedForm;
	}
	
	/**
	 * Create the IdConvertSet for the given type of Id properties.
	 */
	public IdBinder createIdBinder(BeanProperty[] uids) {
		
		if (uids.length == 0){
			// for report type beans that don't need an id
			return EMPTY;
			
		} else if (uids.length == 1){
			if (uids[0].isEmbedded()){
				return new IdBinderEmbedded(idInExpandedForm, (BeanPropertyAssocOne<?>)uids[0]);
			} else {
				return new IdBinderSimple(uids[0]);
			}
		
		} else {
			return new IdBinderMultiple(uids);
		}
	}
	
}
