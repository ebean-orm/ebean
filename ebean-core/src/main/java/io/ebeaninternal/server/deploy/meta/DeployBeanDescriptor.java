package io.ebeaninternal.server.deploy.meta;

import io.ebean.DatabaseBuilder;
import io.ebean.annotation.Cache;
import io.ebean.annotation.DocStore;
import io.ebean.annotation.DocStoreMode;
import io.ebean.annotation.Identity;
import io.ebean.config.TableName;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.event.*;
import io.ebean.event.changelog.ChangeLogFilter;
import io.ebean.plugin.Lookups;
import io.ebean.text.PathProperties;
import io.ebean.util.SplitName;
import io.ebeaninternal.api.ConcurrencyMode;
import io.ebeaninternal.server.core.CacheOptions;
import io.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import io.ebeaninternal.server.deploy.*;
import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;
import io.ebeaninternal.server.idgen.UuidV1IdGenerator;
import io.ebeaninternal.server.idgen.UuidV1RndIdGenerator;
import io.ebeaninternal.server.idgen.UuidV4IdGenerator;
import io.ebeaninternal.server.rawsql.SpiRawSql;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.*;

/**
 * Describes Beans including their deployment information.
 */
public class DeployBeanDescriptor<T> {

  private static final Map<String, String> EMPTY_NAMED_QUERY = new HashMap<>();

  private static final Map<String, SpiRawSql> EMPTY_RAW_MAP = new HashMap<>();

  private static class PropOrder implements Comparator<DeployBeanProperty> {

    @Override
    public int compare(DeployBeanProperty o1, DeployBeanProperty o2) {
      return Integer.compare(o2.getSortOrder(), o1.getSortOrder());
    }
  }

  private static final PropOrder PROP_ORDER = new PropOrder();

  private final DatabaseBuilder.Settings config;
  private final BeanDescriptorManager manager;
  private final Lookup lookup;

  /**
   * Map of BeanProperty Linked so as to preserve order.
   */
  private LinkedHashMap<String, DeployBeanProperty> propMap = new LinkedHashMap<>();
  private Map<String, SpiRawSql> namedRawSql;
  private Map<String, String> namedQuery;
  private EntityType entityType;
  private DeployBeanPropertyAssocOne<?> unidirectional;
  private DeployBeanProperty orderColumn;
  private Class<?> idClass;
  private DeployBeanPropertyAssocOne<?> idClassProperty;
  private DeployIdentityMode identityMode = DeployIdentityMode.auto();
  private PlatformIdGenerator idGenerator;
  /**
   * Set true when explicit auto generated Id.
   */
  private boolean idGeneratedValue;
  /**
   * Used with Identity columns but no getGeneratedKeys support.
   */
  private String selectLastInsertedId;
  private String selectLastInsertedIdDraft;
  /**
   * The concurrency mode for beans of this type.
   */
  private ConcurrencyMode concurrencyMode;
  private List<IndexDefinition> indexDefinitions;
  private String storageEngine;
  /**
   * The base database table.
   */
  private String baseTable;
  private String baseTableAsOf;
  private String baseTableVersionsBetween;
  private String draftTable;
  private String[] dependentTables;
  private boolean historySupport;
  private boolean readAuditing;
  private boolean draftable;
  private boolean draftableElement;
  private TableName baseTableFull;
  private String[] properties;
  /**
   * The EntityBean type used to create new EntityBeans.
   */
  private final Class<T> beanType;
  private final List<BeanPersistController> persistControllers = new ArrayList<>();
  private final List<BeanPersistListener> persistListeners = new ArrayList<>();
  private final List<BeanQueryAdapter> queryAdapters = new ArrayList<>();
  private final List<BeanPostLoad> postLoaders = new ArrayList<>();
  private final List<BeanPostConstructListener> postConstructListeners = new ArrayList<>();
  private CacheOptions cacheOptions = CacheOptions.NO_CACHING;
  /**
   * If set overrides the find implementation. Server side only.
   */
  private BeanFindController beanFinder;
  /**
   * Inheritance information. Server side only.
   */
  private InheritInfo inheritInfo;
  private String name;
  private ChangeLogFilter changeLogFilter;
  private String dbComment;
  private PartitionMeta partitionMeta;
  private TablespaceMeta tablespaceMeta;
  /**
   * One of NONE, INDEX or EMBEDDED.
   */
  private boolean docStoreMapped;
  private DocStore docStore;
  private PathProperties docStorePathProperties;
  private String docStoreQueueId;
  private String docStoreIndexName;
  private String docStoreIndexType;
  private DocStoreMode docStorePersist;
  private DocStoreMode docStoreInsert;
  private DocStoreMode docStoreUpdate;
  private DocStoreMode docStoreDelete;
  private DeployBeanProperty idProperty;
  private TableJoin primaryKeyJoin;

