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
package com.avaje.ebeaninternal.server.type;

import java.util.Currency;

/**
 * ScalarType for java.util.Currency which converts to and from a VARCHAR database column.
 */
public class ScalarTypeCurrency extends ScalarTypeBaseVarchar<Currency> {

	public ScalarTypeCurrency() {
		super(Currency.class);
	}
	
	@Override
    public int getLength() {
        return 3;
    }

    @Override
    public Currency convertFromDbString(String dbValue) {
	    return Currency.getInstance(dbValue);
    }

    @Override
    public String convertToDbString(Currency beanValue) {
        return ((Currency)beanValue).getCurrencyCode();
    }

	public String formatValue(Currency v) {
        return v.toString();
    }

    public Currency parse(String value) {
		return Currency.getInstance(value);
	}
		
}
