package com.avaje.ebeaninternal.server.deploy.id;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.persistence.PersistenceException;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.core.InternString;
import com.avaje.ebeaninternal.server.deploy.BeanFkeyProperty;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssoc;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;
import com.avaje.ebeaninternal.server.deploy.IntersectionRow;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableRequest;

/**
 * Single scalar imported id.
 */
public final class ImportedIdSimple implements ImportedId, Comparable<ImportedIdSimple> {

    /**
     * Helper class to sort ImportedIdSimple. 
     */
    private final static class EntryComparator implements Comparator<ImportedIdSimple> {
        public int compare(ImportedIdSimple o1, ImportedIdSimple o2) {
            return o1.compareTo(o2);
        }
    }
    
    private static final EntryComparator COMPARATOR = new EntryComparator();    
    
	protected final BeanPropertyAssoc<?> owner;

	protected final String localDbColumn;

	protected final String logicalName;

	protected final BeanProperty foreignProperty;
	
	protected final int position;

	public ImportedIdSimple(BeanPropertyAssoc<?> owner, String localDbColumn, BeanProperty foreignProperty, int position) {
		this.owner = owner;
		this.localDbColumn = InternString.intern(localDbColumn);
		this.foreignProperty = foreignProperty;
		this.position = position;
		this.logicalName = InternString.intern(owner.getName()+"."+foreignProperty.getName());
	}

    /**
     * Return the list as an array sorted into the same order as the Bean Properties.
     */
    public static ImportedIdSimple[] sort(List<ImportedIdSimple> list) {
        
        ImportedIdSimple[] importedIds = (ImportedIdSimple[]) list.toArray(new ImportedIdSimple[list.size()]);
    
        // sort into the same order as the BeanProperties
        Arrays.sort(importedIds, COMPARATOR);
        return importedIds;
    }
    
	@Override
    public boolean equals(Object obj) {
        // remove FindBugs warning
        return obj == this;
    }

    public int compareTo(ImportedIdSimple other) {	    	    
        return (position < other.position ? -1 : (position == other.position ? 0 : 1));
    }

    public void addFkeys(String name) {
		BeanFkeyProperty fkey = new BeanFkeyProperty(null, name+"."+foreignProperty.getName(), localDbColumn, owner.getDeployOrder());
		owner.getBeanDescriptor().add(fkey);
	}

	public boolean isScalar(){
		return true;
	}

	public String getLogicalName() {
		return logicalName;
	}

	public String getDbColumn(){
		return localDbColumn;
	}
	
  private Object getIdValue(EntityBean bean) {
    return foreignProperty.getValue(bean);
  }

	public void buildImport(IntersectionRow row, EntityBean other){

	    Object value = getIdValue(other);
		if (value == null){
			String msg = "Foreign Key value null?";
			throw new PersistenceException(msg);
		}

		row.put(localDbColumn, value);
	}

	public void sqlAppend(DbSqlContext ctx) {
		ctx.appendColumn(localDbColumn);
	}


	public void dmlAppend(GenerateDmlRequest request) {
		request.appendColumn(localDbColumn);
	}

	public void dmlWhere(GenerateDmlRequest request, EntityBean bean){

	    if (owner.isDbUpdatable()){
    		Object value = null;
    		if (bean != null){
    			value = getIdValue(bean);
    		}
    		if (value == null){
    			request.appendColumnIsNull(localDbColumn);
    		} else {
    			request.appendColumn(localDbColumn);
    		}
	    }
	}

	public Object bind(BindableRequest request, EntityBean bean) throws SQLException {

		Object value = null;
		if (bean != null){
			value = getIdValue(bean);
		}
		request.bind(value, foreignProperty, localDbColumn);
		return value;
	}

	public BeanProperty findMatchImport(String matchDbColumn) {

		if (matchDbColumn.equals(localDbColumn)) {
			return foreignProperty;
		}
		return null;
	}
}