  /**
   * Construct the BeanDescriptor.
   */
  public DeployBeanDescriptor(BeanDescriptorManager manager, Class<T> beanType, DatabaseBuilder.Settings config) {
    this.manager = manager;
    this.config = config;
    this.beanType = beanType;
    this.lookup = Lookups.getLookup(beanType);
  }

  public BindMaxLength bindMaxLength() {
    return manager.bindMaxLength();
  }

  private String[] readPropertyNames() {
    try {
      return (String[]) lookup.findStaticVarHandle(beanType, "_ebean_props", String[].class).get();
    } catch (Exception e) {
      throw new IllegalStateException("Error getting _ebean_props field on type " + beanType, e);
    }
  }

  public void setPropertyNames(String[] properties) {
    this.properties = properties;
  }

  public String[] propertyNames() {
    if (properties == null) {
      properties = readPropertyNames();
    }
    return properties;
  }

  /**
   * Set the IdClass to use.
   */
  public void setIdClass(Class<?> idClass) {
    this.idClass = idClass;
  }

  /**
   * Return true if there is a IdClass set.
   */
  boolean isIdClass() {
    return idClass != null;
  }

  /**
   * PK is also a FK.
   */
  public void setPrimaryKeyJoin(TableJoin join) {
    this.primaryKeyJoin = join;
    this.identityMode.setIdType(IdType.EXTERNAL);
    this.idGenerator = null;
  }

  public TableJoin getPrimaryKeyJoin() {
    return primaryKeyJoin;
  }

  /**
   * Return the DeployBeanInfo for the given bean class.
   */
  DeployBeanInfo<?> getDeploy(Class<?> cls) {
    return manager.deploy(cls);
  }

  public void setStorageEngine(String storageEngine) {
    this.storageEngine = storageEngine;
  }

  public String getStorageEngine() {
    return storageEngine;
  }

  /**
   * Set to true for @History entity beans that have history.
   */
  public void setHistorySupport() {
    this.historySupport = true;
  }

  /**
   * Return true if this is an @History entity bean.
   */
  public boolean isHistorySupport() {
    return historySupport;
  }

  /**
   * Set read auditing on for this entity bean.
   */
  public void setReadAuditing() {
    readAuditing = true;
  }

  /**
   * Return true if read auditing is on for this entity bean.
   */
  public boolean isReadAuditing() {
    return readAuditing;
  }

  public void setDbComment(String dbComment) {
    this.dbComment = dbComment;
  }

  public String getDbComment() {
    return dbComment;
  }

  public void setPartitionMeta(PartitionMeta partitionMeta) {
    this.partitionMeta = partitionMeta;
  }

  public PartitionMeta getPartitionMeta() {
    if (partitionMeta != null) {
      DeployBeanProperty beanProperty = getBeanProperty(partitionMeta.getProperty());
      if (beanProperty != null) {
        partitionMeta.setColumn(beanProperty.getDbColumn());
      }
    }
    return partitionMeta;
  }

  public void setTablespaceMeta(TablespaceMeta tablespaceMeta) {
    this.tablespaceMeta = tablespaceMeta;
  }

  public TablespaceMeta getTablespaceMeta() {
    return tablespaceMeta;
  }

  public void setDraftable() {
    draftable = true;
  }

  public boolean isDraftable() {
    return draftable;
  }

  public void setDraftableElement() {
    draftable = true;
    draftableElement = true;
  }

  public boolean isDraftableElement() {
    return draftableElement;
  }

  /**
   * Read the top level doc store deployment information.
   */
  public void readDocStore(DocStore docStore) {
    this.docStore = docStore;
    docStoreMapped = true;
    docStoreQueueId = docStore.queueId();
    docStoreIndexName = docStore.indexName();
    docStoreIndexType = docStore.indexType();
    docStorePersist = docStore.persist();
    docStoreInsert = docStore.insert();
    docStoreUpdate = docStore.update();
    docStoreDelete = docStore.delete();
    String doc = docStore.doc();
    if (!doc.isEmpty()) {
      docStorePathProperties = PathProperties.parse(doc);
    }
  }

