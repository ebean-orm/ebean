package io.ebean.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.annotation.MutationDetection;
import io.ebean.annotation.PersistBatch;
import io.ebean.config.dbplatform.IdType;
import io.ebean.datasource.DataSourceConfig;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConfigTest {

  @Test
  public void testLoadFromEbeanProperties() {

    DatabaseConfig config = new DatabaseConfig();
    config.loadFromProperties();

    assertEquals(PersistBatch.NONE, config.getPersistBatch());
    assertNotNull(config.getProperties());
  }

  @Test
  public void evalPropertiesInput() {

    String home = System.getenv("HOME");

    Properties props = new Properties();
    props.setProperty("ddl.initSql", "${user.home}" + fileSeparator + "initSql");

    DatabaseConfig config = new DatabaseConfig();
    config.loadFromProperties(props);

    String ddlInitSql = config.getDdlInitSql();
    assertThat(ddlInitSql).isEqualTo(home + fileSeparator + "initSql");
  }

  @Test
  public void testLoadWithProperties() {

    DatabaseConfig config = new DatabaseConfig();
    config.setPersistBatch(PersistBatch.NONE);
    config.setPersistBatchOnCascade(PersistBatch.NONE);
    config.setAutoReadOnlyDataSource(false);
    config.setReadOnlyDataSource(null);
    config.setReadOnlyDataSourceConfig(new DataSourceConfig());

    Properties props = new Properties();
    props.setProperty("persistBatch", "ALL");
    props.setProperty("persistBatchOnCascade", "ALL");
    props.setProperty("dbuuid", "binary");
    props.setProperty("jdbcFetchSizeFindEach", "42");
    props.setProperty("jdbcFetchSizeFindList", "43");
    props.setProperty("backgroundExecutorShutdownSecs", "98");
    props.setProperty("backgroundExecutorSchedulePoolSize", "4");
    props.setProperty("dbOffline", "true");
    props.setProperty("jsonDateTime", "MILLIS");
    props.setProperty("jsonDate", "MILLIS");
    props.setProperty("jsonMutationDetection", "NONE");
    props.setProperty("autoReadOnlyDataSource", "true");
    props.setProperty("disableL2Cache", "true");
    props.setProperty("notifyL2CacheInForeground", "true");
    props.setProperty("idType", "SEQUENCE");
    props.setProperty("mappingLocations", "classpath:/foo;bar");
    props.setProperty("namingConvention", "io.ebean.config.MatchingNamingConvention");
    props.setProperty("idGeneratorAutomatic", "true");
    props.setProperty("enabledL2Regions", "r0,users,orgs");
    props.setProperty("caseSensitiveCollation", "false");
    props.setProperty("loadModuleInfo", "true");
    props.setProperty("forUpdateNoKey", "true");
    props.setProperty("defaultServer", "false");
    props.setProperty("skipDataSourceCheck", "true");

    props.setProperty("queryPlan.enable", "true");
    props.setProperty("queryPlan.thresholdMicros", "10000");
    props.setProperty("queryPlan.capture", "true");
    props.setProperty("queryPlan.capturePeriodSecs", "42");
    props.setProperty("queryPlan.captureMaxTimeMillis", "560");
    props.setProperty("queryPlan.captureMaxCount", "7");

    config.loadFromProperties(props);

    assertFalse(config.isDefaultServer());
    assertTrue(config.isDisableL2Cache());
    assertTrue(config.isNotifyL2CacheInForeground());
    assertTrue(config.isDbOffline());
    assertTrue(config.isAutoReadOnlyDataSource());
    assertTrue(config.isAutoLoadModuleInfo());
    assertTrue(config.skipDataSourceCheck());

    assertTrue(config.isIdGeneratorAutomatic());
    assertFalse(config.getPlatformConfig().isCaseSensitiveCollation());
    assertTrue(config.getPlatformConfig().isForUpdateNoKey());

    assertThat(config.getNamingConvention()).isInstanceOf(MatchingNamingConvention.class);

    assertEquals(MutationDetection.NONE, config.getJsonMutationDetection());
    config.setJsonMutationDetection(MutationDetection.SOURCE);
    assertEquals(MutationDetection.SOURCE, config.getJsonMutationDetection());
    assertEquals(IdType.SEQUENCE, config.getIdType());
    assertEquals(PersistBatch.ALL, config.getPersistBatch());
    assertEquals(PersistBatch.ALL, config.getPersistBatchOnCascade());
    assertEquals(PlatformConfig.DbUuid.BINARY, config.getPlatformConfig().getDbUuid());
    assertEquals(JsonConfig.DateTime.MILLIS, config.getJsonDateTime());
    assertEquals(JsonConfig.Date.MILLIS, config.getJsonDate());

    assertEquals("r0,users,orgs", config.getEnabledL2Regions());

    assertEquals(42, config.getJdbcFetchSizeFindEach());
    assertEquals(43, config.getJdbcFetchSizeFindList());
    assertEquals(4, config.getBackgroundExecutorSchedulePoolSize());
    assertEquals(98, config.getBackgroundExecutorShutdownSecs());

    assertTrue(config.isQueryPlanEnable());
    assertEquals(10000, config.getQueryPlanThresholdMicros());
    assertTrue(config.isQueryPlanCapture());
    assertEquals(42, config.getQueryPlanCapturePeriodSecs());
    assertEquals(560, config.getQueryPlanCaptureMaxTimeMillis());
    assertEquals(7, config.getQueryPlanCaptureMaxCount());

    assertThat(config.getMappingLocations()).containsExactly("classpath:/foo","bar");

    config.setPersistBatch(PersistBatch.NONE);
    config.setPersistBatchOnCascade(PersistBatch.NONE);

    Properties props1 = new Properties();
    props1.setProperty("ebean.persistBatch", "ALL");
    props1.setProperty("ebean.persistBatchOnCascade", "ALL");

    config.setNotifyL2CacheInForeground(true);
    config.setDisableL2Cache(true);
    props1.setProperty("ebean.disableL2Cache", "false");
    props1.setProperty("ebean.notifyL2CacheInForeground", "false");

    config.loadFromProperties(props1);
    assertFalse(config.isDisableL2Cache());
    assertFalse(config.isNotifyL2CacheInForeground());

    assertEquals(PersistBatch.ALL, config.getPersistBatch());
    assertEquals(PersistBatch.ALL, config.getPersistBatchOnCascade());

    config.setEnabledL2Regions("r0,orgs");
    assertEquals("r0,orgs", config.getEnabledL2Regions());
  }

  @Test
  public void test_defaults() {

    DatabaseConfig config = new DatabaseConfig();
    assertTrue(config.isIdGeneratorAutomatic());
    assertTrue(config.isDefaultServer());
    assertFalse(config.isAutoPersistUpdates());
    assertFalse(config.skipDataSourceCheck());

    config.setIdGeneratorAutomatic(false);
    assertFalse(config.isIdGeneratorAutomatic());
    assertEquals(JsonConfig.DateTime.ISO8601, config.getJsonDateTime());
    assertEquals(JsonConfig.Date.ISO8601, config.getJsonDate());
    assertEquals(MutationDetection.HASH, config.getJsonMutationDetection());
    assertTrue(config.getPlatformConfig().isCaseSensitiveCollation());
    assertTrue(config.isAutoLoadModuleInfo());

    assertFalse(config.isQueryPlanEnable());
    assertEquals(Long.MAX_VALUE, config.getQueryPlanThresholdMicros());
    assertFalse(config.isQueryPlanCapture());
    assertEquals(600, config.getQueryPlanCapturePeriodSecs());
    assertEquals(10000L, config.getQueryPlanCaptureMaxTimeMillis());
    assertEquals(10, config.getQueryPlanCaptureMaxCount());

    config.setLoadModuleInfo(false);
    assertFalse(config.isAutoLoadModuleInfo());
    config.setAutoPersistUpdates(true);
    assertTrue(config.isAutoPersistUpdates());
    config.setSkipDataSourceCheck(true);
    assertTrue(config.skipDataSourceCheck());
  }

  @Test
  public void test_putServiceObject() {

    ObjectMapper objectMapper = new ObjectMapper();

    DatabaseConfig config = new DatabaseConfig();
    config.putServiceObject(objectMapper);

    ObjectMapper mapper0 = config.getServiceObject(ObjectMapper.class);
    ObjectMapper mapper1 = (ObjectMapper)config.getServiceObject("objectMapper");

    assertThat(objectMapper).isSameAs(mapper0);
    assertThat(objectMapper).isSameAs(mapper1);
  }
}
