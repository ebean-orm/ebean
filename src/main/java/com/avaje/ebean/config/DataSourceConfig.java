package com.avaje.ebean.config;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

import com.avaje.ebean.Transaction;
import com.avaje.ebean.util.StringHelper;

/**
 * Used to config a DataSource when using the internal Ebean DataSource
 * implementation.
 * <p>
 * If a DataSource instance is already defined via
 * {@link ServerConfig#setDataSource(javax.sql.DataSource)} or defined as JNDI
 * dataSource via {@link ServerConfig#setDataSourceJndiName(String)} then those
 * will used and not this DataSourceConfig.
 * </p>
 */
public class DataSourceConfig {

  private String url;

  private String username;

  private String password;

  private String driver;

  private int minConnections = 2;

  private int maxConnections = 20;

  private int isolationLevel = Transaction.READ_COMMITTED;

  private boolean autoCommit;
  
  private String heartbeatSql;
  
  private int heartbeatFreqSecs = 30;
  
  private int heartbeatTimeoutSeconds = 3;
  
  private boolean captureStackTrace;

  private int maxStackTraceSize = 5;

  private int leakTimeMinutes = 30;

  private int maxInactiveTimeSecs = 720;
  
  private int maxAgeMinutes = 0;
  
  private int trimPoolFreqSecs = 59;

  private int pstmtCacheSize = 20;
  
  private int cstmtCacheSize = 20;

  private int waitTimeoutMillis = 1000;
  
  private String poolListener;

  private boolean offline;
  
  protected Map<String, String> customProperties;

  /**
   * Return the connection URL.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Set the connection URL.
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Return the database username.
   */
  public String getUsername() {
    return username;
  }

  /**
   * Set the database username.
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Return the database password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Set the database password.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Return the database driver.
   */
  public String getDriver() {
    return driver;
  }

  /**
   * Set the database driver.
   */
  public void setDriver(String driver) {
    this.driver = driver;
  }

  /**
   * Return the transaction isolation level.
   */
  public int getIsolationLevel() {
    return isolationLevel;
  }

  /**
   * Set the transaction isolation level.
   */
  public void setIsolationLevel(int isolationLevel) {
    this.isolationLevel = isolationLevel;
  }
  
  /**
   * Return autoCommit setting.
   */
  public boolean isAutoCommit() {
    return autoCommit;
  }

