package com.avaje.ebeaninternal.server.core;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.config.ExternalTransactionManager;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebeaninternal.api.SpiBackgroundExecutor;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.autofetch.AutoFetchManager;
import com.avaje.ebeaninternal.server.autofetch.AutoFetchManagerFactory;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;
import com.avaje.ebeaninternal.server.deploy.DeployOrmXml;
import com.avaje.ebeaninternal.server.deploy.parse.DeployCreateProperties;
import com.avaje.ebeaninternal.server.deploy.parse.DeployInherit;
import com.avaje.ebeaninternal.server.deploy.parse.DeployUtil;
import com.avaje.ebeaninternal.server.expression.DefaultExpressionFactory;
import com.avaje.ebeaninternal.server.lib.sql.DataSourcePool;
import com.avaje.ebeaninternal.server.persist.Binder;
import com.avaje.ebeaninternal.server.persist.DefaultPersister;
import com.avaje.ebeaninternal.server.query.CQueryEngine;
import com.avaje.ebeaninternal.server.query.DefaultOrmQueryEngine;
import com.avaje.ebeaninternal.server.query.DefaultRelationalQueryEngine;
import com.avaje.ebeaninternal.server.resource.ResourceManager;
import com.avaje.ebeaninternal.server.resource.ResourceManagerFactory;
import com.avaje.ebeaninternal.server.text.json.DJsonContext;
import com.avaje.ebeaninternal.server.transaction.AutoCommitTransactionManager;
import com.avaje.ebeaninternal.server.transaction.DefaultTransactionScopeManager;
import com.avaje.ebeaninternal.server.transaction.ExternalTransactionScopeManager;
import com.avaje.ebeaninternal.server.transaction.JtaTransactionManager;
import com.avaje.ebeaninternal.server.transaction.TransactionManager;
import com.avaje.ebeaninternal.server.transaction.TransactionScopeManager;
import com.avaje.ebeaninternal.server.type.DefaultTypeManager;
import com.avaje.ebeaninternal.server.type.TypeManager;
import com.fasterxml.jackson.core.JsonFactory;

/**
 * Used to extend the ServerConfig with additional objects used to configure and
 * construct an EbeanServer.
 * 
 * @author rbygrave
 */
