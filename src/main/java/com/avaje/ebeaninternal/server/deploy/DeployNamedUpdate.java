package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.annotation.NamedUpdate;

/**
 * Deployment information for a named update.
 */
public class DeployNamedUpdate {

	private final String name;
	
	private final String updateStatement;

	private final boolean notifyCache;

	private String sqlUpdateStatement;

	public DeployNamedUpdate(NamedUpdate update) {
		this.name = update.name();
		this.updateStatement = update.update();
		this.notifyCache = update.notifyCache();
	}

	public void initialise(DeployUpdateParser parser) {
		sqlUpdateStatement = parser.parse(updateStatement);
	}
	
	public String getName() {
		return name;
	}

	public String getSqlUpdateStatement() {
		return sqlUpdateStatement;
	}

	public boolean isNotifyCache() {
		return notifyCache;
	}
	
}
