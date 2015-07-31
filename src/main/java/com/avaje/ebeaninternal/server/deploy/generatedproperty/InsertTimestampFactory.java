package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.api.ClassUtil;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;

/**
 * Helper for creating Insert timestamp GeneratedProperty objects.
 */
public class InsertTimestampFactory {

	final GeneratedInsertLong longTime = new GeneratedInsertLong();

  final Map<Class<?>, GeneratedProperty> map = new HashMap<Class<?>, GeneratedProperty>();

  public InsertTimestampFactory() {
    map.put(Timestamp.class, new GeneratedInsertTimestamp());
    map.put(java.util.Date.class, new GeneratedInsertDate());
    map.put(Long.class, longTime);
    map.put(long.class, longTime);

    if (ClassUtil.isPresent("java.time.LocalDate", this.getClass())) {
      map.put(LocalDateTime.class, new GeneratedInsertJavaTime.LocalDT());
      map.put(OffsetDateTime.class, new GeneratedInsertJavaTime.OffsetDT());
      map.put(ZonedDateTime.class, new GeneratedInsertJavaTime.ZonedDT());
    }
    if (ClassUtil.isPresent("org.joda.time.LocalDateTime", this.getClass())) {
      map.put(org.joda.time.LocalDateTime.class, new GeneratedInsertJodaTime.LocalDT());
      map.put(org.joda.time.DateTime.class, new GeneratedInsertJodaTime.DateTimeDT());
    }

  }

	public void setInsertTimestamp(DeployBeanProperty property) {

		property.setGeneratedProperty(createInsertTimestamp(property));
	}
	
	/**
	 * Create the insert GeneratedProperty depending on the property type.
	 */
	public GeneratedProperty createInsertTimestamp(DeployBeanProperty property) {
		
		Class<?> propType = property.getPropertyType();
    GeneratedProperty generatedProperty = map.get(propType);
    if (generatedProperty != null) {
      return generatedProperty;
    }

		throw new PersistenceException("Generated Insert Timestamp not supported on "+propType.getName());
	}
	
}
