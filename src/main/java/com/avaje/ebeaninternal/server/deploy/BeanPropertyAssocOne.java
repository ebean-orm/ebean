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
import java.util.HashMap;
import java.util.List;

import javax.persistence.PersistenceException;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.InvalidValue;
import com.avaje.ebean.Query;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.core.DefaultSqlUpdate;
import com.avaje.ebeaninternal.server.deploy.id.IdBinder;
import com.avaje.ebeaninternal.server.deploy.id.ImportedId;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.el.ElPropertyChainBuilder;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.query.SplitName;
import com.avaje.ebeaninternal.server.query.SqlBeanLoad;
import com.avaje.ebeaninternal.server.text.json.ReadJsonContext;
import com.avaje.ebeaninternal.server.text.json.WriteJsonContext;

/**
 * Property mapped to a joined bean.
 */
public class BeanPropertyAssocOne<T> extends BeanPropertyAssoc<T> {

    private final boolean oneToOne;

    private final boolean oneToOneExported;

    private final boolean embeddedVersion;

    private final boolean importedPrimaryKey;

    private final LocalHelp localHelp;

    private final BeanProperty[] embeddedProps;

    private final HashMap<String,BeanProperty> embeddedPropsMap;
    
    /**
     * The information for Imported foreign Keys.
     */
    private ImportedId importedId;
    
    private ExportedProperty[] exportedProperties;
    
    private String deleteByParentIdSql;
    private String deleteByParentIdInSql;
    BeanPropertyAssocMany<?> relationshipProperty;

    /**
     * Create based on deploy information of an EmbeddedId.
     */
    public BeanPropertyAssocOne(BeanDescriptorMap owner, DeployBeanPropertyAssocOne<T> deploy) {
        this(owner, null, deploy);
    }

    /**
     * Create the property.
     */
    public BeanPropertyAssocOne(BeanDescriptorMap owner, BeanDescriptor<?> descriptor,
            DeployBeanPropertyAssocOne<T> deploy) {

        super(owner, descriptor, deploy);

        importedPrimaryKey = deploy.isImportedPrimaryKey();
        oneToOne = deploy.isOneToOne();
        oneToOneExported = deploy.isOneToOneExported();

        if (embedded) {
			// Overriding of the columns and use table alias of owning BeanDescriptor
            BeanEmbeddedMeta overrideMeta = BeanEmbeddedMetaFactory.create(owner, deploy, descriptor);
            embeddedProps = overrideMeta.getProperties();
            if (id) {
                embeddedVersion = false;
            } else {
                embeddedVersion = overrideMeta.isEmbeddedVersion();
            }
            embeddedPropsMap = new HashMap<String, BeanProperty>();
            for (int i = 0; i < embeddedProps.length; i++) {
                embeddedPropsMap.put(embeddedProps[i].getName(), embeddedProps[i]);
            }

        } else {
            embeddedProps = null;
            embeddedPropsMap = null;
            embeddedVersion = false;
        }
        localHelp = createHelp(embedded, oneToOneExported);
    }

    @Override
    public void initialise() {
        super.initialise();
        if (!isTransient) {
            if (embedded) {
                // no imported or exported information
            } else if (!oneToOneExported) {
                importedId = createImportedId(this, targetDescriptor, tableJoin);
            } else {
                exportedProperties = createExported();
                                
                String delStmt = "delete from "+targetDescriptor.getBaseTable()+" where ";
                
                deleteByParentIdSql = delStmt + deriveWhereParentIdSql(false);
                deleteByParentIdInSql = delStmt + deriveWhereParentIdSql(true);

            }
        }
    }

    public void setRelationshipProperty(BeanPropertyAssocMany<?> relationshipProperty){
    	this.relationshipProperty = relationshipProperty;
    }
    
    public BeanPropertyAssocMany<?> getRelationshipProperty() {
    	return relationshipProperty;
    }

	public void cacheClear() {
		if (targetDescriptor.isBeanCaching() && relationshipProperty != null) {
			targetDescriptor.cacheClearCachedManyIds(relationshipProperty.getName());
		}
	}
	
