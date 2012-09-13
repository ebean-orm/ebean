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

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.ebeaninternal.server.persist.BeanPersister;

/**
 * Bean persister that uses the Handler and Meta objects.
 * <p>
 * The design of this is based on the immutable Meta objects. They hold a
 * information in the form of lists of Bindable objects. This effectively
 * flattens the structure of the bean with embedded and associated objects into
 * a flat list of Bindable objects.
 * </p>
 */
public final class DmlBeanPersister implements BeanPersister {

	private static final Logger logger = Logger.getLogger(DmlBeanPersister.class.getName());

	private final UpdateMeta updateMeta;

	private final InsertMeta insertMeta;

	private final DeleteMeta deleteMeta;

	
	public DmlBeanPersister(UpdateMeta updateMeta, InsertMeta insertMeta, DeleteMeta deleteMeta) {

		this.updateMeta = updateMeta;
		this.insertMeta = insertMeta;
		this.deleteMeta = deleteMeta;
	}

	/**
	 * execute the bean delete request.
	 */
	public void delete(PersistRequestBean<?> request) {

		DeleteHandler delete = new DeleteHandler(request, deleteMeta);
		execute(request, delete);
	}

	/**
	 * execute the bean insert request.
	 */
	public void insert(PersistRequestBean<?> request) {

		InsertHandler insert = new InsertHandler(request, insertMeta);
		execute(request, insert);
	}

	/**
	 * execute the bean update request.
	 */
	public void update(PersistRequestBean<?> request) {

		UpdateHandler update = new UpdateHandler(request, updateMeta);
		execute(request, update);
	}

	/**
	 * execute request taking batching into account.
	 */
	private void execute(PersistRequest request, PersistHandler handler) {

		SpiTransaction trans = request.getTransaction();
		boolean batchThisRequest = trans.isBatchThisRequest();

		try {

			handler.bind();

			if (batchThisRequest) {
				handler.addBatch();

			} else {
				// immediate insert
				handler.execute();
			}

		} catch (SQLException e) {
	        // log the error to the transaction log
	        String errMsg = StringHelper.replaceStringMulti(e.getMessage(), new String[]{"\r","\n"}, "\\n ");
	        String msg = "ERROR executing DML bindLog["+handler.getBindLog()+"] error["+errMsg+"]";
	        if (request.getTransaction().isLogSummary()) {
	        	request.getTransaction().logInternal(msg);
	        }
	        
			throw new PersistenceException(msg, e);

		} finally {
			if (!batchThisRequest && handler != null) {
				try {
					handler.close();
				} catch (SQLException e) {
					logger.log(Level.SEVERE, null, e);
				}
			}
		}
	}

}
