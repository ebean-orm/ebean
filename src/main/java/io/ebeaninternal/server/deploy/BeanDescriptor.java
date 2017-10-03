package io.ebeaninternal.server.deploy;

import io.ebean.OrderBy;
import io.ebean.PersistenceContextScope;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.ValuePair;
import io.ebean.annotation.DocStoreMode;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.ConstructorMarker;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebean.bean.PersistenceContextUtil;
import io.ebean.config.EncryptKey;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.event.BeanFindController;
import io.ebean.event.BeanPersistController;
import io.ebean.event.BeanPersistListener;
import io.ebean.event.BeanPostConstructListener;
import io.ebean.event.BeanPostLoad;
import io.ebean.event.BeanQueryAdapter;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeLogFilter;
import io.ebean.event.changelog.ChangeType;
import io.ebean.event.readaudit.ReadAuditLogger;
import io.ebean.event.readaudit.ReadAuditPrepare;
import io.ebean.event.readaudit.ReadEvent;
import io.ebean.meta.MetaBeanInfo;
import io.ebean.meta.MetaQueryPlanStatistic;
import io.ebean.plugin.BeanDocType;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.ExpressionPath;
import io.ebean.plugin.Property;
import io.ebeaninternal.api.CQueryPlanKey;
import io.ebeaninternal.api.ConcurrencyMode;
import io.ebeaninternal.api.LoadContext;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiUpdatePlan;
import io.ebeaninternal.api.TransactionEventTable.TableIUD;
import io.ebeaninternal.server.cache.CacheChangeSet;
import io.ebeaninternal.server.cache.CachedBeanData;
import io.ebeaninternal.server.cache.CachedManyIds;
import io.ebeaninternal.server.core.CacheOptions;
import io.ebeaninternal.server.core.DefaultSqlUpdate;
import io.ebeaninternal.server.core.InternString;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.deploy.id.ImportedId;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyLists;
import io.ebeaninternal.server.el.ElComparator;
import io.ebeaninternal.server.el.ElComparatorCompound;
import io.ebeaninternal.server.el.ElComparatorProperty;
import io.ebeaninternal.server.el.ElPropertyChainBuilder;
import io.ebeaninternal.server.el.ElPropertyDeploy;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.persist.DmlUtil;
import io.ebeaninternal.server.query.CQueryPlan;
import io.ebeaninternal.server.query.CQueryPlanStats.Snapshot;
import io.ebeaninternal.server.query.SplitName;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.text.json.ReadJson;
import io.ebeaninternal.server.text.json.SpiJsonWriter;
import io.ebeaninternal.server.type.DataBind;
import io.ebeaninternal.util.SortByClause;
import io.ebeaninternal.util.SortByClauseParser;
import io.ebeanservice.docstore.api.DocStoreBeanAdapter;
import io.ebeanservice.docstore.api.DocStoreUpdateContext;
import io.ebeanservice.docstore.api.DocStoreUpdates;
import io.ebeanservice.docstore.api.mapping.DocMappingBuilder;
import io.ebeanservice.docstore.api.mapping.DocPropertyMapping;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import io.ebeanservice.docstore.api.mapping.DocumentMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Describes Beans including their deployment information.
 */
public class BeanDescriptor<T> implements MetaBeanInfo, BeanType<T> {

  private static final Logger logger = LoggerFactory.getLogger(BeanDescriptor.class);

  private final ConcurrentHashMap<String, SpiUpdatePlan> updatePlanCache = new ConcurrentHashMap<>();

  private final ConcurrentHashMap<CQueryPlanKey, CQueryPlan> queryPlanCache = new ConcurrentHashMap<>();

  private final ConcurrentHashMap<String, ElPropertyValue> elCache = new ConcurrentHashMap<>();

  private final ConcurrentHashMap<String, ElPropertyDeploy> elDeployCache = new ConcurrentHashMap<>();

  private final ConcurrentHashMap<String, ElComparator<T>> comparatorCache = new ConcurrentHashMap<>();

  private final Map<String, RawSql> namedRawSql;

  private final Map<String, String> namedQuery;

  public enum EntityType {
    ORM, EMBEDDED, VIEW, SQL, DOC
  }

  /**
   * The EbeanServer name. Same as the plugin name.
   */
  private final String serverName;

  /**
   * The nature/type of this bean.
   */
  private final EntityType entityType;

  /**
   * Type of Identity generation strategy used.
   */
  private final IdType idType;

  private final boolean idTypePlatformDefault;

  private final PlatformIdGenerator idGenerator;

  /**
   * The database sequence name (optional).
   */
  private final String sequenceName;

  private final int sequenceInitialValue;

  private final int sequenceAllocationSize;

  /**
   * SQL used to return last inserted id. Used for Identity columns where
   * getGeneratedKeys is not supported.
   */
  private final String selectLastInsertedId;

  private final boolean autoTunable;

  /**
   * The concurrency mode for beans of this type.
   */
  private final ConcurrencyMode concurrencyMode;

  private final IndexDefinition[] indexDefinitions;

  private final String[] dependentTables;

  /**
   * The base database table.
   */
  private final String baseTable;
  private final String baseTableAsOf;
  private final String baseTableVersionsBetween;
  private final boolean historySupport;

  private final BeanProperty softDeleteProperty;
  private final boolean softDelete;

  private final String draftTable;

  /**
   * DB table comment.
   */
  private final String dbComment;

  /**
   * Set to true if read auditing is on for this bean type.
   */
  private final boolean readAuditing;

  private final boolean draftable;

  private final boolean draftableElement;

  private final BeanProperty unmappedJson;

  private final BeanProperty tenant;

  private final BeanProperty draft;

  private final BeanProperty draftDirty;

  /**
   * Map of BeanProperty Linked so as to preserve order.
   */
  protected final LinkedHashMap<String, BeanProperty> propMap;

  /**
   * Map of DB column to property path (for nativeSql mapping).
   */
  private final Map<String, String> columnPath = new HashMap<>();

  /**
   * Map of related table to assoc property (for nativeSql mapping).
   */
  private final Map<String, BeanPropertyAssoc<?>> tablePath = new HashMap<>();

  /**
   * The type of bean this describes.
   */
  private final Class<T> beanType;

  protected final Class<?> rootBeanType;

  /**
   * This is not sent to a remote client.
   */
  private final BeanDescriptorMap owner;


  private final String[] properties;

  /**
   * Intercept pre post on insert,update, and delete .
   */
  private volatile BeanPersistController persistController;

  private final BeanPostLoad beanPostLoad;
  private final BeanPostConstructListener beanPostConstructListener;

  /**
   * Listens for post commit insert update and delete events.
   */
  private volatile BeanPersistListener persistListener;

  private final BeanQueryAdapter queryAdapter;

  /**
   * If set overrides the find implementation. Server side only.
   */
  private final BeanFindController beanFinder;

  /**
   * Used for fine grain filtering for the change log.
   */
  private final ChangeLogFilter changeLogFilter;

  /**
   * The table joins for this bean.
   */
  private final TableJoin[] derivedTableJoins;

  /**
   * Inheritance information. Server side only.
   */
  protected final InheritInfo inheritInfo;

  /**
   * Derived list of properties that make up the unique id.
   */
  protected final BeanProperty idProperty;

  private final int idPropertyIndex;

  /**
   * Derived list of properties that are used for version concurrency checking.
   */
  private final BeanProperty versionProperty;

  private final int versionPropertyIndex;

  private final BeanProperty whenModifiedProperty;

  private final BeanProperty whenCreatedProperty;

  /**
   * Properties that are initialised in the constructor need to be 'unloaded' to support partial object queries.
   */
  private volatile int[] unloadProperties;

  /**
   * Properties local to this type (not from a super type).
   */
  private final BeanProperty[] propertiesLocal;

  /**
   * Scalar mutable properties (need to dirty check on update).
   */
  private final BeanProperty[] propertiesMutable;


  private final BeanPropertyAssocOne<?> unidirectional;

  /**
   * list of properties that are Lists/Sets/Maps (Derived).
   */
  private final BeanProperty[] propertiesNonMany;
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

  //private final BeanPropertyAssocOne<?>[] propertiesOneExported;
  private final BeanPropertyAssocOne<?>[] propertiesOneExportedSave;
  private final BeanPropertyAssocOne<?>[] propertiesOneExportedDelete;

  /**
   * list of properties that are embedded beans.
   */
  private final BeanPropertyAssocOne<?>[] propertiesEmbedded;

  /**
   * List of the scalar properties excluding id and secondary table properties.
   */
  private final BeanProperty[] propertiesBaseScalar;

  private final BeanProperty[] propertiesTransient;

  /**
   * All non transient properties excluding the id properties.
   */
  private final BeanProperty[] propertiesNonTransient;
  protected final BeanProperty[] propertiesIndex;
  private final BeanProperty[] propertiesGenInsert;
  private final BeanProperty[] propertiesGenUpdate;

