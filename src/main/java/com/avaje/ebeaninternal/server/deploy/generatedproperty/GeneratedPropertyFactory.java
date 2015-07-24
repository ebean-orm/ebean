package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import java.math.BigDecimal;
import java.util.HashSet;

import com.avaje.ebean.config.CurrentUserProvider;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;

/**
 * Default implementation of GeneratedPropertyFactory.
 */
public class GeneratedPropertyFactory {

	private final CounterFactory counterFactory = new CounterFactory();;

  private final InsertTimestampFactory insertFactory = new InsertTimestampFactory();

  private final UpdateTimestampFactory updateFactory = new UpdateTimestampFactory();

  private final HashSet<String> numberTypes = new HashSet<String>();

  private final GeneratedWhoModified generatedWhoModified;

  private final GeneratedWhoCreated generatedWhoCreated;

  public GeneratedPropertyFactory(CurrentUserProvider currentUserProvider) {

	  if (currentUserProvider != null) {
      generatedWhoCreated = new GeneratedWhoCreated(currentUserProvider);
      generatedWhoModified = new GeneratedWhoModified(currentUserProvider);
    } else {
      generatedWhoCreated = null;
      generatedWhoModified = null;
    }

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

  public void setWhoCreated(DeployBeanProperty property) {
    if (generatedWhoCreated == null) {
      throw new IllegalStateException("No CurrentUserProvider has been set so @WhoCreated is not supported");
    }
    property.setGeneratedProperty(generatedWhoCreated);
  }

  public void setWhoModified(DeployBeanProperty property) {
    if (generatedWhoModified == null) {
      throw new IllegalStateException("No CurrentUserProvider has been set so @WhoModified is not supported");
    }
    property.setGeneratedProperty(generatedWhoModified);
  }

}