  public DeployBeanTable createDeployBeanTable() {
    DeployBeanTable beanTable = new DeployBeanTable(getBeanType());
    beanTable.setBaseTable(baseTable);
    beanTable.setIdProperty(idProperty());
    return beanTable;
  }

  public void setEntityType(EntityType entityType) {
    this.entityType = entityType;
  }

  public boolean isEmbedded() {
    return EntityType.EMBEDDED == entityType;
  }

  public boolean isBaseTableType() {
    EntityType et = getEntityType();
    return EntityType.ORM == et;
  }

  public boolean isDocStoreOnly() {
    return EntityType.DOC == entityType;
  }

  public EntityType getEntityType() {
    if (entityType == null) {
      entityType = EntityType.ORM;
    }
    return entityType;
  }

  /**
   * Return the immutable IdentityMode.
   */
  public IdentityMode buildIdentityMode() {
    return new IdentityMode(identityMode);
  }

  public DeployIdentityMode getIdentityMode() {
    return identityMode;
  }

  public void setIdentityMode(Identity identity) {
    this.identityMode = new DeployIdentityMode(identity);
  }

  /**
   * Set from <code>@Sequence</code>
   */
  public void setIdentitySequence(int initialValue, int allocationSize, String seqName) {
    identityMode.setSequence(initialValue, allocationSize, seqName);
  }

  /**
   * Potentially set sequence name from <code>@GeneratedValue</code>.
   */
  public void setIdentitySequenceGenerator(String genName) {
    identityMode.setSequenceGenerator(genName);
  }

  /**
   * Return the sequence increment to use given sequence batch mode.
   */
  public int setIdentitySequenceBatchMode(boolean sequenceBatchMode) {
    return identityMode.setSequenceBatchMode(sequenceBatchMode);
  }

  public void setIdentityType(IdType type) {
    this.identityMode.setIdType(type);
  }

  /**
   * Return the class type this BeanDescriptor describes.
   */
  public Class<T> getBeanType() {
    return beanType;
  }

  public void setChangeLogFilter(ChangeLogFilter changeLogFilter) {
    this.changeLogFilter = changeLogFilter;
  }

  public ChangeLogFilter getChangeLogFilter() {
    return changeLogFilter;
  }

  /**
   * Returns the Inheritance mapping information. This will be null if this type
   * of bean is not involved in any ORM inheritance mapping.
   */
  public InheritInfo getInheritInfo() {
    return inheritInfo;
  }

  /**
   * Set the ORM inheritance mapping information.
   */
  public void setInheritInfo(InheritInfo inheritInfo) {
    this.inheritInfo = inheritInfo;
  }

  /**
   * Set that this type invalidates query caches.
   */
  public void setInvalidateQueryCache(String region) {
    this.cacheOptions = CacheOptions.invalidateQueryCache(region);
  }

  /**
   * Enable L2 bean and query caching based on Cache annotation.
   */
  public void setCache(Cache cache) {

    String[] properties = cache.naturalKey();
    for (String property : properties) {
      DeployBeanProperty beanProperty = getBeanProperty(property);
      if (beanProperty != null) {
        beanProperty.setNaturalKey();
      }
    }
    if (properties.length == 0) {
      properties = null;
    }
    this.cacheOptions = new CacheOptions(cache, properties);
  }

  /**
   * Return the cache options.
   */
  public CacheOptions getCacheOptions() {
    return cacheOptions;
  }

  DeployBeanPropertyAssocOne<?> getIdClassProperty() {
    return idClassProperty;
  }

  public DeployBeanPropertyAssocOne<?> getUnidirectional() {
    return unidirectional;
  }

  public void setUnidirectional(DeployBeanPropertyAssocOne<?> unidirectional) {
    this.unidirectional = unidirectional;
  }

  public void setOrderColumn(DeployBeanProperty orderColumn) {
    this.orderColumn = orderColumn;
  }

  DeployBeanProperty getOrderColumn() {
    return orderColumn;
  }

  /**
   * Return the concurrency mode used for beans of this type.
   */
  public ConcurrencyMode getConcurrencyMode() {
    return concurrencyMode;
  }

  /**
   * Set the concurrency mode used for beans of this type.
   */
  public void setConcurrencyMode(ConcurrencyMode concurrencyMode) {
    this.concurrencyMode = concurrencyMode;
  }

