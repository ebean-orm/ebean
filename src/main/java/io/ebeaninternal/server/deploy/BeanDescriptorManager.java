package io.ebeaninternal.server.deploy;

import io.ebean.BackgroundExecutor;
import io.ebean.Model;
import io.ebean.RawSqlBuilder;
import io.ebean.annotation.ConstraintMode;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.config.EncryptKey;
import io.ebean.config.EncryptKeyManager;
import io.ebean.config.NamingConvention;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbHistorySupport;
import io.ebean.config.dbplatform.DbIdentity;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.event.changelog.ChangeLogFilter;
import io.ebean.event.changelog.ChangeLogListener;
import io.ebean.event.changelog.ChangeLogPrepare;
import io.ebean.event.changelog.ChangeLogRegister;
import io.ebean.meta.MetricVisitor;
import io.ebean.plugin.BeanType;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.api.ConcurrencyMode;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.TransactionEventTable;
import io.ebeaninternal.server.cache.CacheChangeSet;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.core.InternString;
import io.ebeaninternal.server.core.InternalConfiguration;
import io.ebeaninternal.server.core.Message;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.deploy.id.IdBinderEmbedded;
import io.ebeaninternal.server.deploy.id.IdBinderFactory;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.meta.DeployBeanTable;
import io.ebeaninternal.server.deploy.meta.DeployOrderColumn;
import io.ebeaninternal.server.deploy.meta.DeployTableJoin;
import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;
import io.ebeaninternal.server.deploy.parse.DeployCreateProperties;
import io.ebeaninternal.server.deploy.parse.DeployInherit;
import io.ebeaninternal.server.deploy.parse.DeployUtil;
import io.ebeaninternal.server.deploy.parse.ReadAnnotations;
import io.ebeaninternal.server.deploy.parse.TransientProperties;
import io.ebeaninternal.server.persist.platform.MultiValueBind;
import io.ebeaninternal.server.properties.BeanPropertiesReader;
import io.ebeaninternal.server.properties.BeanPropertyAccess;
import io.ebeaninternal.server.properties.EnhanceBeanPropertyAccess;
import io.ebeaninternal.server.query.CQueryPlan;
import io.ebeaninternal.server.type.ScalarType;
import io.ebeaninternal.server.type.ScalarTypeInteger;
import io.ebeaninternal.server.type.TypeManager;
import io.ebeaninternal.xmlmapping.model.XmAliasMapping;
import io.ebeaninternal.xmlmapping.model.XmColumnMapping;
import io.ebeaninternal.xmlmapping.model.XmEbean;
import io.ebeaninternal.xmlmapping.model.XmEntity;
import io.ebeaninternal.xmlmapping.model.XmNamedQuery;
import io.ebeaninternal.xmlmapping.model.XmRawSql;
import io.ebeanservice.docstore.api.DocStoreBeanAdapter;
import io.ebeanservice.docstore.api.DocStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.MappedSuperclass;
import javax.persistence.PersistenceException;
import javax.persistence.Transient;
import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Creates BeanDescriptors.
 */
public class BeanDescriptorManager implements BeanDescriptorMap {

  private static final Logger logger = LoggerFactory.getLogger(BeanDescriptorManager.class);

  private static final BeanDescComparator beanDescComparator = new BeanDescComparator();

  private final ReadAnnotations readAnnotations;

  private final TransientProperties transientProperties;

  /**
   * Helper to derive inheritance information.
   */
  private final DeployInherit deplyInherit;

  private final BeanPropertyAccess beanPropertyAccess = new EnhanceBeanPropertyAccess();

  private final DeployUtil deployUtil;

  private final PersistControllerManager persistControllerManager;

  private final PostLoadManager postLoadManager;

  private final PostConstructManager postConstructManager;

  private final BeanFinderManager beanFinderManager;

  private final PersistListenerManager persistListenerManager;

  private final BeanQueryAdapterManager beanQueryAdapterManager;

  private final NamingConvention namingConvention;

  private final DeployCreateProperties createProperties;

  private final BeanManagerFactory beanManagerFactory;

  private final ServerConfig serverConfig;

  private final ChangeLogListener changeLogListener;

  private final ChangeLogRegister changeLogRegister;

  private final ChangeLogPrepare changeLogPrepare;

  private final DocStoreFactory docStoreFactory;

  private final MultiValueBind multiValueBind;

  private final TypeManager typeManager;

  private int entityBeanCount;

  private final boolean updateChangesOnly;

  private final BootupClasses bootupClasses;

  private final String serverName;

  private Map<Class<?>, DeployBeanInfo<?>> deployInfoMap = new HashMap<>();

  private final Map<Class<?>, BeanTable> beanTableMap = new HashMap<>();

  private final Map<String, BeanDescriptor<?>> descMap = new HashMap<>();

  private final Map<String, BeanDescriptor<?>> descQueueMap = new HashMap<>();

  private final Map<String, BeanManager<?>> beanManagerMap = new HashMap<>();

  private final Map<String, List<BeanDescriptor<?>>> tableToDescMap = new HashMap<>();

  private final Map<String, List<BeanDescriptor<?>>> tableToViewDescMap = new HashMap<>();

  private List<BeanDescriptor<?>> immutableDescriptorList;

  private final DbIdentity dbIdentity;

  private final DataSource dataSource;

  private final DatabasePlatform databasePlatform;

  private final SpiCacheManager cacheManager;

  private final BackgroundExecutor backgroundExecutor;

  private final EncryptKeyManager encryptKeyManager;

  private final IdBinderFactory idBinderFactory;

  private final BeanLifecycleAdapterFactory beanLifecycleAdapterFactory;

  private final String asOfViewSuffix;

  /**
   * Map of base tables to 'with history views' used to support 'as of' queries.
   */
  private final Map<String, String> asOfTableMap = new HashMap<>();

  /**
   * Map of base tables to 'draft' tables.
   */
  private final Map<String, String> draftTableMap = new HashMap<>();

  private final int queryPlanTTLSeconds;

  /**
   * Create for a given database dbConfig.
   */
  public BeanDescriptorManager(InternalConfiguration config) {

    this.serverConfig = config.getServerConfig();
    this.serverName = InternString.intern(serverConfig.getName());
    this.cacheManager = config.getCacheManager();
    this.docStoreFactory = config.getDocStoreFactory();
    this.backgroundExecutor = config.getBackgroundExecutor();
    this.dataSource = serverConfig.getDataSource();
    this.encryptKeyManager = serverConfig.getEncryptKeyManager();
    this.databasePlatform = serverConfig.getDatabasePlatform();
    this.multiValueBind = config.getMultiValueBind();
    this.idBinderFactory = new IdBinderFactory(databasePlatform.isIdInExpandedForm(), multiValueBind);
    this.queryPlanTTLSeconds = serverConfig.getQueryPlanTTLSeconds();

    this.asOfViewSuffix = getAsOfViewSuffix(databasePlatform, serverConfig);
    String versionsBetweenSuffix = getVersionsBetweenSuffix(databasePlatform, serverConfig);
    this.readAnnotations = new ReadAnnotations(config.getGeneratedPropertyFactory(), asOfViewSuffix, versionsBetweenSuffix, serverConfig);
    this.bootupClasses = config.getBootupClasses();
    this.createProperties = config.getDeployCreateProperties();
    this.namingConvention = serverConfig.getNamingConvention();
    this.dbIdentity = config.getDatabasePlatform().getDbIdentity();
    this.deplyInherit = config.getDeployInherit();
    this.deployUtil = config.getDeployUtil();
    this.typeManager = deployUtil.getTypeManager();

    this.beanManagerFactory = new BeanManagerFactory(config.getDatabasePlatform());

    this.updateChangesOnly = serverConfig.isUpdateChangesOnly();

    this.beanLifecycleAdapterFactory = new BeanLifecycleAdapterFactory(serverConfig);
    this.persistControllerManager = new PersistControllerManager(bootupClasses);
    this.postLoadManager = new PostLoadManager(bootupClasses);
    this.postConstructManager = new PostConstructManager(bootupClasses);
    this.persistListenerManager = new PersistListenerManager(bootupClasses);
    this.beanQueryAdapterManager = new BeanQueryAdapterManager(bootupClasses);
    this.beanFinderManager = new BeanFinderManager(bootupClasses);

    this.transientProperties = new TransientProperties();
    this.changeLogPrepare = config.changeLogPrepare(bootupClasses.getChangeLogPrepare());
    this.changeLogListener = config.changeLogListener(bootupClasses.getChangeLogListener());
    this.changeLogRegister = config.changeLogRegister(bootupClasses.getChangeLogRegister());
  }

