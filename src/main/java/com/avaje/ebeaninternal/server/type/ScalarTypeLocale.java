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

import java.util.Locale;

/**
 * ScalarType for java.util.Currency which converts to and from a VARCHAR
 * database column.
 */
public class ScalarTypeLocale extends ScalarTypeBaseVarchar<Locale> {

    public ScalarTypeLocale() {
        super(Locale.class);
    }

    @Override
    public int getLength() {
        return 20;
    }
    
    @Override
    public Locale convertFromDbString(String dbValue) {
        return parse(dbValue);
    }

    @Override
    public String convertToDbString(Locale beanValue) {
        return ((Locale) beanValue).toString();
    }

    public String formatValue(Locale t) {
        return t.toString();
    }

    public Locale parse(String value) {

        int pos1 = -1;
        int pos2 = -1;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '_') {
                if (pos1 > -1) {
                    pos2 = i;
                    break;
                } else {
                    pos1 = i;
                }
            }
        }
        if (pos1 == -1) {
            return new Locale(value);
        }
        String language = value.substring(0, pos1);
        if (pos2 == -1) {
            String country = value.substring(pos1 + 1);
            return new Locale(language, country);
        } else {
            String country = value.substring(pos1 + 1, pos2);
            String variant = value.substring(pos2 + 1);
            return new Locale(language, country, variant);
        }
    }

}