  /**
   * Add a compound unique constraint.
   */
  public void addIndex(IndexDefinition c) {
    if (indexDefinitions == null) {
      indexDefinitions = new ArrayList<>();
    }
    indexDefinitions.add(c);
  }

  /**
   * Return the compound unique constraints (can be null).
   */
  public IndexDefinition[] getIndexDefinitions() {
    if (indexDefinitions == null) {
      return null;
    } else {
      return indexDefinitions.toArray(new IndexDefinition[0]);
    }
  }

  /**
   * Return the beanFinder. Usually null unless overriding the finder.
   */
  public BeanFindController getBeanFinder() {
    return beanFinder;
  }

  /**
   * Set the BeanFinder to use for beans of this type. This is set to override
   * the finding from the default.
   */
  public void setBeanFinder(BeanFindController beanFinder) {
    this.beanFinder = beanFinder;
  }

  /**
   * Return the BeanPersistController (could be a chain of them, 1 or null).
   */
  public BeanPersistController getPersistController() {
    if (persistControllers.isEmpty()) {
      return null;
    } else if (persistControllers.size() == 1) {
      return persistControllers.get(0);
    } else {
      return new ChainedBeanPersistController(persistControllers);
    }
  }

  /**
   * Return the BeanPersistListener (could be a chain of them, 1 or null).
   */
  public BeanPersistListener getPersistListener() {
    if (persistListeners.isEmpty()) {
      return null;
    } else if (persistListeners.size() == 1) {
      return persistListeners.get(0);
    } else {
      return new ChainedBeanPersistListener(persistListeners);
    }
  }

  public BeanQueryAdapter getQueryAdapter() {
    if (queryAdapters.isEmpty()) {
      return null;
    } else if (queryAdapters.size() == 1) {
      return queryAdapters.get(0);
    } else {
      return new ChainedBeanQueryAdapter(queryAdapters);
    }
  }

  /**
   * Return the BeanPostLoad (could be a chain of them, 1 or null).
   */
  public BeanPostLoad getPostLoad() {
    if (postLoaders.isEmpty()) {
      return null;
    } else if (postLoaders.size() == 1) {
      return postLoaders.get(0);
    } else {
      return new ChainedBeanPostLoad(postLoaders);
    }
  }

  /**
   * Return the BeanPostCreate(could be a chain of them, 1 or null).
   */
  public BeanPostConstructListener getPostConstructListener() {
    if (postConstructListeners.isEmpty()) {
      return null;
    } else if (postConstructListeners.size() == 1) {
      return postConstructListeners.get(0);
    } else {
      return new ChainedBeanPostConstructListener(postConstructListeners);
    }
  }

  public void addPersistController(BeanPersistController controller) {
    persistControllers.add(controller);
  }

  public void addPersistListener(BeanPersistListener listener) {
    persistListeners.add(listener);
  }

  public void addQueryAdapter(BeanQueryAdapter queryAdapter) {
    queryAdapters.add(queryAdapter);
  }

  public void addPostLoad(BeanPostLoad postLoad) {
    postLoaders.add(postLoad);
  }

  public void addPostConstructListener(BeanPostConstructListener postConstructListener) {
    postConstructListeners.add(postConstructListener);
  }

  public String getDraftTable() {
    return draftTable;
  }

  /**
   * For view based entity return the dependant tables.
   */
  public String[] getDependentTables() {
    return dependentTables;
  }

  /**
   * Return the base table. Only properties mapped to the base table are by
   * default persisted.
   */
  public String getBaseTable() {
    return baseTable;
  }

  /**
   * Return the base table with as of suffix.
   */
  public String getBaseTableAsOf() {
    return historySupport ? baseTableAsOf : baseTable;
  }

  /**
   * Return the base table with versions between suffix.
   */
  public String getBaseTableVersionsBetween() {
    return baseTableVersionsBetween;
  }

  /**
   * Return the base table with full structure.
   */
  public TableName getBaseTableFull() {
    return baseTableFull;
  }

  /**
   * Set when entity is based on a view.
   */
  public void setView(String viewName, String[] dependentTables) {
    this.entityType = EntityType.VIEW;
    this.dependentTables = dependentTables;
    setBaseTable(new TableName(viewName), "", "");
  }