  /**
   * Run periodic trim of query plans.
   */
  public void scheduleBackgroundTrim() {
    backgroundExecutor.executePeriodically(this::trimQueryPlans, 30L, TimeUnit.SECONDS);
  }

  private void trimQueryPlans() {
    long lastUsed = System.currentTimeMillis() - (queryPlanTTLSeconds * 1000L);
    for (BeanDescriptor<?> descriptor : immutableDescriptorList) {
      if (!descriptor.isEmbedded()) {
        List<CQueryPlan> trimmedPlans = descriptor.trimQueryPlans(lastUsed);
        if (!trimmedPlans.isEmpty()) {
          logger.trace("trimmed {} query plans for type:{}", trimmedPlans.size(), descriptor.getName());
        }
      }
    }
  }

  @Override
  public ScalarType<?> getScalarType(String cast) {
    return typeManager.getScalarType(cast);
  }

  @Override
  public ScalarType<?> getScalarType(int jdbcType) {
    return typeManager.getScalarType(jdbcType);
  }

  /**
   * Return the AsOfViewSuffix based on the DbHistorySupport.
   */
  private String getAsOfViewSuffix(DatabasePlatform databasePlatform, ServerConfig serverConfig) {

    DbHistorySupport historySupport = databasePlatform.getHistorySupport();
    // with historySupport returns a simple view suffix or the sql2011 as of timestamp suffix
    return (historySupport == null) ? serverConfig.getAsOfViewSuffix() : historySupport.getAsOfViewSuffix(serverConfig.getAsOfViewSuffix());
  }

  /**
   * Return the versions between timestamp suffix based on the DbHistorySupport.
   */
  private String getVersionsBetweenSuffix(DatabasePlatform databasePlatform, ServerConfig serverConfig) {

    DbHistorySupport historySupport = databasePlatform.getHistorySupport();
    // with historySupport returns a simple view suffix or the sql2011 versions between timestamp suffix
    return (historySupport == null) ? serverConfig.getAsOfViewSuffix() : historySupport.getVersionsBetweenSuffix(serverConfig.getAsOfViewSuffix());
  }

  @Override
  public boolean isMultiValueSupported() {
    return multiValueBind.isSupported();
  }

  @Override
  public ServerConfig getServerConfig() {
    return serverConfig;
  }

  @Override
  public <T> DocStoreBeanAdapter<T> createDocStoreBeanAdapter(BeanDescriptor<T> descriptor, DeployBeanDescriptor<T> deploy) {
    return docStoreFactory.createAdapter(descriptor, deploy);
  }

