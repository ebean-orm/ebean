package io.ebean.config;

/**
 * Provides a ServiceLoader based mechanism to configure a DatabaseConfig.
 * <p>
 * Provide an implementation and register it via the standard Java ServiceLoader mechanism
 * via a file at <code>META-INF/services/io.ebean.config.DatabaseConfigProvider</code>.
 * </p>
 * <p>
 * If you are using a DI container like Spring or Guice you are unlikely to use this but instead use a
 * spring specific configuration.  When we are not using a DI container we may use this mechanism to
 * explicitly register the entity beans and avoid classpath scanning.
 * </p>
 * <pre>{@code
 *
 * public class EbeanConfigProvider implements DatabaseConfigProvider {
 *
 *   @Override
 *   public void apply(DatabaseConfig config) {
 *
 *     // register the entity bean classes explicitly
 *     config.addClass(Customer.class);
 *     config.addClass(User.class);
 *     ...
 *   }
 * }
 *
 * }</pre>
 */
public interface DatabaseConfigProvider {

  /**
   * Apply the configuration to the DatabaseConfig.
   * <p>
   * Typically we explicitly register entity bean classes and thus avoid classpath scanning.
   * </p>
   */
  void apply(DatabaseConfig config);
}