	public void cacheDelete(boolean clearOnNull, Object bean) {
		if (targetDescriptor.isBeanCaching() && relationshipProperty != null) {
			Object assocBean = getValue(bean);
			if (assocBean != null) {
    			Object parentId = targetDescriptor.getId(assocBean);
    			if (parentId != null) {
    				targetDescriptor.cacheRemoveCachedManyIds(parentId, relationshipProperty.getName());
    				return;
    			}
			}
			if (clearOnNull) {
				targetDescriptor.cacheClearCachedManyIds(relationshipProperty.getName());
			}
		}
	}

	public ElPropertyValue buildElPropertyValue(String propName, String remainder, ElPropertyChainBuilder chain, boolean propertyDeploy) {
        
        if (embedded){
            BeanProperty embProp = embeddedPropsMap.get(remainder);
            if (embProp == null){
                String msg = "Embedded Property "+remainder+" not found in "+getFullBeanName();
                throw new PersistenceException(msg);
            }
            if (chain == null) {
                chain = new ElPropertyChainBuilder(true, propName);
            }
            chain.add(this);
            return chain.add(embProp).build();
        }
        
        return createElPropertyValue(propName, remainder, chain, propertyDeploy);
    }

    @Override
    public String getElPlaceholder(boolean encrypted) {
        return encrypted ? elPlaceHolderEncrypted : elPlaceHolder;
    }

    public SqlUpdate deleteByParentId(Object parentId, List<Object> parentIdist) {
        if (parentId != null){
            return deleteByParentId(parentId);
        } else {
            return deleteByParentIdList(parentIdist);
        }
    }
    
    private SqlUpdate deleteByParentIdList(List<Object> parentIdist) {

        StringBuilder sb = new StringBuilder(100);
        sb.append(deleteByParentIdInSql);
        
        String inClause = targetIdBinder.getIdInValueExpr(parentIdist.size());
        sb.append(inClause);
        
        DefaultSqlUpdate delete = new DefaultSqlUpdate(sb.toString());
        for (int i = 0; i < parentIdist.size(); i++) {            
            targetIdBinder.bindId(delete, parentIdist.get(i));
        }
        
        return delete;
    }
    
    private SqlUpdate deleteByParentId(Object parentId) {
        
        DefaultSqlUpdate delete = new DefaultSqlUpdate(deleteByParentIdSql);
        if (exportedProperties.length == 1){
            delete.addParameter(parentId);
        } else {
            targetDescriptor.getIdBinder().bindId(delete, parentId);
        }
        return delete;
    }
    
    public List<Object> findIdsByParentId(Object parentId, List<Object> parentIdist, Transaction t) {
        if (parentId != null){
            return findIdsByParentId(parentId, t);
        } else {
            return findIdsByParentIdList(parentIdist, t);
        }
    }
    
    private List<Object> findIdsByParentId(Object parentId, Transaction t) {
        
        String rawWhere = deriveWhereParentIdSql(false);
        
        EbeanServer server = getBeanDescriptor().getEbeanServer();
        Query<?> q = server.find(getPropertyType())
            .where().raw(rawWhere).query();
        
        bindWhereParendId(q, parentId);
        return server.findIds(q, t);
    }
    
    private List<Object> findIdsByParentIdList(List<Object> parentIdist, Transaction t) {

        String rawWhere = deriveWhereParentIdSql(true);
        String inClause = targetIdBinder.getIdInValueExpr(parentIdist.size());
        
        String expr = rawWhere+inClause;
 
        EbeanServer server = getBeanDescriptor().getEbeanServer();
        Query<?> q = (Query<?>)server.find(getPropertyType())
            .where().raw(expr);
       
        for (int i = 0; i < parentIdist.size(); i++) {            
            bindWhereParendId(q, parentIdist.get(i));
        }
        
        return server.findIds(q, t);
    }
    
    private void bindWhereParendId(Query<?> q, Object parentId) {

        if (exportedProperties.length == 1) {
            q.setParameter(1, parentId);
            
        } else {
            int pos = 1;
            for (int i = 0; i < exportedProperties.length; i++) {
                Object embVal = exportedProperties[i].getValue(parentId);
                q.setParameter(pos++, embVal);
            }
        }
    }

