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
	
}
