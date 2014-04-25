package com.avaje.ebeaninternal.server.deploy.id;

import java.sql.SQLException;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssoc;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;
import com.avaje.ebeaninternal.server.deploy.IntersectionRow;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableRequest;

/**
 * Imported concatenated id that is not embedded.
 */
public class ImportedIdMultiple implements ImportedId {
	
	final BeanPropertyAssoc<?> owner;

	final ImportedIdSimple[] imported;
	
	public ImportedIdMultiple(BeanPropertyAssoc<?> owner, ImportedIdSimple[] imported) {
		this.owner = owner;
		this.imported = imported;
	}

	public void addFkeys(String name) {

		// not supporting addFkeys for ImportedIdMultiple
	}
	
	public String getLogicalName() {
		return null;
	}
	
	public boolean isScalar(){
		return false;
	}
	
	public String getDbColumn(){
		return null;
	}


	public void sqlAppend(DbSqlContext ctx) {
		for (int i = 0; i < imported.length; i++) {
			ctx.appendColumn(imported[i].localDbColumn);			
		}
	}
	
	public void dmlAppend(GenerateDmlRequest request) {
		
		for (int i = 0; i < imported.length; i++) {
			request.appendColumn(imported[i].localDbColumn);	
		}
	}

	public void dmlWhere(GenerateDmlRequest request, EntityBean bean){
		if (bean == null){
			for (int i = 0; i < imported.length; i++) {
				request.appendColumnIsNull(imported[i].localDbColumn);	
			}			
		} else {
			for (int i = 0; i < imported.length; i++) {
				Object value = imported[i].foreignProperty.getValue(bean);
				if (value == null){
					request.appendColumnIsNull(imported[i].localDbColumn);
				} else {
					request.appendColumn(imported[i].localDbColumn);
				}
			}
		}
	}
	
	public Object bind(BindableRequest request, EntityBean bean) throws SQLException {
		
		for (int i = 0; i < imported.length; i++) {
		    if (imported[i].owner.isUpdateable()) {
    			Object scalarValue = imported[i].foreignProperty.getValue(bean);
    			request.bind(scalarValue, imported[i].foreignProperty, imported[i].localDbColumn);
		    }
		}
		// hmmm, not worrying about this just yet
		return null;
	}
	
	public void buildImport(IntersectionRow row, EntityBean other){
				
		for (int i = 0; i < imported.length; i++) {
			Object scalarValue = imported[i].foreignProperty.getValue(other);
			row.put(imported[i].localDbColumn, scalarValue);
		}		
	}
	
	/**
	 * Not supported for concatenated id.
	 */
	public BeanProperty findMatchImport(String matchDbColumn) {
		
		BeanProperty p = null;
		for (int i = 0; i < imported.length; i++) {
			p = imported[i].findMatchImport(matchDbColumn);
			if (p != null){
				return p;
			}
		}
		
		return p;
	}
}
