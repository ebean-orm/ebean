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
package com.avaje.ebeaninternal.server.deploy;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.PersistenceException;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Expression;
import com.avaje.ebean.InvalidValue;
import com.avaje.ebean.Query;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollection.ModifyListenMode;
import com.avaje.ebean.bean.BeanCollectionAdd;
import com.avaje.ebean.bean.BeanCollectionLoader;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.DefaultSqlUpdate;
import com.avaje.ebeaninternal.server.deploy.id.ImportedId;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.el.ElPropertyChainBuilder;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.ebeaninternal.server.query.SqlBeanLoad;
import com.avaje.ebeaninternal.server.text.json.ReadJsonContext;
import com.avaje.ebeaninternal.server.text.json.ReadJsonContext.ReadBeanState;
import com.avaje.ebeaninternal.server.text.json.WriteJsonContext;

/**
 * Property mapped to a List Set or Map.
 */
public class BeanPropertyAssocMany<T> extends BeanPropertyAssoc<T> {

	/**
	 * Join for manyToMany intersection table.
	 */
	final TableJoin intersectionJoin;

	/**
	 * For ManyToMany this is the Inverse join used to build reference queries.
	 */
	final TableJoin inverseJoin;

	/**
	 * Flag to indicate that this is a unidirectional relationship.
	 */
	final boolean unidirectional;

	/**
	 * Flag to indicate manyToMany relationship.
	 */
	final boolean manyToMany;

	final String fetchOrderBy;

	final String mapKey;

	/**
	 * The type of the many, set, list or map.
	 */
	final ManyType manyType;

	final String serverName;

	final ModifyListenMode modifyListenMode;

    BeanProperty mapKeyProperty;
    /**
     * Derived list of exported property and matching foreignKey
     */
    ExportedProperty[] exportedProperties;

    /**
     * Property on the 'child' bean that links back to the 'master'.
     */
    BeanPropertyAssocOne<?> childMasterProperty;

    boolean embeddedExportedProperties;

    BeanCollectionHelp<T> help;

	ImportedId importedId;
	
	String deleteByParentIdSql;
    String deleteByParentIdInSql;

	
	final CollectionTypeConverter typeConverter;
	
	/**
	 * Create this property.
	 */
	public BeanPropertyAssocMany(BeanDescriptorMap owner, BeanDescriptor<?> descriptor, DeployBeanPropertyAssocMany<T> deploy) {
		super(owner, descriptor, deploy);
		this.unidirectional = deploy.isUnidirectional();
		this.manyToMany = deploy.isManyToMany();
		this.serverName = descriptor.getServerName();
		this.manyType = deploy.getManyType();
		this.typeConverter = manyType.getTypeConverter();
		this.mapKey = deploy.getMapKey();
		this.fetchOrderBy = deploy.getFetchOrderBy();

		this.intersectionJoin = deploy.createIntersectionTableJoin();
		this.inverseJoin = deploy.createInverseTableJoin();
		this.modifyListenMode = deploy.getModifyListenMode();
	}

	public void initialise() {
		super.initialise();

		if (!isTransient){
	        this.help = BeanCollectionHelpFactory.create(this);

			if (manyToMany){
				// only manyToMany's have imported properties
				importedId = createImportedId(this, targetDescriptor, tableJoin);

			} else {
				// find the property in the many that matches
				// back to the master (Order in the OrderDetail bean)
				childMasterProperty = initChildMasterProperty();
				if (childMasterProperty != null){
					childMasterProperty.setRelationshipProperty(this);
				}
			}

			if (mapKey != null){
				mapKeyProperty = initMapKeyProperty();
			}

			exportedProperties = createExported();
			if (exportedProperties.length > 0){
				embeddedExportedProperties = exportedProperties[0].isEmbedded();
			}
			
			String delStmt;
			if (manyToMany){
			    delStmt = "delete from "+inverseJoin.getTable()+" where ";
			} else {
			    delStmt = "delete from "+targetDescriptor.getBaseTable()+" where ";
			}
			deleteByParentIdSql = delStmt + deriveWhereParentIdSql(false);
			deleteByParentIdInSql = delStmt + deriveWhereParentIdSql(true);
		}
	}
    
