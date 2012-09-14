package com.avaje.ebeaninternal.server.deploy;

public class BeanEmbeddedMeta {

	
	final BeanProperty[] properties;
	
	public BeanEmbeddedMeta(BeanProperty[] properties) {
		this.properties = properties;
	}

	/**
	 * Return the properties with over ridden mapping information.
	 */
	public BeanProperty[] getProperties() {
		return properties;
	}
	
	/**
	 * Return true if at least one property is a version property.
	 */
	public boolean isEmbeddedVersion() {
		for (int i = 0; i < properties.length; i++) {
			if (properties[i].isVersion()){
				return true;
			}
		}
		return false;
	}
	
}