public class InternalConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(InternalConfiguration.class);

  private final ServerConfig serverConfig;

  private final BootupClasses bootupClasses;

  private final DeployInherit deployInherit;

  private final ResourceManager resourceManager;

  private final DeployOrmXml deployOrmXml;

  private final TypeManager typeManager;

  private final Binder binder;

  private final DeployCreateProperties deployCreateProperties;

  private final DeployUtil deployUtil;

  private final BeanDescriptorManager beanDescriptorManager;

  private final TransactionManager transactionManager;

  private final TransactionScopeManager transactionScopeManager;

  private final CQueryEngine cQueryEngine;

  private final ClusterManager clusterManager;

  private final ServerCacheManager cacheManager;

  private final ExpressionFactory expressionFactory;

  private final SpiBackgroundExecutor backgroundExecutor;

  private final PstmtBatch pstmtBatch;

  private final XmlConfig xmlConfig;

  private final JsonFactory jsonFactory;
  
  public InternalConfiguration(XmlConfig xmlConfig, ClusterManager clusterManager,
      ServerCacheManager cacheManager, SpiBackgroundExecutor backgroundExecutor,
      ServerConfig serverConfig, BootupClasses bootupClasses, PstmtBatch pstmtBatch) {

    this.jsonFactory = serverConfig.getJsonFactory();
    this.xmlConfig = xmlConfig;
    this.pstmtBatch = pstmtBatch;
    this.clusterManager = clusterManager;
    this.backgroundExecutor = backgroundExecutor;
    this.cacheManager = cacheManager;
    this.serverConfig = serverConfig;
    this.bootupClasses = bootupClasses;
    this.expressionFactory = new DefaultExpressionFactory();

    this.typeManager = new DefaultTypeManager(serverConfig, bootupClasses);
    this.binder = new Binder(typeManager);

    this.resourceManager = ResourceManagerFactory.createResourceManager(serverConfig);
    this.deployOrmXml = new DeployOrmXml(resourceManager.getResourceSource());
    this.deployInherit = new DeployInherit(bootupClasses);

    this.deployCreateProperties = new DeployCreateProperties(typeManager);
    this.deployUtil = new DeployUtil(typeManager, serverConfig);

    this.beanDescriptorManager = new BeanDescriptorManager(this);
    beanDescriptorManager.deploy();

    this.transactionManager = createTransactionManager();

    this.cQueryEngine = new CQueryEngine(serverConfig.getDatabasePlatform(), binder);

    ExternalTransactionManager externalTransactionManager = serverConfig.getExternalTransactionManager();
    if (externalTransactionManager == null && serverConfig.isUseJtaTransactionManager()) {
      externalTransactionManager = new JtaTransactionManager();
    }
    if (externalTransactionManager != null) {
      externalTransactionManager.setTransactionManager(transactionManager);
      this.transactionScopeManager = new ExternalTransactionScopeManager(transactionManager, externalTransactionManager);
      logger.info("Using Transaction Manager [" + externalTransactionManager.getClass() + "]");
    } else {
      this.transactionScopeManager = new DefaultTransactionScopeManager(transactionManager);
    }

  }
  
  /**
   * Create the TransactionManager taking into account autoCommit mode.
   */
  private TransactionManager createTransactionManager() {
    
    if (isAutoCommitMode()) {
      return new AutoCommitTransactionManager(clusterManager, backgroundExecutor, serverConfig, beanDescriptorManager, this.getBootupClasses());
    }
    
    return new TransactionManager(clusterManager, backgroundExecutor, serverConfig, beanDescriptorManager, this.getBootupClasses());
  }
  
  /**
   * Return true if autoCommit mode is on.
   */
  private boolean isAutoCommitMode() {
    if (serverConfig.isAutoCommitMode()) {
      // explicitly set
      return true;
    }
    DataSource dataSource = serverConfig.getDataSource();
    if (dataSource instanceof DataSourcePool && ((DataSourcePool)dataSource).getAutoCommit()) {
      // We know the DataSourcePool is using autoCommit
      return true;
    }
    return false;
  }

  public JsonContext createJsonContext(SpiEbeanServer server) {

    return new DJsonContext(server, jsonFactory);
  }

  public XmlConfig getXmlConfig() {
    return xmlConfig;
  }

  public AutoFetchManager createAutoFetchManager(SpiEbeanServer server) {
    return AutoFetchManagerFactory.create(server, serverConfig, resourceManager);
  }

  public RelationalQueryEngine createRelationalQueryEngine() {
    return new DefaultRelationalQueryEngine(binder, serverConfig.getDatabaseBooleanTrue());
  }

  public OrmQueryEngine createOrmQueryEngine() {
    return new DefaultOrmQueryEngine(beanDescriptorManager, cQueryEngine);
  }

  public Persister createPersister(SpiEbeanServer server) {
    return new DefaultPersister(server, binder, beanDescriptorManager, pstmtBatch);
  }

  public PstmtBatch getPstmtBatch() {
    return pstmtBatch;
  }

  public ServerCacheManager getCacheManager() {
    return cacheManager;
  }

  public BootupClasses getBootupClasses() {
    return bootupClasses;
  }

  public DatabasePlatform getDatabasePlatform() {
    return serverConfig.getDatabasePlatform();
  }

  public ServerConfig getServerConfig() {
    return serverConfig;
  }

  public ExpressionFactory getExpressionFactory() {
    return expressionFactory;
  }

  public TypeManager getTypeManager() {
    return typeManager;
  }

  public Binder getBinder() {
    return binder;
  }

  public BeanDescriptorManager getBeanDescriptorManager() {
    return beanDescriptorManager;
  }

  public DeployInherit getDeployInherit() {
    return deployInherit;
  }

  public ResourceManager getResourceManager() {
    return resourceManager;
  }

  public DeployOrmXml getDeployOrmXml() {
    return deployOrmXml;
  }

  public DeployCreateProperties getDeployCreateProperties() {
    return deployCreateProperties;
  }

  public DeployUtil getDeployUtil() {
    return deployUtil;
  }

  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  public TransactionScopeManager getTransactionScopeManager() {
    return transactionScopeManager;
  }

  public CQueryEngine getCQueryEngine() {
    return cQueryEngine;
  }

  public ClusterManager getClusterManager() {
    return clusterManager;
  }

  public SpiBackgroundExecutor getBackgroundExecutor() {
    return backgroundExecutor;
  }

}
