package io.ebean.redis;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;

/**
 * Deployment configuration for redis.
 */
public class RedisConfig {

  private String server = "localhost";

  private int port = 6379;

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
   * Return a new JedisPool based on the configuration.
   */
  public JedisPool createPool() {
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(maxTotal);
    poolConfig.setMaxIdle(maxIdle);
    poolConfig.setMinIdle(minIdle);
    poolConfig.setMaxWaitMillis(maxWaitMillis);
    poolConfig.setBlockWhenExhausted(blockWhenExhausted);
    return new JedisPool(poolConfig, server, port, timeout, username, password, ssl);
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
    this.server = reader.get("ebean.redis.server", server);
    this.port = reader.getInt("ebean.redis.port", port);
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
      return val != null ? Integer.parseInt(val.trim()) : defaultVal;
    }

    boolean getBool(String key, boolean defaultVal) {
      final String val = get(key, null);
      return val != null ? Boolean.parseBoolean(val.trim()) : defaultVal;
    }
  }

}
