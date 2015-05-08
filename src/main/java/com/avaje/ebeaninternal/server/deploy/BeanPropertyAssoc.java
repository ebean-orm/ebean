package com.avaje.ebeaninternal.server.deploy;

import java.util.ArrayList;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.core.InternString;
import com.avaje.ebeaninternal.server.deploy.id.IdBinder;
import com.avaje.ebeaninternal.server.deploy.id.ImportedId;
import com.avaje.ebeaninternal.server.deploy.id.ImportedIdEmbedded;
import com.avaje.ebeaninternal.server.deploy.id.ImportedIdSimple;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import com.avaje.ebeaninternal.server.el.ElPropertyChainBuilder;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.query.SqlJoinType;

/**
 * Abstract base for properties mapped to an associated bean, list, set or map.
 */
public abstract class BeanPropertyAssoc<T> extends BeanProperty {

	private static final Logger logger = LoggerFactory.getLogger(BeanPropertyAssoc.class);

	/**
	 * The descriptor of the target. This MUST be initialised after construction
	 * so as to avoid a dependency loop between BeanDescriptors.
	 */
	BeanDescriptor<T> targetDescriptor;

	IdBinder targetIdBinder;

	InheritInfo targetInheritInfo;
	
	String targetIdProperty;

	/**
	 * Persist settings.
	 */
	final BeanCascadeInfo cascadeInfo;

	/**
	 * Join between the beans.
	 */
	final TableJoin tableJoin;

	/**
	 * The type of the joined bean.
	 */
	final Class<T> targetType;

	/**
	 * The join table information.
	 */
	final BeanTable beanTable;
	
	final String mappedBy;

	/**
	 * Whether the associated join type should be an outer join.
	 */
	final boolean isOuterJoin;

	String extraWhere;

	boolean saveRecurseSkippable;

	boolean deleteRecurseSkippable;

	/**
	 * Construct the property.
	 */
	public BeanPropertyAssoc(BeanDescriptorMap owner, BeanDescriptor<?> descriptor, DeployBeanPropertyAssoc<T> deploy) {
		super(owner, descriptor, deploy);
		this.extraWhere = InternString.intern(deploy.getExtraWhere());
		this.isOuterJoin = deploy.isOuterJoin();
		this.beanTable = deploy.getBeanTable();
		this.mappedBy = InternString.intern(deploy.getMappedBy());

		this.tableJoin = new TableJoin(deploy.getTableJoin(), null);

		this.targetType = deploy.getTargetType();
		this.cascadeInfo = deploy.getCascadeInfo();
	}
	
	/**
	 * Initialise post construction.
	 */
	@Override
	public void initialise() {
		// this *MUST* execute after the BeanDescriptor is
		// put into the map to stop infinite recursion
		if (!isTransient){
			targetDescriptor = descriptor.getBeanDescriptor(targetType);
			targetIdBinder = targetDescriptor.getIdBinder();
			targetInheritInfo = targetDescriptor.getInheritInfo();

			saveRecurseSkippable = targetDescriptor.isSaveRecurseSkippable();
			deleteRecurseSkippable = targetDescriptor.isDeleteRecurseSkippable();

			cascadeValidate = cascadeInfo.isValidate();

			if (!targetIdBinder.isComplexId()){
				targetIdProperty = targetIdBinder.getIdProperty();
			}
		}
	}
	
	/**
     * Create a ElPropertyValue for a *ToOne or *ToMany.
     */
    protected ElPropertyValue createElPropertyValue(String propName, String remainder, ElPropertyChainBuilder chain, boolean propertyDeploy) {
        
        // associated or embedded bean
        BeanDescriptor<?> embDesc = getTargetDescriptor();
    
        if (chain == null) {
            chain = new ElPropertyChainBuilder(isEmbedded(), propName);
        }
        chain.add(this);
        if (containsMany()) {
            chain.setContainsMany(true);
        }
        return embDesc.buildElGetValue(remainder, chain, propertyDeploy);
    }
	
    /**
	 * Add table join with table alias based on prefix.
	 */
    public SqlJoinType addJoin(SqlJoinType joinType, String prefix, DbSqlContext ctx) {
    	return tableJoin.addJoin(joinType, prefix, ctx);
    }
    
    /**
	 * Add table join with explicit table alias.
	 */
    public SqlJoinType addJoin(SqlJoinType joinType, String a1, String a2, DbSqlContext ctx) {
    	return tableJoin.addJoin(joinType, a1, a2, ctx);
    }
    
	/**
	 * Return false.
	 */
	public boolean isScalar() {
		return false;
	}
	
	/**
	 * Return the mappedBy property.
	 * This will be null on the owning side.
	 */
	public String getMappedBy() {
		return mappedBy;
	}

	/**
	 * Return the Id property of the target entity type.
	 * <p>
	 * This will return null for multiple Id properties.
	 * </p>
	 */
	public String getTargetIdProperty() {
		return targetIdProperty;
	}

	/**
	 * Return the BeanDescriptor of the target.
	 */
	public BeanDescriptor<T> getTargetDescriptor() {
		return targetDescriptor;
	}
	
	public boolean isSaveRecurseSkippable(Object bean) {
		if (!saveRecurseSkippable){
			// we have to saveRecurse even if the bean is not dirty
			// as this bean has cascade save on some of its properties
			return false;
		}
		if (bean instanceof EntityBean){
			return !((EntityBean)bean)._ebean_getIntercept().isNewOrDirty();
		} else {
			// we don't know so we say no
			return false;
		}
	}