	/**
	 * Get the underlying List Set or Map.
	 * For unwrapping scala collection types etc.
	 */
    public Object getValueUnderlying(Object bean) {

        Object value =  getValue(bean);
        if (typeConverter != null){
            value = typeConverter.toUnderlying(value);
        }
        return value;
    }
    
    @Override
    public Object getValue(Object bean) {
        return super.getValue(bean);
    }

    @Override
    public Object getValueIntercept(Object bean) {
        return super.getValueIntercept(bean);
    }

    @Override
    public void setValue(Object bean, Object value) {
        if (typeConverter != null){
            value = typeConverter.toWrapped(value);
        }
        super.setValue(bean, value);
    }

    @Override
    public void setValueIntercept(Object bean, Object value) {
        if (typeConverter != null){
            value = typeConverter.toWrapped(value);
        }
        super.setValueIntercept(bean, value);
    }

    public ElPropertyValue buildElPropertyValue(String propName, String remainder, ElPropertyChainBuilder chain, boolean propertyDeploy) {
        return createElPropertyValue(propName, remainder, chain, propertyDeploy);
    }
	
    public SqlUpdate deleteByParentId(Object parentId, List<Object> parentIdist) {
        if (parentId != null){
            return deleteByParentId(parentId);
        } else {
            return deleteByParentIdList(parentIdist);
        }
    }

	private SqlUpdate deleteByParentId(Object parentId) {
	    DefaultSqlUpdate sqlDelete = new DefaultSqlUpdate(deleteByParentIdSql);
	    bindWhereParendId(sqlDelete, parentId);
	    return sqlDelete;
	}
	
	/**
	 * Find the Id's of detail beans given a parent Id or list of parent Id's.
	 */
    public List<Object> findIdsByParentId(Object parentId, List<Object> parentIdist, Transaction t, ArrayList<Object> excludeDetailIds) {
        if (parentId != null){
            return findIdsByParentId(parentId, t, excludeDetailIds);
        } else {
            return findIdsByParentIdList(parentIdist, t, excludeDetailIds);
        }
    }
    
    private List<Object> findIdsByParentId(Object parentId, Transaction t, ArrayList<Object> excludeDetailIds) {
        
        String rawWhere = deriveWhereParentIdSql(false);
        
        EbeanServer server = getBeanDescriptor().getEbeanServer();
        Query<?> q = server.find(getPropertyType())
            .where().raw(rawWhere).query();
        
        bindWhereParendId(1, q, parentId);
        
        if (excludeDetailIds != null && !excludeDetailIds.isEmpty()) {
    		Expression idIn = q.getExpressionFactory().idIn(excludeDetailIds);
        	q.where().not(idIn);
        }
        
        return server.findIds(q, t);
    }
    
    private List<Object> findIdsByParentIdList(List<Object> parentIdist, Transaction t, ArrayList<Object> excludeDetailIds) {

        String rawWhere = deriveWhereParentIdSql(true);
        String inClause = targetIdBinder.getIdInValueExpr(parentIdist.size());
        
        String expr = rawWhere+inClause;
        
        EbeanServer server = getBeanDescriptor().getEbeanServer();
        Query<?> q = server.find(getPropertyType())
            .where().raw(expr).query();
       
        int pos = 1;
        for (int i = 0; i < parentIdist.size(); i++) {            
            pos = bindWhereParendId(pos, q, parentIdist.get(i));
        }
        
        if (excludeDetailIds != null && !excludeDetailIds.isEmpty()) {
    		Expression idIn = q.getExpressionFactory().idIn(excludeDetailIds);
        	q.where().not(idIn);
        }
        
        return server.findIds(q, t);
    }
	