  /**
   * Set to true to turn on autoCommit.
   */
  public void setAutoCommit(boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  /**
   * Return the minimum number of connections the pool should maintain.
   */
  public int getMinConnections() {
    return minConnections;
  }

  /**
   * Set the minimum number of connections the pool should maintain.
   */
  public void setMinConnections(int minConnections) {
    this.minConnections = minConnections;
  }

  /**
   * Return the maximum number of connections the pool can reach.
   */
  public int getMaxConnections() {
    return maxConnections;
  }

  /**
   * Set the maximum number of connections the pool can reach.
   */
  public void setMaxConnections(int maxConnections) {
    this.maxConnections = maxConnections;
  }

  /**
   * Return a SQL statement used to test the database is accessible.
   * <p>
   * Note that if this is not set then it can get defaulted from the
   * DatabasePlatform.
   * </p>
   */
  public String getHeartbeatSql() {
    return heartbeatSql;
  }

  /**
   * Set a SQL statement used to test the database is accessible.
   * <p>
   * Note that if this is not set then it can get defaulted from the
   * DatabasePlatform.
   * </p>
   */
  public void setHeartbeatSql(String heartbeatSql) {
    this.heartbeatSql = heartbeatSql;
  }

  
  /**
   * Return the heartbeat frequency in seconds.
   * <p>
   * This is the expected frequency in which the DataSource should be checked to
   * make sure it is healthy and trim idle connections.
   * </p>
   */
  public int getHeartbeatFreqSecs() {
    return heartbeatFreqSecs;
  }

  /**
   * Set the expected heartbeat frequency in seconds.
   */
  public void setHeartbeatFreqSecs(int heartbeatFreqSecs) {
    this.heartbeatFreqSecs = heartbeatFreqSecs;
  }
  
  /**
   * Return the heart beat timeout in seconds.
   */
  public int getHeartbeatTimeoutSeconds() {
    return heartbeatTimeoutSeconds;
  }

  /**
   * Set the heart beat timeout in seconds.
   */
  public void setHeartbeatTimeoutSeconds(int heartbeatTimeoutSeconds) {
    this.heartbeatTimeoutSeconds = heartbeatTimeoutSeconds;
  }

  /**
   * Return true if a stack trace should be captured when obtaining a connection
   * from the pool.
   * <p>
   * This can be used to diagnose a suspected connection pool leak.
   * </p>
   * <p>
   * Obviously this has a performance overhead.
   * </p>
   */
  public boolean isCaptureStackTrace() {
    return captureStackTrace;
  }

  /**
   * Set to true if a stack trace should be captured when obtaining a connection
   * from the pool.
   * <p>
   * This can be used to diagnose a suspected connection pool leak.
   * </p>
   * <p>
   * Obviously this has a performance overhead.
   * </p>
   */
  public void setCaptureStackTrace(boolean captureStackTrace) {
    this.captureStackTrace = captureStackTrace;
  }

  /**
   * Return the max size for reporting stack traces on busy connections.
   */
  public int getMaxStackTraceSize() {
    return maxStackTraceSize;
  }

  /**
   * Set the max size for reporting stack traces on busy connections.
   */
  public void setMaxStackTraceSize(int maxStackTraceSize) {
    this.maxStackTraceSize = maxStackTraceSize;
  }

  /**
   * Return the time in minutes after which a connection could be considered to
   * have leaked.
   */
  public int getLeakTimeMinutes() {
    return leakTimeMinutes;
  }

  /**
   * Set the time in minutes after which a connection could be considered to
   * have leaked.
   */
  public void setLeakTimeMinutes(int leakTimeMinutes) {
    this.leakTimeMinutes = leakTimeMinutes;
  }

  /**
   * Return the size of the PreparedStatement cache (per connection).
   */
  public int getPstmtCacheSize() {
    return pstmtCacheSize;
  }

  /**
   * Set the size of the PreparedStatement cache (per connection).
   */
  public void setPstmtCacheSize(int pstmtCacheSize) {
    this.pstmtCacheSize = pstmtCacheSize;
  }

  /**
   * Return the size of the CallableStatement cache (per connection).
   */
  public int getCstmtCacheSize() {
    return cstmtCacheSize;
  }

  /**
   * Set the size of the CallableStatement cache (per connection).
   */
  public void setCstmtCacheSize(int cstmtCacheSize) {
    this.cstmtCacheSize = cstmtCacheSize;
  }

  /**
   * Return the time in millis to wait for a connection before timing out once
   * the pool has reached its maximum size.
   */
  public int getWaitTimeoutMillis() {
    return waitTimeoutMillis;
  }

  /**
   * Set the time in millis to wait for a connection before timing out once the
   * pool has reached its maximum size.
   */
  public void setWaitTimeoutMillis(int waitTimeoutMillis) {
    this.waitTimeoutMillis = waitTimeoutMillis;
  }

  /**
   * Return the time in seconds a connection can be idle after which it can be
   * trimmed from the pool.
   * <p>
   * This is so that the pool after a busy period can trend over time back
   * towards the minimum connections.
   * </p>
   */
  public int getMaxInactiveTimeSecs() {
    return maxInactiveTimeSecs;
  }

  /**
   * Return the maximum age a connection is allowed to be before it is closed.
   * <p>
   * This can be used to close really old connections.
   * </p>
   */
  public int getMaxAgeMinutes() {
    return maxAgeMinutes;
  }
  
  /**
   * Set the maximum age a connection can be in minutes.
   */
  public void setMaxAgeMinutes(int maxAgeMinutes) {
    this.maxAgeMinutes = maxAgeMinutes;
  }

  /**
   * Set the time in seconds a connection can be idle after which it can be
   * trimmed from the pool.
   * <p>
   * This is so that the pool after a busy period can trend over time back
   * towards the minimum connections.
   * </p>
   */
  public void setMaxInactiveTimeSecs(int maxInactiveTimeSecs) {
    this.maxInactiveTimeSecs = maxInactiveTimeSecs;
  }

  
  /**
   * Return the minimum time gap between pool trim checks.
   * <p>
   * This defaults to 59 seconds meaning that the pool trim check will run every
   * minute assuming the heart beat check runs every 30 seconds.
   * </p>
   */
  public int getTrimPoolFreqSecs() {
    return trimPoolFreqSecs;
  }

  /**
   * Set the minimum trim gap between pool trim checks.
   */
  public void setTrimPoolFreqSecs(int trimPoolFreqSecs) {
    this.trimPoolFreqSecs = trimPoolFreqSecs;
  }

  /**
   * Return the pool listener.
   */
  public String getPoolListener() {
    return poolListener;
  }

  /**
   * Set a pool listener.
   */
  public void setPoolListener(String poolListener) {
    this.poolListener = poolListener;
  }

  /**
   * Return true if the DataSource should be left offline.
   * <p>
   * This is to support DDL generation etc without having a real database.
   * </p>
   */
  public boolean isOffline() {
    return offline;
  }

  /**
   * Set to true if the DataSource should be left offline.
   * <p>
   * This is to support DDL generation etc without having a real database.
   * </p>
   * <p>
   * Note that you MUST specify the database platform name (oracle, postgres,
   * h2, mysql etc) using {@link ServerConfig#setDatabasePlatformName(String)}
   * when you do this.
   * </p>
   */
  public void setOffline(boolean offline) {
    this.offline = offline;
  }
  
  /**
   * Return a map of custom properties for the jdbc driver connection.
   */
  public Map<String, String> getCustomProperties() {
    return customProperties;
  }

  /**
   * Set custom properties for the jdbc driver connection.
   * 
   * @param customProperties
   */
  public void setCustomProperties(Map<String, String> customProperties) {
    this.customProperties = customProperties;
  }

  /**
   * Load the settings by reading the ebean.properties file.
   *
   * @param serverName name of the server
   */
  public void loadSettings(String serverName) {
    loadSettings(new PropertiesWrapper("datasource", serverName, PropertyMap.defaultProperties()));
  }

  /**
   * Load the settings from the properties supplied.
   * <p>
   * You can use this when you have your own properties to use for configuration.
   * </p>
   *
   * @param properties the properties to configure the datasource
   * @param serverName the name of the specific datasource (optional)
   */
  public void loadSettings(Properties properties, String serverName) {
    PropertiesWrapper dbProps = new PropertiesWrapper("datasource", serverName, properties);
    loadSettings(dbProps);
  }

  /**
   * Load the settings from the PropertiesWrapper.
   */
  public void loadSettings(PropertiesWrapper properties) {

    username = properties.get("username", username);
    password = properties.get("password", password);
    driver = properties.get("driver", properties.get("databaseDriver", driver));
    url = properties.get("url", properties.get("databaseUrl", url));

    autoCommit = properties.getBoolean("autoCommit", autoCommit);
    captureStackTrace = properties.getBoolean("captureStackTrace", captureStackTrace);
    maxStackTraceSize = properties.getInt("maxStackTraceSize", maxStackTraceSize);
    leakTimeMinutes = properties.getInt("leakTimeMinutes", leakTimeMinutes);
    maxInactiveTimeSecs = properties.getInt("maxInactiveTimeSecs", maxInactiveTimeSecs);
    trimPoolFreqSecs = properties.getInt("trimPoolFreqSecs", trimPoolFreqSecs);
    maxAgeMinutes = properties.getInt("maxAgeMinutes", maxAgeMinutes);

    minConnections = properties.getInt("minConnections", minConnections);
    maxConnections = properties.getInt("maxConnections", maxConnections);
    pstmtCacheSize = properties.getInt("pstmtCacheSize", pstmtCacheSize);
    cstmtCacheSize = properties.getInt("cstmtCacheSize", cstmtCacheSize);

    waitTimeoutMillis = properties.getInt("waitTimeout", waitTimeoutMillis);

    heartbeatSql = properties.get("heartbeatSql", heartbeatSql);
    heartbeatTimeoutSeconds =  properties.getInt("heartbeatTimeoutSeconds", heartbeatTimeoutSeconds);
    poolListener = properties.get("poolListener", poolListener);
    offline = properties.getBoolean("offline", offline);

    String isoLevel = properties.get("isolationlevel", getTransactionIsolationLevel(isolationLevel));
    this.isolationLevel = getTransactionIsolationLevel(isoLevel);

    String customProperties = properties.get("customProperties", null);
    if (customProperties != null && customProperties.length() > 0) {
      this.customProperties = StringHelper.delimitedToMap(customProperties, ";", "=");
    }

  }

  /**
   * Return the isolation level description from the associated Connection int value.
   */
  public String getTransactionIsolationLevel(int level) {
    switch (level) {
      case Connection.TRANSACTION_NONE : return "NONE";
      case Connection.TRANSACTION_READ_COMMITTED : return "READ_COMMITTED";
      case Connection.TRANSACTION_READ_UNCOMMITTED : return "READ_UNCOMMITTED";
      case Connection.TRANSACTION_REPEATABLE_READ : return "REPEATABLE_READ";
      case Connection.TRANSACTION_SERIALIZABLE : return "SERIALIZABLE";
      default: throw new RuntimeException("Transaction Isolation level [" + level + "] is not known.");
    }
  }

  /**
   * Return the isolation level for a given string description.
   */
  public int getTransactionIsolationLevel(String level) {
    level = level.toUpperCase();
    if (level.startsWith("TRANSACTION")) {
      level = level.substring("TRANSACTION".length());
    }
    level = level.replace("_", "");
    if ("NONE".equalsIgnoreCase(level)) {
      return Connection.TRANSACTION_NONE;
    }
    if ("READCOMMITTED".equalsIgnoreCase(level)) {
      return Connection.TRANSACTION_READ_COMMITTED;
    }
    if ("READUNCOMMITTED".equalsIgnoreCase(level)) {
      return Connection.TRANSACTION_READ_UNCOMMITTED;
    }
    if ("REPEATABLEREAD".equalsIgnoreCase(level)) {
      return Connection.TRANSACTION_REPEATABLE_READ;
    }
    if ("SERIALIZABLE".equalsIgnoreCase(level)) {
      return Connection.TRANSACTION_SERIALIZABLE;
    }

    throw new RuntimeException("Transaction Isolation level [" + level + "] is not known.");
  }
}
