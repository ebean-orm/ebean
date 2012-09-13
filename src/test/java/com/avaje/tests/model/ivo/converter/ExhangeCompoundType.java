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

import com.avaje.ebean.config.CompoundType;
import com.avaje.ebean.config.CompoundTypeProperty;
import com.avaje.tests.model.ivo.CMoney;
import com.avaje.tests.model.ivo.ExhangeCMoneyRate;
import com.avaje.tests.model.ivo.Rate;

public class ExhangeCompoundType implements CompoundType<ExhangeCMoneyRate> {

    public ExhangeCMoneyRate create(Object[] propertyValues) {
        return new ExhangeCMoneyRate((Rate)propertyValues[0], (CMoney)propertyValues[1]);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CompoundTypeProperty<ExhangeCMoneyRate, ?>[] getProperties() {

        CompoundTypeProperty[] props = {new RateProp(), new CMoneyProp()};
        return props;
    }

    static class RateProp implements CompoundTypeProperty<ExhangeCMoneyRate, Rate> {

        public String getName() {
            return "rate";
        }

        public Rate getValue(ExhangeCMoneyRate valueObject) {
            return valueObject.getRate();
        } 
        
        public int getDbType() {
            return 0;
        }

    }
    
    static class CMoneyProp implements CompoundTypeProperty<ExhangeCMoneyRate, CMoney> {

        public String getName() {
            return "cmoney";
        }

        public CMoney getValue(ExhangeCMoneyRate valueObject) {
            return valueObject.getCmoney();
        }

        public int getDbType() {
            return 0;
        }
        
    }
    
}
