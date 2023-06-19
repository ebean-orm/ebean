package io.ebean.config.dbplatform;

/**
 * The database platforms treat values that will exceed length or precision differently.
 * Some will silently truncate varchars, if they are too long. This is fatal, when saving JSONs,
 * because the JSON mostly breaks by this truncation.
 * <p>
 * BindValidators can be implemented depending on the database platform needs.
 * E.g. they can perform length checks of varchars/blobs/clobs/...
 * It would be also possible to implement precision and range checks for timestamps or numeric values.
 * or 'confidental checks' - e.g. detect if all password hashes are SHA256
 *
 * @author Roland Praml, FOCONIS AG
 */
@FunctionalInterface
public interface BindValidatorFactory {

  /**
   * Creates a bindValidator. The <code>propertyDefinition</code> provides information like JDBC-type or DbLength.
   */
  BindValidator create(PropertyDefinition propertyDefinition);

  /**
   * Combines this BindValidatorFactory with another one and returns a new factory.
   */
  default BindValidatorFactory combine(BindValidatorFactory other) {
    return propertyDefinition -> {
      BindValidator validator1 = this.create(propertyDefinition);
      BindValidator validator2 = other.create(propertyDefinition);
      if (validator1 != null && validator2 != null) {
        return value -> {
          validator1.validate(value);
          validator2.validate(value);
        };
      } else if (validator1 != null) {
        return validator1;
      } else {
        return validator2;
      }
    };
  }
}
