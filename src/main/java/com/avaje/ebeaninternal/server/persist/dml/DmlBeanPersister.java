package com.avaje.ebeaninternal.server.persist.dml;

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.ebeaninternal.server.persist.BeanPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(DmlBeanPersister.class);

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
	private void execute(PersistRequestBean<?> request, PersistHandler handler) {

    boolean batched = request.isBatched();
		try {
			handler.bind();
			if (batched) {
				handler.addBatch();
			} else {
				handler.execute();
			}

		} catch (SQLException e) {
      // log the error to the transaction log
      String errMsg = StringHelper.replaceStringMulti(e.getMessage(), new String[]{"\r","\n"}, "\\n ");
      String msg = "ERROR executing DML bindLog["+handler.getBindLog()+"] error["+errMsg+"]";
      if (request.getTransaction().isLogSummary()) {
        request.getTransaction().logSummary(msg);
      }
			throw new PersistenceException(msg, e);

		} finally {
			if (!batched && handler != null) {
				try {
					handler.close();
				} catch (SQLException e) {
					logger.error(null, e);
				}
			}
		}
	}

}
