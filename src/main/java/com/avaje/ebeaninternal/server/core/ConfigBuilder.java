package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.config.ServerConfig;


/**
 * Build a ServerConfig from ebean.properties.
 */
public class ConfigBuilder {

	/**
	 * Create a ServerConfig and load it from ebean.properties.
	 */
	public ServerConfig build(String serverName) {
		
		ServerConfig config = new ServerConfig();
		config.setName(serverName);	
		
		config.loadFromProperties();
		
		return config;
	}
	
	
}
