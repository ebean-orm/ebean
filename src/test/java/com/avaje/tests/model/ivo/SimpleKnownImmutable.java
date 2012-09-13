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
package com.avaje.tests.model.ivo;

import com.avaje.ebeaninternal.server.type.reflect.KnownImmutable;

public class SimpleKnownImmutable implements KnownImmutable {

    public boolean isKnownImmutable(Class<?> cls) {
        
        // Check for all allowed property types...
        if (cls.isPrimitive() || String.class.equals(cls) || Object.class.equals(cls)) {
            return true;
        }
        if (java.util.Date.class.equals(cls) || java.sql.Date.class.equals(cls) || java.sql.Timestamp.class.equals(cls)) {
            // treat as immutable even through they are not strictly so
            return true;
        }
        if (java.math.BigDecimal.class.equals(cls) || java.math.BigInteger.class.equals(cls)) {
            // treat as immutable (contain non-final fields)
            return true;
        }

        if (Integer.class.equals(cls) || Long.class.equals(cls) || Double.class.equals(cls) || Float.class.equals(cls)
                || Short.class.equals(cls) || Byte.class.equals(cls) || Character.class.equals(cls)
                || Boolean.class.equals(cls)) {
            return true;
        }

        return false;
    }
}