  /**
   * The bean class name or the table name for MapBeans.
   */
  private final String fullName;

  /**
   * Flag used to determine if saves can be skipped.
   */
  private boolean saveRecurseSkippable;

  /**
   * Flag used to determine if deletes can be skipped.
   */
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

  /**
   * If true then only changed properties get updated.
   */
  private final boolean updateChangesOnly;

  private final boolean cacheSharableBeans;

  private final String docStoreQueueId;

  private final BeanDescriptorDraftHelp<T> draftHelp;
  private final BeanDescriptorCacheHelp<T> cacheHelp;
  private final BeanDescriptorJsonHelp<T> jsonHelp;
  private DocStoreBeanAdapter<T> docStoreAdapter;
  private DocumentMapping docMapping;
  private boolean docStoreEmbeddedInvalidation;

  private final String defaultSelectClause;

  private SpiEbeanServer ebeanServer;

  /**
   * Construct the BeanDescriptor.
   */
  public BeanDescriptor(BeanDescriptorMap owner, DeployBeanDescriptor<T> deploy) {

    this.owner = owner;
    this.serverName = owner.getServerName();
    this.entityType = deploy.getEntityType();
    this.properties = deploy.getProperties();
    this.name = InternString.intern(deploy.getName());
    this.baseTableAlias = "t0";
    this.fullName = InternString.intern(deploy.getFullName());

    this.beanType = deploy.getBeanType();
    this.rootBeanType = PersistenceContextUtil.root(beanType);
    this.prototypeEntityBean = createPrototypeEntityBean(beanType);

    this.namedQuery = deploy.getNamedQuery();
    this.namedRawSql = deploy.getNamedRawSql();
    this.inheritInfo = deploy.getInheritInfo();

    this.beanFinder = deploy.getBeanFinder();
    this.persistController = deploy.getPersistController();
    this.persistListener = deploy.getPersistListener();
    this.beanPostConstructListener = deploy.getPostConstructListener();
    this.beanPostLoad = deploy.getPostLoad();
    this.queryAdapter = deploy.getQueryAdapter();
    this.changeLogFilter = deploy.getChangeLogFilter();

    this.defaultSelectClause = deploy.getDefaultSelectClause();
    this.idType = deploy.getIdType();
    this.idTypePlatformDefault = deploy.isIdTypePlatformDefault();
    this.idGenerator = deploy.getIdGenerator();
    this.sequenceName = deploy.getSequenceName();
    this.sequenceInitialValue = deploy.getSequenceInitialValue();
    this.sequenceAllocationSize = deploy.getSequenceAllocationSize();
    this.selectLastInsertedId = deploy.getSelectLastInsertedId();
    this.concurrencyMode = deploy.getConcurrencyMode();
    this.updateChangesOnly = deploy.isUpdateChangesOnly();
    this.indexDefinitions = deploy.getIndexDefinitions();

    this.readAuditing = deploy.isReadAuditing();
    this.draftable = deploy.isDraftable();
    this.draftableElement = deploy.isDraftableElement();
    this.historySupport = deploy.isHistorySupport();
    this.draftTable = deploy.getDraftTable();
    this.baseTable = InternString.intern(deploy.getBaseTable());
    this.baseTableAsOf = deploy.getBaseTableAsOf();
    this.baseTableVersionsBetween = deploy.getBaseTableVersionsBetween();
    this.dependentTables = deploy.getDependentTables();
    this.dbComment = deploy.getDbComment();
    this.autoTunable = EntityType.ORM == entityType && (beanFinder == null);

    // helper object used to derive lists of properties
    DeployBeanPropertyLists listHelper = new DeployBeanPropertyLists(owner, this, deploy);

    this.softDeleteProperty = listHelper.getSoftDeleteProperty();
    this.softDelete = (softDeleteProperty != null);
    this.idProperty = listHelper.getId();
    this.versionProperty = listHelper.getVersionProperty();
    this.unmappedJson = listHelper.getUnmappedJson();
    this.tenant = listHelper.getTenant();
    this.draft = listHelper.getDraft();
    this.draftDirty = listHelper.getDraftDirty();
    this.propMap = listHelper.getPropertyMap();
    this.propertiesTransient = listHelper.getTransients();
    this.propertiesNonTransient = listHelper.getNonTransients();
    this.propertiesBaseScalar = listHelper.getBaseScalar();
    this.propertiesEmbedded = listHelper.getEmbedded();
    this.propertiesLocal = listHelper.getLocal();
    this.propertiesMutable = listHelper.getMutable();
    this.unidirectional = listHelper.getUnidirectional();
    this.propertiesOne = listHelper.getOnes();
    this.propertiesOneExportedSave = listHelper.getOneExportedSave();
    this.propertiesOneExportedDelete = listHelper.getOneExportedDelete();
    this.propertiesOneImported = listHelper.getOneImported();
    this.propertiesOneImportedSave = listHelper.getOneImportedSave();
    this.propertiesOneImportedDelete = listHelper.getOneImportedDelete();

    this.propertiesMany = listHelper.getMany();
    this.propertiesNonMany = listHelper.getNonMany();
    this.propertiesManySave = listHelper.getManySave();
    this.propertiesManyDelete = listHelper.getManyDelete();
    this.propertiesManyToMany = listHelper.getManyToMany();
    this.propertiesGenInsert = listHelper.getGeneratedInsert();
    this.propertiesGenUpdate = listHelper.getGeneratedUpdate();

    this.derivedTableJoins = listHelper.getTableJoin();

    boolean noRelationships = propertiesOne.length + propertiesMany.length == 0;

    this.cacheSharableBeans = noRelationships && deploy.getCacheOptions().isReadOnly();
    this.cacheHelp = new BeanDescriptorCacheHelp<>(this, owner.getCacheManager(), deploy.getCacheOptions(), cacheSharableBeans, propertiesOneImported);
    this.jsonHelp = new BeanDescriptorJsonHelp<>(this);
    this.draftHelp = new BeanDescriptorDraftHelp<>(this);

    this.docStoreAdapter = owner.createDocStoreBeanAdapter(this, deploy);
    this.docStoreQueueId = docStoreAdapter.getQueueId();

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
    if (Modifier.isAbstract(beanType.getModifiers())) {
      this.idPropertyIndex = -1;
      this.versionPropertyIndex = -1;
      this.unloadProperties = new int[0];
      this.propertiesIndex = new BeanProperty[0];

    } else {
      EntityBeanIntercept ebi = prototypeEntityBean._ebean_getIntercept();
      this.idPropertyIndex = (idProperty == null) ? -1 : ebi.findProperty(idProperty.getName());
      this.versionPropertyIndex = (versionProperty == null) ? -1 : ebi.findProperty(versionProperty.getName());
      //this.unloadProperties = derivePropertiesToUnload(prototypeEntityBean);
      this.propertiesIndex = new BeanProperty[ebi.getPropertyLength()];
      for (int i = 0; i < propertiesIndex.length; i++) {
        propertiesIndex[i] = propMap.get(ebi.getProperty(i));
      }
    }
  }

