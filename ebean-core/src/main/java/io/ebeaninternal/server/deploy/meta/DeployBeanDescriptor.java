package io.ebeaninternal.server.deploy.meta;

import io.ebean.annotation.Cache;
import io.ebean.annotation.Identity;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.TableName;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.event.*;
import io.ebean.event.changelog.ChangeLogFilter;
import io.ebean.util.SplitName;
import io.ebeaninternal.api.ConcurrencyMode;
import io.ebeaninternal.server.core.CacheOptions;
import io.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import io.ebeaninternal.server.deploy.*;
import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;
import io.ebeaninternal.server.idgen.UuidV1IdGenerator;
import io.ebeaninternal.server.idgen.UuidV1RndIdGenerator;
import io.ebeaninternal.server.idgen.UuidV4IdGenerator;

import java.util.*;

/**
 * Describes Beans including their deployment information.
 */
public class DeployBeanDescriptor<T> {

  private static class PropOrder implements Comparator<DeployBeanProperty> {

    @Override
    public int compare(DeployBeanProperty o1, DeployBeanProperty o2) {
      return Integer.compare(o2.getSortOrder(), o1.getSortOrder());
    }
  }

  private static final PropOrder PROP_ORDER = new PropOrder();

  private static final String I_SCALAOBJECT = "scala.ScalaObject";

  private final DatabaseConfig config;
  private final BeanDescriptorManager manager;
  /**
   * Map of BeanProperty Linked so as to preserve order.
   */
  private LinkedHashMap<String, DeployBeanProperty> propMap = new LinkedHashMap<>();
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
  private String[] dependentTables;
  private boolean historySupport;
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
  private String name;
  private ChangeLogFilter changeLogFilter;
  private String dbComment;
  private PartitionMeta partitionMeta;
  private TablespaceMeta tablespaceMeta;
  private DeployBeanProperty idProperty;
  private TableJoin primaryKeyJoin;

  /**
   * Construct the BeanDescriptor.
   */
  public DeployBeanDescriptor(BeanDescriptorManager manager, Class<T> beanType, DatabaseConfig config) {
    this.manager = manager;
    this.config = config;
    this.beanType = beanType;
  }

  /**
   * Set the IdClass to use.
   */
  public void setIdClass(Class idClass) {
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

  public boolean isScalaObject() {
    Class<?>[] interfaces = beanType.getInterfaces();
    for (Class<?> anInterface : interfaces) {
      String iname = anInterface.getName();
      if (I_SCALAOBJECT.equals(iname)) {
        return true;
      }
    }
    return false;
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

  public String[] getProperties() {
    return properties;
  }

  public void setProperties(String[] props) {
    this.properties = props;
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

  /**
   * Set the SQL used to return the last inserted Id.
   */
  public void setSelectLastInsertedId(String selectLastInsertedId) {
    this.selectLastInsertedId = selectLastInsertedId;
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
