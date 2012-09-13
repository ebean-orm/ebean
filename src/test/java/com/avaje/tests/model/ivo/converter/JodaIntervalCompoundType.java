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
package com.avaje.tests.model.ivo.converter;

import org.joda.time.Interval;

import com.avaje.ebean.config.CompoundType;
import com.avaje.ebean.config.CompoundTypeProperty;

public class JodaIntervalCompoundType implements CompoundType<Interval>{

    public Interval create(Object[] propertyValues) {
        return new Interval((Long)propertyValues[0], (Long)propertyValues[1]);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CompoundTypeProperty<Interval, ?>[] getProperties() {
        CompoundTypeProperty[] props = {new Start(), new End()};
        return props;
    }
    
    static class Start implements CompoundTypeProperty<Interval, Long> {

        public String getName() {
            return "startMillis";
        }

        public Long getValue(Interval valueObject) {
            return valueObject.getStartMillis();
        }

        public int getDbType() {
            return java.sql.Types.TIMESTAMP;
        }
    }

    static class End implements CompoundTypeProperty<Interval, Long> {

        public String getName() {
            return "endMillis";
        }

        public Long getValue(Interval valueObject) {
            return valueObject.getEndMillis();
        }
        
        public int getDbType() {
            return java.sql.Types.TIMESTAMP;
        }

    }

}
