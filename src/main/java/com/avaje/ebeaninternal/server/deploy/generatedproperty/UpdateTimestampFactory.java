package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import java.sql.Timestamp;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;

/**
 * Helper for creating Update timestamp GeneratedProperty objects.
 */
public class UpdateTimestampFactory {

	final GeneratedUpdateTimestamp timestamp = new GeneratedUpdateTimestamp();

	final GeneratedUpdateDate utilDate = new GeneratedUpdateDate();

	final GeneratedUpdateLong longTime = new GeneratedUpdateLong();

	public void setUpdateTimestamp(DeployBeanProperty property) {

		property.setGeneratedProperty(createUpdateTimestamp(property));
	}
	
	/**
	 * Create the update GeneratedProperty depending on the property type.
	 */
	private GeneratedProperty createUpdateTimestamp(DeployBeanProperty property) {
		
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
		
		String msg = "Generated update Timestamp not supported on "+propType.getName();
		throw new PersistenceException(msg);
	}
	
}
