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

import java.sql.Timestamp;

import com.avaje.ebean.config.ScalarTypeConverter;
import com.avaje.tests.model.ivo.SysTime;

public class SysTimeConverter implements ScalarTypeConverter<SysTime, Timestamp> {

    
    public SysTime getNullValue() {
        return null;
    }

    public Timestamp unwrapValue(SysTime beanType) {
        return new Timestamp(beanType.getMillis());
    }

    public SysTime wrapValue(Timestamp scalarType) {
        return new SysTime(scalarType.getTime());
    }

    
}
