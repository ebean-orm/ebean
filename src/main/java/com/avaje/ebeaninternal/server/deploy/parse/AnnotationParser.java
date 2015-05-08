package com.avaje.ebeaninternal.server.deploy.parse;

import java.util.HashMap;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;

import com.avaje.ebeaninternal.server.deploy.BeanCascadeInfo;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;

/**
 * Base class for reading deployment annotations.
 */
public abstract class AnnotationParser extends AnnotationBase {

  protected final DeployBeanInfo<?> info;

  protected final DeployBeanDescriptor<?> descriptor;

  protected final Class<?> beanType;

  protected boolean validationAnnotations;
  
  public AnnotationParser(DeployBeanInfo<?> info) {
    super(info.getUtil());
    this.info = info;
    this.beanType = info.getDescriptor().getBeanType();
    this.descriptor = info.getDescriptor();
    
    try {
      Class.forName("javax.validation.constraints.NotNull");
      validationAnnotations = true;
    } catch (ClassNotFoundException e) {
      // javax.validation not in the classpath so don't 
      // check for NotNull and Size 
      validationAnnotations = false;
    }
  }

  /**
   * read the deployment annotations.
   */
  public abstract void parse();

  /**
   * Helper method to set cascade types to the CascadeInfo on BeanProperty.
   */
  protected void setCascadeTypes(CascadeType[] cascadeTypes, BeanCascadeInfo cascadeInfo) {
    if (cascadeTypes != null && cascadeTypes.length > 0) {
      cascadeInfo.setTypes(cascadeTypes);
    }
  }

  /**
   * Read an AttributeOverrides if they exist for this embedded bean.
   */
  protected void readEmbeddedAttributeOverrides(DeployBeanPropertyAssocOne<?> prop) {

    AttributeOverrides attrOverrides = get(prop, AttributeOverrides.class);
    if (attrOverrides != null) {
      HashMap<String, String> propMap = new HashMap<String, String>();
      AttributeOverride[] aoArray = attrOverrides.value();
      for (int i = 0; i < aoArray.length; i++) {
        String propName = aoArray[i].name();
        String columnName = aoArray[i].column().name();

        propMap.put(propName, columnName);
      }

      prop.getDeployEmbedded().putAll(propMap);
    }

  }

}
