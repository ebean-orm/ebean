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
package com.avaje.ebeaninternal.server.expression;

import java.io.Serializable;

/**
 * This is the path prefix for filterMany.
 * <p>
 * The actual path can change due to FetchConfig query joins that proceed
 * the query that includes the filterMany.
 * </p>
 * 
 * @author rbygrave
 */
public class FilterExprPath implements Serializable {

    private static final long serialVersionUID = -6420905565372842018L;
    
    /**
     * The path of the filterMany.
     */
    private String path;
    
    public FilterExprPath(String path){
        this.path = path;
    }
    
    /**
     * Trim off leading part of the path due to a 
     * proceeding (earlier) query join etc.
     */
    public void trimPath(int prefixTrim) {
        path = path.substring(prefixTrim);
    }

    /**
     * Return the path. This is a prefix used in the filterMany expressions.
     */
    public String getPath() {
        return path;
    }

}
