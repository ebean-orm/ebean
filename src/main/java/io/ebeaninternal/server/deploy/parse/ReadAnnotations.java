package io.ebeaninternal.server.deploy.parse;

import io.ebean.config.ServerConfig;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedPropertyFactory;


/**
 * Read the deployment annotations for the bean.
 */
public class ReadAnnotations {

  private final ReadAnnotationConfig readConfig;

  public ReadAnnotations(GeneratedPropertyFactory generatedPropFactory, String asOfViewSuffix, String versionsBetweenSuffix, ServerConfig serverConfig) {
    this.readConfig = new ReadAnnotationConfig(generatedPropFactory, asOfViewSuffix, versionsBetweenSuffix, serverConfig);
  }

  /**
   * Read the initial non-relationship annotations included Id and EmbeddedId.
   * <p>
   * We then have enough to create BeanTables which are used in readAssociations
   * to resolve the relationships etc.
   * </p>
   */
  public void readInitial(DeployBeanInfo<?> info) {
    try {
      new AnnotationClass(info, readConfig).parse();
      new AnnotationFields(info, readConfig).parse();
    } catch (RuntimeException e) {
      throw new RuntimeException("Error reading annotations for " + info, e);
    }
  }

  /**
   * Read and process the associated relationship annotations.
   * <p>
   * These can only be processed after the BeanTables have been created
   * </p>
   * <p>
   * This uses the factory as a call back to get the BeanTable for a given
   * associated bean.
   * </p>
   */
  public void readAssociations(DeployBeanInfo<?> info, BeanDescriptorManager factory) {

    try {

      new AnnotationAssocOnes(info, readConfig, factory).parse();
      new AnnotationAssocManys(info, readConfig, factory).parse();

      // read the Sql annotations last because they may be
      // dependent on field level annotations
      new AnnotationSql(info, readConfig).parse();

      new AnnotationClass(info, readConfig).parseAttributeOverride();
      info.getDescriptor().postAnnotations();

    } catch (RuntimeException e) {
      throw new RuntimeException("Error reading annotations for " + info, e);
    }
  }
}
