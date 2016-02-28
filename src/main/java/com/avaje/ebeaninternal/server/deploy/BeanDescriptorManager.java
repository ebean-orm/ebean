package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.Model;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.annotation.ConcurrencyMode;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.config.EncryptKey;
import com.avaje.ebean.config.EncryptKeyManager;
import com.avaje.ebean.config.NamingConvention;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.DbHistorySupport;
import com.avaje.ebean.config.dbplatform.DbIdentity;
import com.avaje.ebean.config.dbplatform.IdGenerator;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebean.event.changelog.ChangeLogFilter;
import com.avaje.ebean.event.changelog.ChangeLogListener;
import com.avaje.ebean.event.changelog.ChangeLogPrepare;
import com.avaje.ebean.event.changelog.ChangeLogRegister;
import com.avaje.ebean.plugin.BeanType;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.TransactionEventTable;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.core.InternString;
import com.avaje.ebeaninternal.server.core.InternalConfiguration;
import com.avaje.ebeaninternal.server.core.Message;
import com.avaje.ebeaninternal.server.core.XmlConfig;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import com.avaje.ebeaninternal.server.deploy.id.IdBinder;
import com.avaje.ebeaninternal.server.deploy.id.IdBinderEmbedded;
import com.avaje.ebeaninternal.server.deploy.id.IdBinderFactory;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanTable;
import com.avaje.ebeaninternal.server.deploy.meta.DeployTableJoin;
import com.avaje.ebeaninternal.server.deploy.parse.DeployBeanInfo;
import com.avaje.ebeaninternal.server.deploy.parse.DeployCreateProperties;
import com.avaje.ebeaninternal.server.deploy.parse.DeployInherit;
import com.avaje.ebeaninternal.server.deploy.parse.DeployUtil;
import com.avaje.ebeaninternal.server.deploy.parse.ReadAnnotations;
import com.avaje.ebeaninternal.server.deploy.parse.TransientProperties;
import com.avaje.ebeaninternal.server.idgen.UuidIdGenerator;
import com.avaje.ebeaninternal.server.lib.util.Dnode;
import com.avaje.ebeaninternal.server.properties.BeanPropertiesReader;
import com.avaje.ebeaninternal.server.properties.BeanPropertyInfo;
import com.avaje.ebeaninternal.server.properties.BeanPropertyInfoFactory;
import com.avaje.ebeaninternal.server.properties.EnhanceBeanPropertyInfoFactory;
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

  private final BeanPropertyInfoFactory reflectFactory;

  private final DeployUtil deployUtil;

  private final PersistControllerManager persistControllerManager;

  private final PostLoadManager postLoadManager;

  private final BeanFinderManager beanFinderManager;

  private final PersistListenerManager persistListenerManager;

  private final BeanQueryAdapterManager beanQueryAdapterManager;

  private final NamingConvention namingConvention;

  private final DeployCreateProperties createProperties;

  private final DeployOrmXml deployOrmXml;

  private final BeanManagerFactory beanManagerFactory;

  private final ChangeLogListener changeLogListener;

  private final ChangeLogRegister changeLogRegister;

  private final ChangeLogPrepare changeLogPrepare;

  private int enhancedClassCount;
  
  private final boolean updateChangesOnly;

  private final BootupClasses bootupClasses;

  private final String serverName;

  private final ServerConfig serverConfig;

  private Map<Class<?>, DeployBeanInfo<?>> deplyInfoMap = new HashMap<Class<?>, DeployBeanInfo<?>>();

  private final Map<Class<?>, BeanTable> beanTableMap = new HashMap<Class<?>, BeanTable>();

  private final Map<String, BeanDescriptor<?>> descMap = new HashMap<String, BeanDescriptor<?>>();

  private final Map<String, BeanManager<?>> beanManagerMap = new HashMap<String, BeanManager<?>>();

  private final Map<String, List<BeanDescriptor<?>>> tableToDescMap = new HashMap<String, List<BeanDescriptor<?>>>();

  private List<BeanDescriptor<?>> immutableDescriptorList;

  private final DbIdentity dbIdentity;

  private final DataSource dataSource;

  private final DatabasePlatform databasePlatform;

  private final UuidIdGenerator uuidIdGenerator = new UuidIdGenerator();

  private final ServerCacheManager cacheManager;

  private final BackgroundExecutor backgroundExecutor;

  private final int dbSequenceBatchSize;

  private final EncryptKeyManager encryptKeyManager;

  private final IdBinderFactory idBinderFactory;

  private final XmlConfig xmlConfig;

  private final BeanLifecycleAdapterFactory beanLifecycleAdapterFactory;

  private final boolean eagerFetchLobs;

  private final String asOfViewSuffix;

  /**
   * Map of base tables to 'with history views' used to support 'as of' queries.
   */
  private final Map<String,String> asOfTableMap = new HashMap<String, String>();

  /**
   * Map of base tables to 'draft' tables.
   */
  private final Map<String,String> draftTableMap = new HashMap<String, String>();

  /**
   * Create for a given database dbConfig.
   */
  public BeanDescriptorManager(InternalConfiguration config) {

    this.serverConfig = config.getServerConfig();
    this.serverName = InternString.intern(serverConfig.getName());
    this.cacheManager = config.getCacheManager();
    this.xmlConfig = config.getXmlConfig();
    this.dbSequenceBatchSize = serverConfig.getDatabaseSequenceBatchSize();
    this.backgroundExecutor = config.getBackgroundExecutor();
    this.dataSource = serverConfig.getDataSource();
    this.encryptKeyManager = serverConfig.getEncryptKeyManager();
    this.databasePlatform = serverConfig.getDatabasePlatform();
    this.idBinderFactory = new IdBinderFactory(databasePlatform.isIdInExpandedForm());
    this.eagerFetchLobs = serverConfig.isEagerFetchLobs();

    this.asOfViewSuffix = getAsOfViewSuffix(databasePlatform, serverConfig);
    String versionsBetweenSuffix = getVersionsBetweenSuffix(databasePlatform, serverConfig);
    this.readAnnotations = new ReadAnnotations(config.getGeneratedPropertyFactory(), asOfViewSuffix, versionsBetweenSuffix);
    this.bootupClasses = config.getBootupClasses();
    this.createProperties = config.getDeployCreateProperties();
    this.namingConvention = serverConfig.getNamingConvention();
    this.dbIdentity = config.getDatabasePlatform().getDbIdentity();
    this.deplyInherit = config.getDeployInherit();
    this.deployOrmXml = config.getDeployOrmXml();
    this.deployUtil = config.getDeployUtil();

    this.beanManagerFactory = new BeanManagerFactory(config.getDatabasePlatform());

    this.updateChangesOnly = serverConfig.isUpdateChangesOnly();

    this.beanLifecycleAdapterFactory = new BeanLifecycleAdapterFactory();
    this.persistControllerManager = new PersistControllerManager(bootupClasses);
    this.postLoadManager = new PostLoadManager(bootupClasses);
    this.persistListenerManager = new PersistListenerManager(bootupClasses);
    this.beanQueryAdapterManager = new BeanQueryAdapterManager(bootupClasses);
    this.beanFinderManager = new BeanFinderManager(bootupClasses);

    this.reflectFactory = createReflectionFactory();
    this.transientProperties = new TransientProperties();
    this.changeLogPrepare = config.changeLogPrepare(bootupClasses.getChangeLogPrepare());
    this.changeLogListener = config.changeLogListener(bootupClasses.getChangeLogListener());
    this.changeLogRegister = config.changeLogRegister(bootupClasses.getChangeLogRegister());
  }

  /**
   * Return the AsOfViewSuffix based on the DbHistorySupport.
   */
  private String getAsOfViewSuffix(DatabasePlatform databasePlatform, ServerConfig serverConfig) {

    DbHistorySupport historySupport = databasePlatform.getHistorySupport();
    // with historySupport returns a simple view suffix or the sql2011 as of timestamp suffix
    return (historySupport == null ) ? serverConfig.getAsOfViewSuffix() : historySupport.getAsOfViewSuffix(serverConfig.getAsOfViewSuffix());
  }

  /**
   * Return the versions between timestamp suffix based on the DbHistorySupport.
   */
  private String getVersionsBetweenSuffix(DatabasePlatform databasePlatform, ServerConfig serverConfig) {

    DbHistorySupport historySupport = databasePlatform.getHistorySupport();
    // with historySupport returns a simple view suffix or the sql2011 versions between timestamp suffix
    return (historySupport == null ) ? serverConfig.getAsOfViewSuffix() : historySupport.getVersionsBetweenSuffix(serverConfig.getAsOfViewSuffix());
  }

  @Override
  public ServerConfig getServerConfig() {
    return serverConfig;
  }

  @SuppressWarnings("unchecked")
  public <T> BeanDescriptor<T> getBeanDescriptor(Class<T> entityType) {
    return (BeanDescriptor<T>) descMap.get(entityType.getName());
  }

  @SuppressWarnings("unchecked")
  public <T> BeanDescriptor<T> getBeanDescriptorByClassName(String entityClassName) {
    return (BeanDescriptor<T>) descMap.get(entityClassName);
  }

  public String getServerName() {
    return serverName;
  }

  public ServerCacheManager getCacheManager() {
    return cacheManager;
  }

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

  public IdBinder createIdBinder(BeanProperty idProperty) {
    return idBinderFactory.createIdBinder(idProperty);
  }

  /**
   * Return the map of base tables to draft tables.
   */
  public Map<String,String> getDraftTableMap() {
    return draftTableMap;
  }

  /**
   * Deploy returning the asOfTableMap (which is required by the SQL builders).
   */
  public Map<String,String> deploy() {

    try {
      createListeners();
      readEmbeddedDeployment();
      readEntityDeploymentInitial();
      readEntityBeanTable();
      readEntityDeploymentAssociations();
      readInheritedIdGenerators();

      // creates the BeanDescriptors
      readEntityRelationships();
      readRawSqlQueries();

      List<BeanDescriptor<?>> list = new ArrayList<BeanDescriptor<?>>(descMap.values());
      Collections.sort(list, beanDescComparator);
      immutableDescriptorList = Collections.unmodifiableList(list);

      initialiseAll();
      readForeignKeys();

      readTableToDescriptor();

      logStatus();

      deplyInfoMap.clear();
      deplyInfoMap = null;

      return asOfTableMap;

    } catch (RuntimeException e) {
      String msg = "Error in deployment";
      logger.error(msg, e);
      throw e;
    }
  }

  /**
   * Return the Encrypt key given the table and column name.
   */
  public EncryptKey getEncryptKey(String tableName, String columnName) {
    return encryptKeyManager.getEncryptKey(tableName, columnName);
  }

  /**
   * For SQL based modifications we need to invalidate appropriate parts of the
   * cache.
   */
  public void cacheNotify(TransactionEventTable.TableIUD tableIUD) {

    List<BeanDescriptor<?>> list = getBeanDescriptors(tableIUD.getTableName());
    if (list != null) {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).cacheHandleBulkUpdate(tableIUD);
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

        List<BeanDescriptor<?>> list = tableToDescMap.get(baseTable);
        if (list == null) {
          list = new ArrayList<BeanDescriptor<?>>(1);
          tableToDescMap.put(baseTable, list);
        }
        list.add(desc);
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

    // PASS 1:
    // initialise the ID properties of all the beans
    // first (as they are needed to initialise the
    // associated properties in the second pass).
    for (BeanDescriptor<?> d : descMap.values()) {
      d.initialiseId(asOfTableMap, draftTableMap);
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
      d.initialiseOther(asOfTableMap, asOfViewSuffix, draftTableMap);
    }

    // create BeanManager for each non-embedded entity bean
    for (BeanDescriptor<?> d : descMap.values()) {
      if (!d.isEmbedded()) {
        BeanManager<?> m = beanManagerFactory.create(d);
        beanManagerMap.put(d.getFullName(), m);

        checkForValidEmbeddedId(d);
      }
    }
  }

  private void checkForValidEmbeddedId(BeanDescriptor<?> d) {
    IdBinder idBinder = d.getIdBinder();
    if (idBinder != null && idBinder instanceof IdBinderEmbedded) {
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
   * Return an immutable list of all the BeanDescriptors.
   */
  public List<BeanDescriptor<?>> getBeanDescriptorList() {
    return immutableDescriptorList;
  }

  public BeanTable getBeanTable(Class<?> type) {
    return beanTableMap.get(type);
  }

  @SuppressWarnings("unchecked")
  public <T> BeanManager<T> getBeanManager(Class<T> entityType) {

    return (BeanManager<T>) getBeanManager(entityType.getName());
  }

  public BeanManager<?> getBeanManager(String beanClassName) {
    return beanManagerMap.get(beanClassName);
  }

  public DNativeQuery getNativeQuery(String name) {
    return deployOrmXml.getNativeQuery(name);
  }

  /**
   * Create the BeanControllers, BeanFinders and BeanListeners.
   */
  private void createListeners() {

    int qa = beanQueryAdapterManager.getRegisterCount();
    int cc = persistControllerManager.getRegisterCount();
    int pl = postLoadManager.getRegisterCount();
    int lc = persistListenerManager.getRegisterCount();
    int fc = beanFinderManager.getRegisterCount();

    logger.debug("BeanPersistControllers[" + cc + "] BeanFinders[" + fc + "] BeanPersistListeners[" + lc + "] BeanQueryAdapters[" + qa + "] BeanPostLoaders[" + pl + "]");
  }

  private void logStatus() {
    logger.info("Entities enhanced[" + enhancedClassCount + "]");
  }

  private <T> BeanDescriptor<T> createEmbedded(Class<T> beanClass) {

    DeployBeanInfo<T> info = createDeployBeanInfo(beanClass);
    readDeployAssociations(info);
    return new BeanDescriptor<T>(this, info.getDescriptor());
  }

  private void registerBeanDescriptor(BeanDescriptor<?> desc) {
    descMap.put(desc.getBeanType().getName(), desc);
  }

  /**
   * Read deployment information for all the embedded beans.
   */
  private void readEmbeddedDeployment() {

    List<Class<?>> embeddedClasses = bootupClasses.getEmbeddables();
    for (int i = 0; i < embeddedClasses.size(); i++) {
      Class<?> cls = embeddedClasses.get(i);
      if (logger.isTraceEnabled()) {
        String msg = "load deployinfo for embeddable:" + cls.getName();
        logger.trace(msg);
      }
      BeanDescriptor<?> embDesc = createEmbedded(cls);
      registerBeanDescriptor(embDesc);
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

    List<Class<?>> entityClasses = bootupClasses.getEntities();

    for (Class<?> entityClass : entityClasses) {
      DeployBeanInfo<?> info = createDeployBeanInfo(entityClass);
      deplyInfoMap.put(entityClass, info);
    }
  }

  /**
   * Create the BeanTable information which has the base table and id.
   * <p>
   * This is determined prior to resolving relationship information.
   * </p>
   */
  private void readEntityBeanTable() {

    for (DeployBeanInfo<?> info : deplyInfoMap.values()) {
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

    for (DeployBeanInfo<?> info : deplyInfoMap.values()) {
      readDeployAssociations(info);
    }
  }

  private void readInheritedIdGenerators() {

    for (DeployBeanInfo<?> info : deplyInfoMap.values()) {
      DeployBeanDescriptor<?> descriptor = info.getDescriptor();
      InheritInfo inheritInfo = descriptor.getInheritInfo();
      if (inheritInfo != null && !inheritInfo.isRoot()) {
        DeployBeanInfo<?> rootBeanInfo = deplyInfoMap.get(inheritInfo.getRoot().getType());
        IdGenerator rootIdGen = rootBeanInfo.getDescriptor().getIdGenerator();
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

  /**
   * Parse the named Raw Sql queries using BeanDescriptor.
   */
  private void readRawSqlQueries() {

    for (DeployBeanInfo<?> info : deplyInfoMap.values()) {

      DeployBeanDescriptor<?> deployDesc = info.getDescriptor();
      BeanDescriptor<?> desc = getBeanDescriptor(deployDesc.getBeanType());

      for (DRawSqlMeta rawSqlMeta : deployDesc.getRawSqlMeta()) {
        if (rawSqlMeta.getQuery() != null) {
          DeployNamedQuery nq = new DRawSqlSelectBuilder(namingConvention, desc, rawSqlMeta).parse();
          desc.addNamedQuery(nq);
        }
      }
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void readEntityRelationships() {

    // We only perform 'circular' checks etc after we have
    // all the DeployBeanDescriptors created and in the map.

    for (DeployBeanInfo<?> info : deplyInfoMap.values()) {
      checkMappedBy(info);
    }

    for (DeployBeanInfo<?> info : deplyInfoMap.values()) {
      secondaryPropsJoins(info);
    }

    // Set inheritance info
    for (DeployBeanInfo<?> info : deplyInfoMap.values()) {
        setInheritanceInfo(info);
    }
    
    for (DeployBeanInfo<?> info : deplyInfoMap.values()) {
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
				DeployBeanInfo<?> assoc = deplyInfoMap.get(oneProp.getTargetType());
				if (assoc != null){
					oneProp.getTableJoin().setInheritInfo(assoc.getDescriptor().getInheritInfo());
				}
			}
		}
		
		for (DeployBeanPropertyAssocMany<?> manyProp : info.getDescriptor().propertiesAssocMany()) {
			if (!manyProp.isTransient()) {
				DeployBeanInfo<?> assoc = deplyInfoMap.get(manyProp.getTargetType());
				if (assoc != null){
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
  private void checkMappedBy(DeployBeanInfo<?> info) {

    for (DeployBeanPropertyAssocOne<?> oneProp : info.getDescriptor().propertiesAssocOne()) {
      if (!oneProp.isTransient()) {
        if (oneProp.getMappedBy() != null) {
          checkMappedByOneToOne(oneProp);
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
    DeployBeanInfo<?> info = deplyInfoMap.get(targetType);
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

    Set<String> matchSet = new HashSet<String>();

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

    if (matchSet.size() == 0) {
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
      String shortTypeName = targetType.substring(targetType.lastIndexOf(".") + 1);

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
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void makeUnidirectional(DeployBeanInfo<?> info, DeployBeanPropertyAssocMany<?> oneToMany) {

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

    // create the 'shadow' unidirectional property
    // which is put on the target descriptor
    DeployBeanPropertyAssocOne<?> unidirectional = new DeployBeanPropertyAssocOne(targetDesc, owningType);
    unidirectional.setUndirectionalShadow();
    unidirectional.setNullable(false);
    unidirectional.setDbRead(true);
    unidirectional.setDbInsertable(true);
    unidirectional.setDbUpdateable(false);

    targetDesc.setUnidirectional(unidirectional);

    // specify table and table alias...
    BeanTable beanTable = getBeanTable(owningType);
    unidirectional.setBeanTable(beanTable);
    unidirectional.setName(beanTable.getBaseTable());

    info.setBeanJoinType(unidirectional, true);

    // define the TableJoin
    DeployTableJoin oneToManyJoin = oneToMany.getTableJoin();
    if (!oneToManyJoin.hasJoinColumns()) {
      throw new RuntimeException("No join columns");
    }

    // inverse of the oneToManyJoin
    DeployTableJoin unidirectionalJoin = unidirectional.getTableJoin();
    unidirectionalJoin.setColumns(oneToManyJoin.columns(), true);

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

    DeployBeanDescriptor<?> targetDesc = getTargetDescriptor(prop);

    if (targetDesc.isDraftableElement()) {
      // automatically turning on orphan removal and CascadeType.ALL
      prop.setModifyListenMode(BeanCollection.ModifyListenMode.REMOVALS);
      prop.getCascadeInfo().setSaveDelete(true, true);
    }

    if (prop.getMappedBy() == null) {
      if (!findMappedBy(prop)) {
        makeUnidirectional(info, prop);
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

    DeployBeanDescriptor<T> desc = new DeployBeanDescriptor<T>(beanClass);

    desc.setUpdateChangesOnly(updateChangesOnly);

    beanLifecycleAdapterFactory.addLifecycleMethods(desc);
    
    // set bean controller, finder and listener
    setBeanControllerFinderListener(desc);
    deplyInherit.process(desc);
    desc.checkInheritanceMapping();

    createProperties.createProperties(desc);

    DeployBeanInfo<T> info = new DeployBeanInfo<T>(deployUtil, desc);

    readAnnotations.readInitial(info, eagerFetchLobs);
    return info;
  }

  private <T> void readDeployAssociations(DeployBeanInfo<T> info) {

    DeployBeanDescriptor<T> desc = info.getDescriptor();

    readAnnotations.readAssociations(info, this);

    readXml(desc);

    if (!EntityType.ORM.equals(desc.getEntityType())) {
      // not using base table
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

    if (desc.propertiesId().size() == 0) {
      // bean doesn't have an Id property
      if (desc.isBaseTableType() && desc.getBeanFinder() == null) {
        // expecting an id property
        logger.warn(Message.msg("deploy.nouid", desc.getFullName()));
      }
      return;
    }

    if (IdType.SEQUENCE.equals(desc.getIdType()) && !dbIdentity.isSupportsSequence()) {
      // explicit sequence but not supported by the DatabasePlatform
      logger.info("Explicit sequence on " + desc.getFullName() + " but not supported by DB Platform - ignored");
      desc.setIdType(null);
    }
    if (IdType.IDENTITY.equals(desc.getIdType()) && !dbIdentity.isSupportsIdentity()) {
      // explicit identity but not supported by the DatabasePlatform
      logger.info("Explicit Identity on " + desc.getFullName() + " but not supported by DB Platform - ignored");
      desc.setIdType(null);
    }

    if (desc.getIdType() == null) {
      if (desc.isPrimaryKeyCompoundOrNonNumeric()) {
        // assuming that this is a user supplied key like ISO country code or ISO currency code or lookup table code
        logger.debug("Expecting user defined identity on " + desc.getFullName() + " - not using db sequence or autoincrement");
        desc.setIdType(IdType.EXTERNAL);
        return;
      }
      // use the default. IDENTITY or SEQUENCE.
      desc.setIdType(dbIdentity.getIdType());
      desc.setIdTypePlatformDefault();
    }

    if (IdType.GENERATOR.equals(desc.getIdType())) {
      String genName = desc.getIdGeneratorName();
      if (UuidIdGenerator.AUTO_UUID.equals(genName)) {
        desc.setIdGenerator(uuidIdGenerator);
        return;
      }
    }

    if (desc.getBaseTable() == null) {
      // no base table so not going to set Identity
      // of sequence information
      return;
    }

    if (IdType.IDENTITY.equals(desc.getIdType())) {
      // used when getGeneratedKeys is not supported (SQL Server 2000)
      String selectLastInsertedId = dbIdentity.getSelectLastInsertedId(desc.getBaseTable());
      desc.setSelectLastInsertedId(selectLastInsertedId);
      return;
    }

    String seqName = desc.getIdGeneratorName();
    if (seqName != null) {
      logger.debug("explicit sequence " + seqName + " on " + desc.getFullName());
    } else {
      String primaryKeyColumn = desc.getSinglePrimaryKeyColumn();
      // use namingConvention to define sequence name
      seqName = namingConvention.getSequenceName(desc.getBaseTable(), primaryKeyColumn);
    }

    // create the sequence based IdGenerator
    IdGenerator seqIdGen = createSequenceIdGenerator(seqName);
    desc.setIdGenerator(seqIdGen);
  }

  private IdGenerator createSequenceIdGenerator(String seqName) {
    return databasePlatform.createSequenceIdGenerator(backgroundExecutor, dataSource, seqName, dbSequenceBatchSize);
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

  private void readXml(DeployBeanDescriptor<?> deployDesc) {

    List<Dnode> eXml = xmlConfig.findEntityXml(deployDesc.getFullName());
    readXmlRawSql(deployDesc, eXml);

    Dnode entityXml = deployOrmXml.findEntityDeploymentXml(deployDesc.getFullName());

    if (entityXml != null) {
      readXmlNamedQueries(deployDesc, entityXml);
      readXmlSql(deployDesc, entityXml);
    }
  }

  /**
   * Read sql-select (FUTURE: additionally sql-insert, sql-update, sql-delete).
   * If found this entity bean is based on raw sql.
   */
  private void readXmlSql(DeployBeanDescriptor<?> deployDesc, Dnode entityXml) {

    List<Dnode> sqlSelectList = entityXml.findAll("sql-select", entityXml.getLevel() + 1);
    for (int i = 0; i < sqlSelectList.size(); i++) {
      Dnode sqlSelect = sqlSelectList.get(i);
      readSqlSelect(deployDesc, sqlSelect);
    }
  }

  private String findContent(Dnode node, String nodeName) {
    Dnode found = node.find(nodeName);
    if (found != null) {
      return found.getNodeContent();
    } else {
      return null;
    }
  }

  private void readSqlSelect(DeployBeanDescriptor<?> deployDesc, Dnode sqlSelect) {

    String name = sqlSelect.getStringAttr("name", "default");
    String extend = sqlSelect.getStringAttr("extend", null);
    String queryDebug = sqlSelect.getStringAttr("debug", null);
    boolean debug = (queryDebug != null && queryDebug.equalsIgnoreCase("true"));

    // the raw sql select
    String query = findContent(sqlSelect, "query");
    String where = findContent(sqlSelect, "where");
    String having = findContent(sqlSelect, "having");
    String columnMapping = findContent(sqlSelect, "columnMapping");

    DRawSqlMeta m = new DRawSqlMeta(name, extend, query, debug, where, having, columnMapping);

    deployDesc.add(m);

  }

  private void readXmlRawSql(DeployBeanDescriptor<?> deployDesc, List<Dnode> entityXml) {

    List<Dnode> rawSqlQueries = xmlConfig.find(entityXml, "raw-sql");
    for (int i = 0; i < rawSqlQueries.size(); i++) {
      Dnode rawSqlDnode = rawSqlQueries.get(i);
      String name = rawSqlDnode.getAttribute("name");
      if (isEmpty(name)) {
        throw new IllegalStateException("raw-sql for " + deployDesc.getFullName() + " missing name attribute");
      }
      Dnode queryNode = rawSqlDnode.find("query");
      if (queryNode == null) {
        throw new IllegalStateException("raw-sql for " + deployDesc.getFullName() + " missing query element");
      }
      String sql = queryNode.getNodeContent();
      if (isEmpty(sql)) {
        throw new IllegalStateException("raw-sql for " + deployDesc.getFullName() + " has empty sql in the query element?");
      }

      List<Dnode> columnMappings = rawSqlDnode.findAll("columnMapping", 1);

      RawSqlBuilder rawSqlBuilder = RawSqlBuilder.parse(sql);
      for (int j = 0; j < columnMappings.size(); j++) {
        Dnode cm = columnMappings.get(j);
        String column = cm.getAttribute("column");
        String property = cm.getAttribute("property");
        rawSqlBuilder.columnMapping(column, property);
      }
      RawSql rawSql = rawSqlBuilder.create();

      DeployNamedQuery namedQuery = new DeployNamedQuery(name, rawSql);
      deployDesc.add(namedQuery);
    }
  }

  private boolean isEmpty(String s) {
    return s == null || s.trim().length() == 0;
  }

  /**
   * Read named queries for this bean type.
   */
  private void readXmlNamedQueries(DeployBeanDescriptor<?> deployDesc, Dnode entityXml) {

    // look for named-query...
    List<Dnode> namedQueries = entityXml.findAll("named-query", 1);

    for (Dnode namedQueryXml : namedQueries) {

      String name = namedQueryXml.getAttribute("name");
      Dnode query = namedQueryXml.find("query");
      if (query == null) {
        logger.warn("orm.xml " + deployDesc.getFullName() + " named-query missing query element?");

      } else {
        String oql = query.getNodeContent();
        // TODO: QueryHints not read from xml yet
        if (name == null || oql == null) {
          logger.warn("orm.xml " + deployDesc.getFullName() + " named-query has no query content?");
        } else {
          // add the named query
          DeployNamedQuery q = new DeployNamedQuery(name, oql, null);
          deployDesc.add(q);
        }
      }
    }
  }

  private BeanPropertyInfoFactory createReflectionFactory() {

    return new EnhanceBeanPropertyInfoFactory();
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

    Class<?> beanType = desc.getBeanType();

    BeanPropertiesReader reflectProps = new BeanPropertiesReader(beanType);
    
    BeanPropertyInfo beanReflect = reflectFactory.create(beanType);
    desc.setProperties(reflectProps.getProperties());

    for (DeployBeanProperty prop : desc.propertiesAll()) {
      String propName = prop.getName();
      Integer pos = reflectProps.getPropertyIndex(propName);
      if (pos == null) {
        if (isPersistentField(prop)) {
          throw new IllegalStateException("Property " + propName + " not found in " + reflectProps + " for type " + beanType);
        }
        
      } else {
        final int propertyIndex = pos;
        prop.setPropertyIndex(propertyIndex);
        prop.setGetter(beanReflect.getGetter(propertyIndex));
        prop.setSetter(beanReflect.getSetter(propertyIndex));
      }
    }
  }

  /**
   * Return true if this is a persistent field (not transient or static).
   */
  private boolean isPersistentField(DeployBeanProperty prop) {

    Field field = prop.getField();
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
    for (int i = 0; i < props.size(); i++) {
      if (props.get(i).isVersionColumn()) {
        hasVersionProperty = true;
      }
    }

    return hasVersionProperty;
  }

  private boolean hasEntityBeanInterface(Class<?> beanClass) {

    Class<?>[] interfaces = beanClass.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      if (interfaces[i].equals(EntityBean.class)) {
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
      throw new IllegalStateException("Bean "+beanClass+" is not enhanced?");
    }

    // the bean already implements EntityBean
    checkInheritedClasses(beanClass);

    if (!beanClass.getName().startsWith("com.avaje.ebean.meta")) {
      enhancedClassCount++;
    }
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
      throw new IllegalStateException("Super type "+superclass+" is not enhanced?");
    }
    
    // recursively continue up the inheritance hierarchy
    checkInheritedClasses(superclass);
  }

  /**
   * Return true if this is a MappedSuperclass bean with no persistent properties.
   * If so it is ok for it not to be enhanced.
   */
  private boolean isMappedSuperWithNoProperties(Class<?> beanClass) {
    
    MappedSuperclass annotation = beanClass.getAnnotation(MappedSuperclass.class);
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

  /**
   * Comparator to sort the BeanDescriptors by name.
   */
  private static final class BeanDescComparator implements Comparator<BeanDescriptor<?>>, Serializable {

    private static final long serialVersionUID = 1L;

    public int compare(BeanDescriptor<?> o1, BeanDescriptor<?> o2) {

      return o1.getName().compareTo(o2.getName());
    }
  }
}