  /**
   * Set the base table. Only properties mapped to the base table are by default persisted.
   */
  public void setBaseTable(TableName baseTableFull, String asOfSuffix, String versionsBetweenSuffix) {
    this.baseTableFull = baseTableFull;
    this.baseTable = baseTableFull == null ? null : baseTableFull.getQualifiedName();
    this.baseTableAsOf = baseTable + asOfSuffix;
    this.baseTableVersionsBetween = baseTable + versionsBetweenSuffix;
    this.draftTable = (draftable) ? baseTable + "_draft" : baseTable;
  }

  public void sortProperties() {
    ArrayList<DeployBeanProperty> list = new ArrayList<>(propMap.values());
    list.sort(PROP_ORDER);
    propMap = new LinkedHashMap<>(list.size());
    for (DeployBeanProperty property : list) {
      addBeanProperty(property);
    }
  }

  public void postAnnotations() {
    if (idClass != null) {
      idClassProperty = new DeployBeanPropertyAssocOne<>(this, idClass);
      idClassProperty.setName("_$IdClass$");
      idClassProperty.setEmbedded();
      idClassProperty.setNullable(false);
    }
  }

  /**
   * Add a bean property.
   */
  public DeployBeanProperty addBeanProperty(DeployBeanProperty prop) {
    return propMap.put(prop.getName(), prop);
  }

  public Collection<DeployBeanProperty> properties() {
    return propMap.values();
  }

  /**
   * Get a BeanProperty by its name.
   */
  public DeployBeanProperty getBeanProperty(String propName) {
    return propMap.get(propName);
  }

  /**
   * Return the bean class name this descriptor is used for.
   * <p>
   * If this BeanDescriptor is for a table then this returns the table name
   * instead.
   * </p>
   */
  public String getFullName() {
    return beanType.getName();
  }

  /**
   * Return the bean short name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the bean shortName.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Return the SQL used to return the last inserted Id.
   * <p>
   * Used with Identity columns where getGeneratedKeys is not supported.
   * </p>
   */
  public String getSelectLastInsertedId() {
    return selectLastInsertedId;
  }

  public String getSelectLastInsertedIdDraft() {
    return selectLastInsertedIdDraft;
  }

  /**
   * Set the SQL used to return the last inserted Id.
   */
  public void setSelectLastInsertedId(String selectLastInsertedId, String selectLastInsertedIdDraft) {
    this.selectLastInsertedId = selectLastInsertedId;
    this.selectLastInsertedIdDraft = selectLastInsertedIdDraft;
  }

  /**
   * Return the actual IdGenerator for this bean type (can be null).
   */
  public PlatformIdGenerator getIdGenerator() {
    return idGenerator;
  }