    public void addFkey() {
        if (importedId != null) {
            importedId.addFkeys(name);
        }
    }

    @Override
    public boolean isValueLoaded(Object value) {
        if (value instanceof EntityBean) {
            return ((EntityBean) value)._ebean_getIntercept().isLoaded();
        }
        return true;
    }

    @Override
    public InvalidValue validateCascade(Object value) {

        BeanDescriptor<?> target = getTargetDescriptor();
        return target.validate(true, value);
    }

    private boolean hasChangedEmbedded(Object bean, Object oldValues) {

        Object embValue = getValue(oldValues);
        if (embValue instanceof EntityBean) {
            // the embedded bean .. has its own old values
            return ((EntityBean) embValue)._ebean_getIntercept().isNewOrDirty();
        }
        if (embValue == null) {
            return getValue(bean) != null;
        } else {
            return false;
        }
    }

    @Override
    public boolean hasChanged(Object bean, Object oldValues) {
        if (embedded) {
            return hasChangedEmbedded(bean, oldValues);
        }
        Object value = getValue(bean);
        Object oldVal = getValue(oldValues);
        if (oneToOneExported) {
            // FKey on other side
            return false;
        } else {
            if (value == null) {
                return oldVal != null;
            } else if (oldValues == null) {
                return true;
            }

            return importedId.hasChanged(value, oldVal);
        }
    }

    /**
     * Return meta data for the deployment of the embedded bean specific to this
     * property.
     */
    public BeanProperty[] getProperties() {
        return embeddedProps;
    }

    public void buildSelectExpressionChain(String prefix, List<String> selectChain) {

        prefix = SplitName.add(prefix, name);

        if (!embedded){
            targetIdBinder.buildSelectExpressionChain(prefix, selectChain);
            
        } else {
            for (int i = 0; i < embeddedProps.length; i++) {
                embeddedProps[i].buildSelectExpressionChain(prefix, selectChain);
            }       
        }
    }

    
    /**
     * Return true if this a OneToOne property. Otherwise assumed ManyToOne.
     */
    public boolean isOneToOne() {
        return oneToOne;
    }

    /**
     * Return true if this is the exported side of a OneToOne.
     */
    public boolean isOneToOneExported() {
        return oneToOneExported;
    }

    /**
     * Returns true if the associated bean has version properties.
     */
    public boolean isEmbeddedVersion() {
        return embeddedVersion;
    }

    /**
     * If true this bean maps to the primary key.
     */
    public boolean isImportedPrimaryKey() {
        return importedPrimaryKey;
    }

    /**
     * Same as getPropertyType(). Return the type of the bean this property
     * represents.
     */
    public Class<?> getTargetType() {
        return getPropertyType();
    }

    public Object getCacheDataValue(Object bean){
    	if (embedded) {
    		throw new RuntimeException();
    	} else {
    		Object ap = getValue(bean);
    		if (ap == null){
    			return null;
    		} else {
        		return targetDescriptor.getId(ap);    			
    		}
    	}
    }
    
    public void setCacheDataValue(Object bean, Object cacheData, Object oldValues, boolean readOnly){
    	if (cacheData != null) {
    		if (embedded){
        		throw new RuntimeException();
    		} else {
		    	boolean vanillaMode = false;
		    	T ref  = targetDescriptor.createReference(vanillaMode, Boolean.FALSE, cacheData, null);
		    	setValue(bean, ref);
		    	if (oldValues != null){
		    		setValue(oldValues, ref);
		    	}
		    	if (readOnly && !vanillaMode){
		    		((EntityBean)ref)._ebean_intercept().setReadOnly(true);
		    	}
    		}
    	}
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
        return !embedded;
    }

    @Override
    public boolean isAssocProperty() {
        return !embedded;
    }

    
    /**
     * Create a vanilla bean of the target type to be used as an embeddedId
     * value.
     */
    public Object createEmbeddedId() {
        return getTargetDescriptor().createVanillaBean();
    }