	private SqlUpdate deleteByParentIdList(List<Object> parentIdist) {

        StringBuilder sb = new StringBuilder(100);
        sb.append(deleteByParentIdInSql);

        String inClause = targetIdBinder.getIdInValueExpr(parentIdist.size());
        sb.append(inClause);
        
        DefaultSqlUpdate delete = new DefaultSqlUpdate(sb.toString());
        for (int i = 0; i < parentIdist.size(); i++) {            
            bindWhereParendId(delete, parentIdist.get(i));
        }
        
        return delete;
    }
	
	/**
	 * Set the lazy load server to help create reference collections (that lazy
	 * load on demand).
	 */
	public void setLoader(BeanCollectionLoader loader){
		if (help != null){
			help.setLoader(loader);
		}
	}

	/**
	 * Return the mode for listening to modifications to collections for this
	 * association.
	 */
	public ModifyListenMode getModifyListenMode() {
		return modifyListenMode;
	}
	
	/**
	 * Ignore changes for Many properties.
	 */
	public boolean hasChanged(Object bean, Object oldValues) {
		return false;
	}

	@Override
	public void appendSelect(DbSqlContext ctx, boolean subQuery) {
	}

    @Override
    public void loadIgnore(DbReadContext ctx) {
        // nothing to ignore for Many
    }
	
	@Override
	public void load(SqlBeanLoad sqlBeanLoad) throws SQLException {
		sqlBeanLoad.loadAssocMany(this);
	}
	
	@Override
	public Object readSet(DbReadContext ctx, Object bean, Class<?> type) throws SQLException {
		return null;
	}

	@Override
	public Object read(DbReadContext ctx) throws SQLException {
		return null;
	}


	@Override
	public boolean isValueLoaded(Object value) {
		if (value instanceof BeanCollection<?>){
			return ((BeanCollection<?>)value).isPopulated();
		}
		return true;
	}

	public void add(BeanCollection<?> collection, Object bean) {
		help.add(collection, bean);
	}

	@Override
	public InvalidValue validateCascade(Object manyValue) {

		ArrayList<InvalidValue> errs = help.validate(manyValue);

		if (errs == null){
			return null;
		} else {
			return new InvalidValue("recurse.many", targetDescriptor.getFullName(), manyValue, InvalidValue.toArray(errs));
		}
	}

	/**
	 * Refresh the appropriate list set or map.
	 */
	public void refresh(EbeanServer server, Query<?> query, Transaction t, Object parentBean) {
		help.refresh(server, query, t, parentBean);
	}

	/**
	 * Apply the refreshed BeanCollection to the property of the parentBean.
	 */
	public void refresh(BeanCollection<?> bc, Object parentBean) {
		help.refresh(bc, parentBean);
	}

	/**
     * Return the Id values from the given bean.
     */
    @Override
    public Object[] getAssocOneIdValues(Object bean) {
        return targetDescriptor.getIdBinder().getIdValues(bean);
    }

    /**
     * Return the Id expression to add to where clause etc.
     */
    public String getAssocOneIdExpr(String prefix, String operator) {
        return targetDescriptor.getIdBinder().getAssocOneIdExpr(prefix, operator);
    }
    
    /**
     * Return the logical id value expression taking into account embedded id's.
     */
    @Override
    public String getAssocIdInValueExpr(int size){
        return targetDescriptor.getIdBinder().getIdInValueExpr(size);        
    }
    
    /**
     * Return the logical id in expression taking into account embedded id's.
     */
    @Override
    public String getAssocIdInExpr(String prefix){
        return targetDescriptor.getIdBinder().getAssocIdInExpr(prefix);
    }


    @Override
    public boolean isAssocId() {
        return true;
    }
    
    @Override
    public boolean isAssocProperty() {
        return true;
    }

	/**
	 * Returns true.
	 */
	@Override
	public boolean containsMany(){
		return true;
	}

	/**
	 * Return the many type.
	 */
	public ManyType getManyType() {
		return manyType;
	}

	/**
	 * Return true if this is many to many.
	 */
	public boolean isManyToMany() {
		return manyToMany;
	}

