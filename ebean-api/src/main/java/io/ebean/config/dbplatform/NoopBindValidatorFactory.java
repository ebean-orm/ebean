package io.ebean.config.dbplatform;

/**
 * noop implementation. Use 'ebean.bindValidatorFactory=io.ebean.config.dbplatform.NoopBindValidatorFactory'.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class NoopBindValidatorFactory implements BindValidatorFactory {

  /**
   * Create BindValidator for length-based properties.
   */
  @Override
  public BindValidator create(PropertyDefinition property) {
    return null;
  }

}
