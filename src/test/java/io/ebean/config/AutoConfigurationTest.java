package io.ebean.config;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.avaje.datasource.DataSourceConfig;
import org.avaje.datasource.DataSourceFactory;
import org.avaje.datasource.DataSourcePool;
import org.junit.*;
import org.tests.model.basic.UTDetail;

import io.ebean.EbeanServerFactory;
import io.ebean.EbeanServerFactory_ServerConfigStart_Test.OnStartupViaClass;
import io.ebean.config.dbplatform.DatabasePlatform;

public class AutoConfigurationTest {

  private String tenant;
  
  @Test
  @Ignore
  public void testAutoconfigure() {
    Properties applicationProperties = new Properties();
    applicationProperties.put("datasource.db.username","SA");
    applicationProperties.put("datasource.db.password","SA");
    applicationProperties.put("datasource.db.databaseUrl","jdbc:h2:mem:testdb");
    applicationProperties.put("datasource.db.databaseDriver", "java.lang.Object");
    applicationProperties.put("ebean.migration.run","true");
    
    ServerConfig config = new ServerConfig() {
      @Override
      public void setDatabasePlatform(DatabasePlatform databasePlatform) {
        super.setDatabasePlatform(databasePlatform);
        getMigrationConfig().setMigrationPath("dbmigration/" + databasePlatform.getPlatform().name().toLowerCase());
        getMigrationConfig().setPlatform(databasePlatform.getPlatform());
        
      }
    };
    config.loadFromProperties(applicationProperties );
    config.addClass(UTDetail.class);
    config.addClass(OnStartupViaClass.class);
    
    config.setTenantMode(TenantMode.DB_WITH_MASTER);
    config.setCurrentTenantProvider(() -> tenant);
    config.setCurrentUserProvider(() -> "I");
    config.setTenantDataSourceProvider(new TenantDataSourceProvider() {
      
      ConcurrentMap<String, DataSourcePool> dataSourcePoolPool = new ConcurrentHashMap<>();
      @Override
      public DataSource dataSource(Object tenantId) {
        if (tenantId == null) {
          return config.getDataSource();
        } else {
         return dataSourcePoolPool.computeIfAbsent((String)tenantId, key-> {
          DataSourceConfig ds = new DataSourceConfig();
          ds.setUrl("jdbc:h2:mem:testdb_" + tenantId);

          ds.setUsername("sa");
          ds.setPassword("sa");
          DataSourceFactory factory = config.service(DataSourceFactory.class);
          return factory.createPool("ten-"+tenantId, ds);
        });
        }
      }
    });
    
    EbeanServerFactory.create(config);

    
  }
}