  public BeanDescriptor<?> getBeanDescriptorByQueueId(String queueId) {
    return descQueueMap.get(queueId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> BeanDescriptor<T> getBeanDescriptor(Class<T> entityType) {
    return (BeanDescriptor<T>) descMap.get(entityType.getName());
  }

  @SuppressWarnings("unchecked")
  public <T> BeanDescriptor<T> getBeanDescriptorByClassName(String entityClassName) {
    return (BeanDescriptor<T>) descMap.get(entityClassName);
  }

  @Override
  public String getServerName() {
    return serverName;
  }

  @Override
  public SpiCacheManager getCacheManager() {
    return cacheManager;
  }

  @Override
  public NamingConvention getNamingConvention() {
    return namingConvention;
  }

  /**
   * Set the internal EbeanServer instance to all BeanDescriptors.
   */
  public void setEbeanServer(SpiEbeanServer internalEbean) {
    for (BeanDescriptor<?> desc : immutableDescriptorList) {
      desc.setEbeanServer(internalEbean);
    }
  }

  @Override
  public IdBinder createIdBinder(BeanProperty idProperty) {
    return idBinderFactory.createIdBinder(idProperty);
  }

  /**
   * Return the map of base tables to draft tables.
   */
  public Map<String, String> getDraftTableMap() {
    return draftTableMap;
  }

  /**
   * Deploy returning the asOfTableMap (which is required by the SQL builders).
   */
  public Map<String, String> deploy(List<XmEbean> mappings) {

    try {
      createListeners();
      readEntityDeploymentInitial();
      readXmlMapping(mappings);
      readEmbeddedDeployment();
      readEntityBeanTable();
      readEntityDeploymentAssociations();
      readInheritedIdGenerators();
      setProfileIds();
      // creates the BeanDescriptors
      readEntityRelationships();

      List<BeanDescriptor<?>> list = new ArrayList<>(descMap.values());
      list.sort(beanDescComparator);
      immutableDescriptorList = Collections.unmodifiableList(list);

      initialiseAll();
      readForeignKeys();

      readTableToDescriptor();

      logStatus();

      deployInfoMap.clear();
      deployInfoMap = null;

      return asOfTableMap;

    } catch (RuntimeException e) {
      logger.error("Error in deployment", e);
      throw e;
    }
  }

  private void readXmlMapping(List<XmEbean> mappings) {
    if (mappings != null) {
      ClassLoader classLoader = serverConfig.getClassLoadConfig().getClassLoader();
      for (XmEbean mapping : mappings) {
        List<XmEntity> entityDeploy = mapping.getEntity();
        for (XmEntity deploy : entityDeploy) {
          readEntityMapping(classLoader, deploy);
        }
      }
    }
  }

  private void readEntityMapping(ClassLoader classLoader, XmEntity entityDeploy) {

    String entityClassName = entityDeploy.getClazz();
    Class<?> entityClass;
    try {
      entityClass = Class.forName(entityClassName, false, classLoader);
    } catch (Exception e) {
      logger.error("Could not load entity bean class " + entityClassName + " for ebean.xml entry");
      return;
    }

    DeployBeanInfo<?> info = deployInfoMap.get(entityClass);
    if (info == null) {
      logger.error("No entity bean for ebean.xml entry " + entityClassName);

    } else {
      for (XmRawSql sql : entityDeploy.getRawSql()) {
        RawSqlBuilder builder = RawSqlBuilder.parse(sql.getQuery().getValue());
        for (XmColumnMapping columnMapping : sql.getColumnMapping()) {
          builder.columnMapping(columnMapping.getColumn(), columnMapping.getProperty());
        }
        for (XmAliasMapping aliasMapping : sql.getAliasMapping()) {
          builder.tableAliasMapping(aliasMapping.getAlias(), aliasMapping.getProperty());
        }
        info.addRawSql(sql.getName(), builder.create());
      }

      for (XmNamedQuery namedQuery : entityDeploy.getNamedQuery()) {
        info.addNamedQuery(namedQuery.getName(), namedQuery.getQuery().getValue());
      }
    }
  }

  /**
   * Return the Encrypt key given the table and column name.
   */
  @Override
  public EncryptKey getEncryptKey(String tableName, String columnName) {
    return encryptKeyManager.getEncryptKey(tableName, columnName);
  }

  /**
   * For SQL based modifications we need to invalidate appropriate parts of the cache.
   */
  public void cacheNotify(TransactionEventTable.TableIUD tableIUD, CacheChangeSet changeSet) {

    String tableName = tableIUD.getTableName().toLowerCase();
    List<BeanDescriptor<?>> normalBeanTypes = tableToDescMap.get(tableName);
    if (normalBeanTypes != null) {
      // 'normal' entity beans based on a "base table"
      for (BeanDescriptor<?> normalBeanType : normalBeanTypes) {
        normalBeanType.cachePersistTableIUD(tableIUD, changeSet);
      }
    }
    List<BeanDescriptor<?>> viewBeans = tableToViewDescMap.get(tableName);
    if (viewBeans != null) {
      // entity beans based on a "view"
      for (BeanDescriptor<?> viewBean : viewBeans) {
        viewBean.cachePersistTableIUD(tableIUD, changeSet);
      }
    }
  }

  /**
   * Return the BeanDescriptors mapped to the table.
   */
  public List<BeanDescriptor<?>> getBeanDescriptors(String tableName) {
    return tableToDescMap.get(tableName.toLowerCase());
  }

  /**
   * Return the BeanDescriptors mapped to the table.
   */
  public List<? extends BeanType<?>> getBeanTypes(String tableName) {
    return tableToDescMap.get(tableName.toLowerCase());
  }

  /**
   * Invalidate entity beans based on views via their dependent tables.
   */
  public void processViewInvalidation(Set<String> viewInvalidation) {

    for (String depTable : viewInvalidation) {
      List<BeanDescriptor<?>> list = tableToViewDescMap.get(depTable.toLowerCase());
      if (list != null) {
        for (BeanDescriptor<?> desc : list) {
          desc.clearQueryCache();
        }
      }
    }
  }

  /**
   * Build a map of table names to BeanDescriptors.
   * <p>
   * This is generally used to maintain caches from table names.
   * </p>
   */
  private void readTableToDescriptor() {

    for (BeanDescriptor<?> desc : descMap.values()) {
      String baseTable = desc.getBaseTable();
      if (baseTable != null) {
        baseTable = baseTable.toLowerCase();
        List<BeanDescriptor<?>> list = tableToDescMap.computeIfAbsent(baseTable, k -> new ArrayList<>(1));
        list.add(desc);
      }
      if (desc.getEntityType() == EntityType.VIEW && desc.isQueryCaching()) {
        // build map of tables to view entities dependent on those tables
        // for the purpose of invalidating appropriate query caches
        String[] dependentTables = desc.getDependentTables();
        if (dependentTables != null && dependentTables.length > 0) {
          for (String depTable : dependentTables) {
            depTable = depTable.toLowerCase();
            List<BeanDescriptor<?>> list = tableToViewDescMap.computeIfAbsent(depTable, k -> new ArrayList<>(1));
            list.add(desc);
          }
        }
      }
    }
  }

  private void readForeignKeys() {

    for (BeanDescriptor<?> d : descMap.values()) {
      d.initialiseFkeys();
    }
  }

  /**
   * Initialise all the BeanDescriptors.
   * <p>
   * This occurs after all the BeanDescriptors have been created. This resolves
   * circular relationships between BeanDescriptors.
   * </p>
   * <p>
   * Also responsible for creating all the BeanManagers which contain the
   * persister, listener etc.
   * </p>
   */
  private void initialiseAll() {

    // now that all the BeanDescriptors are in their map
    // we can initialise them which sorts out circular
    // dependencies for OneToMany and ManyToOne etc

    BeanDescriptorInitContext initContext = new BeanDescriptorInitContext(asOfTableMap, draftTableMap, asOfViewSuffix);

    // PASS 1:
    // initialise the ID properties of all the beans
    // first (as they are needed to initialise the
    // associated properties in the second pass).
    for (BeanDescriptor<?> d : descMap.values()) {
      d.initialiseId(initContext);
    }

    // PASS 2:
    // now initialise all the inherit info
    for (BeanDescriptor<?> d : descMap.values()) {
      d.initInheritInfo();
    }

    // PASS 3:
    // now initialise all the associated properties
    for (BeanDescriptor<?> d : descMap.values()) {
      // also look for intersection tables with
      // associated history support and register them
      // into the asOfTableMap
      d.initialiseOther(initContext);
    }

    // PASS 4:
    // now initialise document mapping which needs target descriptors
    for (BeanDescriptor<?> d : descMap.values()) {
      d.initialiseDocMapping();
    }

    // create BeanManager for each non-embedded entity bean
    for (BeanDescriptor<?> d : descMap.values()) {
      d.initLast();
      if (!d.isEmbedded()) {
        BeanManager<?> m = beanManagerFactory.create(d);
        beanManagerMap.put(d.getFullName(), m);
        checkForValidEmbeddedId(d);
      }
    }
  }

  private void checkForValidEmbeddedId(BeanDescriptor<?> d) {
    IdBinder idBinder = d.getIdBinder();
    if (idBinder instanceof IdBinderEmbedded) {
      IdBinderEmbedded embId = (IdBinderEmbedded) idBinder;
      BeanDescriptor<?> idBeanDescriptor = embId.getIdBeanDescriptor();
      Class<?> idType = idBeanDescriptor.getBeanType();
      try {
        idType.getDeclaredMethod("hashCode");
        idType.getDeclaredMethod("equals", Object.class);
      } catch (NoSuchMethodException e) {
        checkMissingHashCodeOrEquals(e, idType, d.getBeanType());
      }
    }
  }

  private void checkMissingHashCodeOrEquals(Exception source, Class<?> idType, Class<?> beanType) {

    String msg = "SERIOUS ERROR: The hashCode() and equals() methods *MUST* be implemented ";
    msg += "on Embedded bean " + idType + " as it is used as an Id for " + beanType;
    throw new PersistenceException(msg, source);
  }

  /**
   * Return true if there are 'view based entities' using l2 query caching and so need
   * to be invalidated based on changes to dependent tables.
   */
  public boolean requiresViewEntityCacheInvalidation() {
    return !tableToViewDescMap.isEmpty();
  }

  /**
   * Return an immutable list of all the BeanDescriptors.
   */
  public List<BeanDescriptor<?>> getBeanDescriptorList() {
    return immutableDescriptorList;
  }

  public BeanTable getBeanTable(Class<?> type) {
    return beanTableMap.get(type);
  }

  /**
   * Return a BeanTable for an ElementCollection.
   */
  public BeanTable createCollectionBeanTable(String fullTableName, Class<?> targetType) {
    return new BeanTable(this, fullTableName, targetType);
  }

  @SuppressWarnings("unchecked")
  public <T> BeanManager<T> getBeanManager(Class<T> entityType) {

    return (BeanManager<T>) getBeanManager(entityType.getName());
  }

  public BeanManager<?> getBeanManager(String beanClassName) {
    return beanManagerMap.get(beanClassName);
  }

  /**
   * Create the BeanControllers, BeanFinders and BeanListeners.
   */
  private void createListeners() {

    int qa = beanQueryAdapterManager.getRegisterCount();
    int cc = persistControllerManager.getRegisterCount();
    int pl = postLoadManager.getRegisterCount();
    int pc = postConstructManager.getRegisterCount();
    int lc = persistListenerManager.getRegisterCount();
    int fc = beanFinderManager.getRegisterCount();

    logger.debug("BeanPersistControllers[{}] BeanFinders[{}] BeanPersistListeners[{}] BeanQueryAdapters[{}] BeanPostLoaders[{}] BeanPostConstructors[{}]", cc, fc, lc, qa, pl, pc);
  }

  private void logStatus() {
    logger.debug("Entities[{}]", entityBeanCount);
  }

  private <T> BeanDescriptor<T> createEmbedded(Class<T> beanClass) {
    DeployBeanInfo<T> info = getDeploy(beanClass);
    return new BeanDescriptor<>(this, info.getDescriptor());
  }

  /**
   * Return the bean deploy info for the given class.
   */
  @SuppressWarnings("unchecked")
  public <T> DeployBeanInfo<T> getDeploy(Class<T> cls) {
    return (DeployBeanInfo<T>) deployInfoMap.get(cls);
  }

  private void registerBeanDescriptor(BeanDescriptor<?> desc) {
    descMap.put(desc.getBeanType().getName(), desc);
    if (desc.isDocStoreMapped()) {
      descQueueMap.put(desc.getDocStoreQueueId(), desc);
    }
  }

  /**
   * Read deployment information for all the embedded beans.
   */
  private void readEmbeddedDeployment() {

    List<Class<?>> embeddedClasses = bootupClasses.getEmbeddables();
    for (Class<?> embeddedClass : embeddedClasses) {
      registerBeanDescriptor(createEmbedded(embeddedClass));
    }
  }

  /**
   * Read the initial deployment information for the entities.
   * <p>
   * This stops short of reading relationship meta data until after the
   * BeanTables have all been created.
   * </p>
   */
  private void readEntityDeploymentInitial() {

    for (Class<?> entityClass : bootupClasses.getEntities()) {
      DeployBeanInfo<?> info = createDeployBeanInfo(entityClass);
      deployInfoMap.put(entityClass, info);
    }
    for (Class<?> entityClass : bootupClasses.getEmbeddables()) {
      DeployBeanInfo<?> info = createDeployBeanInfo(entityClass);
      readDeployAssociations(info);
      deployInfoMap.put(entityClass, info);
    }
  }

  /**
   * Create the BeanTable information which has the base table and id.
   * <p>
   * This is determined prior to resolving relationship information.
   * </p>
   */
  private void readEntityBeanTable() {

    for (DeployBeanInfo<?> info : deployInfoMap.values()) {
      BeanTable beanTable = createBeanTable(info);
      beanTableMap.put(beanTable.getBeanType(), beanTable);
    }
  }

  /**
   * Create the BeanTable information which has the base table and id.
   * <p>
   * This is determined prior to resolving relationship information.
   * </p>
   */
  private void readEntityDeploymentAssociations() {

    for (DeployBeanInfo<?> info : deployInfoMap.values()) {
      readDeployAssociations(info);
    }
  }

  private void readInheritedIdGenerators() {

    for (DeployBeanInfo<?> info : deployInfoMap.values()) {
      DeployBeanDescriptor<?> descriptor = info.getDescriptor();
      InheritInfo inheritInfo = descriptor.getInheritInfo();
      if (inheritInfo != null && !inheritInfo.isRoot()) {
        DeployBeanInfo<?> rootBeanInfo = deployInfoMap.get(inheritInfo.getRoot().getType());
        PlatformIdGenerator rootIdGen = rootBeanInfo.getDescriptor().getIdGenerator();
        if (rootIdGen != null) {
          descriptor.setIdGenerator(rootIdGen);
        }
      }
    }
  }

  /**
   * Set profileIds based on descriptor full name order.
   */
  private void setProfileIds() {

    List<? extends DeployBeanDescriptor<?>> deployDescriptors = deployInfoMap.values().stream()
      .map(DeployBeanInfo::getDescriptor)
      .collect(Collectors.toList());

    deployDescriptors.sort(Comparator.comparing(DeployBeanDescriptor::getFullName));

    short id = 0;
    for (DeployBeanDescriptor<?> desc : deployDescriptors) {
      if (!desc.isEmbedded()) {
        desc.setProfileId(++id);
      }
    }
  }

  /**
   * Create the BeanTable from the deployment information gathered so far.
   */
  private BeanTable createBeanTable(DeployBeanInfo<?> info) {

    DeployBeanDescriptor<?> deployDescriptor = info.getDescriptor();
    DeployBeanTable beanTable = deployDescriptor.createDeployBeanTable();
    return new BeanTable(beanTable, this);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void readEntityRelationships() {

    // We only perform 'circular' checks etc after we have
    // all the DeployBeanDescriptors created and in the map.

    List<DeployBeanPropertyAssocOne<?>> primaryKeyJoinCheck = new ArrayList<>();
    for (DeployBeanInfo<?> info : deployInfoMap.values()) {
      checkMappedBy(info, primaryKeyJoinCheck);
    }
    for (DeployBeanPropertyAssocOne<?> prop : primaryKeyJoinCheck) {
      checkUniDirectionalPrimaryKeyJoin(prop);
    }

    for (DeployBeanInfo<?> info : deployInfoMap.values()) {
      secondaryPropsJoins(info);
    }

    // Set inheritance info
    for (DeployBeanInfo<?> info : deployInfoMap.values()) {
      setInheritanceInfo(info);
    }

    for (DeployBeanInfo<?> info : deployInfoMap.values()) {
      registerBeanDescriptor(new BeanDescriptor(this, info.getDescriptor()));
    }
  }

  /**
   * Sets the inheritance info. ~EMG fix for join problem
   *
   * @param info the new inheritance info
   */
  private void setInheritanceInfo(DeployBeanInfo<?> info) {

    for (DeployBeanPropertyAssocOne<?> oneProp : info.getDescriptor().propertiesAssocOne()) {
      if (!oneProp.isTransient()) {
        DeployBeanInfo<?> assoc = deployInfoMap.get(oneProp.getTargetType());
        if (assoc != null) {
          oneProp.getTableJoin().setInheritInfo(assoc.getDescriptor().getInheritInfo());
        }
      }
    }

    for (DeployBeanPropertyAssocMany<?> manyProp : info.getDescriptor().propertiesAssocMany()) {
      if (!manyProp.isTransient()) {
        DeployBeanInfo<?> assoc = deployInfoMap.get(manyProp.getTargetType());
        if (assoc != null) {
          manyProp.getTableJoin().setInheritInfo(assoc.getDescriptor().getInheritInfo());
        }
      }
    }
  }

  private void secondaryPropsJoins(DeployBeanInfo<?> info) {

    DeployBeanDescriptor<?> descriptor = info.getDescriptor();
    for (DeployBeanProperty prop : descriptor.propertiesBase()) {
      if (prop.isSecondaryTable()) {
        String tableName = prop.getSecondaryTable();
        // find a join to that table...
        DeployBeanPropertyAssocOne<?> assocOne = descriptor.findJoinToTable(tableName);
        if (assocOne == null) {
          String msg = "Error with property " + prop.getFullBeanName() + ". Could not find a Relationship to table " + tableName
            + ". Perhaps you could use a @JoinColumn instead.";
          throw new RuntimeException(msg);
        }
        DeployTableJoin tableJoin = assocOne.getTableJoin();
        prop.setSecondaryTableJoin(tableJoin, assocOne.getName());
      }
    }
  }

  /**
   * Check the mappedBy attributes for properties on this descriptor.
   * <p>
   * This will read join information defined on the 'owning/other' side of the
   * relationship. It also does some extra work for unidirectional
   * relationships.
   * </p>
   */
  private void checkMappedBy(DeployBeanInfo<?> info, List<DeployBeanPropertyAssocOne<?>> primaryKeyJoinCheck) {

    for (DeployBeanPropertyAssocOne<?> oneProp : info.getDescriptor().propertiesAssocOne()) {
      if (!oneProp.isTransient()) {
        if (oneProp.getMappedBy() != null) {
          checkMappedByOneToOne(oneProp);
        } else if (oneProp.isPrimaryKeyJoin()) {
          primaryKeyJoinCheck.add(oneProp);
        }
      }
    }

    for (DeployBeanPropertyAssocMany<?> manyProp : info.getDescriptor().propertiesAssocMany()) {
      if (!manyProp.isTransient()) {
        if (manyProp.isManyToMany()) {
          checkMappedByManyToMany(manyProp);
        } else {
          checkMappedByOneToMany(info, manyProp);
        }
      }
    }
  }

  private DeployBeanDescriptor<?> getTargetDescriptor(DeployBeanPropertyAssoc<?> prop) {

    Class<?> targetType = prop.getTargetType();
    DeployBeanInfo<?> info = deployInfoMap.get(targetType);
    if (info == null) {
      String msg = "Can not find descriptor [" + targetType + "] for " + prop.getFullBeanName();
      throw new PersistenceException(msg);
    }

    return info.getDescriptor();
  }

  /**
   * Check that the many property has either an implied mappedBy property or
   * mark it as unidirectional.
   */
  private boolean findMappedBy(DeployBeanPropertyAssocMany<?> prop) {

    // this is the entity bean type - that owns this property
    Class<?> owningType = prop.getOwningType();

    Set<String> matchSet = new HashSet<>();

    // get the bean descriptor that holds the mappedBy property
    DeployBeanDescriptor<?> targetDesc = getTargetDescriptor(prop);
    List<DeployBeanPropertyAssocOne<?>> ones = targetDesc.propertiesAssocOne();
    for (DeployBeanPropertyAssocOne<?> possibleMappedBy : ones) {
      Class<?> possibleMappedByType = possibleMappedBy.getTargetType();
      if (possibleMappedByType.equals(owningType)) {
        prop.setMappedBy(possibleMappedBy.getName());
        matchSet.add(possibleMappedBy.getName());
      }
    }

    if (matchSet.isEmpty()) {
      // this is a unidirectional relationship
      // ... that is no matching property on the 'detail' bean
      return false;
    }
    if (matchSet.size() == 1) {
      // all right with the world
      return true;
    }
    if (matchSet.size() == 2) {
      // try to find a match implicitly using a common naming convention
      // e.g. List<Bug> loggedBugs; ... search for "logged" in matchSet
      String name = prop.getName();

      // get the target type short name
      String targetType = prop.getTargetType().getName();
      String shortTypeName = targetType.substring(targetType.lastIndexOf('.') + 1);

      // name includes (probably ends with) the target type short name?
      int p = name.indexOf(shortTypeName);
      if (p > 1) {
        // ok, get the 'interesting' part of the property name
        // That is the name without the target type
        String searchName = name.substring(0, p).toLowerCase();

        // search for this in the possible matches
        for (String possibleMappedBy : matchSet) {
          String possibleLower = possibleMappedBy.toLowerCase();
          if (possibleLower.contains(searchName)) {
            // we have a match..
            prop.setMappedBy(possibleMappedBy);

            String m = "Implicitly found mappedBy for " + targetDesc + "." + prop;
            m += " by searching for [" + searchName + "] against " + matchSet;
            logger.debug(m);

            return true;
          }
        }

      }
    }
    // multiple options so should specify mappedBy property
    String msg = "Error on " + prop.getFullBeanName() + " missing mappedBy.";
    msg += " There are [" + matchSet.size() + "] possible properties in " + targetDesc;
    msg += " that this association could be mapped to. Please specify one using ";
    msg += "the mappedBy attribute on @OneToMany.";
    throw new PersistenceException(msg);
  }

  private void makeOrderColumn(DeployBeanPropertyAssocMany<?> oneToMany) {

    DeployBeanDescriptor<?> targetDesc = getTargetDescriptor(oneToMany);

    DeployOrderColumn orderColumn = oneToMany.getOrderColumn();
    DeployBeanProperty orderProperty = new DeployBeanProperty(targetDesc, Integer.class, ScalarTypeInteger.INSTANCE, null);

    orderProperty.setName(DeployOrderColumn.LOGICAL_NAME);
    orderProperty.setDbColumn(orderColumn.getName());
    orderProperty.setNullable(orderColumn.isNullable());
    orderProperty.setDbInsertable(orderColumn.isInsertable());
    orderProperty.setDbUpdateable(orderColumn.isUpdatable());
    orderProperty.setDbRead(true);

    targetDesc.setOrderColumn(orderProperty);
  }

  /**
   * A OneToMany with no matching mappedBy property in the target so must be
   * unidirectional.
   * <p>
   * This means that inserts MUST cascade for this property.
   * </p>
   * <p>
   * Create a "Shadow"/Unidirectional property on the target. It is used with
   * inserts to set the foreign key value (e.g. inserts the foreign key value
   * into the order_id column on the order_lines table).
   * </p>
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private void makeUnidirectional(DeployBeanPropertyAssocMany<?> oneToMany) {

    DeployBeanDescriptor<?> targetDesc = getTargetDescriptor(oneToMany);

    Class<?> owningType = oneToMany.getOwningType();

    if (!oneToMany.getCascadeInfo().isSave()) {
      // The property MUST have persist cascading so that inserts work.

      Class<?> targetType = oneToMany.getTargetType();
      String msg = "Error on " + oneToMany.getFullBeanName() + ". @OneToMany MUST have ";
      msg += "Cascade.PERSIST or Cascade.ALL because this is a unidirectional ";
      msg += "relationship. That is, there is no property of type " + owningType + " on " + targetType;

      throw new PersistenceException(msg);
    }

    // mark this property as unidirectional
    oneToMany.setUnidirectional();

    // specify table and table alias...
    BeanTable beanTable = getBeanTable(owningType);

    // define the TableJoin
    DeployTableJoin oneToManyJoin = oneToMany.getTableJoin();
    if (!oneToManyJoin.hasJoinColumns()) {
      throw new RuntimeException("No join columns");
    }
    createUnidirectional(targetDesc, owningType, beanTable, oneToManyJoin);
  }

  /**
   * Create and add a Unidirectional property (for ElementCollection) which maps to the foreign key.
   */
  public <A> void createUnidirectional(DeployBeanDescriptor<?> targetDesc, Class<A> targetType, BeanTable beanTable, DeployTableJoin oneToManyJoin) {

    // create the 'shadow' unidirectional property
    // which is put on the target descriptor
    DeployBeanPropertyAssocOne<A> unidirectional = new DeployBeanPropertyAssocOne<>(targetDesc, targetType);
    unidirectional.setUndirectionalShadow();
    unidirectional.setNullable(false);
    unidirectional.setDbRead(true);
    unidirectional.setDbInsertable(true);
    unidirectional.setDbUpdateable(false);
    unidirectional.setBeanTable(beanTable);
    unidirectional.setName(beanTable.getBaseTable());
    unidirectional.setJoinType(true);
    unidirectional.setJoinColumns(oneToManyJoin.columns(), true);

    targetDesc.setUnidirectional(unidirectional);
  }

  private void checkMappedByOneToOne(DeployBeanPropertyAssocOne<?> prop) {

    // check that the mappedBy property is valid and read
    // its associated join information if it is available
    String mappedBy = prop.getMappedBy();

    // get the mappedBy property
    DeployBeanDescriptor<?> targetDesc = getTargetDescriptor(prop);
    DeployBeanProperty mappedProp = targetDesc.getBeanProperty(mappedBy);
    if (mappedProp == null) {
      String m = "Error on " + prop.getFullBeanName();
      m += "  Can not find mappedBy property [" + targetDesc + "." + mappedBy + "] ";
      throw new PersistenceException(m);
    }

    if (!(mappedProp instanceof DeployBeanPropertyAssocOne<?>)) {
      String m = "Error on " + prop.getFullBeanName();
      m += ". mappedBy property [" + targetDesc + "." + mappedBy + "]is not a OneToOne?";
      throw new PersistenceException(m);
    }

    DeployBeanPropertyAssocOne<?> mappedAssocOne = (DeployBeanPropertyAssocOne<?>) mappedProp;

    if (!mappedAssocOne.isOneToOne()) {
      String m = "Error on " + prop.getFullBeanName();
      m += ". mappedBy property [" + targetDesc + "." + mappedBy + "]is not a OneToOne?";
      throw new PersistenceException(m);
    }

    DeployTableJoin tableJoin = prop.getTableJoin();
    if (!tableJoin.hasJoinColumns()) {
      // define Join as the inverse of the mappedBy property
      DeployTableJoin otherTableJoin = mappedAssocOne.getTableJoin();
      otherTableJoin.copyWithoutType(tableJoin, true, tableJoin.getTable());
    }

    if (mappedAssocOne.isPrimaryKeyJoin()) {
      // bi-directional PrimaryKeyJoin ...
      mappedAssocOne.setPrimaryKeyJoin(false);
      prop.setPrimaryKeyExport();
      addPrimaryKeyJoin(prop);
    }
  }

  private void checkUniDirectionalPrimaryKeyJoin(DeployBeanPropertyAssocOne<?> prop) {
    if (prop.isPrimaryKeyJoin()) {
      // uni-directional PrimaryKeyJoin ...
      prop.setPrimaryKeyExport();
      addPrimaryKeyJoin(prop);
    }
  }

  /**
   * If the property has mappedBy set then do two things. Make sure the mappedBy
   * property exists, and secondly read its join information.
   * <p>
   * We can use the join information from the mappedBy property and reverse it
   * for using in the OneToMany direction.
   * </p>
   */
  private void checkMappedByOneToMany(DeployBeanInfo<?> info, DeployBeanPropertyAssocMany<?> prop) {

    if (prop.isElementCollection()) {
      // skip mapping check
      return;
    }
    DeployBeanDescriptor<?> targetDesc = getTargetDescriptor(prop);

    if (targetDesc.isDraftableElement()) {
      // automatically turning on orphan removal and CascadeType.ALL
      prop.setModifyListenMode(BeanCollection.ModifyListenMode.REMOVALS);
      prop.getCascadeInfo().setSaveDelete(true, true);
    }

    if (prop.hasOrderColumn()) {
      makeOrderColumn(prop);
    }

    if (prop.getMappedBy() == null) {
      // if we are doc store only we are done
      // this allows the use of @OneToMany in @DocStore - Entities
      if (info.getDescriptor().isDocStoreOnly()) {
        prop.setUnidirectional();
        return;
      }

      if (!findMappedBy(prop)) {
        if (!prop.isO2mJoinTable()) {
          makeUnidirectional(prop);
        }
        return;
      }
    }

    // check that the mappedBy property is valid and read
    // its associated join information if it is available
    String mappedBy = prop.getMappedBy();

    // get the mappedBy property
    DeployBeanProperty mappedProp = targetDesc.getBeanProperty(mappedBy);
    if (mappedProp == null) {
      String m = "Error on " + prop.getFullBeanName();
      m += "  Can not find mappedBy property [" + mappedBy + "] ";
      m += "in [" + targetDesc + "]";
      throw new PersistenceException(m);
    }

    if (!(mappedProp instanceof DeployBeanPropertyAssocOne<?>)) {
      String m = "Error on " + prop.getFullBeanName();
      m += ". mappedBy property [" + mappedBy + "]is not a ManyToOne?";
      m += "in [" + targetDesc + "]";
      throw new PersistenceException(m);
    }

    DeployBeanPropertyAssocOne<?> mappedAssocOne = (DeployBeanPropertyAssocOne<?>) mappedProp;

    DeployTableJoin tableJoin = prop.getTableJoin();
    if (!tableJoin.hasJoinColumns()) {
      // define Join as the inverse of the mappedBy property
      DeployTableJoin otherTableJoin = mappedAssocOne.getTableJoin();
      otherTableJoin.copyTo(tableJoin, true, tableJoin.getTable());
    }

    PropertyForeignKey foreignKey = mappedAssocOne.getForeignKey();
    if (foreignKey != null) {
      ConstraintMode onDelete = foreignKey.getOnDelete();
      switch (onDelete) {
        case SET_DEFAULT:
        case SET_NULL:
        case CASCADE: {
          // turn off cascade delete when we are using the foreign
          // key constraint to cascade the delete or set null
          prop.getCascadeInfo().setDelete(false);
        }
      }
    }
  }

  /**
   * For mappedBy copy the joins from the other side.
   */
  private void checkMappedByManyToMany(DeployBeanPropertyAssocMany<?> prop) {

    // get the bean descriptor that holds the mappedBy property
    String mappedBy = prop.getMappedBy();
    if (mappedBy == null) {
      if (getTargetDescriptor(prop).isDraftable()) {
        prop.setIntersectionDraftTable();
      }
      return;
    }

    // get the mappedBy property
    DeployBeanDescriptor<?> targetDesc = getTargetDescriptor(prop);
    DeployBeanProperty mappedProp = targetDesc.getBeanProperty(mappedBy);

    if (mappedProp == null) {
      String m = "Error on " + prop.getFullBeanName();
      m += "  Can not find mappedBy property [" + mappedBy + "] ";
      m += "in [" + targetDesc + "]";
      throw new PersistenceException(m);
    }

    if (!(mappedProp instanceof DeployBeanPropertyAssocMany<?>)) {
      String m = "Error on " + prop.getFullBeanName();
      m += ". mappedBy property [" + targetDesc + "." + mappedBy + "] is not a ManyToMany?";
      throw new PersistenceException(m);
    }

    DeployBeanPropertyAssocMany<?> mappedAssocMany = (DeployBeanPropertyAssocMany<?>) mappedProp;

    if (!mappedAssocMany.isManyToMany()) {
      String m = "Error on " + prop.getFullBeanName();
      m += ". mappedBy property [" + targetDesc + "." + mappedBy + "] is not a ManyToMany?";
      throw new PersistenceException(m);
    }

    // define the relationships/joins on this side as the
    // reverse of the other mappedBy side ...

    // DeployTableJoin mappedJoin = mappedAssocMany.getTableJoin();
    DeployTableJoin mappedIntJoin = mappedAssocMany.getIntersectionJoin();
    DeployTableJoin mappendInverseJoin = mappedAssocMany.getInverseJoin();

    String intTableName = mappedIntJoin.getTable();

    DeployTableJoin tableJoin = prop.getTableJoin();
    mappedIntJoin.copyTo(tableJoin, true, targetDesc.getBaseTable());

    DeployTableJoin intJoin = new DeployTableJoin();
    mappendInverseJoin.copyTo(intJoin, false, intTableName);
    prop.setIntersectionJoin(intJoin);

    DeployTableJoin inverseJoin = new DeployTableJoin();
    mappedIntJoin.copyTo(inverseJoin, false, intTableName);
    prop.setInverseJoin(inverseJoin);

    if (targetDesc.isDraftable()) {
      prop.setIntersectionDraftTable();
    }
  }

  private <T> void setBeanControllerFinderListener(DeployBeanDescriptor<T> descriptor) {

    persistControllerManager.addPersistControllers(descriptor);
    postLoadManager.addPostLoad(descriptor);
    postConstructManager.addPostConstructListeners(descriptor);
    persistListenerManager.addPersistListeners(descriptor);
    beanQueryAdapterManager.addQueryAdapter(descriptor);
    beanFinderManager.addFindControllers(descriptor);

    if (changeLogRegister != null) {
      ChangeLogFilter changeFilter = changeLogRegister.getChangeFilter(descriptor.getBeanType());
      if (changeFilter != null) {
        descriptor.setChangeLogFilter(changeFilter);
      }
    }

  }

  /**
   * Read the initial deployment information for a given bean type.
   */
  private <T> DeployBeanInfo<T> createDeployBeanInfo(Class<T> beanClass) {

    DeployBeanDescriptor<T> desc = new DeployBeanDescriptor<>(this, beanClass, serverConfig);

    desc.setUpdateChangesOnly(updateChangesOnly);

    beanLifecycleAdapterFactory.addLifecycleMethods(desc);

    // set bean controller, finder and listener
    setBeanControllerFinderListener(desc);
    deplyInherit.process(desc);
    desc.checkInheritanceMapping();

    createProperties.createProperties(desc);

    DeployBeanInfo<T> info = new DeployBeanInfo<>(deployUtil, desc);

    readAnnotations.readInitial(info);
    return info;
  }

  private <T> void readDeployAssociations(DeployBeanInfo<T> info) {

    DeployBeanDescriptor<T> desc = info.getDescriptor();

    readAnnotations.readAssociations(info, this);

    if (EntityType.SQL == desc.getEntityType()) {
      desc.setBaseTable(null, null, null);
    }

    // mark transient properties
    transientProperties.process(desc);
    setScalarType(desc);

    if (!desc.isEmbedded()) {
      // Set IdGenerator or use DB Identity
      setIdGeneration(desc);

      // find the appropriate default concurrency mode
      setConcurrencyMode(desc);
    }

    // generate the byte code
    createByteCode(desc);
  }

  /**
   * Set the Identity generation mechanism.
   */
  private <T> void setIdGeneration(DeployBeanDescriptor<T> desc) {

    if (desc.getIdGenerator() != null) {
      // already assigned (So custom or UUID)
      return;
    }
    if (desc.idProperty() == null) {
      // bean doesn't have an Id property
      if (desc.isBaseTableType() && desc.getBeanFinder() == null) {
        // expecting an id property
        logger.debug(Message.msg("deploy.nouid", desc.getFullName()));
      }
      return;
    }

    if (IdType.SEQUENCE == desc.getIdType() && !dbIdentity.isSupportsSequence()) {
      // explicit sequence but not supported by the DatabasePlatform
      logger.info("Explicit sequence on " + desc.getFullName() + " but not supported by DB Platform - ignored");
      desc.setIdType(null);
    }
    if (IdType.IDENTITY == desc.getIdType() && !dbIdentity.isSupportsIdentity()) {
      // explicit identity but not supported by the DatabasePlatform
      logger.info("Explicit Identity on " + desc.getFullName() + " but not supported by DB Platform - ignored");
      desc.setIdType(null);
    }

    if (desc.getIdType() == null) {
      if (desc.isPrimaryKeyCompoundOrNonNumeric()) {
        // assuming that this is a user supplied key like ISO country code or ISO currency code or lookup table code
        logger.debug("Expecting user defined identity on {} - not using db sequence or autoincrement", desc.getFullName());
        desc.setIdType(IdType.EXTERNAL);
        return;
      }
      if (desc.isIdGeneratedValue() || serverConfig.isIdGeneratorAutomatic()) {
        // use IDENTITY or SEQUENCE based on platform
        desc.setIdType(dbIdentity.getIdType());
        desc.setIdTypePlatformDefault();
      } else {
        // externally/application supplied Id values
        desc.setIdType(IdType.EXTERNAL);
        return;
      }
    }

    if (desc.getBaseTable() == null) {
      // no base table so not going to set Identity
      // of sequence information
      return;
    }

    if (IdType.IDENTITY == desc.getIdType()) {
      // used when getGeneratedKeys is not supported (SQL Server 2000)
      String selectLastInsertedId = dbIdentity.getSelectLastInsertedId(desc.getBaseTable());
      desc.setSelectLastInsertedId(selectLastInsertedId);
      return;
    }

    if (IdType.SEQUENCE == desc.getIdType()) {
      String seqName = desc.getIdGeneratorName();
      if (seqName != null) {
        logger.debug("explicit sequence {} on {}", seqName, desc.getFullName());
      } else {
        String primaryKeyColumn = desc.getSinglePrimaryKeyColumn();
        // use namingConvention to define sequence name
        seqName = namingConvention.getSequenceName(desc.getBaseTable(), primaryKeyColumn);
      }

      if (databasePlatform.isSequenceBatchMode()) {
        // use sequence next step 1 as we are going to batch fetch them instead
        desc.setSequenceAllocationSize(1);
      }
      int stepSize = desc.getSequenceAllocationSize();
      desc.setIdGenerator(createSequenceIdGenerator(seqName, stepSize));
    }
  }

  private PlatformIdGenerator createSequenceIdGenerator(String seqName, int stepSize) {
    return databasePlatform.createSequenceIdGenerator(backgroundExecutor, dataSource, stepSize, seqName);
  }

  private void createByteCode(DeployBeanDescriptor<?> deploy) {

    // check to see if the bean supports EntityBean interface
    // generate a subclass if required
    setEntityBeanClass(deploy);

    // use Code generation or Standard reflection to support
    // getter and setter methods
    setBeanReflect(deploy);
  }

  /**
   * Set the Scalar Types on all the simple types. This is done AFTER transients
   * have been identified. This is because a non-transient field MUST have a
   * ScalarType. It is useful for transients to have ScalarTypes because then
   * they can be used in a SqlSelect query.
   * <p>
   * Enums are treated a bit differently in that they always have a ScalarType
   * as one is built for them.
   * </p>
   */
  private void setScalarType(DeployBeanDescriptor<?> deployDesc) {

    for (DeployBeanProperty prop : deployDesc.propertiesAll()) {
      if (!(prop instanceof DeployBeanPropertyAssoc<?>)) {
        deployUtil.setScalarType(prop);
      }
    }
  }

  /**
   * Set BeanReflect BeanReflectGetter and BeanReflectSetter properties.
   * <p>
   * This sets the implementation of constructing entity beans and the setting
   * and getting of properties. It is generally faster to use code generation
   * rather than reflection to do this.
   * </p>
   */
  private void setBeanReflect(DeployBeanDescriptor<?> desc) {

    // Set the BeanReflectGetter and BeanReflectSetter that typically
    // use generated code. NB: Due to Bug 166 so now doing this for
    // abstract classes as well.

    BeanPropertiesReader reflectProps = new BeanPropertiesReader(desc.getBeanType());
    desc.setProperties(reflectProps.getProperties());

    for (DeployBeanProperty prop : desc.propertiesAll()) {
      String propName = prop.getName();
      Integer pos = reflectProps.getPropertyIndex(propName);
      if (pos == null) {
        if (isPersistentField(prop)) {
          throw new IllegalStateException(
            "If you are running in an IDE with enhancement plugin try a Build -> Rebuild Project to recompile and enhance all entity beans. " +
            "Error - property " + propName + " not found in " + reflectProps + " for type " + desc.getBeanType());
        }

      } else {
        final int propertyIndex = pos;
        prop.setPropertyIndex(propertyIndex);
        prop.setGetter(beanPropertyAccess.getGetter(propertyIndex));
        prop.setSetter(beanPropertyAccess.getSetter(propertyIndex));
        if (prop.isAggregation()) {
          prop.setAggregationPrefix(DetermineAggPath.manyPath(prop.getRawAggregation(), desc));
        }
      }
    }
  }

  /**
   * Return true if this is a persistent field (not transient or static).
   */
  private boolean isPersistentField(DeployBeanProperty prop) {

    Field field = prop.getField();
    if (field == null) {
      return false;
    }
    int modifiers = field.getModifiers();
    return !(Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) && !field.isAnnotationPresent(Transient.class);
  }

  /**
   * DevNote: It is assumed that Embedded can contain version properties. It is
   * also assumed that Embedded beans do NOT themselves contain Embedded beans
   * which contain version properties.
   */
  private void setConcurrencyMode(DeployBeanDescriptor<?> desc) {

    if (desc.getConcurrencyMode() != null) {
      // concurrency mode explicitly set during deployment
      return;
    }

    if (checkForVersionProperties(desc)) {
      desc.setConcurrencyMode(ConcurrencyMode.VERSION);
    } else {
      desc.setConcurrencyMode(ConcurrencyMode.NONE);
    }
  }

  /**
   * Search for version properties also including embedded beans.
   */
  private boolean checkForVersionProperties(DeployBeanDescriptor<?> desc) {

    boolean hasVersionProperty = false;

    List<DeployBeanProperty> props = desc.propertiesBase();
    for (DeployBeanProperty prop : props) {
      if (prop.isVersionColumn()) {
        hasVersionProperty = true;
      }
    }

    return hasVersionProperty;
  }

  private boolean hasEntityBeanInterface(Class<?> beanClass) {

    Class<?>[] interfaces = beanClass.getInterfaces();
    for (Class<?> anInterface : interfaces) {
      if (anInterface.equals(EntityBean.class)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Test the bean type to see if it implements EntityBean interface already.
   */
  private void setEntityBeanClass(DeployBeanDescriptor<?> desc) {

    Class<?> beanClass = desc.getBeanType();

    if (!hasEntityBeanInterface(beanClass)) {
      throw new IllegalStateException("Bean " + beanClass + " is not enhanced?");
    }

    // the bean already implements EntityBean
    checkInheritedClasses(beanClass);

    entityBeanCount++;
  }

  /**
   * Check that the inherited classes are the same as the entity bean (aka all
   * enhanced or all dynamically subclassed).
   */
  private void checkInheritedClasses(Class<?> beanClass) {

    Class<?> superclass = beanClass.getSuperclass();
    if (Object.class.equals(superclass)) {
      // we got to the top of the inheritance
      return;
    }
    if (Model.class.equals(superclass)) {
      // top of the inheritance. Not enhancing Model at this stage
      return;
    }
    if (!EntityBean.class.isAssignableFrom(superclass)) {
      if (isMappedSuperWithNoProperties(superclass)) {
        // ok to stop and treat just the same as Object.class
        return;
      }
      throw new IllegalStateException("Super type " + superclass + " is not enhanced?");
    }

    // recursively continue up the inheritance hierarchy
    checkInheritedClasses(superclass);
  }

  /**
   * Return true if this is a MappedSuperclass bean with no persistent properties.
   * If so it is ok for it not to be enhanced.
   */
  private boolean isMappedSuperWithNoProperties(Class<?> beanClass) {
    // do not search recursive here
    MappedSuperclass annotation = AnnotationUtil.findAnnotation(beanClass, MappedSuperclass.class);
    if (annotation == null) {
      return false;
    }
    Field[] fields = beanClass.getDeclaredFields();
    for (Field field : fields) {
      if (!Modifier.isStatic(field.getModifiers())
        && !Modifier.isTransient(field.getModifiers())
        && !field.isAnnotationPresent(Transient.class)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Return the changeLogPrepare (for setting user context into the ChangeSet
   * in the foreground thread).
   */
  public ChangeLogPrepare getChangeLogPrepare() {
    return changeLogPrepare;
  }

  /**
   * Return the changeLogListener (that actually does the logging).
   */
  public ChangeLogListener getChangeLogListener() {
    return changeLogListener;
  }

  public void addPrimaryKeyJoin(DeployBeanPropertyAssocOne<?> prop) {

    String baseTable = prop.getDesc().getBaseTable();
    DeployTableJoin inverse = prop.getTableJoin().createInverse(baseTable);

    TableJoin inverseJoin = new TableJoin(inverse);

    DeployBeanInfo<?> target = deployInfoMap.get(prop.getTargetType());
    target.setPrimaryKeyJoin(inverseJoin);
  }

  /**
   * Create a DeployBeanDescriptor for an ElementCollection target.
   */
  public <A> DeployBeanDescriptor<A> createDeployDescriptor(Class<A> targetType) {
    return new DeployBeanDescriptor<>(this, targetType, serverConfig);
  }

  /**
   * Create a BeanDescriptor for an ElementCollection target.
   */
  public <A> BeanDescriptor<A> createElementDescriptor(DeployBeanDescriptor<A> elementDescriptor, ManyType manyType, boolean scalar) {

    ElementHelp elementHelp = elementHelper(manyType);
    if (manyType.isMap()) {
      if (scalar) {
        return new BeanDescriptorElementScalarMap<>(this, elementDescriptor, elementHelp);
      } else {
        return new BeanDescriptorElementEmbeddedMap<>(this, elementDescriptor, elementHelp);
      }
    }
    if (scalar) {
      return new BeanDescriptorElementScalar<>(this, elementDescriptor, elementHelp);
    } else {
      return new BeanDescriptorElementEmbedded<>(this, elementDescriptor, elementHelp);
    }
  }

  private ElementHelp elementHelper(ManyType manyType) {
    switch (manyType) {
      case LIST: return new ElementHelpList();
      case SET: return new ElementHelpSet();
      case MAP: return new ElementHelpMap();
      default:
        throw new IllegalStateException("manyType unexpected "+manyType);
    }
  }

  public void visitMetrics(MetricVisitor visitor) {
    for (BeanDescriptor<?> desc : immutableDescriptorList) {
      desc.visitMetrics(visitor);
    }
  }

  /**
   * Comparator to sort the BeanDescriptors by name.
   */
  private static final class BeanDescComparator implements Comparator<BeanDescriptor<?>>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(BeanDescriptor<?> o1, BeanDescriptor<?> o2) {

      return o1.getName().compareTo(o2.getName());
    }
  }
}
