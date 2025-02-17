package io.ebeaninternal.server.deploy;

import io.ebean.BackgroundExecutor;
import io.ebean.DatabaseBuilder;
import io.ebean.Model;
import io.ebean.RawSqlBuilder;
import io.ebean.Transaction;
import io.ebean.annotation.ConstraintMode;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.config.*;
import io.ebean.config.dbplatform.*;
import io.ebean.core.type.ScalarType;
import io.ebean.event.changelog.ChangeLogFilter;
import io.ebean.event.changelog.ChangeLogListener;
import io.ebean.event.changelog.ChangeLogPrepare;
import io.ebean.event.changelog.ChangeLogRegister;
import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.MetricVisitor;
import io.ebean.meta.QueryPlanInit;
import io.ebean.plugin.BeanType;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.cache.CacheChangeSet;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.core.InternString;
import io.ebeaninternal.server.core.InternalConfiguration;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.deploy.id.IdBinderEmbedded;
import io.ebeaninternal.server.deploy.id.IdBinderFactory;
import io.ebeaninternal.server.deploy.meta.*;
import io.ebeaninternal.server.deploy.parse.*;
import io.ebeaninternal.server.persist.platform.MultiValueBind;
import io.ebeaninternal.server.properties.BeanPropertiesReader;
import io.ebeaninternal.server.properties.BeanPropertyAccess;
import io.ebeaninternal.server.properties.EnhanceBeanPropertyAccess;
import io.ebeaninternal.server.transaction.DataSourceSupplier;
import io.ebeaninternal.server.type.TypeManager;
import io.ebeaninternal.xmapping.api.XmapEbean;
import io.ebeaninternal.xmapping.api.XmapEntity;
import io.ebeaninternal.xmapping.api.XmapNamedQuery;
import io.ebeaninternal.xmapping.api.XmapRawSql;
import io.ebeanservice.docstore.api.DocStoreBeanAdapter;
import io.ebeanservice.docstore.api.DocStoreFactory;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Transient;
import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.System.Logger.Level.*;

/**
 * Creates BeanDescriptors.
 */
public final class BeanDescriptorManager implements BeanDescriptorMap, SpiBeanTypeManager {

  private static final System.Logger log = CoreLog.internal;

  private static final BeanDescComparator beanDescComparator = new BeanDescComparator();
  public static final String JAVA_LANG_RECORD = "java.lang.Record";

  private final ReadAnnotations readAnnotations;
  private final TransientProperties transientProperties;
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
  private final DatabaseBuilder.Settings config;
  private final ChangeLogListener changeLogListener;
  private final ChangeLogRegister changeLogRegister;
  private final ChangeLogPrepare changeLogPrepare;
  private final DocStoreFactory docStoreFactory;
  private final MultiValueBind multiValueBind;
  private final TypeManager typeManager;
  private final BootupClasses bootupClasses;
  private final String serverName;
  private final List<BeanDescriptor<?>> elementDescriptors = new ArrayList<>();
  private final Map<Class<?>, BeanTable> beanTableMap = new HashMap<>();
  private final Map<String, BeanDescriptor<?>> descMap = new HashMap<>();
  private final Map<String, BeanDescriptor<?>> descQueueMap = new HashMap<>();
  private final Map<String, BeanManager<?>> beanManagerMap = new HashMap<>();
  private final Map<String, List<BeanDescriptor<?>>> tableToDescMap = new HashMap<>();
  private final Map<String, List<BeanDescriptor<?>>> tableToViewDescMap = new HashMap<>();
  private final DbIdentity dbIdentity;
  private final DataSourceSupplier dataSourceSupplier;
  private final DatabasePlatform databasePlatform;
  private final SpiCacheManager cacheManager;
  private final BackgroundExecutor backgroundExecutor;
  private final EncryptKeyManager encryptKeyManager;
  private final IdBinderFactory idBinderFactory;
  private final BeanLifecycleAdapterFactory beanLifecycleAdapterFactory;
  private final String asOfViewSuffix;
  private final boolean jacksonCorePresent;
  private final int queryPlanTTLSeconds;
  private final BindMaxLength bindMaxLength;
  private int entityBeanCount;
  private List<BeanDescriptor<?>> immutableDescriptorList;
  /**
   * Map of base tables to 'with history views' used to support 'as of' queries.
   */
  private final Map<String, String> asOfTableMap = new HashMap<>();
  /**
   * Map of base tables to 'draft' tables.
   */
  private final Map<String, String> draftTableMap = new HashMap<>();

  // temporary collections used during startup and then cleared
  private Map<Class<?>, DeployBeanInfo<?>> deployInfoMap = new HashMap<>();
  private Set<Class<?>> embeddedIdTypes = new HashSet<>();
  private List<DeployBeanInfo<?>> embeddedBeans = new ArrayList<>();