	/**
	 * ManyToMany only, join from local table to intersection table.
	 */
	public TableJoin getIntersectionTableJoin() {
		return intersectionJoin;
	}

	/**
	 * Set the join properties from the parent bean to the child bean.
	 * This is only valid for OneToMany and NOT valid for ManyToMany.
	 */
	public void setJoinValuesToChild(Object parent, Object child, Object mapKeyValue) {

		if (mapKeyProperty != null){
			mapKeyProperty.setValue(child, mapKeyValue);
		}

		if (!manyToMany){
			if (childMasterProperty != null){
				// bidirectional in the sense that the 'master' property
				// exists on the 'detail' bean
				childMasterProperty.setValue(child, parent);
			} else {
				// unidirectional in the sense that the 'master' property
				// does NOT exist on the 'detail' bean
			}
		}
	}

	/**
	 * Return the order by clause used to order the fetching of the data for
	 * this list, set or map.
	 */
	public String getFetchOrderBy() {
		return fetchOrderBy;
	}

	/**
	 * Return the default mapKey when returning a Map.
	 */
	public String getMapKey() {
		return mapKey;
	}

    public BeanCollection<?> createReferenceIfNull(Object parentBean) {

        Object v = getValue(parentBean);
        if (v instanceof BeanCollection<?>){
            BeanCollection<?> bc = (BeanCollection<?>)v;
            return bc.isReference() ? bc : null;
        } else {
            return createReference(parentBean);
        }
    }
    
	public BeanCollection<?> createReference(Object parentBean) {

		BeanCollection<?> ref = help.createReference(parentBean, name);
		setValue(parentBean, ref);
		return ref;
	}

	public Object createEmpty(boolean vanilla) {
		return help.createEmpty(vanilla);
	}
	
	public BeanCollectionAdd getBeanCollectionAdd(Object bc, String mapKey) {
		return help.getBeanCollectionAdd(bc, mapKey);		
	}
	
	public Object getParentId(Object parentBean) {
		return descriptor.getId(parentBean);
	}
	
	private void bindWhereParendId(DefaultSqlUpdate sqlUpd, Object parentId){
        
        if (exportedProperties.length == 1){
            sqlUpd.addParameter(parentId);
            return;
        }
        for (int i = 0; i < exportedProperties.length; i++) {
            Object embVal = exportedProperties[i].getValue(parentId);
            sqlUpd.addParameter(embVal);
        }
	}
	
    private int bindWhereParendId(int pos, Query<?> q, Object parentId) {

        if (exportedProperties.length == 1) {
            q.setParameter(pos++, parentId);
            
        } else {
            
            for (int i = 0; i < exportedProperties.length; i++) {
                Object embVal = exportedProperties[i].getValue(parentId);
                q.setParameter(pos++, embVal);
            }
        }
        return pos;
    }
	
    private String deriveWhereParentIdSql(boolean inClause) {
        
        StringBuilder sb = new StringBuilder();
        
        if (inClause){
            sb.append("(");
        }
        for (int i = 0; i < exportedProperties.length; i++) {
            String fkColumn = exportedProperties[i].getForeignDbColumn();
            if (i > 0){
                String s = inClause ? "," : " and ";
                sb.append(s);
            }
            sb.append(fkColumn);
            if (!inClause){
                sb.append("=? ");
            }
        }
        if (inClause){
            sb.append(")");
        }
        return sb.toString();
    }
	
