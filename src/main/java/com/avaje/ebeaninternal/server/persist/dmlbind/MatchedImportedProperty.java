package com.avaje.ebeaninternal.server.persist.dmlbind;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;

/**
 * Matches local embedded id properties to 'matching' properties from a
 * ManyToOne associated bean that is a 'imported primary key'.
 * <p>
 * This object is designed to help BindableIdEmbedded and BindableIdMap to
 * create a concatenated id from the id values from ManyToOne associated beans.
 * This can be done when those ManyToOne associated beans make up the primary
 * key. This typically means the BindableIdEmbedded is for a intersection table
 * of a Many to Many relationship.
 * </p>
 */
class MatchedImportedProperty {

	private final BeanPropertyAssocOne<?> assocOne;

	private final BeanProperty foreignProp;

	private final BeanProperty localProp;

	protected MatchedImportedProperty(BeanPropertyAssocOne<?> assocOne, BeanProperty foreignProp,
			BeanProperty localProp) {
		this.assocOne = assocOne;
		this.foreignProp = foreignProp;
		this.localProp = localProp;
	}

	protected void populate(EntityBean sourceBean, EntityBean destBean) {
		Object assocBean = assocOne.getValue(sourceBean);
		if (assocBean == null) {
			String msg = "The assoc bean for " + assocOne + " is null?";
			throw new NullPointerException(msg);
		}

		Object value = foreignProp.getValue((EntityBean)assocBean);
		localProp.setValue(destBean, value);
	}

	/**
	 * Create the array of matchedImportedProperty based on the properties and descriptor.
	 */
	 protected static MatchedImportedProperty[] build(BeanProperty[] props, BeanDescriptor<?> desc) {

		MatchedImportedProperty[] matches = new MatchedImportedProperty[props.length];

		for (int i = 0; i < props.length; i++) {
			// find matching assoc one property for dbColumn
			matches[i] = MatchedImportedProperty.findMatch(props[i], desc);
			if (matches[i] == null) {
				// ok, the assoc ones are not on the bean?
				return null;
			}
		}
		return matches;
	}
	
	private static MatchedImportedProperty findMatch(BeanProperty prop, BeanDescriptor<?> desc) {

		// find matching against the local database column
		String dbColumn = prop.getDbColumn();

		BeanPropertyAssocOne<?>[] assocOnes = desc.propertiesOne();
		for (int i = 0; i < assocOnes.length; i++) {
			if (assocOnes[i].isImportedPrimaryKey()) {
				
				// search using the ImportedId from the assoc one
				BeanProperty foreignMatch = assocOnes[i].getImportedId().findMatchImport(dbColumn);

				if (foreignMatch != null) {
					return new MatchedImportedProperty(assocOnes[i], foreignMatch, prop);
				}
			}
		}
		
		// there was no matching assoc one property.
		// example UserRole bean missing assoc one to User?
		return null;
	}

}