  /**
   * Create for a given database dbConfig.
   */
  public BeanDescriptorManager(InternalConfiguration config) {
    this.config = config.getConfig();
    this.serverName = InternString.intern(this.config.getName());
    this.cacheManager = config.getCacheManager();
    this.docStoreFactory = config.getDocStoreFactory();
    this.backgroundExecutor = config.getBackgroundExecutor();
    this.dataSourceSupplier = config.getDataSourceSupplier();
    this.encryptKeyManager = this.config.getEncryptKeyManager();
    this.databasePlatform = this.config.getDatabasePlatform();
    this.multiValueBind = config.getMultiValueBind();
    this.idBinderFactory = new IdBinderFactory(databasePlatform.idInExpandedForm(), multiValueBind);
    this.queryPlanTTLSeconds = this.config.getQueryPlanTTLSeconds();
    this.asOfViewSuffix = asOfViewSuffix(databasePlatform, this.config);
    String versionsBetweenSuffix = versionsBetweenSuffix(databasePlatform, this.config);
    this.readAnnotations = new ReadAnnotations(config.getGeneratedPropertyFactory(), asOfViewSuffix, versionsBetweenSuffix, this.config);
    this.bootupClasses = config.getBootupClasses();
    this.createProperties = config.getDeployCreateProperties();
    this.namingConvention = this.config.getNamingConvention();
    this.dbIdentity = config.getDatabasePlatform().dbIdentity();
    this.deplyInherit = config.getDeployInherit();
    this.deployUtil = config.getDeployUtil();
    this.typeManager = deployUtil.typeManager();
    this.beanManagerFactory = new BeanManagerFactory(config.getDatabasePlatform());
    this.beanLifecycleAdapterFactory = new BeanLifecycleAdapterFactory(this.config);
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
    this.jacksonCorePresent = config.isJacksonCorePresent();
    this.bindMaxLength = initMaxLength();
  }

  BindMaxLength initMaxLength() {
    LengthCheck lengthCheck = this.config.getLengthCheck();
    switch (lengthCheck) {
      case OFF:
        return null;
      case UTF8:
        return BindMaxLength.ofUtf8();
      default:
        return BindMaxLength.ofStandard();
    }
  }

  @Override
  public boolean isJacksonCorePresent() {
    return jacksonCorePresent;
  }

  /**
   * Run periodic trim of query plans.
   */
  public void scheduleBackgroundTrim() {
    backgroundExecutor.scheduleWithFixedDelay(this::trimQueryPlans, 117L, 60L, TimeUnit.SECONDS);
  }

  private void trimQueryPlans() {
    long lastUsed = System.currentTimeMillis() - (queryPlanTTLSeconds * 1000L);
    for (BeanDescriptor<?> descriptor : immutableDescriptorList) {
      if (!descriptor.isEmbedded()) {
        descriptor.trimQueryPlans(lastUsed);
      }
    }
  }

  @Override
  public ScalarType<?> scalarType(String cast) {
    return typeManager.type(cast);
  }

  @Override
  public ScalarType<?> scalarType(int jdbcType) {
    return typeManager.type(jdbcType);
  }

  /**
   * Return the AsOfViewSuffix based on the DbHistorySupport.
   */
  private String asOfViewSuffix(DatabasePlatform databasePlatform, DatabaseBuilder.Settings config) {
    DbHistorySupport historySupport = databasePlatform.historySupport();
    // with historySupport returns a simple view suffix or the sql2011 as of timestamp suffix
    return (historySupport == null) ? config.getAsOfViewSuffix() : historySupport.getAsOfViewSuffix(config.getAsOfViewSuffix());
  }

  /**
   * Return the versions between timestamp suffix based on the DbHistorySupport.
   */
  private String versionsBetweenSuffix(DatabasePlatform databasePlatform, DatabaseBuilder.Settings config) {
    DbHistorySupport historySupport = databasePlatform.historySupport();
    // with historySupport returns a simple view suffix or the sql2011 versions between timestamp suffix
    return (historySupport == null) ? config.getAsOfViewSuffix() : historySupport.getVersionsBetweenSuffix(config.getAsOfViewSuffix());
  }

  @Override
  public boolean isMultiValueSupported() {
    return multiValueBind.isSupported();
  }

  @Override
  public DatabaseBuilder.Settings config() {
    return config;
  }

  @Override
  public <T> DocStoreBeanAdapter<T> createDocStoreBeanAdapter(BeanDescriptor<T> descriptor, DeployBeanDescriptor<T> deploy) {
    return docStoreFactory.createAdapter(descriptor, deploy);
  }

  public BeanDescriptor<?> descriptorByQueueId(String queueId) {
    return descQueueMap.get(queueId);
  }

