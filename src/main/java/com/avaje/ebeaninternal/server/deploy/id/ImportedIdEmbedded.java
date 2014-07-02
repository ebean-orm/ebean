package com.avaje.ebeaninternal.server.deploy.id;

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanFkeyProperty;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssoc;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;
import com.avaje.ebeaninternal.server.deploy.IntersectionRow;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableRequest;

/**
 * Imported Embedded id.
 */
public class ImportedIdEmbedded implements ImportedId {
	
	private final BeanPropertyAssoc<?> owner;

	private final BeanPropertyAssocOne<?> foreignAssocOne;

	private final ImportedIdSimple[] imported;
	
	public ImportedIdEmbedded(BeanPropertyAssoc<?> owner, BeanPropertyAssocOne<?> foreignAssocOne, ImportedIdSimple[] imported) {
		this.owner = owner;
		this.foreignAssocOne = foreignAssocOne;
		this.imported = imported;
	}
	
	public void addFkeys(String name) {
		
		BeanProperty[] embeddedProps = foreignAssocOne.getProperties();
		
		for (int i = 0; i < imported.length; i++) {
			String n = name+"."+foreignAssocOne.getName()+"."+embeddedProps[i].getName();
			BeanFkeyProperty fkey = new BeanFkeyProperty(null, n, imported[i].localDbColumn, foreignAssocOne.getDeployOrder());
			owner.getBeanDescriptor().add(fkey);
		}
	}
	
	public boolean isScalar(){
		return false;
	}
	
	public String getLogicalName() {
		return owner.getName()+"."+foreignAssocOne.getName();
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

		Object embeddedId = null;
		if (bean != null) {
			embeddedId = foreignAssocOne.getValue(bean);
		}
		
		if (embeddedId == null){
			for (int i = 0; i < imported.length; i++) {
                if (imported[i].owner.isDbUpdatable()) {
                    request.appendColumnIsNull(imported[i].localDbColumn);
                }
			}
		} else {
		  EntityBean embedded = (EntityBean)embeddedId;
			for (int i = 0; i < imported.length; i++) {
			    if (imported[i].owner.isDbUpdatable()) {
    				Object value = imported[i].foreignProperty.getValue(embedded);
    				if (value == null){
    					request.appendColumnIsNull(imported[i].localDbColumn);	
    				} else {
    					request.appendColumn(imported[i].localDbColumn);	
    				}
			    }
			}
		}
	}
	
	public Object bind(BindableRequest request, EntityBean bean) throws SQLException {

        Object embeddedId = null;

        if (bean != null) {
            embeddedId = foreignAssocOne.getValue(bean);
        }
		
		if (embeddedId == null){
			for (int i = 0; i < imported.length; i++) {
			    if (imported[i].owner.isUpdateable()) {
			        request.bind(null, imported[i].foreignProperty, imported[i].localDbColumn);
			    }
			}
			
		} else {
		  EntityBean embedded = (EntityBean)embeddedId;
			for (int i = 0; i < imported.length; i++) {
			    if (imported[i].owner.isUpdateable()) {
    				Object scalarValue = imported[i].foreignProperty.getValue(embedded);
    				request.bind(scalarValue, imported[i].foreignProperty, imported[i].localDbColumn);
			    }
			}
		}
		// hmmm, not worrying about this just yet
		return null;
	}

	public void buildImport(IntersectionRow row, EntityBean other){
		
		EntityBean embeddedId = (EntityBean)foreignAssocOne.getValue(other);
		if (embeddedId == null){
			String msg = "Foreign Key value null?";
			throw new PersistenceException(msg);
		}
		
		for (int i = 0; i < imported.length; i++) {
			Object scalarValue = imported[i].foreignProperty.getValue(embeddedId);
			row.put(imported[i].localDbColumn, scalarValue);
		}
				
	}
	
	/**
	 * Not supported for embedded id.
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
