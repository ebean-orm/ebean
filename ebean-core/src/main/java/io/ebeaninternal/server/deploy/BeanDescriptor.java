package io.ebeaninternal.server.deploy;

import io.ebean.*;
import io.ebean.annotation.DocStoreMode;
import io.ebean.bean.*;
import io.ebean.cache.QueryCacheEntry;
import io.ebean.DatabaseBuilder;
import io.ebean.config.EncryptKey;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarType;
import io.ebean.event.*;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeLogFilter;
import io.ebean.event.changelog.ChangeType;
import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.MetricVisitor;
import io.ebean.meta.QueryPlanInit;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.ExpressionPath;
import io.ebean.plugin.Lookups;
import io.ebean.plugin.Property;
import io.ebean.util.SplitName;
import io.ebeaninternal.api.*;
import io.ebeaninternal.api.TransactionEventTable.TableIUD;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.api.json.SpiJsonWriter;
import io.ebeaninternal.server.bind.DataBind;
import io.ebeaninternal.server.cache.CacheChangeSet;
import io.ebeaninternal.server.cache.CachedBeanData;
import io.ebeaninternal.server.cache.CachedManyIds;
import io.ebeaninternal.server.core.*;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.deploy.id.IdBinderSimple;
import io.ebeaninternal.server.deploy.id.ImportedId;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyLists;
import io.ebeaninternal.server.el.*;
import io.ebeaninternal.server.persist.DeleteMode;
import io.ebeaninternal.server.query.*;
import io.ebeaninternal.server.querydefn.DefaultOrmQuery;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;
import io.ebeaninternal.util.SortByClause;
import io.ebeaninternal.util.SortByClauseParser;

import jakarta.persistence.PersistenceException;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.ebeaninternal.server.persist.DmlUtil.isNullOrZero;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

/**
 * Describes Beans including their deployment information.
 */
public class BeanDescriptor<T> implements BeanType<T>, STreeType, SpiBeanType {

  private static final System.Logger log = CoreLog.internal;

  public enum EntityType {
    ORM, EMBEDDED, VIEW, SQL, DOC
  }

  private final ConcurrentHashMap<String, SpiUpdatePlan> updatePlanCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<CQueryPlanKey, CQueryPlan> queryPlanCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, ElPropertyValue> elCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, ElPropertyDeploy> elDeployCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, ElComparator<T>> comparatorCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, STreeProperty> dynamicProperty = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Map<String, String>> pathMaps = new ConcurrentHashMap<>();

  private final boolean multiValueSupported;
  private boolean batchEscalateOnCascadeInsert;
  private boolean batchEscalateOnCascadeDelete;
  private final BeanIudMetrics iudMetrics;
  private final EntityType entityType;
  /**
   * Set when Id property is marked with GeneratedValue annotation.
   */
  private final boolean idGeneratedValue;
  private final PlatformIdGenerator idGenerator;
  private final IdentityMode identityMode;
  private final IdType idType;
  /**
   * SQL used to return last inserted id. Used for Identity columns where
   * getGeneratedKeys is not supported.
   */
  private final String selectLastInsertedId;
  private final boolean autoTunable;
  private final ConcurrencyMode concurrencyMode;
  private final IndexDefinition[] indexDefinitions;
  private final String[] dependentTables;
  private final String baseTable;
  private final String baseTableAsOf;
  private final String baseTableVersionsBetween;
  private final boolean historySupport;
  private final TableJoin primaryKeyJoin;
  private final BeanProperty softDeleteProperty;
  private final boolean softDelete;
  private final PartitionMeta partitionMeta;
  private final TablespaceMeta tablespaceMeta;
  private final String storageEngine;
  private final String dbComment;
  private final BeanProperty unmappedJson;
  private final BeanProperty tenant;
  private final LinkedHashMap<String, BeanProperty> propMap;
  /**
   * Map of DB column to property path (for nativeSql mapping).
   */
  private final Map<String, String> columnPath = new HashMap<>();
  /**
   * Map of related table to assoc property (for nativeSql mapping).
   */
  private final Map<String, BeanPropertyAssoc<?>> tablePath = new HashMap<>();

  final Class<T> beanType;
  final Class<?> rootBeanType;
  private final BeanDescriptorMap owner;
  final String[] properties;

  private final BeanPostLoad beanPostLoad;
  private final BeanPostConstructListener beanPostConstructListener;
  private volatile BeanPersistController persistController;
  private volatile BeanPersistListener persistListener;
  private final BeanQueryAdapter queryAdapter;
  private final BeanFindController beanFinder;
  private final ChangeLogFilter changeLogFilter;
  private final boolean abstractType;
  private final BeanProperty idProperty;
  private final int idPropertyIndex;
  private final BeanProperty versionProperty;
  private final int versionPropertyIndex;
  private final BeanProperty whenModifiedProperty;
  private final BeanProperty whenCreatedProperty;
  /**
   * Properties that are initialised in the constructor need to be 'unloaded' to support partial object queries.
   */
  private final int[] unloadProperties;
  /**
   * Scalar mutable properties (need to dirty check on update).
   */
  private final BeanProperty[] propertiesMutable;
  private final BeanPropertyAssocOne<?> unidirectional;
  private final BeanProperty orderColumn;
  private final BeanProperty[] propertiesNonMany;
  private final BeanProperty[] propertiesAggregate;
  private final BeanPropertyAssocMany<?>[] propertiesMany;
  private final BeanPropertyAssocMany<?>[] propertiesManySave;
  private final BeanPropertyAssocMany<?>[] propertiesManyDelete;
  private final BeanPropertyAssocMany<?>[] propertiesManyToMany;
  /**
   * list of properties that are associated beans and not embedded (Derived).
   */
  private final BeanPropertyAssocOne<?>[] propertiesOne;
  private final BeanPropertyAssocOne<?>[] propertiesOneImported;
  private final BeanPropertyAssocOne<?>[] propertiesOneImportedSave;
  private final BeanPropertyAssocOne<?>[] propertiesOneImportedDelete;
  private final BeanPropertyAssocOne<?>[] propertiesOneExportedSave;
  private final BeanPropertyAssocOne<?>[] propertiesOneExportedDelete;
  private final BeanPropertyAssocOne<?>[] propertiesEmbedded;
  private final BeanProperty[] propertiesBaseScalar;
  private final BeanProperty[] propertiesTransient;
  /**
   * All non transient properties excluding the id properties.
   */
  private final BeanProperty[] propertiesNonTransient;
  final BeanProperty[] propertiesIndex;
  private final BeanProperty[] propertiesGenInsert;
  private final BeanProperty[] propertiesGenUpdate;
  private final List<BeanProperty[]> propertiesUnique = new ArrayList<>();
  private final boolean idOnlyReference;
  private BeanNaturalKey beanNaturalKey;


  private final String fullName;
  private boolean saveRecurseSkippable;
  private boolean deleteRecurseSkippable;
  private final EntityBean prototypeEntityBean;

  private final IdBinder idBinder;
  private String idBinderInLHSSql;
  private String idBinderIdSql;
  private String deleteByIdSql;
  private String deleteByIdInSql;
  private String whereIdInSql;
  private String softDeleteByIdSql;
  private String softDeleteByIdInSql;

  private final String name;
  private final String baseTableAlias;
  private final boolean cacheSharableBeans;
  private final BeanDescriptorCacheHelp<T> cacheHelp;
  private final BeanDescriptorJsonHelp<T> jsonHelp;
  private final String defaultSelectClause;
  private SpiEbeanServer ebeanServer;

  public BeanDescriptor(BeanDescriptorMap owner, DeployBeanDescriptor<T> deploy) {
    this.owner = owner;
    this.multiValueSupported = owner.isMultiValueSupported();
    this.entityType = deploy.getEntityType();
    this.properties = deploy.propertyNames();
    this.name = InternString.intern(deploy.getName());
    this.baseTableAlias = "t0";
    this.fullName = InternString.intern(deploy.getFullName());
    this.beanType = deploy.getBeanType();
    this.rootBeanType = PersistenceContextUtil.root(beanType);
    this.prototypeEntityBean = createPrototypeEntityBean(beanType);
    this.iudMetrics = new BeanIudMetrics(name);
    this.beanFinder = deploy.getBeanFinder();
    this.persistController = deploy.getPersistController();
    this.persistListener = deploy.getPersistListener();
    this.beanPostConstructListener = deploy.getPostConstructListener();
    this.beanPostLoad = deploy.getPostLoad();
    this.queryAdapter = deploy.getQueryAdapter();
    this.changeLogFilter = deploy.getChangeLogFilter();
    this.defaultSelectClause = deploy.getDefaultSelectClause();
    this.identityMode = deploy.buildIdentityMode();
    this.idType = identityMode.getIdType();
    this.idGeneratedValue = deploy.isIdGeneratedValue();
    this.idGenerator = deploy.getIdGenerator();
    this.selectLastInsertedId = deploy.getSelectLastInsertedId();
    this.concurrencyMode = deploy.getConcurrencyMode();
    this.indexDefinitions = deploy.getIndexDefinitions();
    this.historySupport = deploy.isHistorySupport();
    this.baseTable = InternString.intern(deploy.getBaseTable());
    this.baseTableAsOf = deploy.getBaseTableAsOf();
    this.primaryKeyJoin = deploy.getPrimaryKeyJoin();
    this.baseTableVersionsBetween = deploy.getBaseTableVersionsBetween();
    this.dependentTables = deploy.getDependentTables();
    this.dbComment = deploy.getDbComment();
    this.partitionMeta = deploy.getPartitionMeta();
    this.tablespaceMeta = deploy.getTablespaceMeta();
    this.storageEngine = deploy.getStorageEngine();
    this.autoTunable = entityType == EntityType.ORM || entityType == EntityType.VIEW;
    // helper object used to derive lists of properties
    DeployBeanPropertyLists listHelper = new DeployBeanPropertyLists(owner, this, deploy);
    this.softDeleteProperty = listHelper.getSoftDeleteProperty();
    // if formula is set, the property is virtual only (there is no column in db) the formula must evaluate to true,
    // if there is a join to a deleted bean. Example: '@Formula(select = "${ta}.user_id is null")'
    // this is required to support markAsDelete on beans that may have no FK constraint.
    this.softDelete = (softDeleteProperty != null && !softDeleteProperty.isFormula());
    this.idProperty = listHelper.getId();
    this.versionProperty = listHelper.getVersionProperty();
    this.unmappedJson = listHelper.getUnmappedJson();
    this.tenant = listHelper.getTenant();
    this.propMap = listHelper.getPropertyMap();
    this.propertiesTransient = listHelper.getTransients();
    this.propertiesNonTransient = listHelper.getNonTransients();
    this.propertiesBaseScalar = listHelper.getBaseScalar();
    this.propertiesEmbedded = listHelper.getEmbedded();
    this.propertiesMutable = listHelper.getMutable();
    this.unidirectional = listHelper.getUnidirectional();
    this.orderColumn = listHelper.getOrderColumn();
    this.propertiesOne = listHelper.getOnes();
    this.propertiesOneExportedSave = listHelper.getOneExportedSave();
    this.propertiesOneExportedDelete = listHelper.getOneExportedDelete();
    this.propertiesOneImported = listHelper.getOneImported();
    this.propertiesOneImportedSave = listHelper.getOneImportedSave();
    this.propertiesOneImportedDelete = listHelper.getOneImportedDelete();
    this.propertiesMany = listHelper.getMany();
    this.propertiesNonMany = listHelper.getNonMany();
    this.propertiesAggregate = listHelper.getAggregates();
    this.propertiesManySave = listHelper.getManySave();
    this.propertiesManyDelete = listHelper.getManyDelete();
    this.propertiesManyToMany = listHelper.getManyToMany();
    this.propertiesGenInsert = listHelper.getGeneratedInsert();
    this.propertiesGenUpdate = listHelper.getGeneratedUpdate();
    this.idOnlyReference = isIdOnlyReference(propertiesBaseScalar);
    boolean noRelationships = propertiesOne.length + propertiesMany.length == 0;
    this.cacheSharableBeans = noRelationships && deploy.getCacheOptions().isReadOnly();
    this.cacheHelp = new BeanDescriptorCacheHelp<>(this, owner.cacheManager(), deploy.getCacheOptions(), cacheSharableBeans, propertiesOneImported);
    this.jsonHelp = initJsonHelp();
    // Check if there are no cascade save associated beans ( subject to change
    // in initialiseOther()). Note that if we are in an inheritance hierarchy
    // then we also need to check every BeanDescriptors in the InheritInfo as
    // well. We do that later in initialiseOther().
    saveRecurseSkippable = (0 == (propertiesOneExportedSave.length + propertiesOneImportedSave.length + propertiesManySave.length));

    // Check if there are no cascade delete associated beans (also subject to
    // change in initialiseOther()).
    deleteRecurseSkippable = (0 == (propertiesOneExportedDelete.length + propertiesOneImportedDelete.length + propertiesManyDelete.length));

    // object used to handle Id values
    this.idBinder = owner.createIdBinder(idProperty);
    this.whenModifiedProperty = findWhenModifiedProperty();
    this.whenCreatedProperty = findWhenCreatedProperty();
    // derive the index position of the Id and Version properties
    this.abstractType = Modifier.isAbstract(beanType.getModifiers());
    if (abstractType) {
      this.idPropertyIndex = -1;
      this.versionPropertyIndex = -1;
      this.unloadProperties = new int[0];
      this.propertiesIndex = new BeanProperty[0];
    } else {
      EntityBeanIntercept ebi = prototypeEntityBean._ebean_getIntercept();
      this.idPropertyIndex = (idProperty == null) ? -1 : ebi.findProperty(idProperty.name());
      this.versionPropertyIndex = (versionProperty == null) ? -1 : ebi.findProperty(versionProperty.name());
      this.unloadProperties = derivePropertiesToUnload(prototypeEntityBean);
      this.propertiesIndex = new BeanProperty[ebi.propertyLength()];
      for (int i = 0; i < propertiesIndex.length; i++) {
        propertiesIndex[i] = propMap.get(ebi.property(i));
      }
    }
  }

