package com.avaje.ebeaninternal.server.deploy.meta;

/**
 * Property mapped to a joined bean.
 */
public class DeployBeanPropertyAssocOne<T> extends DeployBeanPropertyAssoc<T> {

	boolean oneToOne;
	
	boolean oneToOneExported;

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

  /**
   * Return true if this has multiple properties (expected for embedded id).
   */
  public boolean isCompound() {
    // just checking for compound and not doing numeric check at this stage
    return getDeployEmbedded().getPropertyColumnMap().size() > 1;
  }

	@Override
    public String getDbColumn() {
		DeployTableJoinColumn[] columns = tableJoin.columns();
		if (columns.length == 1){
			return columns[0].getLocalDbColumn();
		}
	    return super.getDbColumn();
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
	public void setOneToOne() {
		this.oneToOne = true;
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
	public void setOneToOneExported() {
		this.oneToOneExported = true;
	}

}
