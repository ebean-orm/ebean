package com.avaje.ebeaninternal.server.deploy.meta;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor.EntityType;


/**
 * Property mapped to a joined bean.
 */
public class DeployBeanPropertyAssocOne<T> extends DeployBeanPropertyAssoc<T> {

	boolean oneToOne;
	
	boolean oneToOneExported;

	boolean importedPrimaryKey;

	DeployBeanEmbedded deployEmbedded;
	
	/**
	 * Create the property.
	 */
	public DeployBeanPropertyAssocOne(DeployBeanDescriptor<?> desc, Class<T> targetType) {
		super(desc, targetType);
	}

	/**
	 * Return the deploy information specifically for the deployment
	 * of Embedded beans.
	 */
	public DeployBeanEmbedded getDeployEmbedded() {
		// deployment should be single threaded 
		if (deployEmbedded == null){
			deployEmbedded = new DeployBeanEmbedded();
		}
		return deployEmbedded;
	}

	@Override
    public String getDbColumn() {
		DeployTableJoinColumn[] columns = tableJoin.columns();
		if (columns.length == 1){
			return columns[0].getLocalDbColumn();
		}
	    return super.getDbColumn();
    }

	@Override
    public String getElPlaceHolder(EntityType et) {
	    return super.getElPlaceHolder(et);
    }

	/**
	 * Return true if this a OneToOne property. Otherwise assumed ManyToOne.
	 */
	public boolean isOneToOne() {
		return oneToOne;
	}

	/**
	 * Set to true if this is a OneToOne.
	 */
	public void setOneToOne(boolean oneToOne) {
		this.oneToOne = oneToOne;
	}

	/**
	 * Return true if this is the exported side of a OneToOne.
	 */
	public boolean isOneToOneExported() {
		return oneToOneExported;
	}

	/**
	 * Set to true if this is the exported side of a OneToOne. This means
	 * it doesn't 'own' the foreign key column. A OneToMany without the many.
	 */
	public void setOneToOneExported(boolean oneToOneExported) {
		this.oneToOneExported = oneToOneExported;
	}

	/**
	 * If true this bean maps to the primary key.
	 */
	public boolean isImportedPrimaryKey() {
		return importedPrimaryKey;
	}

	/**
	 * Set to true if the bean maps to the primary key.
	 */
	public void setImportedPrimaryKey(boolean importedPrimaryKey) {
		this.importedPrimaryKey = importedPrimaryKey;
	}

}
