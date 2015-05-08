package com.avaje.ebeaninternal.server.deploy.meta;

import com.avaje.ebean.bean.BeanCollection.ModifyListenMode;
import com.avaje.ebeaninternal.server.deploy.ManyType;
import com.avaje.ebeaninternal.server.deploy.TableJoin;

/**
 * Property mapped to a List Set or Map.
 */
public class DeployBeanPropertyAssocMany<T> extends DeployBeanPropertyAssoc<T> {

	ModifyListenMode modifyListenMode = ModifyListenMode.NONE;
	
	/**
	 * Flag to indicate manyToMany relationship.
	 */
	boolean manyToMany;

	/**
	 * Flag to indicate this is a unidirectional relationship.
	 */
	boolean unidirectional;

	/**
	 * Join for manyToMany intersection table.
	 */
	DeployTableJoin intersectionJoin;

	/**
	 * For ManyToMany this is the Inverse join used to build reference queries.
	 */
	DeployTableJoin inverseJoin;

	String fetchOrderBy;

	String mapKey;

	/**
	 * The type of the many, set, list or map.
	 */
	ManyType manyType;

	/**
	 * Create this property.
	 */
	public DeployBeanPropertyAssocMany(DeployBeanDescriptor<?> desc, Class<T> targetType, ManyType manyType) {
		super(desc, targetType);
		this.manyType = manyType;
	}

	/**
	 * When generics is not used for manyType you can specify via annotations.
	 * <p>
	 * Really only expect this for Scala due to a Scala compiler bug at the moment.
	 * Otherwise I'd probably not bother support this.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	public void setTargetType(Class<?> cls){
		this.targetType = (Class<T>)cls;
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
	 * Set to true if this is a many to many.
	 */
	public void setManyToMany(boolean isManyToMany) {
		this.manyToMany = isManyToMany;
	}

	/**
	 * Return the mode for listening to changes to the List Set or Map.
	 */
	public ModifyListenMode getModifyListenMode() {
		return modifyListenMode;
	}

	/**
	 * Set the mode for listening to changes to the List Set or Map.
	 */
	public void setModifyListenMode(ModifyListenMode modifyListenMode) {
		this.modifyListenMode = modifyListenMode;
	}

	/**
	 * Return true if this is a unidirectional relationship.
	 */
	public boolean isUnidirectional() {
		return unidirectional;
	}

	/**
	 * Set to true if this is a unidirectional relationship.
	 */
	public void setUnidirectional(boolean unidirectional) {
		this.unidirectional = unidirectional;
	}

	/**
	 * Create the immutable version of the intersection join.
	 */
	public TableJoin createIntersectionTableJoin() {
		if (intersectionJoin != null){
			return new TableJoin(intersectionJoin, null);
		} else {
			return null;
		}
	}
	
	/**
	 * Create the immutable version of the inverse join.
	 */
	public TableJoin createInverseTableJoin() {
		if (inverseJoin != null){
			return new TableJoin(inverseJoin, null);
		} else {
			return null;
		}
	}
	
	/**
	 * ManyToMany only, join from local table to intersection table.
	 */
	public DeployTableJoin getIntersectionJoin() {
		return intersectionJoin;
	}

	public DeployTableJoin getInverseJoin() {
		return inverseJoin;
	}

	/**
	 * ManyToMany only, join from local table to intersection table.
	 */
	public void setIntersectionJoin(DeployTableJoin intersectionJoin) {
		this.intersectionJoin = intersectionJoin;
	}

	/**
	 * ManyToMany only, join from foreign table to intersection table.
	 */
	public void setInverseJoin(DeployTableJoin inverseJoin) {
		this.inverseJoin = inverseJoin;
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

	/**
	 * Set the default mapKey to use when returning a Map.
	 */
	public void setMapKey(String mapKey) {
		if (mapKey != null && mapKey.length() > 0) {
			this.mapKey = mapKey;
		}
	}

	/**
	 * Set the order by clause used to order the fetching or the data for this
	 * list, set or map.
	 */
	public void setFetchOrderBy(String orderBy) {
		if (orderBy != null && orderBy.length() > 0) {
			fetchOrderBy = orderBy;
		}
	}

}
