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

import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;
import com.avaje.ebeaninternal.server.persist.dml.DmlMode;

/**
 * Creates the appropriate Bindable for a BeanProperty.
 * <p>
 * Lob properties can be excluded and it creates BindablePropertyInsertGenerated
 * and BindablePropertyUpdateGenerated as required.
 * </p>
 */
public class FactoryProperty {

    private final boolean bindEncryptDataFirst;
    
	public FactoryProperty(boolean bindEncryptDataFirst) {
	    this.bindEncryptDataFirst = bindEncryptDataFirst;
	}

	/**
	 * Create a Bindable for the property given the mode and withLobs flag.
	 */
	public Bindable create(BeanProperty prop, DmlMode mode, boolean withLobs) {

		if (DmlMode.INSERT.equals(mode) && !prop.isDbInsertable()){
			return null;
		}
		if (DmlMode.UPDATE.equals(mode) && !prop.isDbUpdatable()){
			return null;
		}
		
		if (prop.isLob()) {
			if (DmlMode.WHERE.equals(mode) || !withLobs) {
				// Lob exclusion
				return null;
			} else {
			    return prop.isDbEncrypted() ? new BindableEncryptedProperty(prop, bindEncryptDataFirst) : new BindableProperty(prop);
			}
		}

		GeneratedProperty gen = prop.getGeneratedProperty();
		if (gen != null) {
			if (DmlMode.INSERT.equals(mode)) {
				if (gen.includeInInsert()) {
					return new BindablePropertyInsertGenerated(prop, gen);					
				} else {
					return null;
				}

			}
			if (DmlMode.UPDATE.equals(mode)) {
				if (gen.includeInUpdate()) {
					return new BindablePropertyUpdateGenerated(prop, gen);
				} else {
					// An 'Insert Timestamp' is never updated 
					return null;
				}
			}
		}

        return prop.isDbEncrypted() ? new BindableEncryptedProperty(prop, bindEncryptDataFirst) : new BindableProperty(prop);
	}
}