    /**
     * Return an empty reference object.
     */
    public Object createEmptyReference() {
        return targetDescriptor.createEntityBean();
    }

    public void elSetReference(Object bean) {
        Object value = getValueIntercept(bean);
        if (value != null) {
            ((EntityBean) value)._ebean_getIntercept().setReference();
        }
    }

    @Override
    public Object elGetReference(Object bean) {
        Object value = getValueIntercept(bean);
        if (value == null) {
            value = targetDescriptor.createEntityBean();
            setValueIntercept(bean, value);
        }
        return value;
    }

    public ImportedId getImportedId() {
        return importedId;
    }

    private String deriveWhereParentIdSql(boolean inClause) {
        
        StringBuilder sb = new StringBuilder();
        
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
        return sb.toString();
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
    private ExportedProperty findMatch(boolean embeddedProp,BeanProperty prop) {

        String matchColumn = prop.getDbColumn();

        String searchTable = tableJoin.getTable();
        TableJoinColumn[] columns = tableJoin.columns();
        
        for (int i = 0; i < columns.length; i++) {
            String matchTo = columns[i].getLocalDbColumn();

            if (matchColumn.equalsIgnoreCase(matchTo)) {
                String foreignCol = columns[i].getForeignDbColumn();
                return new ExportedProperty(embeddedProp, foreignCol, prop);
            }
        }

        String msg = "Error with the Join on ["+getFullBeanName()
            +"]. Could not find the matching foreign key for ["+matchColumn+"] in table["+searchTable+"]?"
            +" Perhaps using a @JoinColumn with the name/referencedColumnName attributes swapped?";
        throw new PersistenceException(msg);
    }

    
    @Override
    public void appendSelect(DbSqlContext ctx, boolean subQuery) {
        if (!isTransient) {
            localHelp.appendSelect(ctx, subQuery);
        }
    }

    @Override
    public void appendFrom(DbSqlContext ctx, boolean forceOuterJoin) {
        if (!isTransient) {
            localHelp.appendFrom(ctx, forceOuterJoin);
        }
    }

    @Override
    public Object readSet(DbReadContext ctx, Object bean, Class<?> type) throws SQLException {
        boolean assignable = (type == null || owningType.isAssignableFrom(type));
        return localHelp.readSet(ctx, bean, assignable);
    }

    /**
	 * Read the data from the resultSet effectively ignoring it and returning null.
     */
    @Override
    public Object read(DbReadContext ctx) throws SQLException {
        // just read the resultSet incrementing the column index
        // pass in null for the bean so any data read is ignored
        return localHelp.read(ctx);
    }

    @Override
    public void loadIgnore(DbReadContext ctx) {
        localHelp.loadIgnore(ctx);
    }

    @Override
    public void load(SqlBeanLoad sqlBeanLoad) throws SQLException {
        Object dbVal = sqlBeanLoad.load(this);
        if (embedded && sqlBeanLoad.isLazyLoad()){
    		if (dbVal instanceof EntityBean){
    			((EntityBean)dbVal)._ebean_getIntercept().setLoaded();
    		}
        }
    }

    private LocalHelp createHelp(boolean embedded, boolean oneToOneExported) {
        if (embedded) {
            return new Embedded();
        } else if (oneToOneExported) {
            return new ReferenceExported();
        } else {
            return new Reference(this);
        }
    }

    /**
     * Local interface to handle Embedded, Reference and Reference Exported
     * cases.
     */
    private abstract class LocalHelp {

        abstract void loadIgnore(DbReadContext ctx);

        abstract Object read(DbReadContext ctx) throws SQLException;

        abstract Object readSet(DbReadContext ctx, Object bean, boolean assignAble) throws SQLException;

        abstract void appendSelect(DbSqlContext ctx, boolean subQuery);

        abstract void appendFrom(DbSqlContext ctx, boolean forceOuterJoin);
        
    }

    private final class Embedded extends LocalHelp {

        void loadIgnore(DbReadContext ctx) {
            for (int i = 0; i < embeddedProps.length; i++) {
                embeddedProps[i].loadIgnore(ctx);
            }
        }

        @Override
        Object readSet(DbReadContext ctx, Object bean, boolean assignable) throws SQLException {
            Object dbVal = read(ctx);
            if (bean != null && assignable) {
                // set back to the parent bean
                setValue(bean, dbVal);
                ctx.propagateState(dbVal);
                return dbVal;

            } else {
                return null;
            }
        }

        Object read(DbReadContext ctx) throws SQLException {

            EntityBean embeddedBean = targetDescriptor.createEntityBean();

            boolean notNull = false;
            for (int i = 0; i < embeddedProps.length; i++) {
                Object value = embeddedProps[i].readSet(ctx, embeddedBean, null);
                if (value != null) {
                    notNull = true;
                }
            }
            if (notNull) {
                ctx.propagateState(embeddedBean);
                return embeddedBean;
            } else {
                return null;
            }
        }

        @Override
        void appendFrom(DbSqlContext ctx, boolean forceOuterJoin) {
        }

        @Override
        void appendSelect(DbSqlContext ctx, boolean subQuery) {
            for (int i = 0; i < embeddedProps.length; i++) {
                embeddedProps[i].appendSelect(ctx, subQuery);
            }
        }
    }

    /**
     * For imported reference - this is the common case.
     */
    private final class Reference extends LocalHelp {

        //private final BeanPropertyAssocOne<?> beanProp;

        Reference(BeanPropertyAssocOne<?> beanProp) {
//            this.beanProp = beanProp;
        }
        
        void loadIgnore(DbReadContext ctx) {
            targetIdBinder.loadIgnore(ctx);
            if (targetInheritInfo != null) {
                ctx.getDataReader().incrementPos(1);
            }
        }

        Object readSet(DbReadContext ctx, Object bean, boolean assignable) throws SQLException {
            Object val = read(ctx);
            if (bean != null && assignable) {
                setValue(bean, val);
                ctx.propagateState(val);
            }
            return val;
        }

        /**
         * Read and set a Reference bean.
         */
        @Override
        Object read(DbReadContext ctx) throws SQLException {

            BeanDescriptor<?> rowDescriptor = null;
            Class<?> rowType = targetType;
            if (targetInheritInfo != null) {
                // read discriminator to determine the type
                InheritInfo rowInheritInfo = targetInheritInfo.readType(ctx);
                if (rowInheritInfo != null) {
                    rowType = rowInheritInfo.getType();
                    rowDescriptor = rowInheritInfo.getBeanDescriptor();
                }
            }

            // read the foreign key column(s)
            Object id = targetIdBinder.read(ctx);
            if (id == null) {
                return null;
            }

            // check transaction context to see if it already exists
            Object existing = ctx.getPersistenceContext().get(rowType, id);

            if (existing != null) {
                return existing;
            } 
            
            // parent always null for this case (but here to document)
            Object parent = null;
            boolean vanillaMode = ctx.isVanillaMode();
            //ReferenceOptions options = ctx.getReferenceOptionsFor(beanProp);
            
            Boolean readOnly = ctx.isReadOnly();
            Object ref;
            if (targetInheritInfo != null) {
				// for inheritance hierarchy create the correct type for this row...
                ref = rowDescriptor.createReference(vanillaMode, readOnly, id, parent);
            } else {
                ref = targetDescriptor.createReference(vanillaMode, readOnly, id, parent);
            }
            
            Object existingBean = ctx.getPersistenceContext().putIfAbsent(id, ref);
            if (existingBean != null) {
                // advanced case when we use multiple concurrent threads to
                // build a single object graph, and another thread has since
                // loaded a matching bean so we will use that instead.
                ref = existingBean;
                
            } else if (!vanillaMode){
                EntityBeanIntercept ebi = ((EntityBean) ref)._ebean_getIntercept();
                if (Boolean.TRUE.equals(ctx.isReadOnly())){
                    ebi.setReadOnly(true);
                }
                ctx.register(name, ebi);
            }

            return ref;
        }

        @Override
        void appendFrom(DbSqlContext ctx, boolean forceOuterJoin) {
            if (targetInheritInfo != null) {
                // add join to support the discriminator column
                String relativePrefix = ctx.getRelativePrefix(name);
                tableJoin.addJoin(forceOuterJoin, relativePrefix, ctx);
            }
        }

        /**
         * Append columns for foreign key columns.
         */
        @Override
        void appendSelect(DbSqlContext ctx, boolean subQuery) {

            if (!subQuery && targetInheritInfo != null) {
                // add discriminator column
                String relativePrefix = ctx.getRelativePrefix(getName());
                String tableAlias = ctx.getTableAlias(relativePrefix);
                ctx.appendColumn(tableAlias, targetInheritInfo.getDiscriminatorColumn());
            }
            importedId.sqlAppend(ctx);
        }
    }

    /**
     * For OneToOne exported reference - not so common.
     */
    private final class ReferenceExported extends LocalHelp {

        @Override
        void loadIgnore(DbReadContext ctx) {
            targetDescriptor.getIdBinder().loadIgnore(ctx);
        }

        /**
         * Read and set a Reference bean.
         */
        @Override
        Object readSet(DbReadContext ctx, Object bean, boolean assignable) throws SQLException {

            Object dbVal = read(ctx);
            if (bean != null && assignable) {
                setValue(bean, dbVal);
                ctx.propagateState(dbVal);
            }
            return dbVal;
        }

        @Override
        Object read(DbReadContext ctx) throws SQLException {

            // TODO: Support for Inheritance hierarchy on exported OneToOne ?
            IdBinder idBinder = targetDescriptor.getIdBinder();
            Object id = idBinder.read(ctx);
            if (id == null) {
                return null;
            }

            PersistenceContext persistCtx = ctx.getPersistenceContext();
            Object existing = persistCtx.get(targetType, id);

            if (existing != null) {
                return existing;
            } 
            boolean vanillaMode = ctx.isVanillaMode();
            Object parent = null;
            Object ref = targetDescriptor.createReference(vanillaMode, ctx.isReadOnly(), id, parent);
            
            if (!vanillaMode){
                EntityBeanIntercept ebi = ((EntityBean) ref)._ebean_getIntercept(); 
                if (Boolean.TRUE.equals(ctx.isReadOnly())) {
                    ebi.setReadOnly(true);
                }
                persistCtx.put(id, ref);
                ctx.register(name, ebi);
            }
            return ref;
        }

        /**
         * Append columns for foreign key columns.
         */
        @Override
        void appendSelect(DbSqlContext ctx, boolean subQuery) {

            // set appropriate tableAlias for
            // the exported id columns

            String relativePrefix = ctx.getRelativePrefix(getName());
            ctx.pushTableAlias(relativePrefix);

            IdBinder idBinder = targetDescriptor.getIdBinder();
            idBinder.appendSelect(ctx, subQuery);

            ctx.popTableAlias();
        }

        @Override
        void appendFrom(DbSqlContext ctx, boolean forceOuterJoin) {

            String relativePrefix = ctx.getRelativePrefix(getName());
            tableJoin.addJoin(forceOuterJoin, relativePrefix, ctx);
        }
    }
    
    @Override
    public void jsonWrite(WriteJsonContext ctx, Object bean) {
        
        Object value = getValueIntercept(bean);
        if (value == null){
            ctx.beginAssocOneIsNull(name);
            
        } else {
            if (ctx.isParentBean(value)){
                // bi-directional and already rendered parent
                
            } else {
                ctx.pushParentBean(bean);
                ctx.beginAssocOne(name);
                BeanDescriptor<?> refDesc = descriptor.getBeanDescriptor(value.getClass());
                refDesc.jsonWrite(ctx, value);
                ctx.endAssocOne();
                ctx.popParentBean();
            }
        }
    }
    
    @Override
    public void jsonRead(ReadJsonContext ctx, Object bean){
        
        T assocBean = targetDescriptor.jsonReadBean(ctx, name);
        setValue(bean, assocBean);
    }
}
