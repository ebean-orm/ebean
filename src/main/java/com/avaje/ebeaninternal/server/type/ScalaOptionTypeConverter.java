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
package com.avaje.ebeaninternal.server.type;

import scala.Option;

import com.avaje.ebean.config.ScalarTypeConverter;

/**
 * A type converter to support scala.Option.
 * 
 * @author rbygrave
 *
 * @param <S> the underlying type
 */
public class ScalaOptionTypeConverter<S> implements ScalarTypeConverter<scala.Option<S>, S>{
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Option<S> getNullValue() {
        return (scala.Option)scala.None$.MODULE$;
    }

    public S unwrapValue(Option<S> beanType) {
        
        if (beanType.isEmpty()){
            return null;
        } else {
            return beanType.get();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Option<S> wrapValue(S scalarType) {
        if (scalarType == null){
            return (scala.Option)scala.None$.MODULE$;
        }
        if (scalarType instanceof scala.Some){
            return (Option<S>)scalarType;
        }
        return new scala.Some<S>(scalarType);
    }

    
}
