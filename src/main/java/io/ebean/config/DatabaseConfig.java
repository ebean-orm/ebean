package io.ebean.config;

import io.ebean.DatabaseFactory;

/**
 * The configuration used for creating a Database.
 * <p>
 * Used to programmatically construct an Database and optionally register it
 * with the DB singleton.
 * </p>
 * <p>
 * If you just use DB thout this programmatic configuration Ebean will read
 * the application.properties file and take the configuration from there. This usually
 * includes searching the class path and automatically registering any entity
 * classes and listeners etc.
 * </p>
 * <pre>{@code
 *
 * DatabaseConfig config = new DatabaseConfig();
 *
 * // read the ebean.properties and load
 * // those settings into this serverConfig object
 * config.loadFromProperties();
 *
 * // explicitly register the entity beans to avoid classpath scanning
 * config.addClass(Customer.class);
 * config.addClass(User.class);
 *
 * Database db = DatabaseFactory.create(config);
 *
 * }</pre>
 *
 * <p>
 * Note that ServerConfigProvider provides a standard Java ServiceLoader mechanism that can
 * be used to apply configuration to the DatabaseConfig.
 * </p>
 *
 * @author emcgreal
 * @author rbygrave
 * @see DatabaseFactory
 */
public class DatabaseConfig extends ServerConfig {

}
