package io.ebean.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Loads local Redis connection settings for integration tests from {@code redis-local.yml}.
 * <p>
 * The YAML file supports multiple documents separated by {@code ---}, one per deployment mode.
 * System properties such as {@code -Debean.redis.server=127.0.0.1} override file values via
 * {@link RedisConfig#loadProperties(Properties)}.
 * </p>
 */
final class RedisLocalTestSupport {

  private static final String CONFIG_FILE = "/redis-local.yml";

  private RedisLocalTestSupport() {
  }

  static boolean isConfigured() {
    return RedisLocalTestSupport.class.getResource(CONFIG_FILE) != null;
  }

  static RedisConfig standaloneConfig() {
    return configForMode(RedisConfig.Mode.STANDALONE);
  }

  static RedisConfig sentinelConfig() {
    return configForMode(RedisConfig.Mode.SENTINEL);
  }

  static RedisConfig configForMode(RedisConfig.Mode mode) {
    RedisConfig config = new RedisConfig();
    config.loadProperties(propertiesForMode(mode));
    return config;
  }

  static Properties propertiesForMode(RedisConfig.Mode mode) {
    for (Properties document : loadDocuments()) {
      RedisConfig.Mode documentMode = modeOf(document);
      if (documentMode == mode) {
        return document;
      }
    }
    throw new IllegalStateException(
      "No document for mode " + mode + " in redis-local.yml (expected ebean.redis.mode or ebean.redis.model)");
  }

  static List<Properties> loadDocuments() {
    try (InputStream in = RedisLocalTestSupport.class.getResourceAsStream(CONFIG_FILE)) {
      if (in == null) {
        return List.of();
      }
      String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      List<Properties> documents = new ArrayList<>();
      for (String document : splitDocuments(content)) {
        documents.add(parseRedisDocument(document));
      }
      return documents;
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
    }
  }

  static List<String> splitDocuments(String content) {
    List<String> documents = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    for (String line : content.split("\n", -1)) {
      if ("---".equals(line.trim())) {
        if (current.length() > 0) {
          documents.add(current.toString());
          current = new StringBuilder();
        }
      } else {
        current.append(line).append('\n');
      }
    }
    if (current.length() > 0) {
      documents.add(current.toString());
    }
    return documents;
  }

  /**
   * Parse a single YAML document with {@code ebean.redis} settings.
   */
  static Properties parseRedisDocument(String document) {
    Properties properties = new Properties();
    boolean inEbean = false;
    boolean inRedis = false;
    for (String line : document.split("\n")) {
      String trimmed = line.trim();
      if (trimmed.isEmpty() || trimmed.startsWith("#")) {
        continue;
      }
      if ("ebean:".equals(trimmed)) {
        inEbean = true;
        inRedis = false;
        continue;
      }
      if (inEbean && "redis:".equals(trimmed)) {
        inRedis = true;
        continue;
      }
      if (!inRedis || !trimmed.contains(":")) {
        continue;
      }
      int colon = trimmed.indexOf(':');
      String key = trimmed.substring(0, colon).trim();
      String value = trimmed.substring(colon + 1).trim();
      if ("model".equals(key)) {
        key = "mode";
      }
      properties.setProperty("ebean.redis." + key, value);
    }
    return properties;
  }

  static boolean isReachable(RedisConfig config) {
    try (Pool<Jedis> pool = config.createPool();
         Jedis jedis = pool.getResource()) {
      return "PONG".equalsIgnoreCase(jedis.ping());
    } catch (Exception e) {
      return false;
    }
  }

  static String testKey(String prefix) {
    return "ebean-redis-it:" + prefix + ":" + System.nanoTime();
  }

  private static RedisConfig.Mode modeOf(Properties document) {
    String mode = document.getProperty("ebean.redis.mode");
    if (mode == null || mode.isBlank()) {
      throw new IllegalStateException("Missing ebean.redis.mode in redis-local.yml document: " + document);
    }
    return RedisConfig.parseMode(mode);
  }
}