	public void setPredicates(SpiQuery<?> query, Object parentBean) {

		if (manyToMany){
			// for ManyToMany lazy loading we need to include a
			// join to the intersection table. The predicate column
			// is not on the 'destination many table'.
			query.setIncludeTableJoin(inverseJoin);
		}

		if (embeddedExportedProperties) {
			// use the EmbeddedId object instead of the parentBean
			BeanProperty[] uids = descriptor.propertiesId();
			parentBean = uids[0].getValue(parentBean);
		}

		for (int i = 0; i < exportedProperties.length; i++) {
			Object val = exportedProperties[i].getValue(parentBean);
			String fkColumn = exportedProperties[i].getForeignDbColumn();
			if (!manyToMany){
				fkColumn = targetDescriptor.getBaseTableAlias()+"."+fkColumn;
			} else {
				// use hard coded alias for intersection table
				fkColumn = "int_."+fkColumn;
			}
			query.where().eq(fkColumn, val);
		}

		if (extraWhere != null){
			// replace the table alias place holder
			String ta = targetDescriptor.getBaseTableAlias();
			String where = StringHelper.replaceString(extraWhere, "${ta}", ta);
			query.where().raw(where);
		}

		if (fetchOrderBy != null){
			query.order(fetchOrderBy);
		}
	}

	/**
	 * Create the array of ExportedProperty used to build reference objects.
	 */
	private ExportedProperty[] createExported() {

		BeanProperty[] uids = descriptor.propertiesId();

		ArrayList<ExportedProperty> list = new ArrayList<ExportedProperty>();

		if (uids.length == 1 && uids[0].isEmbedded()) {

			BeanPropertyAssocOne<?> one = (BeanPropertyAssocOne<?>) uids[0];
			BeanDescriptor<?> targetDesc = one.getTargetDescriptor();
			BeanProperty[] emIds = targetDesc.propertiesBaseScalar();
			try {
				for (int i = 0; i < emIds.length; i++) {
					ExportedProperty expProp = findMatch(true, emIds[i]);
					list.add(expProp);
				}
			} catch (PersistenceException e){
				// not found as individual scalar properties
				e.printStackTrace();
			}

		} else {
			for (int i = 0; i < uids.length; i++) {
				ExportedProperty expProp = findMatch(false, uids[i]);
				list.add(expProp);
			}
		}

		return (ExportedProperty[]) list.toArray(new ExportedProperty[list.size()]);
	}

	/**
	 * Find the matching foreignDbColumn for a given local property.
	 */
	private ExportedProperty findMatch(boolean embedded,BeanProperty prop) {

		String matchColumn = prop.getDbColumn();

		String searchTable;
		TableJoinColumn[] columns;
		if (manyToMany){
			// look for column going to intersection
			columns = intersectionJoin.columns();
			searchTable = intersectionJoin.getTable();

		} else {
			columns = tableJoin.columns();
			searchTable = tableJoin.getTable();
		}
		for (int i = 0; i < columns.length; i++) {
			String matchTo = columns[i].getLocalDbColumn();

			if (matchColumn.equalsIgnoreCase(matchTo)) {
				String foreignCol = columns[i].getForeignDbColumn();
                return new ExportedProperty(embedded, foreignCol, prop);
			}
		}

		String msg = "Error with the Join on ["+getFullBeanName()
			+"]. Could not find the matching foreign key for ["+matchColumn+"] in table["+searchTable+"]?"
			+" Perhaps using a @JoinColumn with the name/referencedColumnName attributes swapped?";
		throw new PersistenceException(msg);
	}

	/**
	 * Return the child property that links back to the master bean.
	 * <p>
	 * Note that childMasterProperty will be null if a field is used instead of
	 * a ManyToOne bean association.
	 * </p>
	 */
	private BeanPropertyAssocOne<?> initChildMasterProperty() {

		if (unidirectional){
			return null;
		}

		// search for the property, to see if it exists
		Class<?> beanType = descriptor.getBeanType();
		BeanDescriptor<?> targetDesc = getTargetDescriptor();

		BeanPropertyAssocOne<?>[] ones = targetDesc.propertiesOne();
		for (int i = 0; i < ones.length; i++) {
			BeanPropertyAssocOne<?> prop = ones[i];
			if (mappedBy != null){
				// match using mappedBy as property name
				if (mappedBy.equalsIgnoreCase(prop.getName())) {
					return prop;
				}
			} else {
				// assume only one property that matches parent object type
				if (prop.getTargetType().equals(beanType)) {
					// found it, stop search
					return prop;
				}
			}
		}

		String msg = "Can not find Master [" + beanType + "] in Child[" + targetDesc + "]";
		throw new RuntimeException(msg);
	}

