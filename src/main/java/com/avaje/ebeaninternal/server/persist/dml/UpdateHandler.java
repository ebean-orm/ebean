package com.avaje.ebeaninternal.server.persist.dml;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.persistence.OptimisticLockException;

import com.avaje.ebeaninternal.api.DerivedRelationshipData;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.SpiUpdatePlan;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.type.DataBind;

/**
 * Update bean handler.
 */
public class UpdateHandler extends DmlHandler {

	private final UpdateMeta meta;
	
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

		sql  = updatePlan.getSql();
		
		SpiTransaction t = persistRequest.getTransaction();

		PreparedStatement pstmt;
		if (persistRequest.isBatched()) {
			pstmt = getPstmt(t, sql, persistRequest, false);
		} else {
			pstmt = getPstmt(t, sql, false);
		}
		dataBind = new DataBind(pstmt);
		
		meta.bind(persistRequest, this, updatePlan);
		
		setUpdateGenValues();
		
		logSql(sql);
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
	    }
	}

  public void registerDerivedRelationship(DerivedRelationshipData derivedRelationship) {
    persistRequest.getTransaction().registerDerivedRelationship(derivedRelationship);
  }
	
}