  /**
   * Derive an array of property positions for properties that are initialised in the constructor.
   * These properties need to be unloaded when populating beans for queries.
   */
  private int[] derivePropertiesToUnload(EntityBean prototypeEntityBean) {

    boolean[] loaded = prototypeEntityBean._ebean_getIntercept().getLoaded();
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
  private EntityBean createPrototypeEntityBean(Class<T> beanType) {
    if (Modifier.isAbstract(beanType.getModifiers())) {
      return null;
    }
    try {
      try {
        Constructor<T> ctor = beanType.getConstructor(ConstructorMarker.class);
        return (EntityBean) ctor.newInstance(new Object[1]);
      } catch (NoSuchMethodError e) {
        return (EntityBean) beanType.newInstance();
      }
    } catch (Exception e) {
      throw new IllegalStateException("Error trying to create the prototypeEntityBean for " + beanType, e);
    }
  }

  /**
   * Return the ServerConfig.
   */
  public ServerConfig getServerConfig() {
    return owner.getServerConfig();
  }

  /**
   * Set the server. Primarily so that the Many's can lazy load.
   */
  public void setEbeanServer(SpiEbeanServer ebeanServer) {
    this.ebeanServer = ebeanServer;
    for (BeanPropertyAssocMany<?> aPropertiesMany : propertiesMany) {
      // used for creating lazy loading lists etc
      aPropertiesMany.setLoader(ebeanServer);
    }
  }

  /**
   * Return the EbeanServer instance that owns this BeanDescriptor.
   */
  public SpiEbeanServer getEbeanServer() {
    return ebeanServer;
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
  public EntityType getEntityType() {
    return entityType;
  }

  public String[] getProperties() {
    return properties;
  }

  /**
   * Initialise the Id properties first.
   * <p>
   * These properties need to be initialised prior to the association properties
   * as they are used to get the imported and exported properties.
   * </p>
   *
   * @param withHistoryTables map populated if @History is supported on this entity bean
   */
  public void initialiseId(Map<String, String> withHistoryTables, Map<String, String> draftTables) {

    if (logger.isTraceEnabled()) {
      logger.trace("BeanDescriptor initialise " + fullName);
    }

    if (draftable) {
      draftTables.put(baseTable, draftTable);
    }
    if (historySupport) {
      // add mapping (used to swap out baseTable for asOf queries)
      withHistoryTables.put(baseTable, baseTableAsOf);
    }

    if (inheritInfo != null) {
      inheritInfo.setDescriptor(this);
    }

    if (isEmbedded()) {
      // initialise all the properties
      for (BeanProperty prop : propertiesAll()) {
        prop.initialise();
      }
    } else {
      // initialise just the Id properties
      if (idProperty != null) {
        idProperty.initialise();
      }
    }
  }

  /**
   * Initialise the exported and imported parts for associated properties.
   *
   * @param asOfTableMap   the map of base tables to associated 'with history' tables
   * @param asOfViewSuffix the suffix added to the table name to derive the 'with history' view name
   * @param draftTableMap  the map of base tables to associated 'draft' tables.
   */
  public void initialiseOther(Map<String, String> asOfTableMap, String asOfViewSuffix, Map<String, String> draftTableMap) {

    for (BeanPropertyAssocMany<?> aPropertiesManyToMany1 : propertiesManyToMany) {
      // register associated draft table for M2M intersection
      aPropertiesManyToMany1.registerDraftIntersectionTable(draftTableMap);
    }

    if (historySupport) {
      // history support on this bean so check all associated intersection tables
      // and if they are not excluded register the associated 'with history' table
      for (BeanPropertyAssocMany<?> aPropertiesManyToMany : propertiesManyToMany) {
        // register associated history table for M2M intersection
        if (!aPropertiesManyToMany.isExcludedFromHistory()) {
          TableJoin intersectionTableJoin = aPropertiesManyToMany.getIntersectionTableJoin();
          String intersectionTableName = intersectionTableJoin.getTable();
          asOfTableMap.put(intersectionTableName, intersectionTableName + asOfViewSuffix);
        }
      }
    }

    if (!isEmbedded()) {
      // initialise all the non-id properties
      for (BeanProperty prop : propertiesAll()) {
        if (!prop.isId()) {
          prop.initialise();
        }
        prop.registerColumn(this, null);
      }
    }

    if (unidirectional != null) {
      unidirectional.initialise();
    }

    idBinder.initialise();
    idBinderInLHSSql = idBinder.getBindIdInSql(baseTableAlias);
    idBinderIdSql = idBinder.getBindIdSql(baseTableAlias);
    String idBinderInLHSSqlNoAlias = idBinder.getBindIdInSql(null);
    String idEqualsSql = idBinder.getBindIdSql(null);

    deleteByIdSql = "delete from " + baseTable + " where " + idEqualsSql;
    whereIdInSql = " where " + idBinderInLHSSqlNoAlias + " ";
    deleteByIdInSql = "delete from " + baseTable + whereIdInSql;

    if (softDelete) {
      softDeleteByIdSql = "update " + baseTable + " set " + getSoftDeleteDbSet() + " where " + idEqualsSql;
      softDeleteByIdInSql = "update " + baseTable + " set " + getSoftDeleteDbSet() + " where " + idBinderInLHSSqlNoAlias + " ";
    } else {
      softDeleteByIdSql = null;
      softDeleteByIdInSql = null;
    }
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
  public void initLast() {
    docStoreEmbeddedInvalidation = docStoreAdapter.hasEmbeddedInvalidation();
  }

  /**
   * Initialise the document mapping.
   */
  @SuppressWarnings("unchecked")
  public void initialiseDocMapping() {
    for (BeanPropertyAssocMany<?> aPropertiesMany : propertiesMany) {
      aPropertiesMany.initialisePostTarget();
    }
    if (inheritInfo != null && !inheritInfo.isRoot()) {
      docStoreAdapter = (DocStoreBeanAdapter<T>) inheritInfo.getRoot().desc().docStoreAdapter();
    }
    docMapping = docStoreAdapter.createDocMapping();
    docStoreAdapter.registerPaths();
    cacheHelp.deriveNotifyFlags();
  }

  public void initInheritInfo() {
    if (inheritInfo != null) {
      // need to check every BeanDescriptor in the inheritance hierarchy
      if (saveRecurseSkippable) {
        saveRecurseSkippable = inheritInfo.isSaveRecurseSkippable();
      }
      if (deleteRecurseSkippable) {
        deleteRecurseSkippable = inheritInfo.isDeleteRecurseSkippable();
      }
    }
  }

  public void merge(EntityBean bean, EntityBean existing) {

    EntityBeanIntercept fromEbi = bean._ebean_getIntercept();
    EntityBeanIntercept toEbi = existing._ebean_getIntercept();

    int propertyLength = toEbi.getPropertyLength();
    String[] names = getProperties();

    for (int i = 0; i < propertyLength; i++) {

      if (fromEbi.isLoadedProperty(i)) {
        BeanProperty property = getBeanProperty(names[i]);
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
   * Return the ReadAuditLogger for logging read audit events.
   */
  public ReadAuditLogger getReadAuditLogger() {
    return ebeanServer.getReadAuditLogger();
  }

  /**
   * Return the ReadAuditPrepare for preparing read audit events prior to logging.
   */
  public ReadAuditPrepare getReadAuditPrepare() {
    return ebeanServer.getReadAuditPrepare();
  }

  public boolean isChangeLog() {
    return changeLogFilter != null;
  }

  /**
   * Return true if this request should be included in the change log.
   */
  public BeanChange getChangeLogBean(PersistRequestBean<T> request) {
    switch (request.getType()) {
      case INSERT:
        return changeLogFilter.includeInsert(request) ? insertBeanChange(request) : null;
      case UPDATE:
      case SOFT_DELETE:
        return changeLogFilter.includeUpdate(request) ? updateBeanChange(request) : null;
      case DELETE:
        return changeLogFilter.includeDelete(request) ? deleteBeanChange(request) : null;
      default:
        throw new IllegalStateException("Unhandled request type " + request.getType());
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
    return beanChange(ChangeType.DELETE, request.getBeanId(), null, null);
  }

  /**
   * Return the bean change for an update generating 'new values' and 'old values' in JSON form.
   */
  private BeanChange updateBeanChange(PersistRequestBean<T> request) {

    try {
      BeanChangeJson changeJson = new BeanChangeJson(this, request.isStatelessUpdate());
      request.getEntityBeanIntercept().addDirtyPropertyValues(changeJson);
      changeJson.flush();

      return beanChange(ChangeType.UPDATE, request.getBeanId(), changeJson.newJson(), changeJson.oldJson());

    } catch (RuntimeException e) {
      logger.error("Failed to write ChangeLog entry for update", e);
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

      jsonWriteForInsert(jsonWriter, request.getEntityBean());
      jsonWriter.gen().flush();

      return beanChange(ChangeType.INSERT, request.getBeanId(), writer.toString(), null);

    } catch (IOException e) {
      logger.error("Failed to write ChangeLog entry for insert", e);
      return null;
    }
  }

  SpiJsonWriter createJsonWriter(StringWriter writer) {
    return ebeanServer.jsonExtended().createJsonWriter(writer);
  }

  /**
   * Populate the diff for inserts with flattened non-null property values.
   */
  protected void jsonWriteForInsert(SpiJsonWriter jsonWriter, EntityBean newBean) throws IOException {
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

  public SqlUpdate deleteById(Object id, List<Object> idList, boolean softDelete) {
    if (id != null) {
      return deleteById(id, softDelete);
    } else {
      return deleteByIdList(idList, softDelete);
    }
  }

  /**
   * Return the "where id in" sql (for use with UpdateQuery).
   */
  public String getWhereIdInSql() {
    return whereIdInSql;
  }

  /**
   * Return the "delete by id" sql.
   */
  public String getDeleteByIdInSql() {
    return deleteByIdInSql;
  }

  /**
   * Return SQL that can be used to delete a list of Id's without any optimistic
   * concurrency checking.
   */
  private SqlUpdate deleteByIdList(List<Object> idList, boolean softDelete) {

    String baseSql = softDelete ? softDeleteByIdInSql : deleteByIdInSql;
    StringBuilder sb = new StringBuilder(baseSql);
    String inClause = idBinder.getIdInValueExprDelete(idList.size());
    sb.append(inClause);

    DefaultSqlUpdate delete = new DefaultSqlUpdate(sb.toString());
    idBinder.addIdInBindValues(delete, idList);

    return delete;
  }

  /**
   * Return SQL that can be used to delete by Id without any optimistic
   * concurrency checking.
   */
  private SqlUpdate deleteById(Object id, boolean softDelete) {

    String baseSql = softDelete ? softDeleteByIdSql : deleteByIdSql;
    DefaultSqlUpdate sqlDelete = new DefaultSqlUpdate(baseSql);

    Object[] bindValues = idBinder.getBindValues(id);
    for (Object bindValue : bindValues) {
      sqlDelete.addParameter(bindValue);
    }

    return sqlDelete;
  }

  /**
   * Add objects to ElPropertyDeploy etc. These are used so that expressions on
   * foreign keys don't require an extra join.
   */
  public void add(BeanFkeyProperty fkey) {
    elDeployCache.put(fkey.getName(), fkey);
  }

  public void initialiseFkeys() {
    for (BeanPropertyAssocOne<?> aPropertiesOneImported : propertiesOneImported) {
      if (!aPropertiesOneImported.isFormula()) {
        aPropertiesOneImported.addFkey();
      }
    }
  }

  /**
   * Return the cache options.
   */
  public CacheOptions getCacheOptions() {
    return cacheHelp.getCacheOptions();
  }

  /**
   * Return the Encrypt key given the BeanProperty.
   */
  public EncryptKey getEncryptKey(BeanProperty p) {
    return owner.getEncryptKey(baseTable, p.getDbColumn());
  }

  /**
   * Return the Encrypt key given the table and column name.
   */
  public EncryptKey getEncryptKey(String tableName, String columnName) {
    return owner.getEncryptKey(tableName, columnName);
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
  public String getDefaultSelectClause() {
    return defaultSelectClause;
  }

  /**
   * Return true if this object is the root level object in its entity
   * inheritance.
   */
  public boolean isInheritanceRoot() {
    return inheritInfo == null || inheritInfo.isRoot();
  }

  /**
   * Return true if this type maps to a root type of a doc store document (not embedded or ignored).
   */
  @Override
  public boolean isDocStoreMapped() {
    return docStoreAdapter.isMapped();
  }

  /**
   * Return true if this bean type has embedded doc store invalidation.
   */
  public boolean isDocStoreEmbeddedInvalidation() {
    return docStoreEmbeddedInvalidation;
  }

  /**
   * Return the queueId used to uniquely identify this type when queuing an index updateAdd.
   */
  @Override
  public String getDocStoreQueueId() {
    return docStoreQueueId;
  }

  @Override
  public DocumentMapping getDocMapping() {
    return docMapping;
  }

  /**
   * Return the doc store helper for this bean type.
   */
  @Override
  public BeanDocType<T> docStore() {
    return docStoreAdapter;
  }

  /**
   * Return doc store adapter for internal use for processing persist requests.
   */
  public DocStoreBeanAdapter<T> docStoreAdapter() {
    return docStoreAdapter;
  }

  /**
   * Build the Document mapping recursively with the given prefix relative to the root of the document.
   */
  public void docStoreMapping(final DocMappingBuilder mapping, final String prefix) {

    if (prefix != null && idProperty != null) {
      // id property not included in the
      idProperty.docStoreMapping(mapping, prefix);
    }

    if (inheritInfo != null) {
      String discCol = inheritInfo.getDiscriminatorColumn();
      if (Types.VARCHAR == inheritInfo.getDiscriminatorType()) {
        mapping.add(new DocPropertyMapping(discCol, DocPropertyType.ENUM));
      } else {
        mapping.add(new DocPropertyMapping(discCol, DocPropertyType.INTEGER));
      }
    }
    for (BeanProperty prop : propertiesNonTransient) {
      prop.docStoreMapping(mapping, prefix);
    }
    if (inheritInfo != null) {
      inheritInfo.visitChildren(inheritInfo1 -> {
        for (BeanProperty localProperty : inheritInfo1.localProperties()) {
          localProperty.docStoreMapping(mapping, prefix);
        }
      });
    }
  }

  /**
   * Return the root bean type if part of inheritance hierarchy.
   */
  @Override
  public BeanType<?> root() {
    if (inheritInfo != null && !inheritInfo.isRoot()) {
      return inheritInfo.getRoot().desc();
    }
    return this;
  }

  /**
   * Return the full name taking into account inheritance.
   */
  public String rootName() {
    if (inheritInfo != null && !inheritInfo.isRoot()) {
      return inheritInfo.getRoot().desc().getName();
    }
    return name;
  }

  /**
   * Return the named ORM query.
   */
  public String getNamedQuery(String name) {
    return namedQuery.get(name);
  }

  /**
   * Return the named RawSql query.
   */
  public RawSql getNamedRawSql(String named) {
    return namedRawSql.get(named);
  }

  /**
   * Return the type of DocStoreMode that should occur for this type of persist request
   * given the transactions requested mode.
   */
  public DocStoreMode getDocStoreMode(PersistRequest.Type persistType, DocStoreMode txnMode) {
    return docStoreAdapter.getMode(persistType, txnMode);
  }

  public void docStoreInsert(Object idValue, PersistRequestBean<T> persistRequest, DocStoreUpdateContext bulkUpdate) throws IOException {
    docStoreAdapter.insert(idValue, persistRequest, bulkUpdate);
  }

  public void docStoreUpdate(Object idValue, PersistRequestBean<T> persistRequest, DocStoreUpdateContext bulkUpdate) throws IOException {
    docStoreAdapter.update(idValue, persistRequest, bulkUpdate);
  }

  /**
   * Check if this update invalidates an embedded part of a doc store document.
   */
  public void docStoreUpdateEmbedded(PersistRequestBean<T> request, DocStoreUpdates docStoreUpdates) {
    docStoreAdapter.updateEmbedded(request, docStoreUpdates);
  }

  public void docStoreDeleteById(Object idValue, DocStoreUpdateContext txn) throws IOException {
    docStoreAdapter.deleteById(idValue, txn);
  }

  public T publish(T draftBean, T liveBean) {
    return draftHelp.publish(draftBean, liveBean);
  }

  /**
   * Reset properties on the draft bean based on @DraftDirty and @DraftReset.
   */
  public boolean draftReset(T draftBean) {
    return draftHelp.draftReset(draftBean);
  }

  /**
   * Return the draft dirty boolean property or null if there is not one assigned to this bean type.
   */
  public BeanProperty getDraftDirty() {
    return draftDirty;
  }

  /**
   * Prepare the query for multi-tenancy check for document store only use.
   */
  public void prepareQuery(SpiQuery<T> query) {
    if (tenant != null && !query.isNativeSql()) {
      Object tenantId = ebeanServer.currentTenantId();
      if (tenantId != null) {
        query.where().eq(tenant.getName(), tenantId);
      }
    }
    if (isDocStoreOnly()) {
      query.setUseDocStore(true);
    }
  }

  /**
   * Return true if there is bean or query caching for this type.
   */
  public boolean isCaching() {
    return cacheHelp.isCaching();
  }

  /**
   * Return true if there is currently bean caching for this type of bean.
   */
  @Override
  public boolean isBeanCaching() {
    return cacheHelp.isBeanCaching();
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
  public boolean isCacheNotify(PersistRequest.Type type, boolean publish) {
    if (draftable && !publish) {
      // no caching when editing draft beans
      return false;
    }
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
  public void queryCachePut(Object id, Object queryResult) {
    cacheHelp.queryCachePut(id, queryResult);
  }

  /**
   * Add a query cache clear into the changeSet.
   */
  public void queryCacheClear(CacheChangeSet changeSet) {
    cacheHelp.queryCacheClear(changeSet);
  }

  /**
   * Try to load the beanCollection from cache return true if successful.
   */
  public boolean cacheManyPropLoad(BeanPropertyAssocMany<?> many, BeanCollection<?> bc, Object parentId, Boolean readOnly) {
    return cacheHelp.manyPropLoad(many, bc, parentId, readOnly);
  }

  /**
   * Put the beanCollection into the cache.
   */
  public void cacheManyPropPut(BeanPropertyAssocMany<?> many, BeanCollection<?> bc, Object parentId) {
    cacheHelp.manyPropPut(many, bc, parentId);
  }

  /**
   * Update the bean collection entry in the cache.
   */
  public void cacheManyPropPut(String name, Object parentId, CachedManyIds entry) {
    cacheHelp.cachePutManyIds(parentId, name, entry);
  }

  public void cacheManyPropRemove(String propertyName, Object parentId) {
    cacheHelp.manyPropRemove(propertyName, parentId);
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
    return cacheHelp.beanCacheGet(id, readOnly, context);
  }

  /**
   * Remove a bean from the cache given its Id.
   */
  public void cacheHandleDeleteById(Object id) {
    cacheHelp.beanCacheRemove(id);
  }

  /**
   * Returns true if it managed to populate/load the bean from the cache.
   */
  public boolean cacheBeanLoad(EntityBean bean, EntityBeanIntercept ebi, Object id, PersistenceContext context) {
    return cacheHelp.beanCacheLoad(bean, ebi, id, context);
  }

  /**
   * Returns true if it managed to populate/load the bean from the cache.
   */
  public boolean cacheBeanLoad(EntityBeanIntercept ebi, PersistenceContext context) {
    EntityBean bean = ebi.getOwner();
    Object id = getId(bean);
    return cacheBeanLoad(bean, ebi, id, context);
  }

  /**
   * Try to hit the cache using the natural key.
   */
  public Object cacheNaturalKeyIdLookup(SpiQuery<T> query) {
    return cacheHelp.naturalKeyIdLookup(query);
  }

  public void cacheNaturalKeyPut(Object id, Object newKey) {
    cacheHelp.cacheNaturalKeyPut(id, newKey);
  }

  /**
   * Invalidate parts of cache due to SqlUpdate or external modification etc.
   */
  public void cacheHandleBulkUpdate(TableIUD tableIUD) {
    cacheHelp.handleBulkUpdate(tableIUD);
  }

  /**
   * Handle a delete by id request adding an cache change into the changeSet.
   */
  public void cacheHandleDeleteById(Object id, CacheChangeSet changeSet) {
    cacheHelp.handleDelete(id, changeSet);
  }

  /**
   * Remove a bean from the cache given its Id.
   */
  public void cacheHandleDelete(Object id, PersistRequestBean<T> deleteRequest, CacheChangeSet changeSet) {
    cacheHelp.handleDelete(id, deleteRequest, changeSet);
  }

  /**
   * Add the insert changes to the changeSet.
   */
  public void cacheHandleInsert(PersistRequestBean<T> insertRequest, CacheChangeSet changeSet) {
    cacheHelp.handleInsert(insertRequest, changeSet);
  }

  /**
   * Add the update to the changeSet.
   */
  public void cacheHandleUpdate(Object id, PersistRequestBean<T> updateRequest, CacheChangeSet changeSet) {
    cacheHelp.handleUpdate(id, updateRequest, changeSet);
  }

  /**
   * Apply the update to the cache.
   */
  public void cacheBeanUpdate(Object id, Map<String, Object> changes, boolean updateNaturalKey, long version) {
    cacheHelp.cacheBeanUpdate(id, changes, updateNaturalKey, version);
  }

  /**
   * Prepare the read audit of a findFutureList() query.
   */
  public void readAuditFutureList(SpiQuery<T> spiQuery) {
    if (isReadAuditing()) {
      ReadEvent event = new ReadEvent(fullName);
      // prepare in the foreground thread while we have the user context
      // information (query is processed/executed later in bg thread)
      readAuditPrepare(event);
      spiQuery.setFutureFetchAudit(event);
    }
  }

  /**
   * Write a bean read to the read audit log.
   */
  public void readAuditBean(String queryKey, String bindLog, Object bean) {
    ReadEvent event = new ReadEvent(fullName, queryKey, bindLog, getIdForJson(bean));
    readAuditPrepare(event);
    getReadAuditLogger().auditBean(event);
  }

  private void readAuditPrepare(ReadEvent event) {
    ReadAuditPrepare prepare = getReadAuditPrepare();
    if (prepare != null) {
      prepare.prepare(event);
    }
  }

  /**
   * Write a many bean read to the read audit log.
   */
  public void readAuditMany(String queryKey, String bindLog, List<Object> ids) {
    ReadEvent event = new ReadEvent(fullName, queryKey, bindLog, ids);
    readAuditPrepare(event);
    getReadAuditLogger().auditMany(event);
  }

  /**
   * Write a futureList many read to the read audit log.
   */
  public void readAuditFutureMany(ReadEvent event) {
    // this has already been prepared (in foreground thread)
    getReadAuditLogger().auditMany(event);
  }

  /**
   * Return the base table alias. This is always the first letter of the bean name.
   */
  public String getBaseTableAlias() {
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

  public DeployPropertyParser createDeployPropertyParser() {
    return new DeployPropertyParser(this);
  }

  /**
   * Convert the logical orm update statement into sql by converting the bean
   * properties and bean name to database columns and table.
   */
  public String convertOrmUpdateToSql(String ormUpdateStatement) {
    return new DeployUpdateParser(this).parse(ormUpdateStatement);
  }

  @Override
  public List<MetaQueryPlanStatistic> collectQueryPlanStatistics(boolean reset) {
    return collectQueryPlanStatisticsInternal(reset, false);
  }

  @Override
  public List<MetaQueryPlanStatistic> collectAllQueryPlanStatistics(boolean reset) {
    return collectQueryPlanStatisticsInternal(reset, false);
  }

  public List<MetaQueryPlanStatistic> collectQueryPlanStatisticsInternal(boolean reset, boolean collectAll) {
    List<MetaQueryPlanStatistic> list = new ArrayList<>(queryPlanCache.size());
    for (CQueryPlan queryPlan : queryPlanCache.values()) {
      Snapshot snapshot = queryPlan.getSnapshot(reset);
      if (collectAll || snapshot.getExecutionCount() > 0) {
        list.add(snapshot);
      }
    }
    return list;
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
   * Execute the postLoad if a BeanPostLoad exists for this bean.
   */
  public void postLoad(Object bean) {
    if (beanPostLoad != null) {
      beanPostLoad.postLoad(bean);
    }
  }

  public CQueryPlan getQueryPlan(CQueryPlanKey key) {
    return queryPlanCache.get(key);
  }

  public void putQueryPlan(CQueryPlanKey key, CQueryPlan plan) {
    queryPlanCache.put(key, plan);
  }

  /**
   * Get a UpdatePlan for a given hash.
   */
  public SpiUpdatePlan getUpdatePlan(String key) {
    return updatePlanCache.get(key);
  }

  /**
   * Add a UpdatePlan to the cache with a given hash.
   */
  public void putUpdatePlan(String key, SpiUpdatePlan plan) {
    updatePlanCache.put(key, plan);
  }

  /**
   * Return a Sql update statement to set the importedId value (deferred execution).
   */
  public String getUpdateImportedIdSql(ImportedId prop) {
    return "update " + baseTable + " set " + prop.importedIdClause() + " where " + idBinder.getBindIdSql(null);
  }

  /**
   * Return true if updates should only include changed properties. Otherwise
   * all loaded properties are included in the update.
   */
  public boolean isUpdateChangesOnly() {
    return updateChangesOnly;
  }

  /**
   * Return true if save does not recurse to other beans. That is return true if
   * there are no assoc one or assoc many beans that cascade save.
   */
  public boolean isSaveRecurseSkippable() {
    return saveRecurseSkippable;
  }

  /**
   * Return true if delete does not recurse to other beans. That is return true
   * if there are no assoc one or assoc many beans that cascade delete.
   */
  public boolean isDeleteRecurseSkippable() {
    return deleteRecurseSkippable;
  }

  /**
   * Return true if delete can use a single SQL statement.
   * <p>
   * This implies cascade delete does not continue depth wise and that this is no
   * associated L2 bean caching.
   */
  public boolean isDeleteByStatement() {
    return deleteRecurseSkippable && !isBeanCaching();
  }

  /**
   * Return the 'when modified' property if there is one defined.
   */
  @Override
  public BeanProperty getWhenModifiedProperty() {
    return whenModifiedProperty;
  }

  /**
   * Return the 'when created' property if there is one defined.
   */
  @Override
  public BeanProperty getWhenCreatedProperty() {
    return whenCreatedProperty;
  }

  /**
   * Find a property annotated with @WhenCreated or @CreatedTimestamp.
   */
  private BeanProperty findWhenCreatedProperty() {

    for (BeanProperty aPropertiesBaseScalar : propertiesBaseScalar) {
      if (aPropertiesBaseScalar.isGeneratedWhenCreated()) {
        return aPropertiesBaseScalar;
      }
    }
    return null;
  }

  /**
   * Find a property annotated with @WhenModified or @UpdatedTimestamp.
   */
  private BeanProperty findWhenModifiedProperty() {

    for (BeanProperty aPropertiesBaseScalar : propertiesBaseScalar) {
      if (aPropertiesBaseScalar.isGeneratedWhenModified()) {
        return aPropertiesBaseScalar;
      }
    }
    return null;
  }

  /**
   * Return the many property included in the query or null if one is not.
   */
  public BeanPropertyAssocMany<?> getManyProperty(SpiQuery<?> query) {

    OrmQueryDetail detail = query.getDetail();
    for (BeanPropertyAssocMany<?> aPropertiesMany : propertiesMany) {
      if (detail.includesPath(aPropertiesMany.getName())) {
        return aPropertiesMany;
      }
    }

    return null;
  }

  /**
   * Return a raw expression for 'where parent id in ...' clause.
   */
  public String getParentIdInExpr(int parentIdSize, String rawWhere) {
    String inClause = idBinder.getIdInValueExpr(parentIdSize);
    return idBinder.isIdInExpandedForm() ? inClause : rawWhere + inClause;
  }

  /**
   * Return the IdBinder which is helpful for handling the various types of Id.
   */
  public IdBinder getIdBinder() {
    return idBinder;
  }

  /**
   * Return the sql for binding an id. This is the columns with table alias that
   * make up the id.
   */
  public String getIdBinderIdSql() {
    return idBinderIdSql;
  }

  /**
   * Return the sql for binding id's using an IN clause.
   */
  public String getIdBinderInLHSSql() {
    return idBinderInLHSSql;
  }

  /**
   * Bind the idValue to the preparedStatement.
   * <p>
   * This takes care of the various id types such as embedded beans etc.
   * </p>
   */
  public void bindId(DataBind dataBind, Object idValue) throws SQLException {
    idBinder.bindId(dataBind, idValue);
  }

  /**
   * Return the id as an array of scalar bindable values.
   * <p>
   * This 'flattens' any EmbeddedId or multiple Id property cases.
   * </p>
   */
  public Object[] getBindIdValues(Object idValue) {
    return idBinder.getBindValues(idValue);
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
  public EntityBean createEntityBean(boolean isNew) {
    try {
      EntityBean bean = (EntityBean) prototypeEntityBean._ebean_newInstance();

      if (beanPostConstructListener != null) {
        beanPostConstructListener.autowire(bean); // calls all registered listeners
        beanPostConstructListener.postConstruct(bean); // calls first the @PostConstruct method and then the listeners
      }
      
      int[] unloadProperties = this.unloadProperties; 
      if (unloadProperties == null) {
        synchronized (this) {
          if (this.unloadProperties == null) {
            unloadProperties = this.unloadProperties = derivePropertiesToUnload(bean);
          }
        }
      }
      if (unloadProperties.length > 0) {
        // 'unload' any properties initialised in the default constructor
        EntityBeanIntercept ebi = bean._ebean_getIntercept();
        for (int unloadProperty : unloadProperties) {
          ebi.setPropertyUnloaded(unloadProperty);
        }
      }
      if (beanPostConstructListener != null && isNew) {
        beanPostConstructListener.postCreate(bean);
        // if bean is not new, postLoad will be executed later in the bean's lifecycle
      }
      return bean;

    } catch (Exception ex) {
      throw new PersistenceException(ex);
    }
  }

  /**
   * Creates a new entitybean without invoking {@link BeanPostConstructListener#postCreate(Object)}
   */
  public EntityBean createEntityBean() {
    return createEntityBean(false);
  }

  /**
   * Create a reference bean based on the id.
   */
  @SuppressWarnings("unchecked")
  public T createReference(Boolean readOnly, boolean disableLazyLoad, Object id, PersistenceContext pc) {

    if (cacheSharableBeans && !disableLazyLoad && !Boolean.FALSE.equals(readOnly)) {
      CachedBeanData d = cacheHelp.beanCacheGetData(id);
      if (d != null) {
        Object shareableBean = d.getSharableBean();
        if (shareableBean != null) {
          if (isReadAuditing()) {
            readAuditBean("ref", "", shareableBean);
          }
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
        ebi.setBeanLoader(ebeanServer);
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

  /**
   * Return the bean property traversing the object graph and taking into
   * account inheritance.
   */
  public BeanProperty getBeanPropertyFromPath(String path) {
    BeanDescriptor<?> other = this;
    while (true) {

      String[] split = SplitName.splitBegin(path);
      if (split[1] == null) {
        return other._findBeanProperty(split[0]);
      }
      BeanPropertyAssoc<?> assocProp = (BeanPropertyAssoc<?>) other._findBeanProperty(split[0]);
      BeanDescriptor<?> targetDesc = assocProp.getTargetDescriptor();

      path = split[1];
      other = targetDesc;
    }
  }

  @Override
  public BeanType<?> getBeanTypeAtPath(String path) {
    return getBeanDescriptor(path);
  }

  /**
   * Return the BeanDescriptor for a given path of Associated One or Many beans.
   */
  public BeanDescriptor<?> getBeanDescriptor(String path) {
    BeanDescriptor<?> result = this;
    while (true) {
      if (path == null) {
        return result;
      }
      String[] splitBegin = SplitName.splitBegin(path);

      BeanProperty beanProperty = result.findBeanProperty(splitBegin[0]);
      if (beanProperty instanceof BeanPropertyAssoc<?>) {
        BeanPropertyAssoc<?> assocProp = (BeanPropertyAssoc<?>) beanProperty;
        path = splitBegin[1];
        result = assocProp.getTargetDescriptor();

      } else {
        throw new PersistenceException("Invalid path " + path + " from " + result.getFullName());
      }
    }
  }

  /**
   * Return the BeanDescriptor of another bean type.
   */
  public <U> BeanDescriptor<U> getBeanDescriptor(Class<U> otherType) {
    return owner.getBeanDescriptor(otherType);
  }

  /**
   * Return the "shadow" property to support unidirectional relationships.
   * <p>
   * For bidirectional this is a real property on the bean. For unidirectional
   * relationships we have this 'shadow' property which is not externally
   * visible.
   * </p>
   */
  public BeanPropertyAssocOne<?> getUnidirectional() {
    BeanDescriptor<?> other = this;
    while (true) {
      if (other.unidirectional != null) {
        return other.unidirectional;
      }
      if (other.inheritInfo != null && !other.inheritInfo.isRoot()) {
        other = other.inheritInfo.getParent().desc();
        continue;
      }
      return null;
    }
  }

  /**
   * Get a property value from a bean of this type.
   */
  public Object getValue(EntityBean bean, String property) {
    return getBeanProperty(property).getValue(bean);
  }

  /**
   * Return true if this bean type should use IdGeneration.
   * <p>
   * If this is false and the Id is null it is assumed that a database auto
   * increment feature is being used to populate the id.
   * </p>
   */
  public boolean isUseIdGenerator() {
    return idGenerator != null;
  }

  /**
   * Return bean class name.
   */
  public String getDescriptorId() {
    return fullName;
  }

  /**
   * Return the class type this BeanDescriptor describes.
   */
  @Override
  public Class<T> getBeanType() {
    return beanType;
  }

  /**
   * Return the bean class name this descriptor is used for.
   * <p>
   * If this BeanDescriptor is for a table then this returns the table name
   * instead.
   * </p>
   */
  @Override
  public String getFullName() {
    return fullName;
  }

  /**
   * Return the short name of the entity bean.
   */
  @Override
  public String getName() {
    return name;
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
   * Delete a bean from the persistence context (such that we don't fetch it in the same transaction).
   */
  public void contextDeleted(PersistenceContext pc, Object idValue) {
    pc.deleted(rootBeanType, idValue);
  }

  /**
   * Return the Id property name or null if no Id property exists.
   */
  public String getIdName() {
    return (idProperty == null) ? null : idProperty.getName();
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

  @Override
  public Object beanId(Object bean) {
    return getId((EntityBean) bean);
  }

  @Override
  public Object getBeanId(T bean) {
    return getId((EntityBean) bean);
  }

  /**
   * Return the Id value for the bean with embeddedId beans converted into maps.
   * <p>
   * The usage is to provide simple id types for JSON processing (for embeddedId's).
   * </p>
   */
  public Object getIdForJson(Object bean) {
    return idBinder.getIdForJson((EntityBean) bean);
  }

  /**
   * Convert the idValue assuming embeddedId values are Maps.
   * <p>
   * The usage is to provide simple id types for JSON processing (for embeddedId's).
   * </p>
   */
  public Object convertIdFromJson(Object idValue) {
    return idBinder.convertIdFromJson(idValue);
  }

  /**
   * Return the default order by that may need to be added if a many property is
   * included in the query.
   */
  public String getDefaultOrderBy() {
    return idBinder.getDefaultOrderBy();
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
  public void setBeanId(T bean, Object idValue) {
    idBinder.convertSetId(idValue, (EntityBean) bean);
  }

  /**
   * Convert and set the id value.
   * <p>
   * If the bean is not null, the id value is set to the id property of the bean
   * after it has been converted to the correct type.
   * </p>
   */
  public Object convertSetId(Object idValue, EntityBean bean) {
    return idBinder.convertSetId(idValue, bean);
  }

  @Override
  public Property getProperty(String propName) {
    return findBeanProperty(propName);
  }

  /**
   * Get a BeanProperty by its name.
   */
  public BeanProperty getBeanProperty(String propName) {
    return propMap.get(propName);
  }

  public void sort(List<T> list, String sortByClause) {

    ElComparator<T> comparator = getElComparator(sortByClause);
    list.sort(comparator);
  }

  public ElComparator<T> getElComparator(String propNameOrSortBy) {
    return comparatorCache.computeIfAbsent(propNameOrSortBy, this::createComparator);
  }

  /**
   * Register all the assoc many properties on this bean that are not populated with the load context.
   * <p>
   * This provides further lazy loading via the load context.
   * </p>
   */
  public void lazyLoadRegister(String prefix, EntityBeanIntercept ebi, EntityBean bean, LoadContext loadContext) {

    // load the List/Set/Map proxy objects (deferred fetching of lists)
    BeanPropertyAssocMany<?>[] manys = propertiesMany();
    for (BeanPropertyAssocMany<?> many : manys) {
      if (!ebi.isLoadedProperty(many.getPropertyIndex())) {
        BeanCollection<?> ref = many.createReferenceIfNull(bean);
        if (ref != null && !ref.isRegisteredWithLoadContext()) {
          String path = SplitName.add(prefix, many.getName());
          loadContext.register(path, ref);
        }
      }
    }
  }

  /**
   * Return true if the lazy loading property is a Many in which case just
   * define a Reference for the collection and not invoke a query.
   */
  public boolean lazyLoadMany(EntityBeanIntercept ebi) {

    int lazyLoadProperty = ebi.getLazyLoadPropertyIndex();
    if (lazyLoadProperty == -1) {
      return false;
    }

    if (inheritInfo != null) {
      return descOf(ebi.getOwner().getClass()).lazyLoadMany(ebi, lazyLoadProperty);
    }
    return lazyLoadMany(ebi, lazyLoadProperty);
  }

  /**
   * Check for lazy loading of many property.
   */
  private boolean lazyLoadMany(EntityBeanIntercept ebi, int lazyLoadProperty) {

    BeanProperty lazyLoadBeanProp = propertiesIndex[lazyLoadProperty];
    if (lazyLoadBeanProp instanceof BeanPropertyAssocMany<?>) {
      BeanPropertyAssocMany<?> manyProp = (BeanPropertyAssocMany<?>) lazyLoadBeanProp;
      manyProp.createReference(ebi.getOwner());
      ebi.setLoadedLazy();
      return true;
    }
    return false;
  }

  /**
   * Return the correct BeanDescriptor based on the bean class type.
   */
  BeanDescriptor<?> descOf(Class<?> type) {
    return inheritInfo.readType(type).desc();
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

    ElPropertyValue elGetValue = getElGetValue(sortProp.getName());

    Boolean nullsHigh = sortProp.getNullsHigh();
    if (nullsHigh == null) {
      nullsHigh = Boolean.TRUE;
    }
    return new ElComparatorProperty<>(elGetValue, sortProp.isAscending(), nullsHigh);
  }

  @Override
  public boolean isValidExpression(String propertyName) {
    try {
      return (getElGetValue(propertyName) != null);
    } catch (PersistenceException e) {
      return false;
    }
  }

  /**
   * Get an Expression language Value object.
   */
  public ElPropertyValue getElGetValue(String propName) {
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
  public ExpressionPath getExpressionPath(String path) {
    return getElGetValue(path);
  }

  /**
   * Similar to ElPropertyValue but also uses foreign key shortcuts.
   * <p>
   * The foreign key shortcuts means we can avoid unnecessary joins.
   * </p>
   */
  public ElPropertyDeploy getElPropertyDeploy(String propName) {
    ElPropertyDeploy elProp = elDeployCache.get(propName);
    if (elProp != null) {
      return elProp;
    }
    if (!propName.contains(".")) {
      // No period means simple property and no need to look for
      // foreign key properties (in order to avoid an extra join)
      elProp = getElGetValue(propName);
    } else {
      elProp = buildElGetValue(propName, null, true);
    }
    if (elProp != null) {
      elDeployCache.put(propName, elProp);
    }
    return elProp;
  }

  protected ElPropertyValue buildElGetValue(String propName, ElPropertyChainBuilder chain, boolean propertyDeploy) {

    if (propertyDeploy && chain != null) {
      ElPropertyDeploy fk = elDeployCache.get(propName);
      if (fk instanceof BeanFkeyProperty) {
        // propertyDeploy chain for foreign key column
        return ((BeanFkeyProperty) fk).create(chain.getExpression(), chain.isContainsMany());
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
      throw new PersistenceException("No property found for [" + propName + "] in expression " + chain.getExpression());
    }
    if (property.containsMany()) {
      chain.setContainsMany();
    }
    return chain.add(property).build();
  }

  /**
   * Return the property path given the db table and column.
   */
  public String findBeanPath(String tableName, String columnName) {
    if (tableName.length() == 0 || tableName.equalsIgnoreCase(baseTable)) {
      return columnPath.get(columnName);
    }
    BeanPropertyAssoc<?> assocProperty = tablePath.get(tableName);
    if (assocProperty != null) {
      String relativePath = assocProperty.getTargetDescriptor().findBeanPath(tableName, columnName);
      if (relativePath != null) {
        return SplitName.add(assocProperty.getName(), relativePath);
      }
    }
    return null;
  }

  /**
   * Find a BeanProperty including searching the inheritance hierarchy.
   * <p>
   * This searches this BeanDescriptor and then searches further down the
   * inheritance tree (not up).
   * </p>
   */
  public BeanProperty findBeanProperty(String propName) {
    int basePos = propName.indexOf('.');
    if (basePos > -1) {
      // embedded property
      String baseName = propName.substring(0, basePos);
      return _findBeanProperty(baseName);
    }

    return _findBeanProperty(propName);
  }

  private BeanProperty _findBeanProperty(String propName) {
    BeanProperty prop = propMap.get(propName);
    if (prop == null && inheritInfo != null) {
      // search in sub types...
      return inheritInfo.findSubTypeProperty(propName);
    }
    return prop;
  }

  /**
   * Reset the many properties to empty state ready for reloading.
   */
  public void resetManyProperties(Object dbBean) {

    EntityBean bean = (EntityBean) dbBean;
    for (BeanPropertyAssocMany<?> aPropertiesMany : propertiesMany) {
      if (aPropertiesMany.isCascadeRefresh()) {
        aPropertiesMany.resetMany(bean);
      }
    }
  }

  /**
   * Return the name of the server this BeanDescriptor belongs to.
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * Return true if this bean can cache sharable instances.
   * <p>
   * This means is has no relationships and has readOnly=true in its cache
   * options.
   * </p>
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
   * Returns the Inheritance mapping information. This will be null if this type
   * of bean is not involved in any ORM inheritance mapping.
   */
  public InheritInfo getInheritInfo() {
    return inheritInfo;
  }

  @Override
  public boolean hasInheritance() {
    return inheritInfo != null;
  }

  @Override
  public String getDiscColumn() {
    return inheritInfo.getDiscriminatorColumn();
  }

  /**
   * Return the discriminator value for this bean type (or null when there is no inheritance).
   */
  public String getDiscValue() {
    return inheritInfo == null ? null : inheritInfo.getDiscriminatorStringValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public T createBeanUsingDisc(Object discValue) {
    return (T) inheritInfo.getType(discValue.toString()).desc().createBean();
  }

  @Override
  public void addInheritanceWhere(SpiQuery<?> query) {
    if (inheritInfo != null && !inheritInfo.isRoot()) {
      query.where().eq(inheritInfo.getDiscriminatorColumn(), inheritInfo.getDiscriminatorValue());
    }
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
  public IndexDefinition[] getIndexDefinitions() {
    return indexDefinitions;
  }

  /**
   * Return the beanListener.
   */
  @Override
  public BeanPersistListener getPersistListener() {
    return persistListener;
  }

  /**
   * Return the beanFinder (Migrate over to getFindController).
   */
  public BeanFindController getBeanFinder() {
    return beanFinder;
  }

  /**
   * Return the find controller (SPI interface).
   */
  @Override
  public BeanFindController getFindController() {
    return beanFinder;
  }

  /**
   * Return the BeanQueryAdapter or null if none is defined.
   */
  @Override
  public BeanQueryAdapter getQueryAdapter() {
    return queryAdapter;
  }

  /**
   * De-register the BeanPersistListener.
   */
  public void deregister(BeanPersistListener listener) {

    // volatile read...
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

    // volatile read...
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
      // volatile read...
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
      // volatile read...
      BeanPersistController currentController = persistController;
      if (currentController == null) {
        persistController = newController;
      } else {
        if (currentController instanceof ChainedBeanPersistController) {
          // add it to the existing chain
          persistController = ((ChainedBeanPersistController) currentController).register(newController);
        } else {
          // build new chain of the 2
          persistController = new ChainedBeanPersistController(currentController, newController);
        }
      }
    }
  }

  /**
   * Return the Controller.
   */
  @Override
  public BeanPersistController getPersistController() {
    return persistController;
  }

  /**
   * Returns true if this bean is based on RawSql.
   */
  public boolean isRawSqlBased() {
    return EntityType.SQL == entityType;
  }

  /**
   * Return the DB comment for the base table.
   */
  public String getDbComment() {
    return dbComment;
  }

  /**
   * Return the dependent tables for a view based entity.
   * <p>
   * These tables
   * </p>
   */
  public String[] getDependentTables() {
    return dependentTables;
  }

  /**
   * Return the base table. Only properties mapped to the base table are by
   * default persisted.
   */
  @Override
  public String getBaseTable() {
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
  public String getBaseTable(SpiQuery.TemporalMode mode) {
    switch (mode) {
      case DRAFT:
        return draftTable;
      case VERSIONS:
        return baseTableVersionsBetween;
      case AS_OF:
        return baseTableAsOf;
      default:
        return baseTable;
    }
  }

  /**
   * Return the associated draft table.
   */
  public String getDraftTable() {
    return draftTable;
  }

  /**
   * Return true if read auditing is on this entity bean.
   */
  public boolean isReadAuditing() {
    return readAuditing;
  }

  public boolean isSoftDelete() {
    return softDelete;
  }

  public void setSoftDeleteValue(EntityBean bean) {
    softDeleteProperty.setSoftDeleteValue(bean);
  }

  public String getSoftDeleteDbSet() {
    return softDeleteProperty.getSoftDeleteDbSet();
  }

  public String getSoftDeletePredicate(String tableAlias) {
    return softDeleteProperty.getSoftDeleteDbPredicate(tableAlias);
  }

  /**
   * Return true if this entity type is draftable.
   */
  public boolean isDraftable() {
    return draftable;
  }

  /**
   * Return true if this entity type is a draftable element (child).
   */
  public boolean isDraftableElement() {
    return draftableElement;
  }

  public void setUnmappedJson(EntityBean bean, Map<String, Object> unmappedProperties) {
    if (unmappedJson != null) {
      unmappedJson.setValueIntercept(bean, unmappedProperties);
    }
  }

  /**
   * Set the Tenant Id value to the bean.
   */
  public void setTenantId(EntityBean entityBean, Object tenantId) {
    if (tenant != null) {
      tenant.setValue(entityBean, tenantId);
    }
  }

  /**
   * Set the draft to true for this entity bean instance.
   * This bean is being loaded via asDraft() query.
   */
  public void setDraft(EntityBean entityBean) {
    if (draft != null) {
      draft.setValue(entityBean, true);
    }
  }

  /**
   * Return true if the bean is considered a 'draft' instance (not 'live').
   */
  public boolean isDraftInstance(EntityBean entityBean) {
    if (draft != null) {
      return Boolean.TRUE == draft.getValue(entityBean);
    }
    // no draft property - so return false
    return false;
  }

  /**
   * Return true if the bean is draftable and considered a 'live' instance.
   */
  public boolean isLiveInstance(EntityBean entityBean) {
    if (draft != null) {
      return Boolean.FALSE == draft.getValue(entityBean);
    }
    // no draft property - so return false
    return false;
  }

  /**
   * If there is a @DraftDirty property set it's value on the bean.
   */
  public void setDraftDirty(EntityBean entityBean, boolean value) {
    if (draftDirty != null) {
      // check to see if the dirty property has already
      // been set and if so do not set the value
      if (!entityBean._ebean_getIntercept().isChangedProperty(draftDirty.getPropertyIndex())) {
        draftDirty.setValueIntercept(entityBean, value);
      }
    }
  }

  /**
   * Optimise the draft query fetching any draftable element relationships.
   */
  public void draftQueryOptimise(Query<T> query) {
    // use per query PersistenceContext to ensure fresh beans loaded
    query.setPersistenceContextScope(PersistenceContextScope.QUERY);
    draftHelp.draftQueryOptimise(query);
  }

  /**
   * Return true if this entity bean has history support.
   */
  public boolean isHistorySupport() {
    return historySupport;
  }

  /**
   * Return the identity generation type.
   */
  @Override
  public IdType getIdType() {
    return idType;
  }

  /**
   * Return true if the identity is the platform default (not explicitly set).
   */
  public boolean isIdTypePlatformDefault() {
    return idTypePlatformDefault;
  }

  /**
   * Return the sequence name.
   */
  @Override
  public String getSequenceName() {
    return sequenceName;
  }

  /**
   * Return the sequence initial value.
   */
  public int getSequenceInitialValue() {
    return sequenceInitialValue;
  }

  /**
   * Return the sequence allocation size.
   */
  public int getSequenceAllocationSize() {
    return sequenceAllocationSize;
  }

  /**
   * Return the SQL used to return the last inserted id.
   * <p>
   * This is only used with Identity columns and getGeneratedKeys is not
   * supported.
   * </p>
   */
  public String getSelectLastInsertedId() {
    return selectLastInsertedId;
  }

  /**
   * Return the TableJoins.
   * <p>
   * For properties mapped to secondary tables rather than the base table.
   * </p>
   */
  public TableJoin[] tableJoins() {
    return derivedTableJoins;
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
   * Return the beans that are embedded. These share the base table with the
   * owner bean.
   */
  public BeanPropertyAssocOne<?>[] propertiesEmbedded() {
    return propertiesEmbedded;
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

  @Override
  public BeanProperty getIdProperty() {
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
    if (!hasIdValue(ebi.getOwner())) {
      // No Id property means it must be an insert
      return true;
    }
    // same as the 'root request'
    return insertMode;
  }

  public boolean isReference(EntityBeanIntercept ebi) {
    return ebi.isReference() || hasIdPropertyOnly(ebi);
  }

  public boolean hasIdPropertyOnly(EntityBeanIntercept ebi) {
    return ebi.hasIdOnly(idPropertyIndex);
  }

  public boolean hasIdValue(EntityBean bean) {
    return (idProperty != null && !DmlUtil.isNullOrZero(idProperty.getValue(bean)));
  }

  public boolean hasVersionProperty(EntityBeanIntercept ebi) {
    return versionPropertyIndex > -1 && ebi.isLoadedProperty(versionPropertyIndex);
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
   * Check for mutable scalar types and mark as dirty if necessary.
   */
  public void checkMutableProperties(EntityBeanIntercept ebi) {
    for (BeanProperty beanProperty : propertiesMutable) {
      int propertyIndex = beanProperty.getPropertyIndex();
      if (!ebi.isDirtyProperty(propertyIndex) && ebi.isLoadedProperty(propertyIndex)) {
        Object value = beanProperty.getValue(ebi.getOwner());
        if (value == null || beanProperty.isDirtyValue(value)) {
          // mutable scalar value which is considered dirty so mark
          // it as such so that it is included in an update
          ebi.markPropertyAsChanged(propertyIndex);
        }
      }
    }
  }

  public ConcurrencyMode getConcurrencyMode(EntityBeanIntercept ebi) {

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

    for (BeanProperty aPropertiesBaseScalar : propertiesBaseScalar) {
      aPropertiesBaseScalar.diff(prefix, map, newBean, oldBean);
    }
    for (BeanPropertyAssocOne<?> aPropertiesOne : propertiesOne) {
      aPropertiesOne.diff(prefix, map, newBean, oldBean);
    }
    for (BeanPropertyAssocOne<?> aPropertiesEmbedded : propertiesEmbedded) {
      aPropertiesEmbedded.diff(prefix, map, newBean, oldBean);
    }
  }

  /**
   * Appends the Id property to the OrderBy clause if it is not believed
   * to be already contained in the order by.
   * <p>
   * This is primarily used for paging queries to ensure that an order by clause is provided and that the order by
   * provides unique ordering of the rows (so that the paging is predicable).
   * </p>
   */
  public void appendOrderById(SpiQuery<T> query) {

    if (idProperty != null && !idProperty.isEmbedded()) {
      OrderBy<T> orderBy = query.getOrderBy();
      if (orderBy == null || orderBy.isEmpty()) {
        RawSql rawSql = query.getRawSql();
        if (rawSql != null) {
          query.order(rawSql.getSql().getOrderBy());
        }
        query.order().asc(idProperty.getName());
      } else if (!orderBy.containsProperty(idProperty.getName())) {
        query.order().asc(idProperty.getName());
      }
    }
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
   * </p>
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
   * </p>
   */
  public BeanProperty getVersionProperty() {
    return versionProperty;
  }

  /**
   * Return the tenant property when multi-tenant partitioning support is used.
   */
  public BeanProperty getTenantProperty() {
    return tenant;
  }

  /**
   * Scalar properties without the unique id or secondary table properties.
   */
  public BeanProperty[] propertiesBaseScalar() {
    return propertiesBaseScalar;
  }

  /**
   * Return the properties local to this type for inheritance.
   */
  public BeanProperty[] propertiesLocal() {
    return propertiesLocal;
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

  protected void jsonWriteDirtyProperties(SpiJsonWriter writeJson, EntityBean bean, boolean[] dirtyProps) throws IOException {
    jsonHelp.jsonWriteDirtyProperties(writeJson, bean, dirtyProps);
  }

  public void jsonWrite(SpiJsonWriter writeJson, EntityBean bean) throws IOException {
    jsonHelp.jsonWrite(writeJson, bean, null);
  }

  public void jsonWrite(SpiJsonWriter writeJson, EntityBean bean, String key) throws IOException {
    jsonHelp.jsonWrite(writeJson, bean, key);
  }

  protected void jsonWriteProperties(SpiJsonWriter writeJson, EntityBean bean) throws IOException {
    jsonHelp.jsonWriteProperties(writeJson, bean);
  }

  public T jsonRead(ReadJson jsonRead, String path) throws IOException {
    return jsonHelp.jsonRead(jsonRead, path);
  }

  protected T jsonReadObject(ReadJson jsonRead, String path) throws IOException {
    return jsonHelp.jsonReadObject(jsonRead, path);
  }

}