	/**
	 * Search for and return the mapKey property.
	 */
	private BeanProperty initMapKeyProperty() {

		// search for the property

		BeanDescriptor<?> targetDesc = getTargetDescriptor();

		Iterator<BeanProperty> it = targetDesc.propertiesAll();
		while (it.hasNext()){
			BeanProperty  prop = it.next();
			if (mapKey.equalsIgnoreCase(prop.getName())) {
				return prop;
			}
		}

		String from = descriptor.getFullName();
		String to = targetDesc.getFullName();
		String msg = from+": Could not find mapKey property ["+mapKey+"] on ["+to+"]";
		throw new PersistenceException(msg);
	}

    public IntersectionRow buildManyDeleteChildren(Object parentBean, ArrayList<Object> excludeDetailIds) {

        IntersectionRow row = new IntersectionRow(tableJoin.getTable());
        if (excludeDetailIds != null && !excludeDetailIds.isEmpty()) {
        	row.setExcludeIds(excludeDetailIds, getTargetDescriptor());
        }
        buildExport(row, parentBean);
        return row;
    }

    public IntersectionRow buildManyToManyDeleteChildren(Object parentBean) {

        IntersectionRow row = new IntersectionRow(intersectionJoin.getTable());
        buildExport(row, parentBean);
        return row;
    }
   
	public IntersectionRow buildManyToManyMapBean(Object parent, Object other) {

		IntersectionRow row = new IntersectionRow(intersectionJoin.getTable());

		buildExport(row, parent);
		buildImport(row, other);
		return row;
	}

	private void buildExport(IntersectionRow row, Object parentBean) {

		if (embeddedExportedProperties) {
	        BeanProperty[] uids = descriptor.propertiesId();
			parentBean = uids[0].getValue(parentBean);
		}
		for (int i = 0; i < exportedProperties.length; i++) {
			Object val = exportedProperties[i].getValue(parentBean);
			String fkColumn = exportedProperties[i].getForeignDbColumn();

			row.put(fkColumn, val);
		}
	}
	
	/**
	 * Set the predicates for lazy loading of the association.
	 * Handles predicates for both OneToMany and ManyToMany.
	 */
	private void buildImport(IntersectionRow row, Object otherBean) {

		importedId.buildImport(row, otherBean);
	}

	/**
	 * Return true if the otherBean has an Id value.
	 */
    public boolean hasImportedId(Object otherBean) {
        
        return null != targetDescriptor.getId(otherBean);
    }

    public void jsonWrite(WriteJsonContext ctx, Object bean) {
        
        Boolean include = ctx.includeMany(name);
        if (Boolean.FALSE.equals(include)){
            return;
        }
        
        Object value = getValueIntercept(bean);
        if (value != null){
            ctx.pushParentBeanMany(bean);
            help.jsonWrite(ctx, name, value, include != null);
            ctx.popParentBeanMany();
        }
    }
    
    public void jsonRead(ReadJsonContext ctx, Object bean){
          
        if (!ctx.readArrayBegin()) {
            // the array is null
            return;
        }
        
        Object collection = help.createEmpty(false);
        BeanCollectionAdd add = getBeanCollectionAdd(collection, null);
        do {
            ReadBeanState detailBeanState = targetDescriptor.jsonRead(ctx, name);
            if (detailBeanState == null){
                // probably empty array
                break;
            } 
            Object detailBean = detailBeanState.getBean();
            add.addBean(detailBean);
            
            if (bean != null && childMasterProperty != null){
                // bind detail bean back to master via mappedBy property
                childMasterProperty.setValue(detailBean, bean);
                detailBeanState.setLoaded(childMasterProperty.getName());
            }
            
            detailBeanState.setLoadedState();
            
            if (!ctx.readArrayNext()){
                break;
            }
        } while(true);
        
        setValue(bean, collection);
    
    }
}