  /**
   * Set the actual IdGenerator for this bean type.
   */
  public void setIdGenerator(PlatformIdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  /**
   * Return true for automatic Id generation strategy.
   */
  public boolean isIdGeneratedValue() {
    return idGeneratedValue;
  }

  /**
   * Set when GeneratedValue explicitly mapped on Id property.
   */
  public void setIdGeneratedValue() {
    this.idGeneratedValue = true;
  }

  /**
   * Assign the standard UUID generator if one has not been set.
   */
  public void setUuidGenerator() {
    if (idGenerator == null) {
      this.identityMode.setIdType(IdType.EXTERNAL);
      switch (config.getUuidVersion()) {
        case VERSION1:
          this.idGenerator = UuidV1IdGenerator.getInstance(config.getUuidStateFile(), config.getUuidNodeId());
          break;
        case VERSION1RND:
          this.idGenerator = UuidV1RndIdGenerator.INSTANCE;
          break;
        case VERSION4:
        default:
          this.idGenerator = UuidV4IdGenerator.INSTANCE;
          break;
      }
    }
  }

  /**
   * Assign a custom external IdGenerator.
   */
  public void setCustomIdGenerator(PlatformIdGenerator idGenerator) {
    this.identityMode.setIdType(IdType.EXTERNAL);
    this.idGenerator = idGenerator;
  }

  /**
   * Summary description.
   */
  @Override
  public String toString() {
    return getFullName();
  }

  /**
   * Return a collection of all BeanProperty deployment information.
   */
  public Collection<DeployBeanProperty> propertiesAll() {
    return propMap.values();
  }

  /**
   * Return the defaultSelectClause using FetchType.LAZY and FetchType.EAGER.
   */
  public String getDefaultSelectClause() {

    StringBuilder sb = new StringBuilder();

    boolean hasLazyFetch = false;

    for (DeployBeanProperty prop : propMap.values()) {
      if (!prop.isTransient() && !(prop instanceof DeployBeanPropertyAssocMany<?>)) {
        if (prop.isFetchEager()) {
          sb.append(prop.getName()).append(',');
        } else {
          hasLazyFetch = true;
        }
      }
    }

    if (!hasLazyFetch) {
      return null;
    }
    String selectClause = sb.toString();
    if (selectClause.isEmpty()) {
      throw new IllegalStateException("Bean " + getFullName() + " has no properties?");
    }
    return selectClause.substring(0, selectClause.length() - 1);
  }

  /**
   * Return true if the primary key is a compound key or if it's database type
   * is non-numeric (and hence not suitable for db identity or sequence.
   */
  public boolean isPrimaryKeyCompoundOrNonNumeric() {
    DeployBeanProperty id = idProperty();
    if (id == null) {
      return false;
    }
    if (id instanceof DeployBeanPropertyAssocOne<?>) {
      return ((DeployBeanPropertyAssocOne<?>) id).isCompound();
    } else {
      return !id.isDbNumberType();
    }
  }

  /**
   * Return the Primary Key column assuming it is a single column (not
   * compound). This is for the purpose of defining a sequence name.
   */
  public String getSinglePrimaryKeyColumn() {
    DeployBeanProperty id = idProperty();
    if (id != null) {
      if (id instanceof DeployBeanPropertyAssoc<?>) {
        // its a compound primary key
        return null;
      } else {
        return id.getDbColumn();
      }
    }
    return null;
  }

  /**
   * Return the BeanProperty that is the Id.
   */
  public DeployBeanProperty idProperty() {
    if (idProperty != null) {
      return idProperty;
    }
    for (DeployBeanProperty prop : propMap.values()) {
      if (prop.isId()) {
        idProperty = prop;
        return idProperty;
      }
    }
    return null;
  }

  public DeployBeanPropertyAssocOne<?> findJoinToTable(String tableName) {

    List<DeployBeanPropertyAssocOne<?>> assocOne = propertiesAssocOne();
    for (DeployBeanPropertyAssocOne<?> prop : assocOne) {
      DeployTableJoin tableJoin = prop.getTableJoin();
      if (tableJoin != null && tableJoin.getTable().equalsIgnoreCase(tableName)) {
        return prop;
      }
    }
    return null;
  }

  /**
   * Return an Iterator of BeanPropertyAssocOne that are not embedded. These are
   * effectively joined beans. For ManyToOne and OneToOne associations.
   */
  public List<DeployBeanPropertyAssocOne<?>> propertiesAssocOne() {

    ArrayList<DeployBeanPropertyAssocOne<?>> list = new ArrayList<>();

    for (DeployBeanProperty prop : propMap.values()) {
      if (prop instanceof DeployBeanPropertyAssocOne<?>) {
        if (!prop.isEmbedded()) {
          list.add((DeployBeanPropertyAssocOne<?>) prop);
        }
      }
    }

    return list;

  }

  /**
   * Return BeanPropertyAssocMany for this descriptor.
   */
  public List<DeployBeanPropertyAssocMany<?>> propertiesAssocMany() {

    ArrayList<DeployBeanPropertyAssocMany<?>> list = new ArrayList<>();

    for (DeployBeanProperty prop : propMap.values()) {
      if (prop instanceof DeployBeanPropertyAssocMany<?>) {
        list.add((DeployBeanPropertyAssocMany<?>) prop);
      }
    }

    return list;
  }

  /**
   * base properties without the unique id properties.
   */
  public List<DeployBeanProperty> propertiesBase() {

    ArrayList<DeployBeanProperty> list = new ArrayList<>();

    for (DeployBeanProperty prop : propMap.values()) {
      if (!(prop instanceof DeployBeanPropertyAssoc<?>) && !prop.isId()) {
        list.add(prop);
      }
    }

    return list;
  }

  /**
   * Check the mapping for class inheritance
   */
  public void checkInheritanceMapping() {
    if (inheritInfo == null) {
      checkInheritance(getBeanType());
    }
  }

  /**
   * Check valid mapping annotations on the class hierarchy.
   */
  private void checkInheritance(Class<?> beanType) {

    Class<?> parent = beanType.getSuperclass();
    if (parent == null || Object.class.equals(parent)) {
      // all good
      return;
    }
    if (parent.isAnnotationPresent(Entity.class)) {
      String msg = "Checking " + getBeanType() + " and found " + parent + " that has @Entity annotation rather than MappedSuperclass?";
      throw new IllegalStateException(msg);
    }
    if (parent.isAnnotationPresent(MappedSuperclass.class)) {
      // continue checking
      checkInheritance(parent);
    }
  }

  public PathProperties getDocStorePathProperties() {
    return docStorePathProperties;
  }

  /**
   * Return true if this type is mapped for a doc store.
   */
  public boolean isDocStoreMapped() {
    return docStoreMapped;
  }

  public String getDocStoreQueueId() {
    return docStoreQueueId;
  }

  public String getDocStoreIndexName() {
    return docStoreIndexName;
  }

  public String getDocStoreIndexType() {
    return docStoreIndexType;
  }

  public DocStore getDocStore() {
    return docStore;
  }

  /**
   * Return the DocStore index behavior for bean inserts.
   */
  public DocStoreMode getDocStoreInsertEvent() {
    return getDocStoreIndexEvent(docStoreInsert);
  }

  /**
   * Return the DocStore index behavior for bean updates.
   */
  public DocStoreMode getDocStoreUpdateEvent() {
    return getDocStoreIndexEvent(docStoreUpdate);
  }

  /**
   * Return the DocStore index behavior for bean deletes.
   */
  public DocStoreMode getDocStoreDeleteEvent() {
    return getDocStoreIndexEvent(docStoreDelete);
  }

  private DocStoreMode getDocStoreIndexEvent(DocStoreMode mostSpecific) {
    if (!docStoreMapped) {
      return DocStoreMode.IGNORE;
    }
    if (mostSpecific != DocStoreMode.DEFAULT) return mostSpecific;
    if (docStorePersist != DocStoreMode.DEFAULT) return docStorePersist;
    return config.getDocStoreConfig().getPersist();
  }

  /**
   * Return the named ORM queries.
   */
  public Map<String, String> getNamedQuery() {
    return (namedQuery != null) ? namedQuery : EMPTY_NAMED_QUERY;
  }

  /**
   * Add a named query.
   */
  public void addNamedQuery(String name, String query) {
    if (namedQuery == null) {
      namedQuery = new LinkedHashMap<>();
    }
    namedQuery.put(name, query);
  }

  /**
   * Return the named RawSql queries.
   */
  public Map<String, SpiRawSql> getNamedRawSql() {
    return (namedRawSql != null) ? namedRawSql : EMPTY_RAW_MAP;
  }

  /**
   * Add a named RawSql from ebean.xml file.
   */
  public void addRawSql(String name, SpiRawSql rawSql) {
    if (namedRawSql == null) {
      namedRawSql = new HashMap<>();
    }
    namedRawSql.put(name, rawSql);
  }

  /**
   * Parse the aggregation formula into expressions with table alias placeholders.
   */
  public String parse(String aggregation) {
    return new Parser(this).parse(aggregation);
  }

  /**
   * Parser for top level properties into EL expressions (table alias placeholders).
   */
  private static class Parser extends DeployPropertyParserMap {

    private final DeployBeanDescriptor<?> descriptor;

    Parser(DeployBeanDescriptor<?> descriptor) {
      super(null);
      this.descriptor = descriptor;
    }

    @Override
    public String deployWord(String expression) {
      return descriptor.getDeployWord(expression);
    }
  }

  private String getDeployWord(String expression) {
    if (expression.charAt(expression.length() - 1) == '(') {
      return null;
    }
    // use 'current' table alias - refer BeanProperty appendSelect() for aggregation
    String[] split = SplitName.split(expression);
    if (split[0] == null) {
      DeployBeanProperty property = propMap.get(expression);
      return (property == null) ? null : "${ta}." + property.getDbColumn();
    } else {
      DeployBeanProperty property = propMap.get(split[0]);
      if (property instanceof DeployBeanPropertyAssoc) {
        DeployBeanPropertyAssoc<?> prop = (DeployBeanPropertyAssoc<?>) property;
        DeployBeanProperty beanProperty = prop.getTargetDeploy().getBeanProperty(split[1]);
        if (beanProperty != null) {
          return "u1." + beanProperty.getDbColumn();
        }
      }
      return null;
    }
  }

}
