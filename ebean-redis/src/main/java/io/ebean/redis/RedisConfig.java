package io.ebean.redis;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.util.Pool;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Deployment configuration for redis.
 */
public class RedisConfig {

  /**
   * Redis deployment mode.
   */
  public enum Mode {
    /** Single redis server (default). */
    STANDALONE,
    /** Redis sentinel high availability. */
    SENTINEL
  }

  private Mode mode = Mode.STANDALONE;
  private String server = "localhost";
  private int port = 6379;
  private String masterName;
  private Set<String> sentinels = Set.of();
  private int maxTotal = 200;
  private int maxIdle = 200;
  private int minIdle = 1;
  private long maxWaitMillis = -1L;
  private boolean blockWhenExhausted = true;
  private int timeout = 2000;
  private String username;
  private String password;
  private boolean ssl;

  /**
   * Return a new connection pool based on the configuration.
   */
  public Pool<Jedis> createPool() {
    JedisPoolConfig poolConfig = poolConfig();
    if (mode == Mode.SENTINEL) {
      return createSentinelPool(poolConfig);
    }
    return createStandalonePool(poolConfig);
  }

  private JedisPoolConfig poolConfig() {
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(maxTotal);
    poolConfig.setMaxIdle(maxIdle);
    poolConfig.setMinIdle(minIdle);
    poolConfig.setMaxWait(Duration.ofMillis(maxWaitMillis));
    poolConfig.setBlockWhenExhausted(blockWhenExhausted);
    return poolConfig;
  }

  private JedisPool createStandalonePool(JedisPoolConfig poolConfig) {
    return new JedisPool(poolConfig, server, port, timeout, username, password, ssl);
  }

  private JedisSentinelPool createSentinelPool(JedisPoolConfig poolConfig) {
    if (masterName == null || masterName.isBlank()) {
      throw new IllegalStateException("ebean.redis.masterName must be set when mode is sentinel");
    }
    if (sentinels.isEmpty()) {
      throw new IllegalStateException("ebean.redis.sentinels must be set when mode is sentinel");
    }
    if (ssl) {
      JedisClientConfig masterConfig = DefaultJedisClientConfig.builder()
        .connectionTimeoutMillis(timeout)
        .socketTimeoutMillis(timeout)
        .user(username)
        .password(password)
        .ssl(true)
        .build();
      JedisClientConfig sentinelConfig = DefaultJedisClientConfig.builder()
        .connectionTimeoutMillis(timeout)
        .socketTimeoutMillis(timeout)
        .user(username)
        .password(password)
        .ssl(true)
        .build();
      return new JedisSentinelPool(masterName, parseSentinelHosts(sentinels), poolConfig, masterConfig, sentinelConfig);
    }
    return new JedisSentinelPool(masterName, sentinels, poolConfig, timeout, timeout, username, password, 0, null);
  }

  static Set<String> parseSentinels(String value) {
    if (value == null || value.isBlank()) {
      return Set.of();
    }
    Set<String> result = new LinkedHashSet<>();
    for (String part : value.split(",")) {
      String trimmed = part.trim();
      if (!trimmed.isEmpty()) {
        result.add(trimmed);
      }
    }
    return Set.copyOf(result);
  }

  static Set<HostAndPort> parseSentinelHosts(Set<String> sentinels) {
    Set<HostAndPort> hosts = new LinkedHashSet<>();
    for (String sentinel : sentinels) {
      hosts.add(HostAndPort.from(sentinel));
    }
    return Set.copyOf(hosts);
  }

  static Mode parseMode(String value) {
    if (value == null || value.isBlank()) {
      return Mode.STANDALONE;
    }
    String mode = value.trim().toLowerCase();
    if ("standalone".equals(mode)) {
      return Mode.STANDALONE;
    }
    if ("sentinel".equals(mode)) {
      return Mode.SENTINEL;
    }
    throw new IllegalArgumentException("Unknown ebean.redis.mode: " + value);
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getMasterName() {
    return masterName;
  }

  public void setMasterName(String masterName) {
    this.masterName = masterName;
  }

  public Set<String> getSentinels() {
    return sentinels;
  }

  public void setSentinels(Set<String> sentinels) {
    this.sentinels = sentinels != null ? Set.copyOf(sentinels) : Set.of();
  }

  public int getMaxTotal() {
    return maxTotal;
  }

  public void setMaxTotal(int maxTotal) {
    this.maxTotal = maxTotal;
  }

  public int getMaxIdle() {
    return maxIdle;
  }

  public void setMaxIdle(int maxIdle) {
    this.maxIdle = maxIdle;
  }

  public int getMinIdle() {
    return minIdle;
  }

  public void setMinIdle(int minIdle) {
    this.minIdle = minIdle;
  }

  public long getMaxWaitMillis() {
    return maxWaitMillis;
  }

  public void setMaxWaitMillis(long maxWaitMillis) {
    this.maxWaitMillis = maxWaitMillis;
  }

  public boolean isBlockWhenExhausted() {
    return blockWhenExhausted;
  }

  public void setBlockWhenExhausted(boolean blockWhenExhausted) {
    this.blockWhenExhausted = blockWhenExhausted;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isSsl() {
    return ssl;
  }

  public void setSsl(boolean ssl) {
    this.ssl = ssl;
  }

  public void loadProperties(Properties properties) {
    Reader reader = new Reader(properties);
    this.mode = parseMode(reader.get("ebean.redis.mode", null));
    this.server = reader.get("ebean.redis.server", server);
    this.port = reader.getInt("ebean.redis.port", port);
    this.masterName = reader.get("ebean.redis.masterName", masterName);
    this.sentinels = parseSentinels(reader.get("ebean.redis.sentinels", null));
    this.ssl = reader.getBool("ebean.redis.ssl", ssl);
    this.minIdle = reader.getInt("ebean.redis.minIdle", minIdle);
    this.maxIdle = reader.getInt("ebean.redis.maxIdle", maxIdle);
    this.maxTotal = reader.getInt("ebean.redis.maxTotal", maxTotal);
    this.maxWaitMillis = reader.getLong("ebean.redis.maxWaitMillis", maxWaitMillis);
    this.timeout = reader.getInt("ebean.redis.timeout", timeout);
    this.username = reader.get("ebean.redis.username", username);
    this.password = reader.get("ebean.redis.password", password);
    this.blockWhenExhausted = reader.getBool("ebean.redis.blockWhenExhausted", blockWhenExhausted);
  }

  private static class Reader {

    private final Properties properties;

    Reader(Properties properties) {
      this.properties = (properties != null) ? properties : new Properties();
    }

    String get(String key, String defaultVal) {
      return System.getProperty(key, properties.getProperty(key, defaultVal));
    }

    int getInt(String key, int defaultVal) {
      final String val = get(key, null);
      return val != null ? Integer.parseInt(val.trim()) : defaultVal;
    }

    long getLong(String key, long defaultVal) {
      final String val = get(key, null);
      return val != null ? Long.parseLong(val.trim()) : defaultVal;
    }

    boolean getBool(String key, boolean defaultVal) {
      final String val = get(key, null);
      return val != null ? Boolean.parseBoolean(val.trim()) : defaultVal;
    }
  }
}
