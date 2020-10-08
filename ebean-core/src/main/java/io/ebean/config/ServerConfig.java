package io.ebean.config;

import com.fasterxml.jackson.core.JsonFactory;
import io.avaje.config.Config;
import io.ebean.DatabaseFactory;
import io.ebean.PersistenceContextScope;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.annotation.Encrypted;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
import io.ebean.cache.ServerCachePlugin;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbEncrypt;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.event.BeanFindController;
import io.ebean.event.BeanPersistController;
import io.ebean.event.BeanPersistListener;
import io.ebean.event.BeanPostConstructListener;
import io.ebean.event.BeanPostLoad;
import io.ebean.event.BeanQueryAdapter;
import io.ebean.event.BulkTableEventListener;
import io.ebean.event.ServerConfigStartup;
import io.ebean.event.changelog.ChangeLogListener;
import io.ebean.event.changelog.ChangeLogPrepare;
import io.ebean.event.changelog.ChangeLogRegister;
import io.ebean.event.readaudit.ReadAuditLogger;
import io.ebean.event.readaudit.ReadAuditPrepare;
import io.ebean.migration.MigrationRunner;
import io.ebean.util.StringHelper;

import javax.persistence.EnumType;
import javax.sql.DataSource;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * Deprecated - please migrate to <code>io.ebean.DatabaseConfig</code>.
 *
 * The configuration used for creating a Database.
 * <p>
 * Used to programmatically construct a Database and optionally register it
 * with the DB singleton.
 * </p>
 * <p>
 * If you just use DB without this programmatic configuration DB will read
 * the application.properties file and take the configuration from there. This usually
 * includes searching the class path and automatically registering any entity
 * classes and listeners etc.
 * </p>
 * <pre>{@code
 *
 * ServerConfig config = new ServerConfig();
 *
 * // read the ebean.properties and load
 * // those settings into this serverConfig object
 * config.loadFromProperties();
 *
 * // explicitly register the entity beans to avoid classpath scanning
 * config.addClass(Customer.class);
 * config.addClass(User.class);
 *
 * Database database = DatabaseFactory.create(config);
 *
 * }</pre>
 *
 * <p>
 * Note that ServerConfigProvider provides a standard Java ServiceLoader mechanism that can
 * be used to apply configuration to the ServerConfig.
 * </p>
 *
 * @author emcgreal
 * @author rbygrave
 * @see DatabaseFactory
 */
@Deprecated
public class ServerConfig extends DatabaseConfig {

}