  @Override
  public SpiBeanType beanType(Class<?> entityType) {
    return descriptor(entityType);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> BeanDescriptor<T> descriptor(Class<T> entityType) {
    return (BeanDescriptor<T>) descMap.get(entityType.getName());
  }

  @SuppressWarnings("unchecked")
  public <T> BeanDescriptor<T> descriptorByClassName(String entityClassName) {
    return (BeanDescriptor<T>) descMap.get(entityClassName);
  }

  @Override
  public String name() {
    return serverName;
  }

  @Override
  public SpiCacheManager cacheManager() {
    return cacheManager;
  }

  @Override
  public NamingConvention namingConvention() {
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
  public Map<String, String> draftTableMap() {
    return draftTableMap;
  }

  /**
   * Deploy returning the asOfTableMap (which is required by the SQL builders).
   */
  public Map<String, String> deploy(List<XmapEbean> mappings) {
    try {
      createListeners();
      readEntityDeploymentInitial();
      readXmlMapping(mappings);
      readEntityBeanTable();
      readEntityDeploymentAssociations();
      readInheritedIdGenerators();
      // creates the BeanDescriptors
      readEntityRelationships();
      List<BeanDescriptor<?>> list = new ArrayList<>(descMap.values());
      list.sort(beanDescComparator);
      immutableDescriptorList = Collections.unmodifiableList(list);
      initialiseAll();
      readForeignKeys();
      readTableToDescriptor();
      logStatus();

      // clear collections we no longer need
      embeddedIdTypes = null;
      embeddedBeans = null;
      deployInfoMap = null;
      return asOfTableMap;
    } catch (BeanNotEnhancedException e) {
      throw e;
    } catch (RuntimeException e) {
      log.log(ERROR, "Error in deployment", e);
      throw e;
    }
  }

  private void readXmlMapping(List<XmapEbean> mappings) {
    if (mappings != null) {
      ClassLoader classLoader = config.getClassLoadConfig().getClassLoader();
      for (XmapEbean mapping : mappings) {
        List<XmapEntity> entityDeploy = mapping.getEntity();
        for (XmapEntity deploy : entityDeploy) {
          readEntityMapping(classLoader, deploy);
        }
      }
    }
  }

  private void readEntityMapping(ClassLoader classLoader, XmapEntity entityDeploy) {
    String entityClassName = entityDeploy.getClazz();
    Class<?> entityClass;
    try {
      entityClass = Class.forName(entityClassName, false, classLoader);
    } catch (Exception e) {
      log.log(ERROR, "Could not load entity bean class " + entityClassName + " for ebean.xml entry");
      return;
    }

    DeployBeanInfo<?> info = deployInfoMap.get(entityClass);
    if (info == null) {
      log.log(ERROR, "No entity bean for ebean.xml entry " + entityClassName);

    } else {
      for (XmapRawSql sql : entityDeploy.getRawSql()) {
        RawSqlBuilder builder;
        try {
          builder = RawSqlBuilder.parse(sql.getQuery());
        } catch (RuntimeException e) {
          builder = RawSqlBuilder.unparsed(sql.getQuery());
        }

        for (Map.Entry<String, String> columnMapping : sql.getColumnMapping().entrySet()) {
          builder.columnMapping(columnMapping.getKey(), columnMapping.getValue());
        }
        for (Map.Entry<String, String> aliasMapping : sql.getAliasMapping().entrySet()) {
          builder.tableAliasMapping(aliasMapping.getKey(), aliasMapping.getValue());
        }
        info.addRawSql(sql.getName(), builder.create());
      }

      for (XmapNamedQuery namedQuery : entityDeploy.getNamedQuery()) {
        info.addNamedQuery(namedQuery.getName(), namedQuery.getQuery());
      }
    }
  }

  /**
   * Return the Encrypt key given the table and column name.
   */
  @Override
  public EncryptKey encryptKey(String tableName, String columnName) {
    return encryptKeyManager.getEncryptKey(tableName, columnName);
  }

  /**
   * For SQL based modifications we need to invalidate appropriate parts of the cache.
   */
  public void cacheNotify(TransactionEventTable.TableIUD tableIUD, CacheChangeSet changeSet) {
    String tableName = tableIUD.tableName().toLowerCase();
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
  public List<BeanDescriptor<?>> descriptors(String tableName) {
    return tableName == null ? Collections.emptyList() : tableToDescMap.get(tableName.toLowerCase());
  }

  /**
   * Return the BeanDescriptors mapped to the table.
   */
  public List<? extends BeanType<?>> beanTypes(String tableName) {
    return tableToDescMap.get(tableName.toLowerCase());
  }

  @Override
  public boolean isTableManaged(String tableName) {
    return tableToDescMap.get(tableName.toLowerCase()) != null
      || tableToViewDescMap.get(tableName.toLowerCase()) != null;
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
   */
  private void readTableToDescriptor() {
    for (BeanDescriptor<?> desc : descMap.values()) {
      String baseTable = desc.baseTable();
      if (baseTable != null) {
        baseTable = baseTable.toLowerCase();
        List<BeanDescriptor<?>> list = tableToDescMap.computeIfAbsent(baseTable, k -> new ArrayList<>(1));
        list.add(desc);
      }
      if (desc.entityType() == EntityType.VIEW && desc.isQueryCaching()) {
        // build map of tables to view entities dependent on those tables
        // for the purpose of invalidating appropriate query caches
        String[] dependentTables = desc.dependentTables();
        if (dependentTables != null) {
          for (String depTable : dependentTables) {
            depTable = depTable.toLowerCase();
            tableToViewDescMap.computeIfAbsent(depTable, k -> new ArrayList<>(1)).add(desc);
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
   * <p>
   * Also responsible for creating all the BeanManagers which contain the
   * persister, listener etc.
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
        beanManagerMap.put(d.fullName(), beanManagerFactory.create(d));
        checkForValidEmbeddedId(d);
      }
    }
  }

  private void checkForValidEmbeddedId(BeanDescriptor<?> d) {
    IdBinder idBinder = d.idBinder();
    if (idBinder instanceof IdBinderEmbedded) {
      IdBinderEmbedded embId = (IdBinderEmbedded) idBinder;
      BeanDescriptor<?> idBeanDescriptor = embId.descriptor();
      Class<?> idType = idBeanDescriptor.type();
      try {
        idType.getDeclaredMethod("hashCode");
        idType.getDeclaredMethod("equals", Object.class);
      } catch (NoSuchMethodException e) {
        checkMissingHashCodeOrEquals(e, idType, d.type());
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
  public List<BeanDescriptor<?>> descriptorList() {
    return immutableDescriptorList;
  }

  public BeanTable beanTable(Class<?> type) {
    return beanTableMap.get(type);
  }

  /**
   * Return a BeanTable for an ElementCollection.
   */
  public BeanTable createCollectionBeanTable(String fullTableName, Class<?> targetType) {
    return new BeanTable(this, fullTableName, targetType);
  }

  @SuppressWarnings("unchecked")
  public <T> BeanManager<T> beanManager(Class<T> entityType) {
    BeanManager<T> mgr = (BeanManager<T>) beanManager(entityType.getName());
    if (mgr == null) {
      errorBeanNotRegistered(entityType);
    }
    return mgr;
  }

  private <T> void errorBeanNotRegistered(Class<T> entityType) {
    if (beanManagerMap.isEmpty()) {
      throw new PersistenceException(errNothingRegistered());
    } else {
      throw new PersistenceException(errNotRegistered(entityType));
    }
  }

  private String errNothingRegistered() {
    return "There are no registered entities. If using query beans, that generates EbeanEntityRegister.java into " +
      "generated sources and is service loaded. If using module-info.java, then probably missing 'provides io.ebean.config.EntityClassRegister with EbeanEntityRegister' clause.";
  }

  private String errNotRegistered(Class<?> beanClass) {
    return "The type [" + beanClass + "] is not a registered entity?"
      + " If you don't explicitly list the entity classes to use Ebean will search for them in the classpath.";
  }

  private BeanManager<?> beanManager(String beanClassName) {
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
    log.log(DEBUG, "BeanPersistControllers[{0}] BeanFinders[{1}] BeanPersistListeners[{2}] BeanQueryAdapters[{3}] BeanPostLoaders[{4}] BeanPostConstructors[{5}]", cc, fc, lc, qa, pl, pc);
  }

  private void logStatus() {
    log.log(DEBUG, "Entities[{0}]", entityBeanCount);
  }

  /**
   * Return the bean deploy info for the given class.
   */
  @SuppressWarnings("unchecked")
  public <T> DeployBeanInfo<T> deploy(Class<T> cls) {
    return (DeployBeanInfo<T>) deployInfoMap.get(cls);
  }

  private void registerDescriptor(DeployBeanInfo<?> info) {
    BeanDescriptor<?> desc = new BeanDescriptor<>(this, info.getDescriptor());
    descMap.put(desc.type().getName(), desc);
    if (desc.isDocStoreMapped()) {
      descQueueMap.put(desc.docStoreQueueId(), desc);
    }
    for (BeanPropertyAssocMany<?> many : desc.propertiesMany()) {
      if (many.isElementCollection()) {
        elementDescriptors.add(many.elementDescriptor());
      }
    }
  }

  /**
   * Read the initial deployment information for the entities.
   * <p>
   * This stops short of reading relationship meta data until after the
   * BeanTables have all been created.
   */
  private void readEntityDeploymentInitial() {
    for (Class<?> entityClass : bootupClasses.getEntities()) {
      DeployBeanInfo<?> info = createDeployBeanInfo(entityClass);
      deployInfoMap.put(entityClass, info);
      Class<?> embeddedIdType = info.getEmbeddedIdType();
      if (embeddedIdType != null) {
        embeddedIdTypes.add(embeddedIdType);
      }
    }
    for (Class<?> entityClass : bootupClasses.getEmbeddables()) {
      DeployBeanInfo<?> info = createDeployBeanInfo(entityClass);
      deployInfoMap.put(entityClass, info);
      if (embeddedIdTypes.contains(entityClass)) {
        // register embeddedId types early - scalar properties only
        // and needed for creating BeanTables (id properties)
        registerEmbeddedBean(info);
      } else {
        // delay register of other embedded beans until after
        // the BeanTables have been created to support ManyToOne
        embeddedBeans.add(info);
      }
    }
  }

  private void registerEmbeddedBean(DeployBeanInfo<?> info) {
    readDeployAssociations(info);
    registerDescriptor(info);
  }

  /**
   * Create the BeanTable information which has the base table and id.
   * <p>
   * This is determined prior to resolving relationship information.
   */
  private void readEntityBeanTable() {
    for (DeployBeanInfo<?> info : deployInfoMap.values()) {
      BeanTable beanTable = createBeanTable(info);
      beanTableMap.put(beanTable.getBeanType(), beanTable);
    }
    // register non-id embedded beans (after bean tables are created)
    for (DeployBeanInfo<?> info : embeddedBeans) {
      registerEmbeddedBean(info);
    }
  }

  /**
   * Create the BeanTable information which has the base table and id.
   * <p>
   * This is determined prior to resolving relationship information.
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
   * Create the BeanTable from the deployment information gathered so far.
   */
  private BeanTable createBeanTable(DeployBeanInfo<?> info) {
    DeployBeanDescriptor<?> deployDescriptor = info.getDescriptor();
    DeployBeanTable beanTable = deployDescriptor.createDeployBeanTable();
    return new BeanTable(beanTable, this);
  }

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
    for (DeployBeanInfo<?> info : deployInfoMap.values()) {
      setInheritanceInfo(info);
    }
    for (DeployBeanInfo<?> info : deployInfoMap.values()) {
      if (!info.isEmbedded()) {
        registerDescriptor(info);
      }
    }
  }

  /**
   * Sets the inheritance info.
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
          String msg = "Error with property " + prop+ ". Could not find a Relationship to table " + tableName
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

  private DeployBeanDescriptor<?> targetDescriptor(DeployBeanPropertyAssoc<?> prop) {
    Class<?> targetType = prop.getTargetType();
    DeployBeanInfo<?> info = deployInfoMap.get(targetType);
    if (info == null) {
      throw new PersistenceException("Can not find descriptor [" + targetType + "] for " + prop);
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
    DeployBeanDescriptor<?> targetDesc = targetDescriptor(prop);
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
      prop.clearTableJoin();
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
            // we have a match
            prop.setMappedBy(possibleMappedBy);
            return true;
          }
        }
      }
    }
    // multiple options so should specify mappedBy property
    String msg = "Error on " + prop + " missing mappedBy.";
    msg += " There are [" + matchSet.size() + "] possible properties in " + targetDesc;
    msg += " that this association could be mapped to. Please specify one using ";
    msg += "the mappedBy attribute on @OneToMany.";
    throw new PersistenceException(msg);
  }

  private void makeOrderColumn(DeployBeanPropertyAssocMany<?> oneToMany) {
    DeployBeanDescriptor<?> targetDesc = targetDescriptor(oneToMany);
    DeployOrderColumn orderColumn = oneToMany.getOrderColumn();
    final ScalarType<?> scalarType = typeManager.type(Integer.class);
    DeployBeanProperty orderProperty = new DeployBeanProperty(targetDesc, Integer.class, scalarType, null);
    orderProperty.setName(DeployOrderColumn.LOGICAL_NAME);
    orderProperty.setDbColumn(orderColumn.getName());
    orderProperty.setNullable(orderColumn.isNullable());
    orderProperty.setDbInsertable(orderColumn.isInsertable());
    orderProperty.setDbUpdateable(orderColumn.isUpdatable());
    orderProperty.setDbRead(true);
    orderProperty.setOwningType(targetDesc.getBeanType());
    final InheritInfo targetInheritInfo = targetDesc.getInheritInfo();
    if (targetInheritInfo != null) {
      for (InheritInfo child : targetInheritInfo.getChildren()) {
        final DeployBeanDescriptor<?> childDescriptor = deployInfoMap.get(child.getType()).getDescriptor();
        childDescriptor.setOrderColumn(orderProperty);
      }
    }
    targetDesc.setOrderColumn(orderProperty);
  }

  /**
   * A OneToMany with no matching mappedBy property in the target so must be
   * unidirectional.
   * <p>
   * This means that inserts MUST cascade for this property.
   * <p>
   * Create a "Shadow"/Unidirectional property on the target. It is used with
   * inserts to set the foreign key value (e.g. inserts the foreign key value
   * into the order_id column on the order_lines table).
   */
  private void makeUnidirectional(DeployBeanPropertyAssocMany<?> oneToMany) {
    DeployBeanDescriptor<?> targetDesc = targetDescriptor(oneToMany);
    Class<?> owningType = oneToMany.getOwningType();
    if (!oneToMany.getCascadeInfo().isSave()) {
      // The property MUST have persist cascading so that inserts work.
      Class<?> targetType = oneToMany.getTargetType();
      String msg = "Error on " + oneToMany + ". @OneToMany MUST have ";
      msg += "Cascade.PERSIST or Cascade.ALL because this is a unidirectional ";
      msg += "relationship. That is, there is no property of type " + owningType + " on " + targetType;
      throw new PersistenceException(msg);
    }

    // mark this property as unidirectional
    oneToMany.setUnidirectional();
    // specify table and table alias...
    BeanTable beanTable = beanTable(owningType);
    // define the TableJoin
    DeployTableJoin oneToManyJoin = oneToMany.getTableJoin();
    if (!oneToManyJoin.hasJoinColumns()) {
      throw new RuntimeException("No join columns found to satisfy the relationship " + oneToMany);
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
    DeployBeanDescriptor<?> targetDesc = targetDescriptor(prop);
    DeployBeanPropertyAssocOne<?> mappedAssocOne = mappedOneToOne(prop, mappedBy, targetDesc);
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

  private DeployBeanPropertyAssocOne<?> mappedOneToOne(DeployBeanPropertyAssocOne<?> prop, String mappedBy, DeployBeanDescriptor<?> targetDesc) {
    DeployBeanProperty mappedProp = targetDesc.getBeanProperty(mappedBy);
    if (mappedProp == null) {
      throw new PersistenceException("Error on " + prop + " Can not find mappedBy property " + targetDesc + "." + mappedBy);
    }
    if (!(mappedProp instanceof DeployBeanPropertyAssocOne<?>)) {
      throw new PersistenceException("Error on " + prop + ". mappedBy property " + targetDesc + "." + mappedBy + " is not a OneToOne?");
    }
    DeployBeanPropertyAssocOne<?> mappedAssocOne = (DeployBeanPropertyAssocOne<?>) mappedProp;
    if (!mappedAssocOne.isOneToOne()) {
      throw new PersistenceException("Error on " + prop + ". mappedBy property " + targetDesc + "." + mappedBy + " is not a OneToOne?");
    }
    return mappedAssocOne;
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
   */
  private void checkMappedByOneToMany(DeployBeanInfo<?> info, DeployBeanPropertyAssocMany<?> prop) {
    if (prop.isElementCollection()) {
      // skip mapping check
      return;
    }
    DeployBeanDescriptor<?> targetDesc = targetDescriptor(prop);
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
    DeployBeanPropertyAssocOne<?> mappedAssocOne = mappedManyToOne(prop, targetDesc, mappedBy);
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

  private DeployBeanPropertyAssocOne<?> mappedManyToOne(DeployBeanPropertyAssocMany<?> prop, DeployBeanDescriptor<?> targetDesc, String mappedBy) {
    DeployBeanProperty mappedProp = targetDesc.getBeanProperty(mappedBy);
    if (mappedProp == null) {
      throw new PersistenceException("Error on " + prop + "  Can not find mappedBy property " + mappedBy + " in " + targetDesc);
    }
    if (!(mappedProp instanceof DeployBeanPropertyAssocOne<?>)) {
      throw new PersistenceException("Error on " + prop + ". mappedBy property " + mappedBy + " is not a ManyToOne? in " + targetDesc);
    }
    return (DeployBeanPropertyAssocOne<?>) mappedProp;
  }

  /**
   * For mappedBy copy the joins from the other side.
   */
  private void checkMappedByManyToMany(DeployBeanPropertyAssocMany<?> prop) {
    // get the bean descriptor that holds the mappedBy property
    String mappedBy = prop.getMappedBy();
    if (mappedBy == null) {
      if (targetDescriptor(prop).isDraftable()) {
        prop.setIntersectionDraftTable();
      }
      return;
    }

    // get the mappedBy property
    DeployBeanDescriptor<?> targetDesc = targetDescriptor(prop);
    DeployBeanPropertyAssocMany<?> mappedAssocMany = mappedManyToMany(prop, mappedBy, targetDesc);

    // define the relationships/joins on this side as the
    // reverse of the other mappedBy side ...
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

  private DeployBeanPropertyAssocMany<?> mappedManyToMany(DeployBeanPropertyAssocMany<?> prop, String mappedBy, DeployBeanDescriptor<?> targetDesc) {
    DeployBeanProperty mappedProp = targetDesc.getBeanProperty(mappedBy);
    if (mappedProp == null) {
      throw new PersistenceException("Error on " + prop + "  Can not find mappedBy property " + mappedBy + " in " + targetDesc);
    }
    if (!(mappedProp instanceof DeployBeanPropertyAssocMany<?>)) {
      throw new PersistenceException("Error on " + prop + ". mappedBy property " + targetDesc + "." + mappedBy + " is not a ManyToMany?");
    }

    DeployBeanPropertyAssocMany<?> mappedAssocMany = (DeployBeanPropertyAssocMany<?>) mappedProp;
    if (!mappedAssocMany.isManyToMany()) {
      throw new PersistenceException("Error on " + prop + ". mappedBy property " + targetDesc + "." + mappedBy + " is not a ManyToMany?");
    }
    return mappedAssocMany;
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
    DeployBeanDescriptor<T> desc = new DeployBeanDescriptor<>(this, beanClass, config);
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
    setAccessors(desc);
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
      return;
    }
    final DeployIdentityMode identityMode = desc.getIdentityMode();
    if (identityMode.isSequence() && !dbIdentity.isSupportsSequence()) {
      // explicit sequence but not supported by the DatabasePlatform
      log.log(INFO, "Explicit sequence on {0} but not supported by DB Platform - ignored", desc.getFullName());
      identityMode.setIdType(IdType.AUTO);
    }
    if (identityMode.isIdentity() && !dbIdentity.isSupportsIdentity()) {
      // explicit identity but not supported by the DatabasePlatform
      log.log(INFO, "Explicit Identity on {0} but not supported by DB Platform - ignored", desc.getFullName());
      identityMode.setIdType(IdType.AUTO);
    }

    if (identityMode.isAuto()) {
      if (desc.isPrimaryKeyCompoundOrNonNumeric()) {
        identityMode.setIdType(IdType.EXTERNAL);
        return;
      }
      if (desc.isIdGeneratedValue() || config.isIdGeneratorAutomatic()) {
        // use IDENTITY or SEQUENCE based on platform
        identityMode.setPlatformType(dbIdentity.getIdType());
      } else {
        // externally/application supplied Id values
        identityMode.setIdType(IdType.EXTERNAL);
        return;
      }
    }

    if (desc.getBaseTable() == null) {
      // no base table so not going to set Identity or sequence information
      return;
    }

    if (identityMode.isIdentity()) {
      // used when getGeneratedKeys is not supported (SQL Server 2000, SAP Hana)
      String selectLastInsertedId = dbIdentity.getSelectLastInsertedId(desc.getBaseTable());
      String selectLastInsertedIdDraft = (!desc.isDraftable()) ? selectLastInsertedId : dbIdentity.getSelectLastInsertedId(desc.getDraftTable());
      desc.setSelectLastInsertedId(selectLastInsertedId, selectLastInsertedIdDraft);
      return;
    }

    if (identityMode.isSequence()) {
      String seqName = identityMode.getSequenceName();
      if (seqName == null || seqName.isEmpty()) {
        String primaryKeyColumn = desc.getSinglePrimaryKeyColumn();
        seqName = namingConvention.getSequenceName(desc.getBaseTable(), primaryKeyColumn);
      }
      int stepSize = desc.setIdentitySequenceBatchMode(databasePlatform.sequenceBatchMode());
      desc.setIdGenerator(createSequenceIdGenerator(seqName, stepSize));
    }
  }

  private PlatformIdGenerator createSequenceIdGenerator(String seqName, int stepSize) {
    return new PlatformIdGenerator() {

      private Map<DataSource, PlatformIdGenerator> map = Collections.synchronizedMap(new WeakHashMap<>());

      private PlatformIdGenerator create() {
        return databasePlatform.createSequenceIdGenerator(backgroundExecutor, dataSourceSupplier.getDataSource(), stepSize, seqName);
      }

      private PlatformIdGenerator get() {
        return map.computeIfAbsent(dataSourceSupplier.getDataSource(), k -> create());
      }

      @Override
      public void preAllocateIds(int allocateSize) {
        get().preAllocateIds(allocateSize);

      }

      @Override
      public Object nextId(Transaction transaction) {
        return get().nextId(transaction);
      }

      @Override
      public boolean isDbSequence() {
        return get().isDbSequence();
      }

      @Override
      public String getName() {
        return get().getName();
      }
    };
  }

  private void setAccessors(DeployBeanDescriptor<?> deploy) {
    // check to see if the bean supports EntityBean interface
    // generate a subclass if required
    confirmEnhanced(deploy);
    // use Code generation or Standard reflection to support
    // getter and setter methods
    setPropertyAccessors(deploy);
  }

  /**
   * Set the Scalar Types on all the simple types. This is done AFTER transients
   * have been identified. This is because a non-transient field MUST have a
   * ScalarType. It is useful for transients to have ScalarTypes because then
   * they can be used in a SqlSelect query.
   * <p>
   * Enums are treated a bit differently in that they always have a ScalarType
   * as one is built for them.
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
   */
  private void setPropertyAccessors(DeployBeanDescriptor<?> desc) {
    // Set the BeanReflectGetter and BeanReflectSetter that typically
    // use generated code. NB: Due to Bug 166 so now doing this for
    // abstract classes as well.
    BeanPropertiesReader reflectProps = new BeanPropertiesReader(desc.propertyNames());
    for (DeployBeanProperty prop : desc.propertiesAll()) {
      String propName = prop.getName();
      Integer pos = reflectProps.propertyIndex(propName);
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
    for (DeployBeanProperty prop : desc.propertiesBase()) {
      if (prop.isVersionColumn()) {
        return true;
      }
    }
    return false;
  }

  private boolean hasEntityBeanInterface(Class<?> beanClass) {
    for (Class<?> anInterface : beanClass.getInterfaces()) {
      if (anInterface.equals(EntityBean.class)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Test the bean type to see if it implements EntityBean interface already.
   */
  private void confirmEnhanced(DeployBeanDescriptor<?> desc) {
    Class<?> beanClass = desc.getBeanType();
    if (!hasEntityBeanInterface(beanClass)) {
      String msg = "Bean " + beanClass + " is not enhanced? Check packages specified in ebean.mf. If you are running in IDEA or " +
        "Eclipse check that the enhancement plugin is installed. See https://ebean.io/docs/trouble-shooting#not-enhanced";
      throw new BeanNotEnhancedException(msg);
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
    if (Object.class.equals(superclass) || Model.class.equals(superclass) || JAVA_LANG_RECORD.equals(superclass.getName())) {
      // we got to the top of the inheritance
      return;
    }
    if (!EntityBean.class.isAssignableFrom(superclass)) {
      if (isMappedSuperWithNoProperties(superclass)) {
        // ok to stop and treat just the same as Object.class
        return;
      }
      throw new BeanNotEnhancedException("Super type " + superclass + " is not enhanced? Check the packages specified in ebean.mf See https://ebean.io/docs/trouble-shooting#not-enhanced");
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
    MappedSuperclass annotation = AnnotationUtil.get(beanClass, MappedSuperclass.class);
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
  public ChangeLogPrepare changeLogPrepare() {
    return changeLogPrepare;
  }

  /**
   * Return the changeLogListener (that actually does the logging).
   */
  public ChangeLogListener changeLogListener() {
    return changeLogListener;
  }

  private void addPrimaryKeyJoin(DeployBeanPropertyAssocOne<?> prop) {
    String baseTable = prop.getDesc().getBaseTable();
    DeployTableJoin inverse = prop.getTableJoin().createInverse(baseTable);
    TableJoin inverseJoin = new TableJoin(inverse, prop.getForeignKey());
    DeployBeanInfo<?> target = deployInfoMap.get(prop.getTargetType());
    target.setPrimaryKeyJoin(inverseJoin);
  }

  /**
   * Create a DeployBeanDescriptor for an ElementCollection target.
   */
  public <A> DeployBeanDescriptor<A> createDeployDescriptor(Class<A> targetType) {
    return new DeployBeanDescriptor<>(this, targetType, config);
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
      case LIST:
        return new ElementHelpList();
      case SET:
        return new ElementHelpSet();
      case MAP:
        return new ElementHelpMap();
      default:
        throw new IllegalStateException("manyType unexpected " + manyType);
    }
  }

  public void visitMetrics(MetricVisitor visitor) {
    for (BeanDescriptor<?> desc : immutableDescriptorList) {
      desc.visitMetrics(visitor);
    }
    for (BeanDescriptor<?> desc : elementDescriptors) {
      desc.visitMetrics(visitor);
    }
  }

  public List<MetaQueryPlan> queryPlanInit(QueryPlanInit request) {
    List<MetaQueryPlan> list = new ArrayList<>();
    for (BeanDescriptor<?> desc : immutableDescriptorList) {
      desc.queryPlanInit(request, list);
    }
    return list;
  }

  public BindMaxLength bindMaxLength() {
    return bindMaxLength;
  }

  /**
   * Comparator to sort the BeanDescriptors by name.
   */
  private static final class BeanDescComparator implements Comparator<BeanDescriptor<?>>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(BeanDescriptor<?> o1, BeanDescriptor<?> o2) {
      return o1.name().compareTo(o2.name());
    }
  }
}