	/**
	 * Return true if save can be skipped for unmodified bean(s) of this
	 * property.
	 * <p>
	 * That is, if a bean of this property is unmodified we don't need to
	 * saveRecurse because none of its associated beans have cascade save set to
	 * true.
	 * </p>
	 */
	public boolean isSaveRecurseSkippable() {
		return saveRecurseSkippable;
	}

	/**
	 * Similar to isSaveRecurseSkippable but in terms of delete.
	 */
	public boolean isDeleteRecurseSkippable() {
		return deleteRecurseSkippable;
	}

	/**
	 * Return true if the unique id properties are all not null for this bean.
	 */
	public boolean hasId(EntityBean bean) {

		BeanDescriptor<?> targetDesc = getTargetDescriptor();
		BeanProperty idProp = targetDesc.getIdProperty();
		if (idProp != null) {
			Object value = idProp.getValue(bean);
			if (value == null) {
				return false;
			}
		}
		// all the unique properties are non-null
		return true;
	}

	/**
	 * Return the type of the target.
	 * <p>
	 * This is the class of the associated bean, or beans contained in a list,
	 * set or map.
	 * </p>
	 */
	public Class<?> getTargetType() {
		return targetType;
	}

	/**
	 * Return an extra clause to add to the query for loading or joining
	 * to this bean type.
	 */
	public String getExtraWhere() {
		return extraWhere;
	}

	/**
	 * Return if this association should use an Outer join.
	 */
	public boolean isOuterJoin() {
		return isOuterJoin;
	}

	/**
	 * Return true if this association is updateable.
	 */
	public boolean isUpdateable() {
		if (tableJoin.columns().length > 0) {
			return tableJoin.columns()[0].isUpdateable();
		}

		return true;
	}

	/**
	 * Return true if this association is insertable.
	 */
	public boolean isInsertable() {
		if (tableJoin.columns().length > 0) {
			return tableJoin.columns()[0].isInsertable();
		}

		return true;
	}

	/**
	 * return the join to use for the bean.
	 */
	public TableJoin getTableJoin() {
		return tableJoin;
	}

	/**
	 * Return the BeanTable for this association.
	 * <p>
	 * This has the table name which is used to determine the relationship for
	 * this association.
	 * </p>
	 */
	public BeanTable getBeanTable() {
		return beanTable;
	}

	/**
	 * Get the persist info.
	 */
	public BeanCascadeInfo getCascadeInfo() {
		return cascadeInfo;
	}

	/**
	 * Build the list of imported property. Matches BeanProperty from the target
	 * descriptor back to local database columns in the TableJoin.
	 */
	protected ImportedId createImportedId(BeanPropertyAssoc<?> owner, BeanDescriptor<?> target, TableJoin join) {

		BeanProperty idProp = target.getIdProperty();
		BeanProperty[] others = target.propertiesBaseScalar();

		if (descriptor.isSqlSelectBased()){
			String dbColumn = owner.getDbColumn();
			return new ImportedIdSimple(owner, dbColumn, idProp, 0);
		}

		TableJoinColumn[] cols = join.columns();

		if (idProp == null) {
		  return null;
		}
		if (!idProp.isEmbedded()) {
			// simple single scalar id
			if (cols.length != 1){
				String msg = "No Imported Id column for ["+idProp+"] in table ["+join.getTable()+"]";
				logger.error(msg);
				return null;
			} else {
			  BeanProperty[] idProps = {idProp};
				return createImportedScalar(owner, cols[0], idProps, others);
			}
		} else {
			// embedded id
			BeanPropertyAssocOne<?> embProp = (BeanPropertyAssocOne<?>)idProp;
			BeanProperty[] embBaseProps = embProp.getTargetDescriptor().propertiesBaseScalar();
			ImportedIdSimple[] scalars = createImportedList(owner, cols, embBaseProps, others);

			return new ImportedIdEmbedded(owner, embProp, scalars);
		}
	}

	private ImportedIdSimple[] createImportedList(BeanPropertyAssoc<?> owner, TableJoinColumn[] cols, BeanProperty[] props, BeanProperty[] others) {

		ArrayList<ImportedIdSimple> list = new ArrayList<ImportedIdSimple>();

		for (int i = 0; i < cols.length; i++) {
			list.add(createImportedScalar(owner, cols[i], props, others));
		}
		
		return ImportedIdSimple.sort(list);
	}

	private ImportedIdSimple createImportedScalar(BeanPropertyAssoc<?> owner, TableJoinColumn col, BeanProperty[] props, BeanProperty[] others) {

		String matchColumn = col.getForeignDbColumn();
		String localColumn = col.getLocalDbColumn();
		
		for (int j = 0; j < props.length; j++) {
			if (props[j].getDbColumn().equalsIgnoreCase(matchColumn)) {
				return new ImportedIdSimple(owner, localColumn, props[j], j);
			}
		}

		for (int j = 0; j < others.length; j++) {
            if (others[j].getDbColumn().equalsIgnoreCase(matchColumn)) {
                return new ImportedIdSimple(owner, localColumn, others[j], j+props.length);
            }
        }
		
		String msg = "Error with the Join on ["+getFullBeanName()
			+"]. Could not find the local match for ["+matchColumn+"] "//in table["+searchTable+"]?"
			+" Perhaps an error in a @JoinColumn";
		throw new PersistenceException(msg);
	}
}
