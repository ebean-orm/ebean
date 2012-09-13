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
package com.avaje.ebeaninternal.server.persist;

import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.core.PersistRequestCallableSql;
import com.avaje.ebeaninternal.server.core.PersistRequestOrmUpdate;
import com.avaje.ebeaninternal.server.core.PersistRequestUpdateSql;
import com.avaje.ebeaninternal.server.core.PstmtBatch;
import com.avaje.ebeaninternal.server.deploy.BeanManager;

/**
 * Default PersistExecute implementation using DML statements.
 * <p>
 * Supports the use of PreparedStatement batching.
 * </p>
 */
public final class DefaultPersistExecute implements PersistExecute {
      
    private final ExeCallableSql exeCallableSql;
    
    private final ExeUpdateSql exeUpdateSql;
    
    private final ExeOrmUpdate exeOrmUpdate;
    
	/**
	 * The default batch size.
	 */
	private final int defaultBatchSize;

	/**
	 * Default for whether to call getGeneratedKeys after batch insert.
	 */
	private final boolean defaultBatchGenKeys;
	
	private final boolean validate;
	
    /**
     * Construct this DmlPersistExecute.
     */
    public DefaultPersistExecute(boolean validate, Binder binder, PstmtBatch pstmtBatch) {
    
    	this.validate = validate;
        this.exeOrmUpdate = new ExeOrmUpdate(binder, pstmtBatch);
        this.exeUpdateSql = new ExeUpdateSql(binder, pstmtBatch);
        this.exeCallableSql = new ExeCallableSql(binder, pstmtBatch);
        
		this.defaultBatchGenKeys = GlobalProperties.getBoolean("batch.getgeneratedkeys", true);
		this.defaultBatchSize = GlobalProperties.getInt("batch.size", 20);
    }

	public BatchControl createBatchControl(SpiTransaction t) {

		// create a BatchControl and set its defaults
		return new BatchControl(t, defaultBatchSize, defaultBatchGenKeys);
	}
	
    /**
     * execute the bean insert request.
     */
    public <T> void executeInsertBean(PersistRequestBean<T> request) {
    	
    	BeanManager<T> mgr = request.getBeanManager();
    	BeanPersister persister = mgr.getBeanPersister();
    	
    	BeanPersistController controller = request.getBeanController();
		if (controller == null || controller.preInsert(request)) {
			if (validate){
				request.validate();
			}
			persister.insert(request);
			// NOTE: the persister fires the postInsert so that this 
			// occurs before ebeanIntercept.setLoaded(true)
		} 
    }
    
    /**
     * execute the bean update request.
     */
    public <T> void executeUpdateBean(PersistRequestBean<T> request) {
    	
    	BeanManager<T> mgr = request.getBeanManager();
    	BeanPersister persister = mgr.getBeanPersister();
    	
    	BeanPersistController controller = request.getBeanController();
		if (controller == null || controller.preUpdate(request)) {
			if (validate){
				request.validate();
			}
			persister.update(request);
			// NOTE: the persister fires the postUpdate so that this 
			// occurs before ebeanIntercept.setLoaded(true)
		} 
    }

    
    /**
     * execute the bean delete request.
     */
    public <T> void executeDeleteBean(PersistRequestBean<T> request) {

    	BeanManager<T> mgr = request.getBeanManager();
    	BeanPersister persister = mgr.getBeanPersister();
    	
    	BeanPersistController controller = request.getBeanController();
		if (controller == null || controller.preDelete(request)) {
			
			persister.delete(request);
			// NOTE: the persister fires the postDelete 
		} 
    }

    /**
     * Execute the updateSqlRequest 
	 */
    public int executeOrmUpdate(PersistRequestOrmUpdate request) {
    	return exeOrmUpdate.execute(request);
    }
    
    /**
     * Execute the updateSqlRequest 
	 */
    public int executeSqlUpdate(PersistRequestUpdateSql request) {
    	return exeUpdateSql.execute(request);
    }
    
    /**
     * Execute the CallableSqlRequest.
	 */
    public int executeSqlCallable(PersistRequestCallableSql request) {
    	return exeCallableSql.execute(request);
    }

}
