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
package com.avaje.ebeaninternal.server.persist.dml;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import javax.persistence.OptimisticLockException;

import com.avaje.ebeaninternal.api.DerivedRelationshipData;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.SpiUpdatePlan;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.type.DataBind;

/**
 * Update bean handler.
 */
public class UpdateHandler extends DmlHandler {


	private final UpdateMeta meta;

	private Set<String> updatedProperties;
	
	private boolean emptySetClause;
	
	public UpdateHandler(PersistRequestBean<?> persist, UpdateMeta meta) {
		super(persist, meta.isEmptyStringAsNull());
		this.meta = meta;
	}
	
	/**
	 * Generate and bind the update statement.
	 */
	public void bind() throws SQLException {

		SpiUpdatePlan updatePlan = meta.getUpdatePlan(persistRequest);

		if (updatePlan.isEmptySetClause()) {
		    emptySetClause = true;
		    return;
		} 
		
		updatedProperties = updatePlan.getProperties();

		sql  = updatePlan.getSql();
		
		SpiTransaction t = persistRequest.getTransaction();
		boolean isBatch = t.isBatchThisRequest();

		PreparedStatement pstmt;
		if (isBatch) {
			pstmt = getPstmt(t, sql, persistRequest, false);

		} else {
			logSql(sql);
			pstmt = getPstmt(t, sql, false);
		}
		dataBind = new DataBind(pstmt);

		bindLogAppend("Binding Update [");
		bindLogAppend(meta.getTableName());
		bindLogAppend("] ");
		
		meta.bind(persistRequest, this, updatePlan);
		
		setUpdateGenValues();
		
		bindLogAppend("]");
		logBinding();
	}

	@Override
    public void addBatch() throws SQLException {
	    if (!emptySetClause){
	        super.addBatch();
	    }
    }

    /**
	 * Execute the update in non-batch.
	 */
    @Override	
	public void execute() throws SQLException, OptimisticLockException {
	    if (!emptySetClause){	    
    		int rowCount = dataBind.executeUpdate();
    		checkRowCount(rowCount);
    		setAdditionalProperties();
	    }
	}

	@Override
	public boolean isIncluded(BeanProperty prop) {

		return prop.isDbUpdatable() && (updatedProperties == null || updatedProperties.contains(prop.getName()));
	}

    public void registerDerivedRelationship(DerivedRelationshipData derivedRelationship) {
	    persistRequest.getTransaction().registerDerivedRelationship(derivedRelationship);
    }
	
}
