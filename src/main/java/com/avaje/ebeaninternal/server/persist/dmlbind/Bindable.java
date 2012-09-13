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

import java.sql.SQLException;
import java.util.List;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Item held by Meta objects used to generate and bind bean insert update and
 * delete statements.
 * <p>
 * An implementation is expected to be immutable and thread safe.
 * </p>
 * <p>
 * The design is to take a bean structure with embedded and associated objects
 * etc and flatten that into lists of Bindable objects. These are put into
 * InsertMeta UpdateMeta and DeleteMeta objects to support the generation of DML
 * and binding of statements in a fast and painless manor.
 * </p>
 */
public interface Bindable {

    /**
     * For Updates including only changed properties add the Bindable to the
     * list if it should be included in the 'update set'.
     */
    public void addChanged(PersistRequestBean<?> request, List<Bindable> list);

    /**
     * append sql to the buffer with prefix and suffix options.
     */
    public void dmlInsert(GenerateDmlRequest request, boolean checkIncludes);

    /**
     * append sql to the buffer with prefix and suffix options.
     */
    public void dmlAppend(GenerateDmlRequest request, boolean checkIncludes);

    /**
     * For WHERE clauses append sql to the buffer with prefix and suffix
     * options. These need to take into account binding of null values.
     */
    public void dmlWhere(GenerateDmlRequest request, boolean checkIncludes, Object bean);

    /**
     * Bind given the request and bean. The bean could be the oldValues bean
     * when binding a update or delete where clause with ALL concurrency mode.
     */
    public void dmlBind(BindableRequest request, boolean checkIncludes, Object bean)
            throws SQLException;

    public void dmlBindWhere(BindableRequest request, boolean checkIncludes, Object bean)
        throws SQLException;

}
