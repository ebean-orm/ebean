package io.ebean.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.DatabaseBuilder;
import io.ebean.annotation.MutationDetection;
import io.ebean.annotation.PersistBatch;
import io.ebean.config.dbplatform.IdType;
import io.ebean.datasource.DataSourceConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {

  @Test
  void testLoadFromEbeanProperties() {
    var config = new DatabaseConfig().settings();
    config.loadFromProperties();

    assertEquals(PersistBatch.NONE, config.getPersistBatch());
    assertNotNull(config.getProperties());
  }

  @Test
  void evalPropertiesInput() {
    String home = System.getProperty("user.home");
    String fileSeparator = System.getProperty("file.separator");

    Properties props = new Properties();
    props.setProperty("ddl.initSql", "${user.home}" + fileSeparator + "initSql");

    var config = new DatabaseConfig().settings();
    config.loadFromProperties(props);

    String ddlInitSql = config.getDdlInitSql();
    assertThat(ddlInitSql).isEqualTo(home + fileSeparator + "initSql");
  }

  @Test
  void testLoadWithProperties() {
    DatabaseBuilder config = new DatabaseConfig();
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
    props.setProperty("readOnlyDatabase", "true");
    props.setProperty("lengthCheck", "ON");
    props.setProperty("includeLabelInSql", "false");
    props.setProperty("lazyLoadBatchSize", "50");
    props.setProperty("queryBatchSize", "60");
    props.setProperty("shutdownHook", "false");

    props.setProperty("queryPlan.enable", "true");
    props.setProperty("queryPlan.thresholdMicros", "10000");
    props.setProperty("queryPlan.capture", "true");
    props.setProperty("queryPlan.capturePeriodSecs", "42");
    props.setProperty("queryPlan.captureMaxTimeMillis", "560");
    props.setProperty("queryPlan.captureMaxCount", "7");
    props.setProperty("queryPlan.explain", "explain (verbose)");

    config.loadFromProperties(props);

    var settings = config.settings();
    assertFalse(settings.isDefaultServer());
    assertTrue(settings.isDisableL2Cache());
    assertTrue(settings.isNotifyL2CacheInForeground());
    assertTrue(settings.isDbOffline());
    assertTrue(settings.isAutoReadOnlyDataSource());
    assertTrue(settings.isAutoLoadModuleInfo());
    assertTrue(settings.isLoadModuleInfo());
    assertTrue(settings.skipDataSourceCheck());
    assertTrue(settings.readOnlyDatabase());
    assertFalse(settings.shutdownHook());
    assertFalse(settings.isIncludeLabelInSql());
    assertThat(settings.getLengthCheck()).isEqualTo(LengthCheck.ON);
    assertThat(settings.getLazyLoadBatchSize()).isEqualTo(50);
    assertThat(settings.getQueryBatchSize()).isEqualTo(60);

    assertTrue(settings.isIdGeneratorAutomatic());
    assertFalse(settings.getPlatformConfig().isCaseSensitiveCollation());
    assertTrue(settings.getPlatformConfig().isForUpdateNoKey());

    assertThat(settings.getNamingConvention()).isInstanceOf(MatchingNamingConvention.class);

    assertEquals(MutationDetection.NONE, settings.getJsonMutationDetection());
    config.setJsonMutationDetection(MutationDetection.SOURCE);
    assertEquals(MutationDetection.SOURCE, settings.getJsonMutationDetection());
    assertEquals(IdType.SEQUENCE, settings.getIdType());
    assertEquals(PersistBatch.ALL, settings.getPersistBatch());
    assertEquals(PersistBatch.ALL, settings.getPersistBatchOnCascade());
    Assertions.assertEquals(PlatformConfig.DbUuid.BINARY, settings.getPlatformConfig().getDbUuid());
    Assertions.assertEquals(JsonConfig.DateTime.MILLIS, settings.getJsonDateTime());
    assertEquals(JsonConfig.Date.MILLIS, settings.getJsonDate());

    assertEquals("r0,users,orgs", settings.getEnabledL2Regions());

    assertEquals(42, settings.getJdbcFetchSizeFindEach());
    assertEquals(43, settings.getJdbcFetchSizeFindList());
    assertEquals(4, settings.getBackgroundExecutorSchedulePoolSize());
    assertEquals(98, settings.getBackgroundExecutorShutdownSecs());

    assertTrue(settings.isQueryPlanEnable());
    assertEquals(10000, settings.getQueryPlanThresholdMicros());
    assertTrue(settings.isQueryPlanCapture());
    assertEquals(42, settings.getQueryPlanCapturePeriodSecs());
    assertEquals(560, settings.getQueryPlanCaptureMaxTimeMillis());
    assertEquals(7, settings.getQueryPlanCaptureMaxCount());
    assertEquals("explain (verbose)", settings.getQueryPlanExplain());

    assertThat(settings.getMappingLocations()).containsExactly("classpath:/foo","bar");

    config.persistBatch(PersistBatch.NONE)
      .persistBatchOnCascade(PersistBatch.NONE)
      .lengthCheck(LengthCheck.ON)
      .lengthCheck(LengthCheck.UTF8)
      .queryPlanExplain("explain (buffers)");


    assertThat(config.settings().getQueryPlanExplain()).isEqualTo("explain (buffers)");
    Properties props1 = new Properties();
    props1.setProperty("ebean.persistBatch", "ALL");
    props1.setProperty("ebean.persistBatchOnCascade", "ALL");

    config.setNotifyL2CacheInForeground(true);
    config.setDisableL2Cache(true);
    props1.setProperty("ebean.disableL2Cache", "false");
    props1.setProperty("ebean.notifyL2CacheInForeground", "false");

    config.loadFromProperties(props1);
    assertFalse(settings.isDisableL2Cache());
    assertFalse(settings.isNotifyL2CacheInForeground());

    assertEquals(PersistBatch.ALL, settings.getPersistBatch());
    assertEquals(PersistBatch.ALL, settings.getPersistBatchOnCascade());
    assertEquals(LengthCheck.UTF8, settings.getLengthCheck());

    config.setEnabledL2Regions("r0,orgs");
    assertEquals("r0,orgs", settings.getEnabledL2Regions());
  }

  @Test
  void test_defaults() {
    DatabaseBuilder.Settings config = new DatabaseConfig().settings();
    assertTrue(config.isIdGeneratorAutomatic());
    assertTrue(config.isDefaultServer());
    assertTrue(config.shutdownHook());
    assertFalse(config.isAutoPersistUpdates());
    assertFalse(config.skipDataSourceCheck());

    config.setIdGeneratorAutomatic(false);
    assertFalse(config.isIdGeneratorAutomatic());
    assertEquals(JsonConfig.DateTime.ISO8601, config.getJsonDateTime());
    assertEquals(JsonConfig.Date.ISO8601, config.getJsonDate());
    assertEquals(MutationDetection.HASH, config.getJsonMutationDetection());
    assertTrue(config.getPlatformConfig().isCaseSensitiveCollation());
    assertTrue(config.isAutoLoadModuleInfo());
    assertTrue(config.isLoadModuleInfo());
    assertThat(config.getLazyLoadBatchSize()).isEqualTo(100);
    assertThat(config.getQueryBatchSize()).isEqualTo(100);

    assertFalse(config.isQueryPlanEnable());
    assertEquals(Long.MAX_VALUE, config.getQueryPlanThresholdMicros());
    assertFalse(config.isQueryPlanCapture());
    assertEquals(600, config.getQueryPlanCapturePeriodSecs());
    assertEquals(10000L, config.getQueryPlanCaptureMaxTimeMillis());
    assertEquals(10, config.getQueryPlanCaptureMaxCount());
    assertThat(config.getQueryPlanExplain()).isNull();
    assertThat(config.getLengthCheck()).isEqualTo(LengthCheck.OFF);
    assertTrue(config.isIncludeLabelInSql());

    config.shutdownHook(false);
    assertFalse(config.shutdownHook());
    config.setLoadModuleInfo(false);
    assertFalse(config.isAutoLoadModuleInfo());
    assertFalse(config.isLoadModuleInfo());
    config.setAutoPersistUpdates(true);
    assertTrue(config.isAutoPersistUpdates());
    config.setSkipDataSourceCheck(true);
    assertTrue(config.skipDataSourceCheck());
    config.lazyLoadBatchSize(20);
    assertThat(config.getLazyLoadBatchSize()).isEqualTo(20);
    config.queryBatchSize(30);
    assertThat(config.getQueryBatchSize()).isEqualTo(30);
  }

  @Test
  void test_putServiceObject() {
    ObjectMapper objectMapper = new ObjectMapper();

    var config = new DatabaseConfig().settings();
    config.putServiceObject(objectMapper);

    ObjectMapper mapper0 = config.getServiceObject(ObjectMapper.class);
    ObjectMapper mapper1 = (ObjectMapper)config.getServiceObject("objectMapper");

    assertThat(objectMapper).isSameAs(mapper0);
    assertThat(objectMapper).isSameAs(mapper1);
  }
}