  public String idSelect() {
    if (idBinder == null) throw new UnsupportedOperationException();
    return idBinder.idSelect();
  }

  public boolean isJacksonCorePresent() {
    return owner.isJacksonCorePresent();
  }

  private BeanDescriptorJsonHelp<T> initJsonHelp() {
    return isJacksonCorePresent() ? new BeanDescriptorJsonHelp<>(this) : null;
  }

  /**
   * Return true if the bean should be treated as a reference bean when it only has its id populated.
   * To be true it has other scalar properties that are not generated on insert.
   */
  private boolean isIdOnlyReference(BeanProperty[] baseScalar) {
    for (BeanProperty beanProperty : baseScalar) {
      if (!beanProperty.isGeneratedOnInsert()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Derive an array of property positions for properties that are initialised in the constructor.
   * These properties need to be unloaded when populating beans for queries.
   */
  private int[] derivePropertiesToUnload(EntityBean prototypeEntityBean) {
    boolean[] loaded = prototypeEntityBean._ebean_getIntercept().loaded();
    int[] props = new int[loaded.length];
    int pos = 0;
    // collect the positions of the properties initialised in the default constructor.
    for (int i = 0; i < loaded.length; i++) {
      if (loaded[i]) {
        props[pos++] = i;
      }
    }
    if (pos == 0) {
      // nothing set in the constructor
      return new int[0];
    }
    // populate a smaller/minimal array
    int[] unload = new int[pos];
    System.arraycopy(props, 0, unload, 0, pos);
    return unload;
  }

  /**
   * Create an entity bean that is used as a prototype/factory to create new instances.
   */
  EntityBean createPrototypeEntityBean(Class<T> beanType) {
    if (Modifier.isAbstract(beanType.getModifiers())) {
      return null;
    }
    try {
      return Lookups.newDefaultInstance(beanType);
    } catch (Throwable e) {
      throw new IllegalStateException("Error trying to create the prototypeEntityBean for " + beanType, e);
    }
  }

  /**
   * Return the DatabaseConfig.
   */
  public DatabaseBuilder.Settings config() {
    return owner.config();
  }

  /**
   * Set the server. Primarily so that the Many's can lazy load.
   */
  public void setEbeanServer(SpiEbeanServer ebeanServer) {
    this.ebeanServer = ebeanServer;
    for (BeanPropertyAssocMany<?> assocMany : propertiesMany) {
      // used for creating lazy loading lists etc
      assocMany.setEbeanServer(ebeanServer);
    }
  }

  /**
   * Return the EbeanServer instance that owns this BeanDescriptor.
   */
  public SpiEbeanServer ebeanServer() {
    return ebeanServer;
  }

  /**
   * Return true if this is an abstract type.
   */
  boolean isAbstractType() {
    return abstractType;
  }

  /**
   * Return true if this is a "Doc Store only" entity bean.
   */
  @Override
  public boolean isDocStoreOnly() {
    return EntityType.DOC == entityType;
  }

  /**
   * Return the type of this domain object.
   */
  public EntityType entityType() {
    return entityType;
  }

  private String[] properties() {
    return properties;
  }

  public BeanProperty propertyByIndex(int pos) {
    return propertiesIndex[pos];
  }

  /**
   * Initialise the Id properties first.
   * <p>
   * These properties need to be initialised prior to the association properties
   * as they are used to get the imported and exported properties.
   */
  void initialiseId(BeanDescriptorInitContext initContext) {
    if (historySupport) {
      // add mapping (used to swap out baseTable for asOf queries)
      initContext.addHistory(baseTable, baseTableAsOf);
    }
    // initialise just the Id property only
    if (idProperty != null) {
      idProperty.initialise(initContext);
    }
  }

  /**
   * Initialise the exported and imported parts for associated properties.
   */
  public void initialiseOther(BeanDescriptorInitContext initContext) {
    if (historySupport) {
      // history support on this bean so check all associated intersection tables
      // and if they are not excluded register the associated 'with history' table
      for (BeanPropertyAssocMany<?> manyToMany : propertiesManyToMany) {
        // register associated history table for M2M intersection
        if (!manyToMany.isExcludedFromHistory()) {
          TableJoin intersectionTableJoin = manyToMany.intersectionTableJoin();
          initContext.addHistoryIntersection(intersectionTableJoin.getTable());
        }
      }
    }

    // initialise all the non-id properties
    for (BeanProperty prop : propertiesAll()) {
      if (!prop.isId()) {
        prop.initialise(initContext);
      }
      prop.registerColumn(this, null);
    }
    if (unidirectional != null) {
      unidirectional.initialise(initContext);
    }
    idBinder.initialise();
    idBinderInLHSSql = idBinder.bindInSql(baseTableAlias);
    idBinderIdSql = idBinder.bindEqSql(baseTableAlias);
    String idBinderInLHSSqlNoAlias = idBinder.bindInSql(null);
    String idEqualsSql = idBinder.bindEqSql(null);
    deleteByIdSql = "delete from " + baseTable + " where " + idEqualsSql;
    whereIdInSql = " where " + idBinderInLHSSqlNoAlias + " ";
    deleteByIdInSql = "delete from " + baseTable + whereIdInSql;
    if (softDelete) {
      softDeleteByIdSql = "update " + baseTable + " set " + softDeleteDbSet() + " where " + idEqualsSql;
      softDeleteByIdInSql = "update " + baseTable + " set " + softDeleteDbSet() + " where " + idBinderInLHSSqlNoAlias + " ";
    } else {
      softDeleteByIdSql = null;
      softDeleteByIdInSql = null;
    }
    initNaturalKey();
  }

  private void initNaturalKey() {
    final String[] naturalKey = cacheHelp.getNaturalKey();
    if (naturalKey != null && naturalKey.length != 0) {
      BeanProperty[] props = new BeanProperty[naturalKey.length];
      for (int i = 0; i < naturalKey.length; i++) {
        props[i] = beanProperty(naturalKey[i]);
      }
      this.beanNaturalKey = new BeanNaturalKey(naturalKey, props);
    }
  }

  private boolean hasCircularImportedId() {
    for (BeanPropertyAssocOne<?> assocOne : propertiesOneImportedSave) {
      if (assocOne.hasCircularImportedId(this)) {
        return true;
      }
    }
    return false;
  }

  boolean hasCircularImportedIdTo(BeanDescriptor<?> sourceDesc) {
    for (BeanPropertyAssocOne<?> assocOne : propertiesOneImportedSave) {
      if (assocOne.targetDescriptor() == sourceDesc) {
        return true;
      }
    }
    return false;
  }

  void registerColumn(String dbColumn, String path) {
    String key = dbColumn.toLowerCase();
    // check for clash with imported OneToOne PK
    if (!columnPath.containsKey(key)) {
      columnPath.put(key, path);
    }
  }

  void registerTable(String baseTable, BeanPropertyAssoc<?> assocProperty) {
    if (baseTable != null) {
      tablePath.put(baseTable.toLowerCase(), assocProperty);
    }
  }

  /**
   * Perform last initialisation for the descriptor.
   */
  void initLast() {
    for (BeanProperty prop : propertiesNonTransient) {
      if (prop.isUnique()) {
        propertiesUnique.add(new BeanProperty[]{prop});
      }
    }
    // convert unique columns to properties
    if (indexDefinitions != null) {
      for (IndexDefinition indexDef : indexDefinitions) {
        if (indexDef.isUnique()) {
          addUniqueColumns(indexDef);
        }
      }
    }
  }

  private void addUniqueColumns(IndexDefinition indexDef) {
    String[] cols = indexDef.getColumns();
    BeanProperty[] props = new BeanProperty[cols.length];
    for (int i = 0; i < cols.length; i++) {
      String propName = findBeanPath("", "", cols[i]);
      if (propName == null) {
        return;
      }
      props[i] = findProperty(propName);
    }
    if (props.length == 1) {
      for (BeanProperty[] inserted : propertiesUnique) {
        if (inserted.length == 1 && inserted[0].equals(props[0])) {
          return; // do not insert duplicates
        }
      }
    }
    propertiesUnique.add(props);
  }

  /**
   * Initialise the document mapping.
   */
  @SuppressWarnings("unchecked")
  void initialiseDocMapping() {
    batchEscalateOnCascadeInsert = supportBatchEscalateOnInsert();
    batchEscalateOnCascadeDelete = supportBatchEscalateOnDelete();
    for (BeanPropertyAssocMany<?> many : propertiesMany) {
      many.initialisePostTarget();
    }
    for (BeanPropertyAssocOne<?> one : propertiesOne) {
      one.initialisePostTarget();
    }
    cacheHelp.deriveNotifyFlags();
  }

  private boolean supportBatchEscalateOnDelete() {
    if (softDelete) {
      return false;
    }
    for (BeanPropertyAssocMany<?> assocMany : propertiesManyDelete) {
      if (assocMany.isCascadeDeleteEscalate()) {
        return true;
      }
    }
    return false;
  }

  private boolean supportBatchEscalateOnInsert() {
    return idType == IdType.IDENTITY || !hasCircularImportedId();
  }

  /**
   * Return false if JDBC batch can't be implicitly escalated to.
   * This happens when we have circular import id situation (need to defer setting identity value).
   */
  public boolean isBatchEscalateOnCascade(PersistRequest.Type type) {
    return type == PersistRequest.Type.INSERT ? batchEscalateOnCascadeInsert : batchEscalateOnCascadeDelete;
  }

  public void metricPersistBatch(PersistRequest.Type type, long startNanos, int size) {
    iudMetrics.addBatch(type, startNanos, size);
  }

  public void metricPersistNoBatch(PersistRequest.Type type, long startNanos) {
    iudMetrics.addNoBatch(type, startNanos);
  }

  public void merge(EntityBean bean, EntityBean existing) {
    EntityBeanIntercept fromEbi = bean._ebean_getIntercept();
    EntityBeanIntercept toEbi = existing._ebean_getIntercept();
    int propertyLength = toEbi.propertyLength();
    String[] names = properties();
    for (int i = 0; i < propertyLength; i++) {
      if (fromEbi.isLoadedProperty(i)) {
        BeanProperty property = beanProperty(names[i]);
        if (!toEbi.isLoadedProperty(i)) {
          Object val = property.getValue(bean);
          property.setValue(existing, val);
        } else if (property.isMany()) {
          property.merge(bean, existing);
        }
      }
    }
  }

  /**
   * Bind all the property values to the SqlUpdate.
   */
  public void bindElementValue(SqlUpdate insert, Object value) {
    EntityBean bean = (EntityBean) value;
    for (BeanProperty property : propertiesBaseScalar) {
      insert.setParameter(property.getValue(bean));
    }
  }

  public boolean isChangeLog() {
    return changeLogFilter != null;
  }

  /**
   * Return true if this request should be included in the change log.
   */
  public BeanChange changeLogBean(PersistRequestBean<T> request) {
    switch (request.type()) {
      case INSERT:
        return changeLogFilter.includeInsert(request) ? insertBeanChange(request) : null;
      case UPDATE:
      case DELETE_SOFT:
        return changeLogFilter.includeUpdate(request) ? updateBeanChange(request) : null;
      case DELETE:
        return changeLogFilter.includeDelete(request) ? deleteBeanChange(request) : null;
      default:
        throw new IllegalStateException("Unhandled request type " + request.type());
    }
  }

  private BeanChange beanChange(ChangeType type, Object id, String data, String oldData) {
    Object tenantId = ebeanServer.currentTenantId();
    return new BeanChange(name, tenantId, id, type, data, oldData);
  }

  /**
   * Return the bean change for a delete.
   */
  private BeanChange deleteBeanChange(PersistRequestBean<T> request) {
    return beanChange(ChangeType.DELETE, request.beanId(), null, null);
  }

  /**
   * Return the bean change for an update generating 'new values' and 'old values' in JSON form.
   */
  private BeanChange updateBeanChange(PersistRequestBean<T> request) {
    try {
      BeanChangeJson changeJson = new BeanChangeJson(this, request.isStatelessUpdate());
      request.intercept().addDirtyPropertyValues(changeJson);
      changeJson.flush();
      return beanChange(ChangeType.UPDATE, request.beanId(), changeJson.newJson(), changeJson.oldJson());
    } catch (RuntimeException e) {
      log.log(ERROR, "Failed to write ChangeLog entry for update", e);
      return null;
    }
  }

  /**
   * Return the bean change for an insert.
   */
  private BeanChange insertBeanChange(PersistRequestBean<T> request) {
    try {
      StringWriter writer = new StringWriter(200);
      SpiJsonWriter jsonWriter = createJsonWriter(writer);
      jsonWriteForInsert(jsonWriter, request.entityBean());
      jsonWriter.flush();
      return beanChange(ChangeType.INSERT, request.beanId(), writer.toString(), null);
    } catch (IOException e) {
      log.log(ERROR, "Failed to write ChangeLog entry for insert", e);
      return null;
    }
  }

  SpiJsonWriter createJsonWriter(StringWriter writer) {
    return ebeanServer.jsonExtended().createJsonWriter(writer);
  }

  SpiJsonReader createJsonReader(String json) {
    return ebeanServer.jsonExtended().createJsonRead(this, json);
  }

  /**
   * Populate the diff for inserts with flattened non-null property values.
   */
  void jsonWriteForInsert(SpiJsonWriter jsonWriter, EntityBean newBean) throws IOException {
    jsonWriter.writeStartObject();
    for (BeanProperty prop : propertiesBaseScalar) {
      prop.jsonWriteForInsert(jsonWriter, newBean);
    }
    for (BeanPropertyAssocOne<?> prop : propertiesOne) {
      prop.jsonWriteForInsert(jsonWriter, newBean);
    }
    for (BeanPropertyAssocOne<?> prop : propertiesEmbedded) {
      prop.jsonWriteForInsert(jsonWriter, newBean);
    }
    jsonWriter.writeEndObject();
  }

  public SqlUpdate deleteById(Object id, List<Object> idList, DeleteMode mode) {
    if (id != null) {
      return deleteById(id, mode);
    } else {
      return deleteByIdList(idList, mode);
    }
  }

  /**
   * Return the "where id in" sql (for use with UpdateQuery).
   */
  public String whereIdInSql() {
    return whereIdInSql;
  }

  /**
   * Return the "delete by id" sql.
   */
  public String deleteByIdInSql() {
    return deleteByIdInSql;
  }

  /**
   * Return SQL that can be used to delete a list of Id's without any optimistic
   * concurrency checking.
   */
  private SqlUpdate deleteByIdList(List<Object> idList, DeleteMode mode) {
    String baseSql = mode.isHard() ? deleteByIdInSql : softDeleteByIdInSql;
    StringBuilder sb = new StringBuilder(baseSql);
    String inClause = idBinder.idInValueExprDelete(idList.size());
    sb.append(inClause);
    DefaultSqlUpdate delete = new DefaultSqlUpdate(sb.toString());
    idBinder.addBindValues(delete, idList);
    return delete;
  }

  /**
   * Return SQL that can be used to delete by Id without any optimistic
   * concurrency checking.
   */
  private SqlUpdate deleteById(Object id, DeleteMode mode) {
    String baseSql = mode.isHard() ? deleteByIdSql : softDeleteByIdSql;
    DefaultSqlUpdate sqlDelete = new DefaultSqlUpdate(baseSql);
    Object[] bindValues = idBinder.bindValues(id);
    for (Object bindValue : bindValues) {
      sqlDelete.setParameter(bindValue);
    }
    return sqlDelete;
  }

  /**
   * Add objects to ElPropertyDeploy etc. These are used so that expressions on
   * foreign keys don't require an extra join.
   */
  public void add(BeanFkeyProperty fkey) {
    elDeployCache.put(fkey.name(), fkey);
  }

  void initialiseFkeys() {
    for (BeanPropertyAssocOne<?> oneImported : propertiesOneImported) {
      if (!oneImported.isFormula()) {
        oneImported.addFkey();
      }
    }
  }

  /**
   * Return the cache options.
   */
  public CacheOptions cacheOptions() {
    return cacheHelp.getCacheOptions();
  }

  /**
   * Return the Encrypt key given the BeanProperty.
   */
  public EncryptKey encryptKey(BeanProperty p) {
    return owner.encryptKey(baseTable, p.dbColumn());
  }

  /**
   * Return the Encrypt key given the table and column name.
   */
  public EncryptKey encryptKey(String tableName, String columnName) {
    return owner.encryptKey(tableName, columnName);
  }

  /**
   * Return the Scalar type for the given JDBC type.
   */
  public ScalarType<?> scalarType(int jdbcType) {
    return owner.scalarType(jdbcType);
  }

  public ScalarType<?> scalarType(String cast) {
    return owner.scalarType(cast);
  }

  /**
   * Return true if this bean type has a default select clause that is not
   * simply select all properties.
   */
  public boolean hasDefaultSelectClause() {
    return defaultSelectClause != null;
  }

  /**
   * Return the default select clause.
   */
  public String defaultSelectClause() {
    return defaultSelectClause;
  }

  /**
   * Prepare the query for multi-tenancy check for document store only use.
   */
  public void prepareQuery(SpiQuery<T> query) {
    if (tenant != null && !query.isNativeSql()) {
      Object tenantId = ebeanServer.currentTenantId();
      if (tenantId != null) {
        tenant.addTenant(query, tenantId);
      }
    }
  }

  /**
   * Return the natural key.
   */
  public BeanNaturalKey naturalKey() {
    return beanNaturalKey;
  }

  /**
   * Return true if there is currently bean caching for this type of bean.
   */
  @Override
  public boolean isBeanCaching() {
    return cacheHelp.isBeanCaching();
  }

  /**
   * Return true if there is a natural key defined for this bean type.
   */
  public boolean isNaturalKeyCaching() {
    return cacheHelp.isNaturalKeyCaching();
  }

  /**
   * Return true if there is query caching for this type of bean.
   */
  @Override
  public boolean isQueryCaching() {
    return cacheHelp.isQueryCaching();
  }

  public boolean isManyPropCaching() {
    return isBeanCaching();
  }

  /**
   * Return true if the persist request needs to notify the cache.
   */
  public boolean isCacheNotify(PersistRequest.Type type) {
    return cacheHelp.isCacheNotify(type);
  }

  @Override
  public void clearBeanCache() {
    cacheHelp.beanCacheClear();
  }

  /**
   * Clear the query cache.
   */
  @Override
  public void clearQueryCache() {
    cacheHelp.queryCacheClear();
  }

  /**
   * Get a query result from the query cache.
   */
  public Object queryCacheGet(Object id) {
    return cacheHelp.queryCacheGet(id);
  }

  /**
   * Put a query result into the query cache.
   */
  public void queryCachePut(Object id, QueryCacheEntry entry) {
    cacheHelp.queryCachePut(id, entry);
  }

  /**
   * Try to load the beanCollection from cache return true if successful.
   */
  public boolean cacheManyPropLoad(BeanPropertyAssocMany<?> many, BeanCollection<?> bc, String parentKey, Boolean readOnly) {
    return cacheHelp.manyPropLoad(many, bc, parentKey, readOnly);
  }

  /**
   * Put the beanCollection into the cache.
   */
  public void cacheManyPropPut(BeanPropertyAssocMany<?> many, BeanCollection<?> bc, String parentKey) {
    cacheHelp.manyPropPut(many, bc, parentKey);
  }

  /**
   * Update the bean collection entry in the cache.
   */
  public void cacheManyPropPut(String name, String parentKey, CachedManyIds entry) {
    cacheHelp.cachePutManyIds(name, parentKey, entry);
  }

  public void cacheManyPropRemove(String propertyName, String parentKey) {
    cacheHelp.manyPropRemove(propertyName, parentKey);
  }

  public void cacheManyPropClear(String propertyName) {
    cacheHelp.manyPropClear(propertyName);
  }

  /**
   * Extract the raw cache data from the embedded bean.
   */
  public CachedBeanData cacheEmbeddedBeanExtract(EntityBean bean) {
    return cacheHelp.beanExtractData(this, bean);
  }

  /**
   * Load the embedded bean (taking into account inheritance).
   */
  public EntityBean cacheEmbeddedBeanLoad(CachedBeanData data, PersistenceContext context) {
    return cacheHelp.embeddedBeanLoad(data, context);
  }

  /**
   * Load the embedded bean as the root type.
   */
  EntityBean cacheEmbeddedBeanLoadDirect(CachedBeanData data, PersistenceContext context) {
    return cacheHelp.embeddedBeanLoadDirect(data, context);
  }

  /**
   * Load the entity bean as the correct bean type.
   */
  EntityBean cacheBeanLoadDirect(Object id, Boolean readOnly, CachedBeanData data, PersistenceContext context) {
    return cacheHelp.loadBeanDirect(id, readOnly, data, context);
  }

  /**
   * Put the bean into the cache.
   */
  public void cacheBeanPut(T bean) {
    cacheBeanPut((EntityBean) bean);
  }

  /**
   * Put a bean into the bean cache (taking into account inheritance).
   */
  public void cacheBeanPut(EntityBean bean) {
    cacheHelp.beanCachePut(bean);
  }

  @SuppressWarnings("unchecked")
  public void cacheBeanPutAll(Collection<?> beans) {
    if (!beans.isEmpty()) {
      cacheHelp.beanPutAll((Collection<EntityBean>) beans);
    }
  }

  void cacheBeanPutAllDirect(Collection<EntityBean> beans) {
    cacheHelp.beanCachePutAllDirect(beans);
  }

  /**
   * Put a bean into the cache as the correct type.
   */
  void cacheBeanPutDirect(EntityBean bean) {
    cacheHelp.beanCachePutDirect(bean);
  }

  /**
   * Return a bean from the bean cache (or null).
   */
  public T cacheBeanGet(Object id, Boolean readOnly, PersistenceContext context) {
    return cacheHelp.beanCacheGet(cacheKey(id), readOnly, context);
  }

  /**
   * Remove a collection of beans from the cache given the ids.
   */
  public void cacheApplyInvalidate(Collection<Object> ids) {
    List<String> keys = new ArrayList<>(ids.size());
    for (Object id : ids) {
      keys.add(cacheKey(id));
    }
    cacheHelp.beanCacheApplyInvalidate(keys);
  }

  /**
   * Hit the bean cache trying to load a list/batch of entities.
   * Return the set of entities that were successfully loaded from L2 cache.
   */
  public Set<EntityBeanIntercept> cacheBeanLoadAll(Set<EntityBeanIntercept> batch, PersistenceContext persistenceContext, int lazyLoadProperty, String propertyName) {
    return cacheHelp.beanCacheLoadAll(batch, persistenceContext, lazyLoadProperty, propertyName);
  }

  /**
   * Returns true if it managed to populate/load the bean from the cache.
   */
  public boolean cacheBeanLoad(EntityBean bean, EntityBeanIntercept ebi, Object id, PersistenceContext context) {
    return cacheHelp.beanCacheLoad(bean, ebi, cacheKey(id), context);
  }

  public BeanCacheResult<T> cacheIdLookup(PersistenceContext context, Collection<?> ids) {
    return cacheHelp.cacheIdLookup(context, ids);
  }

  /**
   * Use natural key lookup to hit the bean cache.
   */
  public BeanCacheResult<T> naturalKeyLookup(PersistenceContext context, Set<Object> keys) {
    return cacheHelp.naturalKeyLookup(context, keys);
  }

  public void cacheNaturalKeyPut(String key, String newKey) {
    cacheHelp.cacheNaturalKeyPut(key, newKey);
  }

  /**
   * Check if bulk update or delete query has a cache impact.
   */
  public void cacheUpdateQuery(boolean update, SpiTransaction transaction) {
    cacheHelp.cacheUpdateQuery(update, transaction);
  }

  /**
   * Invalidate parts of cache due to SqlUpdate or external modification etc.
   */
  void cachePersistTableIUD(TableIUD tableIUD, CacheChangeSet changeSet) {
    cacheHelp.persistTableIUD(tableIUD, changeSet);
  }

  /**
   * Remove a bean from the cache given its Id.
   */
  public void cachePersistDeleteByIds(Collection<Object> ids, CacheChangeSet changeSet) {
    cacheHelp.persistDeleteIds(ids, changeSet);
  }

  /**
   * Remove a bean from the cache given its Id.
   */
  public void cachePersistDelete(Object id, PersistRequestBean<T> deleteRequest, CacheChangeSet changeSet) {
    cacheHelp.persistDelete(id, deleteRequest, changeSet);
  }

  /**
   * Add the insert changes to the changeSet.
   */
  public void cachePersistInsert(PersistRequestBean<T> insertRequest, CacheChangeSet changeSet) {
    cacheHelp.persistInsert(insertRequest, changeSet);
  }

  /**
   * Add the update to the changeSet.
   */
  public void cachePersistUpdate(Object id, PersistRequestBean<T> updateRequest, CacheChangeSet changeSet) {
    cacheHelp.persistUpdate(id, updateRequest, changeSet);
  }

  /**
   * Apply the update to the cache.
   */
  public void cacheApplyBeanUpdate(String key, Map<String, Object> changes, boolean updateNaturalKey, long version) {
    cacheHelp.cacheBeanUpdate(key, changes, updateNaturalKey, version);
  }

  /**
   * Return the base table alias. This is always the first letter of the bean name.
   */
  public String baseTableAlias() {
    return baseTableAlias;
  }

  public void preAllocateIds(int batchSize) {
    if (idGenerator != null) {
      idGenerator.preAllocateIds(batchSize);
    }
  }

  public Object nextId(Transaction t) {
    if (idGenerator != null) {
      return idGenerator.nextId(t);
    } else {
      return null;
    }
  }

  public DeployPropertyParser parser() {
    return new DeployPropertyParser(this);
  }

  /**
   * Convert the logical orm update statement into sql by converting the bean
   * properties and bean name to database columns and table.
   */
  public String convertOrmUpdateToSql(String ormUpdateStatement) {
    return new DeployUpdateParser(this).parse(ormUpdateStatement);
  }

  void queryPlanInit(QueryPlanInit request, List<MetaQueryPlan> list) {
    for (CQueryPlan queryPlan : queryPlanCache.values()) {
      if (request.includeHash(queryPlan.hash())) {
        queryPlan.queryPlanInit(request.thresholdMicros(queryPlan.hash()));
        list.add(queryPlan.createMeta(null, null));
      }
    }
  }

  /**
   * Visit all the ORM query plan metrics (includes UpdateQuery with updates and deletes).
   */
  public void visitMetrics(MetricVisitor visitor) {
    iudMetrics.visit(visitor);
    for (CQueryPlan queryPlan : queryPlanCache.values()) {
      if (!queryPlan.isEmptyStats()) {
        visitor.visitQuery(queryPlan.visit(visitor));
      }
    }
  }

  /**
   * Reset the statistics on all the query plans.
   */
  public void clearQueryStatistics() {
    for (CQueryPlan queryPlan : queryPlanCache.values()) {
      queryPlan.resetStatistics();
    }
  }

  /**
   * Trim query plans not used since the passed in epoch time.
   */
  void trimQueryPlans(long unusedSince) {
    queryPlanCache.values().removeIf(queryPlan -> queryPlan.lastQueryTime() < unusedSince);
  }

  /**
   * Execute the postLoad if a BeanPostLoad exists for this bean.
   */
  @Override
  public void postLoad(Object bean) {
    if (beanPostLoad != null) {
      beanPostLoad.postLoad(bean);
    }
  }

  public CQueryPlan queryPlan(CQueryPlanKey key) {
    return queryPlanCache.get(key);
  }

  public void queryPlan(CQueryPlanKey key, CQueryPlan plan) {
    queryPlanCache.put(key, plan);
  }

  /**
   * Get a UpdatePlan for a given hash.
   */
  public SpiUpdatePlan updatePlan(String key) {
    return updatePlanCache.get(key);
  }

  /**
   * Add a UpdatePlan to the cache with a given hash.
   */
  public void updatePlan(String key, SpiUpdatePlan plan) {
    updatePlanCache.put(key, plan);
  }

  /**
   * Return a Sql update statement to set the importedId value (deferred execution).
   */
  public String updateImportedIdSql(ImportedId prop) {
    return "update " + baseTable + " set " + prop.importedIdClause() + " where " + idBinder.bindEqSql(null);
  }

  /**
   * Return true if save does not recurse to other beans. That is return true if
   * there are no assoc one or assoc many beans that cascade save.
   */
  boolean isSaveRecurseSkippable() {
    return saveRecurseSkippable;
  }

  /**
   * Return true if delete does not recurse to other beans. That is return true
   * if there are no assoc one or assoc many beans that cascade delete.
   */
  boolean isDeleteRecurseSkippable() {
    return deleteRecurseSkippable;
  }

  /**
   * Return true if delete can use a single SQL statement.
   * <p>
   * This implies cascade delete does not continue depth wise and that this is no
   * associated L2 bean caching.
   */
  public boolean isDeleteByStatement() {
    return persistListener == null
      && persistController == null
      && deleteRecurseSkippable && !isBeanCaching();
  }

  public boolean isDeleteByBulk() {
    return persistListener == null
      && persistController == null
      && propertiesManyToMany.length == 0;
  }

  /**
   * Return the 'when modified' property if there is one defined.
   */
  @Override
  public BeanProperty whenModifiedProperty() {
    return whenModifiedProperty;
  }

  /**
   * Return the 'when created' property if there is one defined.
   */
  @Override
  public BeanProperty whenCreatedProperty() {
    return whenCreatedProperty;
  }

  /**
   * Find a property annotated with @WhenCreated or @CreatedTimestamp.
   */
  private BeanProperty findWhenCreatedProperty() {
    for (BeanProperty baseScalar : propertiesBaseScalar) {
      if (baseScalar.isGeneratedWhenCreated()) {
        return baseScalar;
      }
    }
    return null;
  }

  /**
   * Find a property annotated with @WhenModified or @UpdatedTimestamp.
   */
  private BeanProperty findWhenModifiedProperty() {
    for (BeanProperty baseScalar : propertiesBaseScalar) {
      if (baseScalar.isGeneratedWhenModified()) {
        return baseScalar;
      }
    }
    return null;
  }

  /**
   * Return a raw expression for 'where parent id in ...' clause.
   */
  String parentIdInExpr(int parentIdSize, String rawWhere) {
    String inClause = idBinder.idInValueExpr(false, parentIdSize);
    return idBinder.isIdInExpandedForm() ? inClause : rawWhere + inClause;
  }

  /**
   * Return the IdBinder which is helpful for handling the various types of Id.
   */
  @Override
  public IdBinder idBinder() {
    return idBinder;
  }

  /**
   * Return true if this bean type has a simple single Id property.
   */
  boolean isSimpleId() {
    return idBinder instanceof IdBinderSimple;
  }

  @Override
  public boolean hasId() {
    return idProperty != null;
  }

  /**
   * Return false for IdClass case with multiple @Id properties.
   */
  public boolean hasSingleIdProperty() {
    return idPropertyIndex != -1;
  }

  /**
   * Return true if this type has a simple Id and the platform supports mutli-value binding.
   */
  public boolean isMultiValueIdSupported() {
    return multiValueSupported && isSimpleId();
  }

  /**
   * Return true if Id IN expression should have bind parameters padded.
   */
  public boolean isPadInExpression() {
    return !multiValueSupported && isSimpleId();
  }

  /**
   * Return the sql for binding an id. This is the columns with table alias that
   * make up the id.
   */
  public String idBinderIdSql(String alias) {
    if (alias == null) {
      return idBinderIdSql;
    } else {
      return idBinder.bindEqSql(alias);
    }
  }

  /**
   * Return the sql for binding id's using an IN clause.
   */
  public String idBinderInLHSSql() {
    return idBinderInLHSSql;
  }

  /**
   * Bind the idValue to the preparedStatement.
   * <p>
   * This takes care of the various id types such as embedded beans etc.
   */
  public void bindId(DataBind dataBind, Object idValue) throws SQLException {
    idBinder.bindId(dataBind, idValue);
  }

  /**
   * Return the id as an array of scalar bindable values.
   * <p>
   * This 'flattens' any EmbeddedId or multiple Id property cases.
   */
  public Object[] bindIdValues(Object idValue) {
    return idBinder.bindValues(idValue);
  }

  @Override
  @SuppressWarnings("unchecked")
  public T createBean() {
    return (T) createEntityBean(true);
  }

  /**
   * Creates a new EntityBean.
   * The parameter <code>isNew</code> controls either this is a new bean (then
   * {@link BeanPostConstructListener#postCreate(Object)} will be invoked) or
   * a reference (then {@link BeanPostLoad#postLoad(Object)} will be invoked
   * on first access (lazy load) or immediately (eager load)
   */
  private EntityBean createEntityBean(boolean isNew) {
    if (prototypeEntityBean == null) {
      throw new UnsupportedOperationException("cannot create entity bean for abstract entity " + name());
    }
    try {
      EntityBean bean = (EntityBean) prototypeEntityBean._ebean_newInstance();
      if (beanPostConstructListener != null) {
        beanPostConstructListener.autowire(bean); // calls all registered listeners
        beanPostConstructListener.postConstruct(bean); // calls first the @PostConstruct method and then the listeners
      }
      if (isNew) {
        if (beanPostConstructListener != null) {
          beanPostConstructListener.postCreate(bean);
          // if bean is not new, postLoad will be executed later in the bean's lifecycle
        }
        // do not unload properties for new beans!
      } else if (unloadProperties.length > 0) {
        // 'unload' any properties initialised in the default constructor
        EntityBeanIntercept ebi = bean._ebean_getIntercept();
        for (int unloadProperty : unloadProperties) {
          ebi.setPropertyUnloaded(unloadProperty);
        }
      }
      return bean;
    } catch (Exception ex) {
      throw new PersistenceException(ex);
    }
  }

  /**
   * Creates a new entity bean without invoking {@link BeanPostConstructListener#postCreate(Object)}
   */
  @Override
  public EntityBean createEntityBean() {
    return createEntityBean(false);
  }

  @Override
  public EntityBean createEntityBean2(boolean readOnlyNoIntercept) {
    if (readOnlyNoIntercept) {
      return (EntityBean) prototypeEntityBean._ebean_newInstanceReadOnly();
    }
    return createEntityBean(false);
  }

  /**
   * Create an entity bean for JSON marshalling (which differs for the element collection case).
   */
  public EntityBean createEntityBeanForJson() {
    return createEntityBean();
  }

  /**
   * We actually need to do a query because we don't know the type without the discriminator value.
   */
  private T findReferenceBean(Object id, PersistenceContext pc) {
    DefaultOrmQuery<T> query = new DefaultOrmQuery<>(this, ebeanServer, ebeanServer.expressionFactory());
    query.setPersistenceContext(pc);
    return query.setId(id).findOne();
  }

  /**
   * Create a reference with a check for the bean in the persistence context.
   */
  public EntityBean createReference(Boolean readOnly, Object id, PersistenceContext pc) {
    Object refBean = contextGet(pc, id);
    if (refBean == null) {
      refBean = createReference(readOnly, false, id, pc);
    }
    return (EntityBean) refBean;
  }

  /**
   * Create a reference bean based on the id.
   */
  @SuppressWarnings("unchecked")
  public T createReference(Boolean readOnly, boolean disableLazyLoad, Object id, PersistenceContext pc) {
    if (cacheSharableBeans && !disableLazyLoad && !Boolean.FALSE.equals(readOnly)) {
      CachedBeanData d = cacheHelp.beanCacheGetData(cacheKey(id));
      if (d != null) {
        Object shareableBean = d.getSharableBean();
        if (shareableBean != null) {
          return (T) shareableBean;
        }
      }
    }
    try {
      EntityBean eb = createEntityBean();
      id = convertSetId(id, eb);
      EntityBeanIntercept ebi = eb._ebean_getIntercept();
      if (disableLazyLoad) {
        ebi.setDisableLazyLoad(true);
      } else {
        ebi.setBeanLoader(refBeanLoader());
      }
      ebi.setReference(idPropertyIndex);
      if (Boolean.TRUE == readOnly) {
        ebi.setReadOnly(true);
      }
      if (pc != null) {
        contextPut(pc, id, eb);
        ebi.setPersistenceContext(pc);
      }
      return (T) eb;
    } catch (Exception ex) {
      throw new PersistenceException(ex);
    }
  }

  SingleBeanLoader refBeanLoader() {
    return new SingleBeanLoader.Ref(ebeanServer);
  }

  SingleBeanLoader l2BeanLoader() {
    return new SingleBeanLoader.L2(ebeanServer);
  }

  /**
   * Create a non read only reference bean without checking cacheSharableBeans.
   */
  public T createReference(Object id, PersistenceContext pc) {
    return createRef(id, pc);
  }

  @SuppressWarnings("unchecked")
  public T createRef(Object id, PersistenceContext pc) {
    try {
      EntityBean eb = createEntityBean();
      id = convertSetId(id, eb);
      EntityBeanIntercept ebi = eb._ebean_getIntercept();
      ebi.setBeanLoader(refBeanLoader());
      ebi.setReference(idPropertyIndex);
      if (pc != null) {
        contextPut(pc, id, eb);
        ebi.setPersistenceContext(pc);
      }
      return (T) eb;
    } catch (Exception ex) {
      throw new PersistenceException(ex);
    }
  }

  /**
   * Return the bean property traversing the object graph and taking into
   * account inheritance.
   */
  @Override
  public BeanProperty findPropertyFromPath(String path) {
    BeanDescriptor<?> other = this;
    while (true) {
      String[] split = SplitName.splitBegin(path);
      if (split[1] == null) {
        return other._findBeanProperty(split[0]);
      }
      BeanPropertyAssoc<?> assocProp = (BeanPropertyAssoc<?>) other._findBeanProperty(split[0]);
      if (assocProp == null) {
        throw new IllegalStateException("Unknown property path " + split[0] + " from " + path);
      }
      BeanDescriptor<?> targetDesc = assocProp.targetDescriptor();
      path = split[1];
      other = targetDesc;
    }
  }

  @Override
  public BeanType<?> beanTypeAtPath(String path) {
    return descriptor(path);
  }

  /**
   * Return the BeanDescriptor for a given path of Associated One or Many beans.
   */
  public BeanDescriptor<?> descriptor(String path) {
    BeanDescriptor<?> result = this;
    while (true) {
      if (path == null) {
        return result;
      }
      String[] splitBegin = SplitName.splitBegin(path);
      BeanProperty beanProperty = result.findProperty(splitBegin[0]);
      if (beanProperty instanceof BeanPropertyAssoc<?>) {
        BeanPropertyAssoc<?> assocProp = (BeanPropertyAssoc<?>) beanProperty;
        path = splitBegin[1];
        result = assocProp.targetDescriptor();
      } else {
        throw new PersistenceException("Invalid path " + path + " from " + result.fullName());
      }
    }
  }

  /**
   * Return the BeanDescriptor of another bean type.
   */
  public <U> BeanDescriptor<U> descriptor(Class<U> otherType) {
    return owner.descriptor(otherType);
  }

  /**
   * Returns true, if the table is managed (i.e. an existing m2m relation).
   */
  public boolean isTableManaged(String tableName) {
    return owner.isTableManaged(tableName);
  }

  /**
   * Return the order column property.
   */
  public BeanProperty orderColumn() {
    return orderColumn;
  }

  /**
   * Return the "shadow" property to support unidirectional relationships.
   * <p>
   * For bidirectional this is a real property on the bean. For unidirectional
   * relationships we have this 'shadow' property which is not externally
   * visible.
   */
  public BeanPropertyAssocOne<?> unidirectional() {
    return unidirectional;
  }

  /**
   * Return true if this bean type should use IdGeneration.
   * <p>
   * If this is false and the Id is null it is assumed that a database auto
   * increment feature is being used to populate the id.
   */
  public boolean isUseIdGenerator() {
    return idGenerator != null;
  }

  /**
   * Return bean class name.
   */
  public String descriptorId() {
    return fullName;
  }

  /**
   * Return the class type this BeanDescriptor describes.
   */
  @Override
  public Class<T> type() {
    return beanType;
  }

  /**
   * Return the bean class name this descriptor is used for.
   * <p>
   * If this BeanDescriptor is for a table then this returns the table name
   * instead.
   */
  @Override
  public String fullName() {
    return fullName;
  }

  /**
   * Return the short name of the entity bean.
   */
  @Override
  public String name() {
    return name;
  }

  /**
   * Return the simple name of the entity bean.
   */
  public String simpleName() {
    return beanType.getSimpleName();
  }

  /**
   * Summary description.
   */
  @Override
  public String toString() {
    return fullName;
  }

  /**
   * Get the bean from the persistence context.
   */
  public Object contextGet(PersistenceContext pc, Object id) {
    return pc.get(rootBeanType, id);
  }

  /**
   * Get the bean from the persistence context with delete check option.
   */
  public PersistenceContext.WithOption contextGetWithOption(PersistenceContext pc, Object id) {
    return pc.getWithOption(rootBeanType, id);
  }

  /**
   * Put the bean into the persistence context.
   */
  public void contextPut(PersistenceContext pc, Object id, Object bean) {
    pc.put(rootBeanType, id, bean);
  }

  /**
   * Put the bean into the persistence context if it is absent.
   */
  @Override
  public Object contextPutIfAbsent(PersistenceContext pc, Object id, EntityBean localBean) {
    return pc.putIfAbsent(rootBeanType, id, localBean);
  }

  /**
   * Create a reference bean and put it in the persistence context (and return it).
   */
  public Object contextRef(PersistenceContext pc, Boolean readOnly, boolean disableLazyLoad, Object id) {
    return createReference(readOnly, disableLazyLoad, id, pc);
  }

  /**
   * Clear a bean from the persistence context.
   */
  public void contextClear(PersistenceContext pc, Object idValue) {
    pc.clear(rootBeanType, idValue);
  }

  /**
   * Clear a bean from the persistence context.
   */
  public void contextClear(PersistenceContext pc) {
    pc.clear(rootBeanType);
  }

  /**
   * Delete a bean from the persistence context (such that we don't fetch it in the same transaction).
   */
  public void contextDeleted(PersistenceContext pc, Object idValue) {
    pc.deleted(rootBeanType, idValue);
  }

  /**
   * Return the Id property name or null if no Id property exists.
   */
  public String idName() {
    return (idProperty == null) ? null : idProperty.name();
  }

  /**
   * Helper method to return the unique property. If only one property makes up
   * the unique id then it's value is returned. If there is a concatenated
   * unique id then a Map is built with the keys being the names of the
   * properties that make up the unique id.
   */
  public Object getId(EntityBean bean) {
    return (idProperty == null) ? null : idProperty.getValueIntercept(bean);
  }

  /**
   * Return the cache key for the given bean (based on id value).
   */
  public String cacheKeyForBean(EntityBean bean) {
    return cacheKey(idProperty.getValue(bean));
  }

  /**
   * Return the cache key for the given id value.
   */
  public String cacheKey(Object id) {
    return idBinder.cacheKey(id);
  }

  @Override
  public Object id(Object bean) {
    return getId((EntityBean) bean);
  }

  /**
   * Return the Id value for the bean with embeddedId beans converted into maps.
   * <p>
   * The usage is to provide simple id types for JSON processing (for embeddedId's).
   */
  public Object idForJson(Object bean) {
    return idBinder.convertForJson((EntityBean) bean);
  }

  /**
   * Convert the idValue assuming embeddedId values are Maps.
   * <p>
   * The usage is to provide simple id types for JSON processing (for embeddedId's).
   */
  Object convertIdFromJson(Object idValue) {
    return idBinder.convertFromJson(idValue);
  }

  /**
   * Return the default order by that may need to be added if a many property is
   * included in the query.
   */
  public String defaultOrderBy() {
    return idBinder.orderBy();
  }

  /**
   * Convert the type of the idValue if required.
   */
  public Object convertId(Object idValue) {
    return idBinder.convertId(idValue);
  }

  /**
   * Set the bean id value converting if necessary.
   */
  @Override
  public void setId(T bean, Object idValue) {
    idBinder.convertSetId(idValue, (EntityBean) bean);
  }

  /**
   * Convert and set the id value.
   * <p>
   * If the bean is not null, the id value is set to the id property of the bean
   * after it has been converted to the correct type.
   */
  public Object convertSetId(Object idValue, EntityBean bean) {
    return idBinder.convertSetId(idValue, bean);
  }

  /**
   * Set the Id value to the bean (without type conversion).
   */
  public void setId(Object idValue, EntityBean bean) {
    idProperty.setValueIntercept(bean, idValue);
  }

  @Override
  public Property property(String propName) {
    return findProperty(propName);
  }

  /**
   * Get a BeanProperty by its name.
   */
  public BeanProperty beanProperty(String propName) {
    return propMap.get(propName);
  }

  public void sort(List<T> list, String sortByClause) {
    list.sort(elComparator(sortByClause));
  }

  public ElComparator<T> elComparator(String propNameOrSortBy) {
    return comparatorCache.computeIfAbsent(propNameOrSortBy, this::createComparator);
  }

  /**
   * Register all the assoc many properties on this bean that are not populated with the load context.
   * <p>
   * This provides further lazy loading via the load context.
   */
  public void lazyLoadRegister(String prefix, EntityBeanIntercept ebi, EntityBean bean, LoadContext loadContext) {
    // load the List/Set/Map proxy objects (deferred fetching of lists)
    for (BeanPropertyAssocMany<?> many : propertiesMany()) {
      if (!ebi.isLoadedProperty(many.propertyIndex())) {
        BeanCollection<?> ref = many.createReferenceIfNull(bean);
        if (ref != null && !ref.isRegisteredWithLoadContext()) {
          String path = SplitName.add(prefix, many.name());
          loadContext.register(path, many, ref);
        }
      }
    }
  }

  /**
   * Return true if the lazy loading property is a Many in which case just
   * define a Reference for the collection and not invoke a query.
   */
  public boolean lazyLoadMany(EntityBeanIntercept ebi) {
    return lazyLoadMany(ebi, null);
  }

  public boolean lazyLoadMany(EntityBeanIntercept ebi, LoadBeanContext parent) {
    int lazyLoadProperty = ebi.lazyLoadPropertyIndex();
    if (lazyLoadProperty == -1) {
      return false;
    }
    return lazyLoadMany(ebi, lazyLoadProperty, parent);
  }

  /**
   * Return true if this is a generated property.
   */
  public boolean isGeneratedProperty(int propertyIndex) {
    return propertiesIndex[propertyIndex].isGenerated();
  }

  /**
   * Check for lazy loading of many property.
   */
  private boolean lazyLoadMany(EntityBeanIntercept ebi, int lazyLoadProperty, LoadBeanContext loadBeanContext) {
    BeanProperty lazyLoadBeanProp = propertiesIndex[lazyLoadProperty];
    if (lazyLoadBeanProp instanceof BeanPropertyAssocMany<?>) {
      BeanPropertyAssocMany<?> manyProp = (BeanPropertyAssocMany<?>) lazyLoadBeanProp;
      final BeanCollection<?> collection = manyProp.createReference(ebi.owner());
      ebi.setLoadedLazy();
      if (loadBeanContext != null) {
        loadBeanContext.register(manyProp, collection);
      }
      return true;
    }
    return false;
  }

  /**
   * Return a Comparator for local sorting of lists.
   *
   * @param sortByClause list of property names with optional ASC or DESC suffix.
   */
  @SuppressWarnings("unchecked")
  private ElComparator<T> createComparator(String sortByClause) {
    SortByClause sortBy = SortByClauseParser.parse(sortByClause);
    if (sortBy.size() == 1) {
      // simple comparator for a single property
      return createPropertyComparator(sortBy.getProperties().get(0));
    }

    // create a compound comparator based on the list of properties
    ElComparator<T>[] comparators = new ElComparator[sortBy.size()];
    List<SortByClause.Property> sortProps = sortBy.getProperties();
    for (int i = 0; i < sortProps.size(); i++) {
      SortByClause.Property sortProperty = sortProps.get(i);
      comparators[i] = createPropertyComparator(sortProperty);
    }
    return new ElComparatorCompound<>(comparators);
  }

  private ElComparator<T> createPropertyComparator(SortByClause.Property sortProp) {
    ElPropertyValue elGetValue = elGetValue(sortProp.getName());
    if (elGetValue == null) {
      log.log(ERROR, "Sort property [" + sortProp + "] not found in " + beanType + ". Cannot sort.");
      return new ElComparatorNoop<>();
    }
    if (elGetValue.isAssocMany()) {
      log.log(ERROR, "Sort property [" + sortProp + "] in " + beanType + " is a many-property. Cannot sort.");
      return new ElComparatorNoop<>();
    }
    Boolean nullsHigh = sortProp.getNullsHigh();
    if (nullsHigh == null) {
      nullsHigh = Boolean.TRUE;
    }
    return new ElComparatorProperty<>(elGetValue, sortProp.isAscending(), nullsHigh);
  }

  @Override
  public boolean isValidExpression(String propertyName) {
    try {
      return (elGetValue(propertyName) != null);
    } catch (PersistenceException e) {
      return false;
    }
  }

  /**
   * Get an Expression language Value object.
   */
  public ElPropertyValue elGetValue(String propName) {
    ElPropertyValue elGetValue = elCache.get(propName);
    if (elGetValue != null) {
      return elGetValue;
    }
    elGetValue = buildElGetValue(propName, null, false);
    if (elGetValue != null) {
      elCache.put(propName, elGetValue);
    }
    return elGetValue;
  }

  @Override
  public ExpressionPath expressionPath(String path) {
    return elGetValue(path);
  }

  /**
   * Similar to ElPropertyValue but also uses foreign key shortcuts.
   * <p>
   * The foreign key shortcuts means we can avoid unnecessary joins.
   */
  public ElPropertyDeploy elPropertyDeploy(String propName) {
    ElPropertyDeploy elProp = elDeployCache.get(propName);
    if (elProp != null) {
      return elProp;
    }
    if (!propName.contains(".")) {
      // No period means simple property and no need to look for
      // foreign key properties (in order to avoid an extra join)
      elProp = elGetValue(propName);
    } else {
      elProp = buildElGetValue(propName, null, true);
    }
    if (elProp != null) {
      elDeployCache.put(propName, elProp);
    }
    return elProp;
  }

  ElPropertyValue buildElGetValue(String propName, ElPropertyChainBuilder chain, boolean propertyDeploy) {
    if (propertyDeploy && chain != null) {
      ElPropertyDeploy fk = elDeployCache.get(propName);
      if (fk instanceof BeanFkeyProperty) {
        // propertyDeploy chain for foreign key column
        return ((BeanFkeyProperty) fk).create(chain.expression(), chain.isContainsMany());
      }
    }
    int basePos = propName.indexOf('.');
    if (basePos > -1) {
      // nested or embedded property
      String baseName = propName.substring(0, basePos);
      BeanProperty assocProp = _findBeanProperty(baseName);
      if (assocProp == null) {
        return null;
      }
      String remainder = propName.substring(basePos + 1);
      return assocProp.buildElPropertyValue(propName, remainder, chain, propertyDeploy);
    }

    BeanProperty property = _findBeanProperty(propName);
    if (chain == null) {
      return property;
    }
    if (property == null) {
      throw new PersistenceException("No property found for [" + propName + "] in expression " + chain.expression());
    }
    if (property.containsMany()) {
      chain.setContainsMany();
    }
    return chain.add(property).build();
  }

  /**
   * Return the property path given the db table and column.
   */
  public String findBeanPath(String schemaName, String tableName, String columnName) {
    if (matchBaseTable(tableName)) {
      return columnPath.get(columnName);
    }
    BeanPropertyAssoc<?> assocProperty = tablePath.get(tableName);
    if (assocProperty == null) {
      assocProperty = tablePath.get(schemaName + "." + tableName);
    }
    if (assocProperty != null) {
      String relativePath = assocProperty.targetDescriptor().findBeanPath(schemaName, tableName, columnName);
      if (relativePath != null) {
        return SplitName.add(assocProperty.name(), relativePath);
      }
    }
    return null;
  }

  boolean matchBaseTable(String tableName) {
    return tableName.isEmpty()
      || baseTable.equalsIgnoreCase(tableName)
      || baseTable.endsWith("." + tableName);
  }

  /**
   * Return a 'dynamic property' used to read a formula.
   */
  private STreeProperty findSqlTreeFormula(String formula, String path) {
    String key = formula + "-" + path;
    return dynamicProperty.computeIfAbsent(key, (fullKey) -> FormulaPropertyPath.create(this, formula, path));
  }

  /**
   * Return a property that is part of the SQL tree.
   * <p>
   * The property can be a dynamic formula or a well known bean property.
   */
  @Override
  public STreeProperty findPropertyWithDynamic(String propName, String path) {
    if (propName.indexOf('(') > -1) {
      return findSqlTreeFormula(propName, path);
    }
    return findProperty(propName);
  }

  /**
   * Find a BeanProperty including searching the inheritance hierarchy.
   * <p>
   * This searches this BeanDescriptor and then searches further down the
   * inheritance tree (not up).
   */
  @Override
  public BeanProperty findProperty(String propName) {
    int basePos = propName.indexOf('.');
    if (basePos > -1) {
      // embedded property
      String baseName = propName.substring(0, basePos);
      return _findBeanProperty(baseName);
    }

    return _findBeanProperty(propName);
  }

  BeanProperty _findBeanProperty(String propName) {
    BeanProperty prop = propMap.get(propName);
    if (prop == null && "_$IdClass$".equals(propName)) {
      return idProperty;
    }
    return prop;
  }

  /**
   * Reset the many properties to empty state ready for reloading.
   */
  public void resetManyProperties(Object dbBean) {
    EntityBean bean = (EntityBean) dbBean;
    for (BeanPropertyAssocMany<?> many : propertiesMany) {
      if (many.isCascadeRefresh()) {
        many.resetMany(bean);
      }
    }
  }

  /**
   * Return true if this bean can cache sharable instances.
   * <p>
   * This means is has no relationships and has readOnly=true in its cache
   * options.
   */
  public boolean isCacheSharableBeans() {
    return cacheSharableBeans;
  }

  /**
   * Return true if queries for beans of this type are auto tunable.
   */
  public boolean isAutoTunable() {
    return autoTunable;
  }

  /**
   * Return true if this is an embedded bean.
   */
  public boolean isEmbedded() {
    return EntityType.EMBEDDED == entityType;
  }

  /**
   * Return the compound unique constraints.
   */
  public IndexDefinition[] indexDefinitions() {
    return indexDefinitions;
  }

  /**
   * Return the beanListener.
   */
  @Override
  public BeanPersistListener persistListener() {
    return persistListener;
  }

  /**
   * Return the beanFinder (Migrate over to getFindController).
   */
  public BeanFindController beanFinder() {
    return beanFinder;
  }

  /**
   * Return the find controller (SPI interface).
   */
  @Override
  public BeanFindController findController() {
    return beanFinder;
  }

  /**
   * Return the BeanQueryAdapter or null if none is defined.
   */
  @Override
  public BeanQueryAdapter queryAdapter() {
    return queryAdapter;
  }

  /**
   * De-register the BeanPersistListener.
   */
  public void deregister(BeanPersistListener listener) {
    BeanPersistListener currentListener = persistListener;
    if (currentListener != null) {
      if (currentListener instanceof ChainedBeanPersistListener) {
        // remove it from the existing chain
        persistListener = ((ChainedBeanPersistListener) currentListener).deregister(listener);
      } else if (currentListener.equals(listener)) {
        persistListener = null;
      }
    }
  }

  /**
   * De-register the BeanPersistController.
   */
  public void deregister(BeanPersistController controller) {
    BeanPersistController currentController = persistController;
    if (currentController != null) {
      if (currentController instanceof ChainedBeanPersistController) {
        // remove it from the existing chain
        persistController = ((ChainedBeanPersistController) currentController).deregister(controller);
      } else if (currentController.equals(controller)) {
        persistController = null;
      }
    }
  }

  /**
   * Register the new BeanPersistController.
   */
  public void register(BeanPersistListener newPersistListener) {
    if (newPersistListener.isRegisterFor(beanType)) {
      BeanPersistListener currentListener = persistListener;
      if (currentListener == null) {
        persistListener = newPersistListener;
      } else {
        if (currentListener instanceof ChainedBeanPersistListener) {
          // add it to the existing chain
          persistListener = ((ChainedBeanPersistListener) currentListener).register(newPersistListener);
        } else {
          // build new chain of the 2
          persistListener = new ChainedBeanPersistListener(currentListener, newPersistListener);
        }
      }
    }
  }

  /**
   * Register the new BeanPersistController.
   */
  public void register(BeanPersistController newController) {
    if (newController.isRegisterFor(beanType)) {
      BeanPersistController currentController = persistController;
      if (currentController == null) {
        persistController = newController;
      } else {
        if (currentController instanceof ChainedBeanPersistController) {
          persistController = ((ChainedBeanPersistController) currentController).register(newController);
        } else {
          persistController = new ChainedBeanPersistController(currentController, newController);
        }
      }
    }
  }

  /**
   * Return the Controller.
   */
  @Override
  public BeanPersistController persistController() {
    return persistController;
  }

  /**
   * Returns true if this bean is based on RawSql.
   */
  @Override
  public boolean isRawSqlBased() {
    return EntityType.SQL == entityType;
  }

  /**
   * Return the DB comment for the base table.
   */
  public String dbComment() {
    return dbComment;
  }

  /**
   * Return true if foreign keys to the base table should be suppressed.
   */
  public boolean suppressForeignKey() {
    return partitionMeta != null;
  }

  /**
   * Return the partition details of the bean.
   */
  public PartitionMeta partitionMeta() {
    return partitionMeta;
  }

  /**
   * Return the tablespace details of the bean.
   */
  public TablespaceMeta tablespaceMeta() {
    return tablespaceMeta;
  }

  /**
   * Return the storage engine.
   */
  public String storageEngine() {
    return storageEngine;
  }

  /**
   * Return the dependent tables for a view based entity.
   */
  public String[] dependentTables() {
    return dependentTables;
  }

  /**
   * Return the base table. Only properties mapped to the base table are by default persisted.
   */
  @Override
  public String baseTable() {
    return baseTable;
  }

  /**
   * Return true if this type is a base table entity type.
   */
  public boolean isBaseTable() {
    return baseTable != null && entityType == EntityType.ORM;
  }

  /**
   * Return the base table to use given the query temporal mode.
   */
  @Override
  public String baseTable(SpiQuery.TemporalMode mode) {
    switch (mode) {
      case VERSIONS:
        return baseTableVersionsBetween;
      case AS_OF:
        return baseTableAsOf;
      default:
        return baseTable;
    }
  }

  @Override
  public boolean isSoftDelete() {
    return softDelete;
  }

  public void softDeleteValue(EntityBean bean) {
    softDeleteProperty.setSoftDeleteValue(bean);
  }

  String softDeleteDbSet() {
    return softDeleteProperty.softDeleteDbSet();
  }

  @Override
  public String softDeletePredicate(String tableAlias) {
    return softDeleteProperty.softDeleteDbPredicate(tableAlias);
  }

  @Override
  public void markAsDeleted(EntityBean bean) {
    if (softDeleteProperty == null) {
      Object id = getId(bean);
      log.log(INFO, "(Lazy) loading unsuccessful for type:{0} id:{1} - expecting when bean has been deleted", name(), id);
      bean._ebean_getIntercept().setLazyLoadFailure(id);
    } else {
      softDeleteValue(bean);
      bean._ebean_getIntercept().setLoaded();
      setAllLoaded(bean);
    }
  }

  @Override
  public Map<String, String> pathMap(String prefix) {
    return pathMaps.computeIfAbsent(prefix, s -> {
      HashMap<String, String> m = new HashMap<>();
      for (STreePropertyAssocMany many : propsMany()) {
        String name = many.name();
        m.put(name, prefix + "." + name);
      }
      for (STreePropertyAssocOne one : propsOne()) {
        String name = one.name();
        m.put(name, prefix + "." + name);
      }
      return m.isEmpty() ? Collections.emptyMap() : m;
    });
  }

  @Override
  public boolean isEmbeddedPath(String propertyPath) {
    ElPropertyDeploy elProp = elPropertyDeploy(propertyPath);
    if (elProp == null) {
      throw new PersistenceException("Invalid path " + propertyPath + " from " + fullName());
    }
    return elProp.beanProperty().isEmbedded();
  }

  @Override
  public ExtraJoin extraJoin(String propertyPath) {
    ElPropertyValue elGetValue = elGetValue(propertyPath);
    if (elGetValue != null) {
      BeanProperty beanProperty = elGetValue.beanProperty();
      if (beanProperty instanceof BeanPropertyAssoc<?>) {
        BeanPropertyAssoc<?> assocProp = (BeanPropertyAssoc<?>) beanProperty;
        if (!assocProp.isEmbedded()) {
          return new ExtraJoin(assocProp, elGetValue.containsMany());
        }
      }
    }
    return null;
  }

  @Override
  public void inheritanceLoad(SqlBeanLoad sqlBeanLoad, STreeProperty property, DbReadContext ctx) {
    BeanProperty p = beanProperty(property.name());
    if (p != null) {
      p.load(sqlBeanLoad);
    } else {
      property.loadIgnore(ctx);
    }
  }

  void setUnmappedJson(EntityBean bean, Map<String, Object> unmappedProperties) {
    if (unmappedJson != null) {
      unmappedJson.setValueIntercept(bean, unmappedProperties);
    }
  }

  /**
   * Set the Tenant Id value to the bean.
   */
  public void setTenantId(EntityBean entityBean, Object tenantId) {
    if (tenant != null) {
      tenant.setTenantValue(entityBean, tenantId);
    }
  }

  @Override
  public boolean isToManyDirty(EntityBean bean) {
    final EntityBeanIntercept ebi = bean._ebean_getIntercept();
    for (BeanPropertyAssocMany<?> many : propertiesManySave) {
      if (ebi.isLoadedProperty(many.propertyIndex())) {
        final Object value = many.getValue(bean);
        if (value instanceof BeanCollection && ((BeanCollection<?>) value).hasModifications() || value != null) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Return true if this entity bean has history support.
   */
  @Override
  public boolean isHistorySupport() {
    return historySupport;
  }

  /**
   * Return the identity generation type.
   */
  @Override
  public IdType idType() {
    return idType;
  }

  /**
   * Set the generated Id value if appropriate.
   */
  public void setGeneratedId(EntityBean entityBean, Transaction transaction) {
    if (idGenerator == null || idProperty == null || idProperty.isEmbedded()) {
      return;
    }
    if (isNullOrZero(idProperty.getValue(entityBean))) {
      convertSetId(nextId(transaction), entityBean);
    }
  }

  /**
   * Return true if the Id value is marked as a <code>@GeneratedValue</code>.
   */
  public boolean isIdGeneratedValue() {
    return idGeneratedValue;
  }

  public IdentityMode identityMode() {
    return identityMode;
  }

  /**
   * Return the SQL used to return the last inserted id.
   * <p>
   * This is only used with Identity columns and getGeneratedKeys is not
   * supported.
   */
  public String selectLastInsertedId() {
    return selectLastInsertedId;
  }

  /**
   * Return true if this bean uses a SQL select to fetch the last inserted id value.
   */
  public boolean supportsSelectLastInsertedId() {
    return selectLastInsertedId != null;
  }

  @Override
  public Collection<? extends Property> allProperties() {
    return propertiesAll();
  }

  /**
   * Return a collection of all BeanProperty. This includes transient properties.
   */
  public Collection<BeanProperty> propertiesAll() {
    return propMap.values();
  }

  /**
   * Return the property that holds unmapped JSON content.
   */
  public BeanProperty propertyUnmappedJson() {
    return unmappedJson;
  }

  /**
   * Return the non transient non id properties.
   */
  public BeanProperty[] propertiesNonTransient() {
    return propertiesNonTransient;
  }

  /**
   * Return the transient properties.
   */
  public BeanProperty[] propertiesTransient() {
    return propertiesTransient;
  }

  /**
   * Return the beans that are embedded. These share the base table with the owner bean.
   */
  public BeanPropertyAssocOne<?>[] propertiesEmbedded() {
    return propertiesEmbedded;
  }

  /**
   * Return true if the query detail includes an aggregation property.
   */
  public boolean includesAggregation(OrmQueryDetail detail) {
    return detail != null && propertiesAggregate.length > 0 && includesAggregation(detail.getChunk(null, false));
  }

  private boolean includesAggregation(OrmQueryProperties rootProps) {
    if (rootProps != null) {
      final Set<String> included = rootProps.getIncluded();
      if (included != null) {
        for (BeanProperty property : propertiesAggregate) {
          if (included.contains(property.name())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Set all properties to be loaded (recurse to embedded beans).
   */
  public void setAllLoaded(EntityBean bean) {
    bean._ebean_getIntercept().setLoadedPropertyAll();
    for (BeanPropertyAssocOne<?> embedded : propertiesEmbedded) {
      embedded.setAllLoadedEmbedded(bean);
    }
  }

  public TableJoin primaryKeyJoin() {
    return primaryKeyJoin;
  }

  @Override
  public BeanProperty idProperty() {
    return idProperty;
  }

  /**
   * Return true if this bean should be inserted rather than updated.
   *
   * @param ebi        The entity bean intercept
   * @param insertMode true if the 'root request' was an insert rather than an update
   */
  public boolean isInsertMode(EntityBeanIntercept ebi, boolean insertMode) {
    if (ebi.isLoaded()) {
      // must be an update as the bean is loaded
      return false;
    }
    if (idProperty.isEmbedded()) {
      // not using Id generator so just base on isLoaded()
      return !ebi.isLoaded();
    }
    if (!hasIdValue(ebi.owner())) {
      // No Id property means it must be an insert
      return true;
    }
    // same as the 'root request'
    return insertMode;
  }

  public boolean isReference(EntityBeanIntercept ebi) {
    return ebi.isReference() || referenceIdPropertyOnly(ebi);
  }

  public boolean referenceIdPropertyOnly(EntityBeanIntercept ebi) {
    return idOnlyReference && ebi.hasIdOnly(idPropertyIndex);
  }

  public boolean isIdLoaded(EntityBeanIntercept ebi) {
    // assume id loaded for IdClass case with idPropertyIndex == -1
    return idPropertyIndex == -1 || ebi.isLoadedProperty(idPropertyIndex);
  }

  boolean hasIdValue(EntityBean bean) {
    return (idProperty != null && !isNullOrZero(idProperty.getValue(bean)));
  }

  boolean hasVersionProperty(EntityBeanIntercept ebi) {
    return versionPropertyIndex > -1 && ebi.isLoadedProperty(versionPropertyIndex);
  }

  void setReferenceIfIdOnly(EntityBeanIntercept ebi) {
    if (referenceIdPropertyOnly(ebi)) {
      ebi.setReference(idPropertyIndex);
    }
  }

  /**
   * Set the version value returning it in primitive long form.
   */
  @SuppressWarnings("unchecked")
  public long setVersion(EntityBean entityBean, Object versionValue) {
    versionProperty.setValueIntercept(entityBean, versionValue);
    return versionProperty.scalarType.asVersion(versionValue);
  }

  /**
   * Return the version value in primitive long form (if exists and set).
   */
  @SuppressWarnings("unchecked")
  public long getVersion(EntityBean entityBean) {
    if (versionProperty == null) {
      return 0;
    }
    Object value = versionProperty.getValue(entityBean);
    return value == null ? 0 : versionProperty.scalarType.asVersion(value);
  }

  /**
   * Check all mutable scalar types and mark as dirty if necessary.
   */
  public void checkAllMutableProperties(EntityBeanIntercept ebi) {
    for (BeanProperty beanProperty : propertiesMutable) {
      int propertyIndex = beanProperty.propertyIndex();
      if (ebi.isLoadedProperty(propertyIndex)) {
        Object value = beanProperty.getValue(ebi.owner());
        if (beanProperty.checkMutable(value, ebi.isDirtyProperty(propertyIndex), ebi)) {
          // mutable scalar value which is considered dirty so mark
          // it as such so that it is included in an update
          ebi.markPropertyAsChanged(propertyIndex);
        }
      }
    }
  }

  /**
   * Return true if any mutable properties are dirty.
   */
  public void checkAnyMutableProperties(EntityBeanIntercept ebi) {
    for (BeanProperty beanProperty : propertiesMutable) {
      int propertyIndex = beanProperty.propertyIndex();
      if (ebi.isLoadedProperty(propertyIndex)) {
        Object value = beanProperty.getValue(ebi.owner());
        if (beanProperty.checkMutable(value, ebi.isDirtyProperty(propertyIndex), ebi)) {
          ebi.markPropertyAsChanged(propertyIndex);
          return;
        }
      }
    }
  }

  public ConcurrencyMode concurrencyMode(EntityBeanIntercept ebi) {
    if (!hasVersionProperty(ebi)) {
      return ConcurrencyMode.NONE;
    } else {
      return concurrencyMode;
    }
  }

  /**
   * Return the diff comparing the bean values.
   */
  public Map<String, ValuePair> diff(EntityBean newBean, EntityBean oldBean) {
    Map<String, ValuePair> map = new LinkedHashMap<>();
    diff(null, map, newBean, oldBean);
    return map;
  }

  /**
   * Populate the diff for updates with flattened non-null property values.
   */
  public void diff(String prefix, Map<String, ValuePair> map, EntityBean newBean, EntityBean oldBean) {
    for (BeanProperty baseScalar : propertiesBaseScalar) {
      baseScalar.diff(prefix, map, newBean, oldBean);
    }
    for (BeanPropertyAssocOne<?> one : propertiesOne) {
      one.diff(prefix, map, newBean, oldBean);
    }
    for (BeanPropertyAssocOne<?> embedded : propertiesEmbedded) {
      embedded.diff(prefix, map, newBean, oldBean);
    }
  }

  /**
   * Appends the Id property to the OrderBy clause if it is not believed
   * to be already contained in the order by.
   * <p>
   * This is primarily used for paging queries to ensure that an order by clause is provided and that the order by
   * provides unique ordering of the rows (so that the paging is predicable).
   */
  public void appendOrderById(SpiQuery<T> query) {
    if (idProperty != null && !idProperty.isEmbedded() && !query.orderBy().containsProperty(idProperty.name())) {
      query.orderBy().asc(idProperty.name());
    }
  }

  @Override
  public STreeProperty[] propsBaseScalar() {
    return propertiesBaseScalar;
  }

  @Override
  public STreePropertyAssoc[] propsEmbedded() {
    return propertiesEmbedded;
  }

  @Override
  public STreePropertyAssocOne[] propsOne() {
    return propertiesOne;
  }

  @Override
  public STreePropertyAssocMany[] propsMany() {
    return propertiesMany;
  }

  /**
   * All the BeanPropertyAssocOne that are not embedded. These are effectively
   * joined beans. For ManyToOne and OneToOne associations.
   */
  public BeanPropertyAssocOne<?>[] propertiesOne() {
    return propertiesOne;
  }

  /**
   * Returns ManyToOnes and OneToOnes on the imported owning side.
   * <p>
   * Excludes OneToOnes on the exported side.
   */
  public BeanPropertyAssocOne<?>[] propertiesOneImported() {
    return propertiesOneImported;
  }

  /**
   * Imported Assoc Ones with cascade save true.
   */
  public BeanPropertyAssocOne<?>[] propertiesOneImportedSave() {
    return propertiesOneImportedSave;
  }

  /**
   * Imported Assoc Ones with cascade delete true.
   */
  public BeanPropertyAssocOne<?>[] propertiesOneImportedDelete() {
    return propertiesOneImportedDelete;
  }

  /**
   * Exported assoc ones with cascade save.
   */
  public BeanPropertyAssocOne<?>[] propertiesOneExportedSave() {
    return propertiesOneExportedSave;
  }

  /**
   * Exported assoc ones with delete cascade.
   */
  public BeanPropertyAssocOne<?>[] propertiesOneExportedDelete() {
    return propertiesOneExportedDelete;
  }

  /**
   * All Non Assoc Many's for this descriptor.
   */
  public BeanProperty[] propertiesNonMany() {
    return propertiesNonMany;
  }

  /**
   * All Assoc Many's for this descriptor.
   */
  public BeanPropertyAssocMany<?>[] propertiesMany() {
    return propertiesMany;
  }

  /**
   * Assoc Many's with save cascade.
   */
  public BeanPropertyAssocMany<?>[] propertiesManySave() {
    return propertiesManySave;
  }

  /**
   * Assoc Many's with delete cascade.
   */
  public BeanPropertyAssocMany<?>[] propertiesManyDelete() {
    return propertiesManyDelete;
  }

  /**
   * Assoc ManyToMany's.
   */
  public BeanPropertyAssocMany<?>[] propertiesManyToMany() {
    return propertiesManyToMany;
  }

  /**
   * Return the first version property that exists on the bean. Returns null if
   * no version property exists on the bean.
   * <p>
   * Note that this DOES NOT find a version property on an embedded bean.
   */
  public BeanProperty versionProperty() {
    return versionProperty;
  }

  /**
   * Return true if this type is tenant aware.
   */
  public boolean isMultiTenant() {
    return tenant != null;
  }

  /**
   * Return the tenant property when multi-tenant partitioning support is used.
   */
  public BeanProperty tenantProperty() {
    return tenant;
  }

  /**
   * Scalar properties without the unique id or secondary table properties.
   */
  public BeanProperty[] propertiesBaseScalar() {
    return propertiesBaseScalar;
  }

  /**
   * Return the properties set as generated values on insert.
   */
  public BeanProperty[] propertiesGenInsert() {
    return propertiesGenInsert;
  }

  /**
   * Return the properties set as generated values on update.
   */
  public BeanProperty[] propertiesGenUpdate() {
    return propertiesGenUpdate;
  }

  public void jsonWriteDirty(SpiJsonWriter writeJson, EntityBean bean, boolean[] dirtyProps) throws IOException {
    jsonHelp.jsonWriteDirty(writeJson, bean, dirtyProps);
  }

  void jsonWriteDirtyProperties(SpiJsonWriter writeJson, EntityBean bean, boolean[] dirtyProps) throws IOException {
    jsonHelp.jsonWriteDirtyProperties(writeJson, bean, dirtyProps);
  }

  public void jsonWriteMapEntry(SpiJsonWriter ctx, Map.Entry<?, ?> entry) throws IOException {
    throw new IllegalStateException("Unexpected - expect Element override");
  }

  public void jsonWriteElement(SpiJsonWriter ctx, Object element) {
    throw new IllegalStateException("Unexpected - expect Element override");
  }

  public Object jsonReadCollection(SpiJsonReader readJson, EntityBean parentBean) throws IOException {
    throw new IllegalStateException("Unexpected - expect Element override");
  }

  public boolean isJsonReadCollection() {
    return false;
  }

  public void jsonWrite(SpiJsonWriter writeJson, EntityBean bean) throws IOException {
    jsonHelp.jsonWrite(writeJson, bean, null);
  }

  public void jsonWrite(SpiJsonWriter writeJson, EntityBean bean, String key) throws IOException {
    jsonHelp.jsonWrite(writeJson, bean, key);
  }

  void jsonWriteProperties(SpiJsonWriter writeJson, EntityBean bean) {
    jsonHelp.jsonWriteProperties(writeJson, bean);
  }

  public T jsonRead(SpiJsonReader jsonRead, String path, T target) throws IOException {
    return jsonHelp.jsonRead(jsonRead, path, true, target);
  }

  T jsonReadObject(SpiJsonReader jsonRead, String path, T target) throws IOException {
    return jsonHelp.jsonRead(jsonRead, path, false, target);
  }

  public List<BeanProperty[]> uniqueProps() {
    return propertiesUnique;
  }

}
