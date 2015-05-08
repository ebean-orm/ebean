package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import java.math.BigDecimal;
import java.util.HashSet;

import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;

/**
 * Default implementation of GeneratedPropertyFactory.
 */
public class GeneratedPropertyFactory {

	CounterFactory counterFactory;

	InsertTimestampFactory insertFactory;

	UpdateTimestampFactory updateFactory;

	HashSet<String> numberTypes = new HashSet<String>();

	public GeneratedPropertyFactory() {
		counterFactory = new CounterFactory();
		insertFactory = new InsertTimestampFactory();
		updateFactory = new UpdateTimestampFactory();
		

		numberTypes.add(Integer.class.getName());
		numberTypes.add(int.class.getName());
		numberTypes.add(Long.class.getName());
		numberTypes.add(long.class.getName());
		numberTypes.add(Short.class.getName());
		numberTypes.add(short.class.getName());
		numberTypes.add(Double.class.getName());
		numberTypes.add(double.class.getName());
		numberTypes.add(BigDecimal.class.getName());
	}

	private boolean isNumberType(String typeClassName) {
		return numberTypes.contains(typeClassName);
	}
	
	public void setVersion(DeployBeanProperty property) {
		if (isNumberType(property.getPropertyType().getName())) {
			setCounter(property);
		} else {
			setUpdateTimestamp(property);
		}
	}
	
	public void setCounter(DeployBeanProperty property) {
		
		counterFactory.setCounter(property);
	}

	public void setInsertTimestamp(DeployBeanProperty property) {
		
		insertFactory.setInsertTimestamp(property);
	}

	public void setUpdateTimestamp(DeployBeanProperty property) {
		
		updateFactory.setUpdateTimestamp(property);
	}

}
