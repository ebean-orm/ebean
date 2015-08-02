package com.avaje.ebeaninternal.server.deploy.meta;

import com.avaje.ebeaninternal.server.deploy.BeanPropertyCompoundRoot;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyCompoundScalar;
import com.avaje.ebeaninternal.server.type.CtCompoundProperty;
import com.avaje.ebeaninternal.server.type.CtCompoundType;
import com.avaje.ebeaninternal.server.type.CtCompoundTypeScalarList;
import com.avaje.ebeaninternal.server.type.ScalarType;

import java.util.Map.Entry;


/**
 * Property mapped to a joined bean.
 */
public class DeployBeanPropertyCompound extends DeployBeanProperty {

  private final CtCompoundType<?> compoundType;

  private DeployBeanEmbedded deployEmbedded;

  /**
   * Create the property.
   */
  public DeployBeanPropertyCompound(DeployBeanDescriptor<?> desc, Class<?> targetType, CtCompoundType<?> compoundType) {

    super(desc, targetType, null, null);
    this.compoundType = compoundType;
  }

  public BeanPropertyCompoundRoot getFlatProperties() {

    // get a 'flat' list of all the scalar types, their relative property names
    // and also set their matching dbColumn

    // represents the root property
    BeanPropertyCompoundRoot rootProperty = new BeanPropertyCompoundRoot(this);

    // Walk the tree of a compound type collecting the
    // scalar types and non-scalar properties
    CtCompoundTypeScalarList ctMeta = new CtCompoundTypeScalarList();

    compoundType.accumulateScalarTypes(null, ctMeta);


    // for each of the scalar types inside a compound value object
    // build a BeanPropertyCompoundScalar with appropriate deployment
    // information.

    for (Entry<String, ScalarType<?>> entry : ctMeta.entries()) {

      String relativePropertyName = entry.getKey();
      ScalarType<?> scalarType = entry.getValue();

      CtCompoundProperty ctProp = ctMeta.getCompoundType(relativePropertyName);


      String dbColumn = (getName() + "." + relativePropertyName).replace(".", "_");
      dbColumn = getDbColumn(relativePropertyName, dbColumn);

      DeployBeanProperty deploy = new DeployBeanProperty(null, scalarType.getType(), scalarType, null);
      deploy.setScalarType(scalarType);
      deploy.setDbColumn(dbColumn);
      deploy.setName(relativePropertyName);
      deploy.setDbInsertable(true);
      deploy.setDbUpdateable(true);
      deploy.setDbRead(true);

      rootProperty.register(new BeanPropertyCompoundScalar(rootProperty, deploy, ctProp));
    }

    rootProperty.setNonScalarProperties(ctMeta.getNonScalarProperties());
    return rootProperty;
  }

  private String getDbColumn(String propName, String defaultDbColumn) {
    if (deployEmbedded == null) {
      return defaultDbColumn;
    }
    String dbColumn = deployEmbedded.getPropertyColumnMap().get(propName);
    return dbColumn == null ? defaultDbColumn : dbColumn;
  }

  /**
   * Return the deploy information specifically for the deployment
   * of Embedded beans.
   */
  public DeployBeanEmbedded getDeployEmbedded() {
    // deployment should be single threaded
    if (deployEmbedded == null) {
      deployEmbedded = new DeployBeanEmbedded();
    }
    return deployEmbedded;
  }

  public CtCompoundType<?> getCompoundType() {
    return compoundType;
  }

}
