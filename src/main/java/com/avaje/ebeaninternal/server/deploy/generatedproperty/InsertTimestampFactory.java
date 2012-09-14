package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import java.sql.Timestamp;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;

/**
 * Helper for creating Insert timestamp GeneratedProperty objects.
 */
public class InsertTimestampFactory {

	final GeneratedInsertTimestamp timestamp = new GeneratedInsertTimestamp();

	final GeneratedInsertDate utilDate = new GeneratedInsertDate();

	final GeneratedInsertLong longTime = new GeneratedInsertLong();

	public void setInsertTimestamp(DeployBeanProperty property) {

		property.setGeneratedProperty(createInsertTimestamp(property));
	}
	
	/**
	 * Create the insert GeneratedProperty depending on the property type.
	 */
	public GeneratedProperty createInsertTimestamp(DeployBeanProperty property) {
		
		Class<?> propType = property.getPropertyType();
		if (propType.equals(Timestamp.class)) {
			return timestamp;
		}
		if (propType.equals(java.util.Date.class)) {
			return utilDate;
		}
		if (propType.equals(Long.class) || propType.equals(long.class)) {
			return longTime;
		}
		
		//TODO: Support JODA Time objects ... perhaps others?
		
		String msg = "Generated Insert Timestamp not supported on "+propType.getName();
		throw new PersistenceException(msg);
	}
	
}
