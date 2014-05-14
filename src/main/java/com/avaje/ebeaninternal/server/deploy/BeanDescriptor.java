package com.avaje.ebeaninternal.server.deploy;

import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.annotation.ConcurrencyMode;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebean.config.EncryptKey;
import com.avaje.ebean.config.dbplatform.IdGenerator;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebean.event.BeanFinder;
import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanPersistListener;
import com.avaje.ebean.event.BeanQueryAdapter;
import com.avaje.ebean.meta.MetaBeanInfo;
import com.avaje.ebean.meta.MetaQueryPlanStatistic;
import com.avaje.ebean.text.TextException;
import com.avaje.ebean.text.json.JsonWriteBeanVisitor;
import com.avaje.ebeaninternal.api.HashQueryPlan;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.SpiUpdatePlan;
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeaninternal.server.cache.CachedBeanData;
import com.avaje.ebeaninternal.server.cache.CachedManyIds;
import com.avaje.ebeaninternal.server.core.CacheOptions;
import com.avaje.ebeaninternal.server.core.DefaultSqlUpdate;
import com.avaje.ebeaninternal.server.core.InternString;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.id.IdBinder;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyLists;
import com.avaje.ebeaninternal.server.el.ElComparator;
import com.avaje.ebeaninternal.server.el.ElComparatorCompound;
import com.avaje.ebeaninternal.server.el.ElComparatorProperty;
import com.avaje.ebeaninternal.server.el.ElPropertyChainBuilder;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.persist.DmlUtil;
import com.avaje.ebeaninternal.server.query.CQueryPlan;
import com.avaje.ebeaninternal.server.query.CQueryPlanStats.Snapshot;
import com.avaje.ebeaninternal.server.query.SplitName;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;
import com.avaje.ebeaninternal.server.text.json.ReadJsonContext;
import com.avaje.ebeaninternal.server.text.json.ReadJsonContext.ReadBeanState;
import com.avaje.ebeaninternal.server.text.json.WriteJsonContext;
import com.avaje.ebeaninternal.server.text.json.WriteJsonContext.WriteBeanState;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.type.TypeManager;
import com.avaje.ebeaninternal.util.SortByClause;
import com.avaje.ebeaninternal.util.SortByClause.Property;
import com.avaje.ebeaninternal.util.SortByClauseParser;

/**
 * Describes Beans including their deployment information.
 */
public class BeanDescriptor<T> implements MetaBeanInfo {

  private static final Logger logger = LoggerFactory.getLogger(BeanDescriptor.class);

  private final ConcurrentHashMap<Integer, SpiUpdatePlan> updatePlanCache = new ConcurrentHashMap<Integer, SpiUpdatePlan>();

  private final ConcurrentHashMap<HashQueryPlan, CQueryPlan> queryPlanCache = new ConcurrentHashMap<HashQueryPlan, CQueryPlan>();

  private final ConcurrentHashMap<String, ElPropertyValue> elGetCache = new ConcurrentHashMap<String, ElPropertyValue>();

  private final ConcurrentHashMap<String, ElComparator<T>> comparatorCache = new ConcurrentHashMap<String, ElComparator<T>>();

  private final ConcurrentHashMap<String, BeanFkeyProperty> fkeyMap = new ConcurrentHashMap<String, BeanFkeyProperty>();

  public enum EntityType {
    ORM, EMBEDDED, SQL
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

  private final IdGenerator idGenerator;

  /**
   * The database sequence name (optional).
   */
  private final String sequenceName;

  /**
   * SQL used to return last inserted id. Used for Identity columns where
   * getGeneratedKeys is not supported.
   */
  private final String selectLastInsertedId;

  private final boolean autoFetchTunable;

  private final String lazyFetchIncludes;

  /**
   * The concurrency mode for beans of this type.
   */
  private final ConcurrencyMode concurrencyMode;

  /**
   * The tables this bean is dependent on.
   */
  private final String[] dependantTables;

  private final CompoundUniqueContraint[] compoundUniqueConstraints;

  /**
   * Extra deployment attributes.
   */
  private final Map<String, String> extraAttrMap;

  /**
   * The base database table.
   */
  private final String baseTable;

  /**
   * Map of BeanProperty Linked so as to preserve order.
   */
  private final LinkedHashMap<String, BeanProperty> propMap;
  private final LinkedHashMap<String, BeanProperty> propMapByDbColumn;

  /**
   * The type of bean this describes.
   */
  private final Class<T> beanType;

  /**
   * This is not sent to a remote client.
   */
  private final BeanDescriptorMap owner;

  
  private final String[] properties;
  
  private final int propertyCount;
  

  /**
   * Intercept pre post on insert,update,delete and postLoad(). Server side
   * only.
   */
  private volatile BeanPersistController persistController;

  /**
   * Listens for post commit insert update and delete events.
   */
  private volatile BeanPersistListener<T> persistListener;

  private volatile BeanQueryAdapter queryAdapter;

  /**
   * If set overrides the find implementation. Server side only.
   */
  private final BeanFinder<T> beanFinder;

  /**
   * The table joins for this bean.
   */
  private final TableJoin[] derivedTableJoins;

  /**
   * Inheritance information. Server side only.
   */
  private final InheritInfo inheritInfo;

  /**
   * Derived list of properties that make up the unique id.
   */
  private final BeanProperty idProperty;
  private final int idPropertyIndex;

  /**
   * Derived list of properties that are used for version concurrency checking.
   */
  private final BeanProperty versionProperty;
  
  private final int versionPropertyIndex;
  
  /**
   * Properties that are initialised in the constructor need to be 'unloaded' to support partial object queries.
   */
  private final int[] unloadProperties;
  
  /**
   * Properties local to this type (not from a super type).
   */
  private final BeanProperty[] propertiesLocal;

  private final BeanPropertyAssocOne<?> unidirectional;

  /**
   * A hashcode of all the many property names. This is used to efficiently
   * create sets of loaded property names (for partial objects).
   */
  private final int namesOfManyPropsHash;

  /**
   * The set of names of the many properties.
   */
  private final Set<String> namesOfManyProps;

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

  private final BeanPropertyAssocOne<?>[] propertiesOneExported;
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
  private final BeanPropertyCompound[] propertiesBaseCompound;

  private final BeanProperty[] propertiesTransient;

  /**
   * All non transient properties excluding the id properties.
   */
  private final BeanProperty[] propertiesNonTransient;

  /**
   * The bean class name or the table name for MapBeans.
   */
  private final String fullName;

  private final Map<String, DeployNamedQuery> namedQueries;

  private final Map<String, DeployNamedUpdate> namedUpdates;

  /**
   * Flag used to determine if saves can be skipped.
   */
  private boolean saveRecurseSkippable;

  /**
   * Flag used to determine if deletes can be skipped.
   */
  private boolean deleteRecurseSkippable;

  /**
   * Make the TypeManager available for helping SqlSelect.
   */
  private final TypeManager typeManager;

  private final EntityBean prototypeEntityBean;
  
  private final IdBinder idBinder;

  private String idBinderInLHSSql;

  private String idBinderIdSql;

  private String deleteByIdSql;

  private String deleteByIdInSql;

  private final String name;

  private final String baseTableAlias;

  /**
   * If true then only changed properties get updated.
   */
  private final boolean updateChangesOnly;

  private final boolean cacheSharableBeans;
  
  private final BeanDescriptorCacheHelp<T> cacheHelp;

  private final String defaultSelectClause;
  private final Set<String> defaultSelectClauseSet;

  private final String descriptorId;

  private SpiEbeanServer ebeanServer;

  /**
   * Construct the BeanDescriptor.
   */
  public BeanDescriptor(BeanDescriptorMap owner, TypeManager typeManager, DeployBeanDescriptor<T> deploy, String descriptorId) {

    this.owner = owner;
    this.serverName = owner.getServerName();
    this.entityType = deploy.getEntityType();
    this.properties = deploy.getProperties();
    this.propertyCount = this.properties.length;
    this.name = InternString.intern(deploy.getName());
    this.baseTableAlias = "t0";
    this.fullName = InternString.intern(deploy.getFullName());
    this.descriptorId = descriptorId;

    this.typeManager = typeManager;
    this.beanType = deploy.getBeanType();
    this.prototypeEntityBean = createPrototypeEntityBean(beanType);
    
    this.namedQueries = deploy.getNamedQueries();
    this.namedUpdates = deploy.getNamedUpdates();

    this.inheritInfo = deploy.getInheritInfo();

    this.beanFinder = deploy.getBeanFinder();
    this.persistController = deploy.getPersistController();
    this.persistListener = deploy.getPersistListener();
    this.queryAdapter = deploy.getQueryAdapter();

    this.defaultSelectClause = deploy.getDefaultSelectClause();
    this.defaultSelectClauseSet = deploy.parseDefaultSelectClause(defaultSelectClause);

    this.idType = deploy.getIdType();
    this.idGenerator = deploy.getIdGenerator();
    this.sequenceName = deploy.getSequenceName();
    this.selectLastInsertedId = deploy.getSelectLastInsertedId();
    this.lazyFetchIncludes = InternString.intern(deploy.getLazyFetchIncludes());
    this.concurrencyMode = deploy.getConcurrencyMode();
    this.updateChangesOnly = deploy.isUpdateChangesOnly();

    this.dependantTables = deploy.getDependantTables();
    this.compoundUniqueConstraints = deploy.getCompoundUniqueConstraints();

    this.extraAttrMap = deploy.getExtraAttributeMap();

    this.baseTable = InternString.intern(deploy.getBaseTable());

    this.autoFetchTunable = EntityType.ORM.equals(entityType) && (beanFinder == null);

    // helper object used to derive lists of properties
    DeployBeanPropertyLists listHelper = new DeployBeanPropertyLists(owner, this, deploy);

    this.idProperty = listHelper.getId();
    this.versionProperty = listHelper.getVersionProperty();
    this.propMap = listHelper.getPropertyMap();
    this.propMapByDbColumn = getReverseMap(propMap);
    this.propertiesTransient = listHelper.getTransients();
    this.propertiesNonTransient = listHelper.getNonTransients();
    this.propertiesBaseScalar = listHelper.getBaseScalar();
    this.propertiesBaseCompound = listHelper.getBaseCompound();
    this.propertiesEmbedded = listHelper.getEmbedded();
    this.propertiesLocal = listHelper.getLocal();
    this.unidirectional = listHelper.getUnidirectional();
    this.propertiesOne = listHelper.getOnes();
    this.propertiesOneExported = listHelper.getOneExported();
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

    this.namesOfManyProps = deriveManyPropNames();
    this.namesOfManyPropsHash = namesOfManyProps.hashCode();

    this.derivedTableJoins = listHelper.getTableJoin();

    boolean noRelationships = propertiesOne.length + propertiesMany.length == 0;
    
    this.cacheSharableBeans = noRelationships && deploy.getCacheOptions().isReadOnly();
    this.cacheHelp = new BeanDescriptorCacheHelp<T>(this, owner.getCacheManager(), deploy.getCacheOptions(), cacheSharableBeans, propertiesOneImported);

    
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

    // derive the index position of the Id and Version properties
    if (Modifier.isAbstract(beanType.getModifiers())) {
      this.idPropertyIndex = -1;
      this.versionPropertyIndex = -1;
      this.unloadProperties = new int[0];
      
    } else {
      EntityBeanIntercept ebi = prototypeEntityBean._ebean_getIntercept();
      this.idPropertyIndex = (idProperty == null) ? -1 : ebi.findProperty(idProperty.getName());
      this.versionPropertyIndex = (versionProperty == null) ? -1 : ebi.findProperty(versionProperty.getName());
      this.unloadProperties = derivePropertiesToUnload(prototypeEntityBean);
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
    for (int i = 0; i < pos; i++) {
      unload[i] = props[i];
    }
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
      return (EntityBean) beanType.newInstance();
    } catch (Exception e) {
      throw new IllegalStateException("Error trying to create the prototypeEntityBean for "+beanType, e);
    }
  }
  
  private LinkedHashMap<String, BeanProperty> getReverseMap(LinkedHashMap<String, BeanProperty> propMap) {

    LinkedHashMap<String, BeanProperty> revMap = new LinkedHashMap<String, BeanProperty>(propMap.size() * 2);

    for (BeanProperty prop : propMap.values()) {
      if (prop.getDbColumn() != null) {
        revMap.put(prop.getDbColumn(), prop);
      }
    }

    return revMap;
  }

  /**
   * Set the server. Primarily so that the Many's can lazy load.
   */
  public void setEbeanServer(SpiEbeanServer ebeanServer) {
    this.ebeanServer = ebeanServer;
    for (int i = 0; i < propertiesMany.length; i++) {
      // used for creating lazy loading lists etc
      propertiesMany[i].setLoader(ebeanServer);
    }
  }

  /**
   * Determine the concurrency mode based on the existence of a non-null version
   * property value.
   */
  public ConcurrencyMode determineConcurrencyMode(EntityBean bean) {

    if (versionProperty == null) {
      return ConcurrencyMode.NONE;
    }
    Object v = versionProperty.getValue(bean);
    return (v == null) ? ConcurrencyMode.NONE : ConcurrencyMode.VERSION;
  }

  /**
   * Return the Set of embedded beans that have changed.
   */
  public Set<String> getDirtyEmbeddedProperties(EntityBean bean) {

    HashSet<String> dirtyProperties = null;

    for (int i = 0; i < propertiesEmbedded.length; i++) {
      Object embValue = propertiesEmbedded[i].getValue(bean);
      if (embValue instanceof EntityBean) {
        if (((EntityBean) embValue)._ebean_getIntercept().isDirty()) {
          // this embedded is dirty so should be included in an update
          if (dirtyProperties == null) {
            dirtyProperties = new HashSet<String>();
          }
          dirtyProperties.add(propertiesEmbedded[i].getName());
        }
      } else {
        // must assume it is dirty
        if (dirtyProperties == null) {
          dirtyProperties = new HashSet<String>();
        }
        dirtyProperties.add(propertiesEmbedded[i].getName());
      }
    }

    return dirtyProperties;
  }

  /**
   * Return the EbeanServer instance that owns this BeanDescriptor.
   */
  public SpiEbeanServer getEbeanServer() {
    return ebeanServer;
  }

  /**
   * Return the type of this domain object.
   */
  public EntityType getEntityType() {
    return entityType;
  }

  public int getPropertyCount() {
    return propertyCount;
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
   */
  public void initialiseId() {

    if (logger.isTraceEnabled()) {
      logger.trace("BeanDescriptor initialise " + fullName);
    }

    if (inheritInfo != null) {
      inheritInfo.setDescriptor(this);
    }

    if (isEmbedded()) {
      // initialise all the properties
      Iterator<BeanProperty> it = propertiesAll();
      while (it.hasNext()) {
        BeanProperty prop = it.next();
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
   */
  public void initialiseOther() {

    if (!isEmbedded()) {
      // initialise all the non-id properties
      Iterator<BeanProperty> it = propertiesAll();
      while (it.hasNext()) {
        BeanProperty prop = it.next();
        if (!prop.isId()) {
          prop.initialise();
        }
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
    deleteByIdInSql = "delete from " + baseTable + " where " + idBinderInLHSSqlNoAlias + " ";

    if (!isEmbedded()) {
      // parse every named update up front into sql dml
      for (DeployNamedUpdate namedUpdate : namedUpdates.values()) {
        DeployUpdateParser parser = new DeployUpdateParser(this);
        namedUpdate.initialise(parser);
      }
    }
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

  /**
   * Initialise the cache once the server has started.
   */
  public void cacheInitialise() {
    cacheHelp.initialise();
  }

  protected boolean hasInheritance() {
    return inheritInfo != null;
  }

  public SqlUpdate deleteById(Object id, List<Object> idList) {
    if (id != null) {
      return deleteById(id);
    } else {
      return deleteByIdList(idList);
    }
  }

  /**
   * Return SQL that can be used to delete a list of Id's without any optimistic
   * concurrency checking.
   */
  private SqlUpdate deleteByIdList(List<Object> idList) {

    StringBuilder sb = new StringBuilder(deleteByIdInSql);
    String inClause = idBinder.getIdInValueExprDelete(idList.size());
    sb.append(inClause);

    DefaultSqlUpdate delete = new DefaultSqlUpdate(sb.toString());
    for (int i = 0; i < idList.size(); i++) {
      idBinder.bindId(delete, idList.get(i));
    }
    return delete;
  }

  /**
   * Return SQL that can be used to delete by Id without any optimistic
   * concurrency checking.
   */
  private SqlUpdate deleteById(Object id) {

    DefaultSqlUpdate sqlDelete = new DefaultSqlUpdate(deleteByIdSql);

    Object[] bindValues = idBinder.getBindValues(id);
    for (int i = 0; i < bindValues.length; i++) {
      sqlDelete.addParameter(bindValues[i]);
    }

    return sqlDelete;
  }

  /**
   * Add objects to ElPropertyDeploy etc. These are used so that expressions on
   * foreign keys don't require an extra join.
   */
  public void add(BeanFkeyProperty fkey) {
    fkeyMap.put(fkey.getName(), fkey);
  }

  public void initialiseFkeys() {
    for (int i = 0; i < propertiesOneImported.length; i++) {
      propertiesOneImported[i].addFkey();
    }
  }

  public boolean calculateUseCache(Boolean queryUseCache) {
    return (queryUseCache != null) ? queryUseCache.booleanValue() : isBeanCaching();
  }

  public T cacheNaturalKey(SpiQuery<T> query, SpiTransaction t) {
    return cacheHelp.naturalKeyLookup(query, t);
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
   * Execute the warming cache query (if defined) and load the cache.
   */
  public void runCacheWarming() {
    cacheHelp.runCacheWarming(ebeanServer);
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
   * Return the default select clause already parsed into an ordered Set.
   */
  public Set<String> getDefaultSelectClauseSet() {
    return defaultSelectClauseSet;
  }

  /**
   * Return true if this object is the root level object in its entity
   * inheritance.
   */
  public boolean isInheritanceRoot() {
    return inheritInfo == null || inheritInfo.isRoot();
  }

  /**
   * Return true if there is currently query caching for this type of bean.
   */
  public boolean isQueryCaching() {
    return cacheHelp.isQueryCaching();
  }

  /**
   * Return true if there is currently bean caching for this type of bean.
   */
  public boolean isBeanCaching() {
    return cacheHelp.isBeanCaching();
  }

  public boolean isManyPropCaching() {
    return isBeanCaching();
  }

  /**
   * Return true if the persist request needs to notify the cache.
   */
  public boolean isCacheNotify() {
    return cacheHelp.isCacheNotify();
  }

  /**
   * Clear the query cache.
   */
  public void queryCacheClear() {
    cacheHelp.queryCacheClear();
  }

  /**
   * Get a query result from the query cache.
   */
  public BeanCollection<T> queryCacheGet(Object id) {
    return cacheHelp.queryCacheGet(id);
  }

  /**
   * Put a query result into the query cache.
   */
  public void queryCachePut(Object id, BeanCollection<T> query) {
    cacheHelp.queryCachePut(id, query);
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

  public void cacheManyPropRemove(Object parentId, String propertyName) {
    cacheHelp.manyPropRemove(parentId, propertyName);
  }

  public void cacheManyPropClear(String propertyName) {
    cacheHelp.manyPropClear(propertyName);    
  }

  /**
   * Return the CachedManyIds for a given bean and property. Returns null if not in the cache.
   */
  public CachedManyIds cacheManyPropGet(Object parentId, String propertyName) {
    return cacheHelp.manyPropGet(parentId, propertyName);
  }

  /**
   * Clear the bean cache.
   */
  public void cacheBeanClear() {
    cacheHelp.beanCacheClear();
  }

  public void cacheBeanPut(T bean) {
    cacheBeanPutData((EntityBean)bean);
  }
  
  /**
   * Extract the raw cache data from the bean.
   */
  public CachedBeanData cacheBeanExtractData(EntityBean bean) {
    return cacheHelp.beanExtractData(bean);
  }
  
  /**
   * Load the raw cache data into the bean.
   */
  public void cacheBeanLoadData(EntityBean bean, CachedBeanData data) {
    cacheHelp.beanLoadData(bean, data);
  }
  
  /**
   * Put a bean into the bean cache.
   */
  public void cacheBeanPutData(EntityBean bean) {
    cacheHelp.beanCachePut(bean);
  }

  /**
   * Return a bean from the bean cache (or null).
   */
  public T cacheBeanGet(SpiQuery<T> query, PersistenceContext context) {
    return cacheHelp.beanCacheGet(query, context);
  }

  /**
   * Remove a bean from the cache given its Id.
   */
  public void cacheBeanRemove(Object id) {
    cacheHelp.beanCacheRemove(id);
  }
  
  /**
   * Returns true if it managed to populate/load the bean from the cache.
   */
  public boolean cacheBeanLoad(EntityBean bean, EntityBeanIntercept ebi, Object id) {
    return cacheHelp.beanCacheLoad(bean, ebi, id);
  }
  
  /**
   * Returns true if it managed to populate/load the bean from the cache.
   */
  public boolean cacheBeanLoad(EntityBeanIntercept ebi) {
    EntityBean bean = ebi.getOwner();
    Object id = getId(bean);
    return cacheBeanLoad(bean, ebi, id);
  }

  /**
   * Try to hit the cache using the natural key.
   */
  public T cacheNaturalKeyLookup(SpiQuery<T> query, SpiTransaction t) {
    return cacheHelp.naturalKeyLookup(query, t);
  }

  /**
   * Invalidate parts of cache due to SqlUpdate or external modification etc.
   */
  public void cacheHandleBulkUpdate(TableIUD tableIUD) {
    cacheHelp.handleBulkUpdate(tableIUD);    
  }

  /**
   * Remove a bean from the cache given its Id.
   */
  public void cacheHandleDelete(Object id, PersistRequestBean<T> deleteRequest) {
    cacheHelp.handleDelete(id, deleteRequest);
  }

  public void cacheHandleInsert(Object id, PersistRequestBean<T> insertRequest) {
    cacheHelp.handleInsert(id, insertRequest);
  }

  /**
   * Update the cached bean data.
   */
  public void cacheHandleUpdate(Object id, PersistRequestBean<T> updateRequest) {
    cacheHelp.handleUpdate(id, updateRequest);
  }
  
  /**
   * Return the base table alias. This is always the first letter of the bean
   * name.
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
    List<MetaQueryPlanStatistic> list = new ArrayList<MetaQueryPlanStatistic>(queryPlanCache.size());
    for (CQueryPlan queryPlan :  queryPlanCache.values()) {
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
    Iterator<CQueryPlan> it = queryPlanCache.values().iterator();
    while (it.hasNext()) {
      CQueryPlan queryPlan = (CQueryPlan) it.next();
      queryPlan.resetStatistics();
    }
  }

  /**
   * Execute the postLoad if a BeanPersistController exists for this bean.
   */
  @SuppressWarnings("unchecked")
  public void postLoad(Object bean, Set<String> includedProperties) {
    BeanPersistController c = persistController;
    if (c != null) {
      c.postLoad((T) bean, includedProperties);
    }
  }

  /**
   * Return the query plans for this BeanDescriptor.
   */
  public Iterator<CQueryPlan> queryPlans() {
    return queryPlanCache.values().iterator();
  }

  public CQueryPlan getQueryPlan(HashQueryPlan key) {
    return queryPlanCache.get(key);
  }

  public void putQueryPlan(HashQueryPlan key, CQueryPlan plan) {
    queryPlanCache.put(key, plan);
  }

  /**
   * Get a UpdatePlan for a given hash.
   */
  public SpiUpdatePlan getUpdatePlan(Integer key) {
    return updatePlanCache.get(key);
  }

  /**
   * Add a UpdatePlan to the cache with a given hash.
   */
  public void putUpdatePlan(Integer key, SpiUpdatePlan plan) {
    updatePlanCache.put(key, plan);
  }

  /**
   * Return the TypeManager.
   */
  public TypeManager getTypeManager() {
    return typeManager;
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
   * Return the many property included in the query or null if one is not.
   */
  public BeanPropertyAssocMany<?> getManyProperty(SpiQuery<?> query) {

    OrmQueryDetail detail = query.getDetail();
    for (int i = 0; i < propertiesMany.length; i++) {
      if (detail.includes(propertiesMany[i].getName())) {
        return propertiesMany[i];
      }
    }

    return null;
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

  /**
   * Return a named query.
   */
  public DeployNamedQuery getNamedQuery(String name) {
    return namedQueries.get(name);
  }

  public DeployNamedQuery addNamedQuery(DeployNamedQuery deployNamedQuery) {
    return namedQueries.put(deployNamedQuery.getName(), deployNamedQuery);
  }

  /**
   * Return a named update.
   */
  public DeployNamedUpdate getNamedUpdate(String name) {
    return namedUpdates.get(name);
  }

  /**
   * Creates a new EntityBean.
   */
  public EntityBean createEntityBean() {
    try {
      EntityBean bean = (EntityBean)prototypeEntityBean._ebean_newInstance();
      
      if (unloadProperties.length > 0) {
        // 'unload' any properties initialised in the default constructor
        EntityBeanIntercept ebi = bean._ebean_getIntercept();
        for (int i = 0; i < unloadProperties.length; i++) {
          ebi.setPropertyUnloaded(unloadProperties[i]);
        }
      }
      return bean;
      
    } catch (Exception ex) {
      throw new PersistenceException(ex);
    }
  }

  /**
   * Create a reference bean based on the id.
   */
  @SuppressWarnings("unchecked")
  public T createReference(Boolean readOnly, Object id) {

    if (cacheSharableBeans && !Boolean.FALSE.equals(readOnly)) {
      CachedBeanData d = (CachedBeanData) cacheHelp.beanCacheGetData(id);
      if (d != null) {
        Object shareableBean = d.getSharableBean();
        if (shareableBean != null) {
          return (T) shareableBean;
        }
      }
    }
    try {
      EntityBean eb = createEntityBean();

      convertSetId(id, eb);

      EntityBeanIntercept ebi = eb._ebean_getIntercept();
      ebi.setBeanLoaderByServerName(ebeanServer.getName());


      // Note: not creating proxies for many's...
      ebi.setReference(idPropertyIndex);

      return (T) eb;

    } catch (Exception ex) {
      throw new PersistenceException(ex);
    }
  }

  /**
   * Return the BeanProperty for the given deployment name.
   */
  public BeanProperty getBeanPropertyFromDbColumn(String dbColumn) {
    return propMapByDbColumn.get(dbColumn);
  }

  /**
   * Return the bean property traversing the object graph and taking into
   * account inheritance.
   */
  public BeanProperty getBeanPropertyFromPath(String path) {

    String[] split = SplitName.splitBegin(path);
    if (split[1] == null) {
      return _findBeanProperty(split[0]);
    }
    BeanPropertyAssoc<?> assocProp = (BeanPropertyAssoc<?>) _findBeanProperty(split[0]);
    BeanDescriptor<?> targetDesc = assocProp.getTargetDescriptor();

    return targetDesc.getBeanPropertyFromPath(split[1]);
  }

  /**
   * Return the BeanDescriptor for a given path of Associated One or Many beans.
   */
  public BeanDescriptor<?> getBeanDescriptor(String path) {
    if (path == null) {
      return this;
    }
    String[] splitBegin = SplitName.splitBegin(path);

    BeanProperty beanProperty = propMap.get(splitBegin[0]);
    if (beanProperty instanceof BeanPropertyAssoc<?>) {
      BeanPropertyAssoc<?> assocProp = (BeanPropertyAssoc<?>) beanProperty;
      return assocProp.getTargetDescriptor().getBeanDescriptor(splitBegin[1]);

    } else {
      throw new RuntimeException("Error getting BeanDescriptor for path " + path + " from " + getFullName());
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
    if (unidirectional != null) {
      return unidirectional;
    }
    if (inheritInfo != null && !inheritInfo.isRoot()) {
      return inheritInfo.getParent().getBeanDescriptor().getUnidirectional();
    }
    return null;
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
   * Return the alternate "Id" that identifies this BeanDescriptor. This is an
   * alternative to using the bean class name.
   */
  public String getDescriptorId() {
    return descriptorId;
  }

  /**
   * Return the class type this BeanDescriptor describes.
   */
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
  public String getFullName() {
    return fullName;
  }

  /**
   * Return the short name of the entity bean.
   */
  public String getName() {
    return name;
  }

  /**
   * Summary description.
   */
  public String toString() {
    return fullName;
  }

  /**
   * Helper method to return the unique property. If only one property makes up
   * the unique id then it's value is returned. If there is a concatenated
   * unique id then a Map is built with the keys being the names of the
   * properties that make up the unique id.
   */
  public Object getId(EntityBean bean) {
    return (idProperty == null) ? null : idProperty.getValue(bean);
  }

  /**
   * Return false if the id is a simple scalar and false if it is embedded or
   * concatenated.
   */
  public boolean isComplexId() {
    return idBinder.isComplexId();
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
    return idBinder.convertSetId(idValue, null);
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

  /**
   * Get a BeanProperty by its name.
   */
  public BeanProperty getBeanProperty(String propName) {
    return (BeanProperty) propMap.get(propName);
  }

  public void sort(List<T> list, String sortByClause) {

    ElComparator<T> comparator = getElComparator(sortByClause);
    Collections.sort(list, comparator);
  }

  public ElComparator<T> getElComparator(String propNameOrSortBy) {
    ElComparator<T> c = comparatorCache.get(propNameOrSortBy);
    if (c == null) {
      c = createComparator(propNameOrSortBy);
      comparatorCache.put(propNameOrSortBy, c);
    }
    return c;
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
    String lazyLoadPropertyName = ebi.getProperty(lazyLoadProperty);
    BeanProperty lazyLoadBeanProp = getBeanProperty(lazyLoadPropertyName);

    if (lazyLoadBeanProp instanceof BeanPropertyAssocMany<?>) {
      BeanPropertyAssocMany<?> manyProp = (BeanPropertyAssocMany<?>) lazyLoadBeanProp;
      manyProp.createReference(ebi.getOwner());
      ebi.setLoadedLazy();
      return true;
    }

    return false;
  }

  /**
   * Return a Comparator for local sorting of lists.
   * 
   * @param sortByClause
   *          list of property names with optional ASC or DESC suffix.
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

    List<Property> sortProps = sortBy.getProperties();
    for (int i = 0; i < sortProps.size(); i++) {
      Property sortProperty = sortProps.get(i);
      comparators[i] = createPropertyComparator(sortProperty);
    }

    return new ElComparatorCompound<T>(comparators);
  }

  private ElComparator<T> createPropertyComparator(Property sortProp) {

    ElPropertyValue elGetValue = getElGetValue(sortProp.getName());

    Boolean nullsHigh = sortProp.getNullsHigh();
    if (nullsHigh == null) {
      nullsHigh = Boolean.TRUE;
    }
    return new ElComparatorProperty<T>(elGetValue, sortProp.isAscending(), nullsHigh);
  }

  /**
   * Get an Expression language Value object.
   */
  public ElPropertyValue getElGetValue(String propName) {
    return getElPropertyValue(propName, false);
  }

  /**
   * Similar to ElPropertyValue but also uses foreign key shortcuts.
   * <p>
   * The foreign key shortcuts means we can avoid unnecessary joins.
   * </p>
   */
  public ElPropertyDeploy getElPropertyDeploy(String propName) {
    ElPropertyDeploy fk = fkeyMap.get(propName);
    if (fk != null) {
      return fk;
    }
    return getElPropertyValue(propName, true);
  }

  private ElPropertyValue getElPropertyValue(String propName, boolean propertyDeploy) {
    ElPropertyValue elGetValue = elGetCache.get(propName);
    if (elGetValue == null) {
      // need to build it potentially navigating the BeanDescriptors
      elGetValue = buildElGetValue(propName, null, propertyDeploy);
      if (elGetValue == null) {
        return null;
      }
      if (elGetValue instanceof BeanFkeyProperty) {
        fkeyMap.put(propName, (BeanFkeyProperty) elGetValue);
      } else {
        elGetCache.put(propName, elGetValue);
      }
    }
    return elGetValue;
  }

  protected ElPropertyValue buildElGetValue(String propName, ElPropertyChainBuilder chain, boolean propertyDeploy) {

    if (propertyDeploy && chain != null) {
      BeanFkeyProperty fk = fkeyMap.get(propName);
      if (fk != null) {
        return fk.create(chain.getExpression());
      }
    }

    int basePos = propName.indexOf('.');
    if (basePos > -1) {
      // nested or embedded property
      String baseName = propName.substring(0, basePos);
      String remainder = propName.substring(basePos + 1);

      BeanProperty assocProp = _findBeanProperty(baseName);
      if (assocProp == null) {
        return null;
      }
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
      chain.setContainsMany(true);
    }
    return chain.add(property).build();
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

  protected Object getBeanPropertyWithInheritance(EntityBean bean, String propName) {

    BeanDescriptor<?> desc = getBeanDescriptor(bean.getClass());
    BeanProperty beanProperty = desc.findBeanProperty(propName);
    return beanProperty.getValue(bean);
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
   * Return true if queries for beans of this type are autoFetch tunable.
   */
  public boolean isAutoFetchTunable() {
    return autoFetchTunable;
  }

  /**
   * Returns the Inheritance mapping information. This will be null if this type
   * of bean is not involved in any ORM inheritance mapping.
   */
  public InheritInfo getInheritInfo() {
    return inheritInfo;
  }

  /**
   * Return true if this is an embedded bean.
   */
  public boolean isEmbedded() {
    return EntityType.EMBEDDED.equals(entityType);
  }

  public boolean isBaseTableType() {
    return EntityType.ORM.equals(entityType);
  }

  /**
   * Return the concurrency mode used for beans of this type.
   */
  public ConcurrencyMode getConcurrencyMode() {
    return concurrencyMode;
  }

  /**
   * Return the tables this bean is dependent on. This implies that if any of
   * these tables are modified then cached beans may be invalidated.
   */
  public String[] getDependantTables() {
    return dependantTables;
  }

  /**
   * Return the compound unique constraints.
   */
  public CompoundUniqueContraint[] getCompoundUniqueConstraints() {
    return compoundUniqueConstraints;
  }

  /**
   * Return the beanListener.
   */
  public BeanPersistListener<T> getPersistListener() {
    return persistListener;
  }

  /**
   * Return the beanFinder. Usually null unless overriding the finder.
   */
  public BeanFinder<T> getBeanFinder() {
    return beanFinder;
  }

  /**
   * Return the BeanQueryAdapter or null if none is defined.
   */
  public BeanQueryAdapter getQueryAdapter() {
    return queryAdapter;
  }

  /**
   * De-register the BeanPersistListener.
   */
  @SuppressWarnings("unchecked")
  public void deregister(BeanPersistListener<?> listener) {
    // volatile read...
    BeanPersistListener<T> currListener = persistListener;
    if (currListener == null) {
      // nothing to deregister
    } else {
      BeanPersistListener<T> deregListener = (BeanPersistListener<T>) listener;
      if (currListener instanceof ChainedBeanPersistListener<?>) {
        // remove it from the existing chain
        persistListener = ((ChainedBeanPersistListener<T>) currListener).deregister(deregListener);
      } else if (currListener.equals(deregListener)) {
        persistListener = null;
      }
    }
  }

  /**
   * De-register the BeanPersistController.
   */
  public void deregister(BeanPersistController controller) {
    // volatile read...
    BeanPersistController c = persistController;
    if (c == null) {
      // nothing to deregister
    } else {
      if (c instanceof ChainedBeanPersistController) {
        // remove it from the existing chain
        persistController = ((ChainedBeanPersistController) c).deregister(controller);
      } else if (c.equals(controller)) {
        persistController = null;
      }
    }
  }

  /**
   * Register the new BeanPersistController.
   */
  @SuppressWarnings("unchecked")
  public void register(BeanPersistListener<?> newPersistListener) {

    if (!PersistListenerManager.isRegisterFor(beanType, newPersistListener)) {
      // skip
    } else {
      BeanPersistListener<T> newListener = (BeanPersistListener<T>) newPersistListener;
      // volatile read...
      BeanPersistListener<T> currListener = persistListener;
      if (currListener == null) {
        persistListener = newListener;
      } else {
        if (currListener instanceof ChainedBeanPersistListener<?>) {
          // add it to the existing chain
          persistListener = ((ChainedBeanPersistListener<T>) currListener).register(newListener);
        } else {
          // build new chain of the 2
          persistListener = new ChainedBeanPersistListener<T>(currListener, newListener);
        }
      }
    }
  }

  /**
   * Register the new BeanPersistController.
   */
  public void register(BeanPersistController newController) {

    if (!newController.isRegisterFor(beanType)) {
      // skip
    } else {
      // volatile read...
      BeanPersistController c = persistController;
      if (c == null) {
        persistController = newController;
      } else {
        if (c instanceof ChainedBeanPersistController) {
          // add it to the existing chain
          persistController = ((ChainedBeanPersistController) c).register(newController);
        } else {
          // build new chain of the 2
          persistController = new ChainedBeanPersistController(c, newController);
        }
      }
    }
  }

  /**
   * Return the Controller.
   */
  public BeanPersistController getPersistController() {
    return persistController;
  }

  /**
   * Returns true if this bean is based on a table (or possibly view) and
   * returns false if this bean is based on a raw sql select statement.
   * <p>
   * When false querying this bean is based on a supplied sql select statement
   * placed in the orm xml file (as opposed to Ebean generated sql).
   * </p>
   */
  public boolean isSqlSelectBased() {
    return EntityType.SQL.equals(entityType);
  }

  /**
   * Return the base table. Only properties mapped to the base table are by
   * default persisted.
   */
  public String getBaseTable() {
    return baseTable;
  }

  /**
   * Get a named extra attribute.
   */
  public String getExtraAttribute(String key) {
    return (String) extraAttrMap.get(key);
  }

  /**
   * Return the identity generation type.
   */
  public IdType getIdType() {
    return idType;
  }

  /**
   * Return the sequence name.
   */
  public String getSequenceName() {
    return sequenceName;
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
   * Return the IdGenerator.
   */
  public IdGenerator getIdGenerator() {
    return idGenerator;
  }

  /**
   * Return the includes for getReference().
   */
  public String getLazyFetchIncludes() {
    return lazyFetchIncludes;
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

  /**
   * Return an Iterator of all BeanProperty. This includes transient properties.
   */
  public Iterator<BeanProperty> propertiesAll() {
    return propMap.values().iterator();
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

  public BeanProperty getIdProperty() {
    return idProperty;
  }

  public boolean isInsertMode(EntityBeanIntercept ebi) {
    
    if (ebi.isLoaded()) {
      return false;
    }
    
    // determine based on Id property
    if (idProperty.isEmbedded()) {
      return !ebi.isLoaded();
    }
    //if (idGenerator == null) {
    //  return !ebi.isLoaded();
    //} else {
      return !hasIdProperty(ebi);
    //}
  }
  
  public boolean isReference(EntityBeanIntercept ebi) {
    return ebi.isReference() || hasIdPropertyOnly(ebi);
  }
  
  public boolean hasIdPropertyOnly(EntityBeanIntercept ebi) {
    return ebi.hasIdOnly(idPropertyIndex);
  }
  
  public boolean hasIdProperty(EntityBeanIntercept ebi) {
    if (idPropertyIndex > -1) {
      return ebi.isLoadedProperty(idPropertyIndex);
    }
    return false;
  }

  public boolean hasVersionProperty(EntityBeanIntercept ebi) {
    if (versionPropertyIndex > -1) {
      return ebi.isLoadedProperty(versionPropertyIndex);
    }
    return false;
  }

  public ConcurrencyMode getConcurrencyMode(EntityBeanIntercept ebi) {
    if (!hasVersionProperty(ebi)) {
      return ConcurrencyMode.NONE;
    } else {
      return concurrencyMode;     
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
   * Returns OneToOnes that are on the exported side of a OneToOne.
   * <p>
   * These associations do not own the relationship.
   * </p>
   */
  public BeanPropertyAssocOne<?>[] propertiesOneExported() {
    return propertiesOneExported;
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

  private Set<String> deriveManyPropNames() {

    LinkedHashSet<String> names = new LinkedHashSet<String>();
    for (int i = 0; i < propertiesMany.length; i++) {
      names.add(propertiesMany[i].getName());
    }

    return Collections.unmodifiableSet(names);
  }

  /**
   * Return a hash of the names of the many properties on this bean type. This
   * is used for efficient building of included properties sets for partial
   * objects.
   */
  public int getNamesOfManyPropsHash() {
    return namesOfManyPropsHash;
  }

  /**
   * Returns the set of many property names for this bean type.
   */
  public Set<String> getNamesOfManyProps() {
    return namesOfManyProps;
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
   * Return true if this is an Update (rather than insert) given that the bean
   * is involved in a stateless update.
   */
  public boolean isStatelessUpdate(EntityBean bean) {
    if (versionProperty == null) {
      Object versionValue = getId(bean);
      return !DmlUtil.isNullOrZero(versionValue);
    } else {
      Object versionValue = versionProperty.getValue(bean);
      return !DmlUtil.isNullOrZero(versionValue);
    }
  }

  /**
   * Scalar properties without the unique id or secondary table properties.
   */
  public BeanProperty[] propertiesBaseScalar() {
    return propertiesBaseScalar;
  }

  /**
   * Return properties that are immutable compound value objects.
   * <p>
   * These are compound types but are not enhanced (Embedded are enhanced).
   * </p>
   */
  public BeanPropertyCompound[] propertiesBaseCompound() {
    return propertiesBaseCompound;
  }

  /**
   * Return the properties local to this type for inheritance.
   */
  public BeanProperty[] propertiesLocal() {
    return propertiesLocal;
  }

  public void jsonWrite(WriteJsonContext ctx, EntityBean bean) {

    if (bean != null) {

      ctx.appendObjectBegin();
      WriteBeanState prevState = ctx.pushBeanState(bean);

      if (inheritInfo != null) {
        InheritInfo localInheritInfo = inheritInfo.readType(bean.getClass());
        String discValue = localInheritInfo.getDiscriminatorStringValue();
        String discColumn = localInheritInfo.getDiscriminatorColumn();
        ctx.appendDiscriminator(discColumn, discValue);

        BeanDescriptor<?> localDescriptor = localInheritInfo.getBeanDescriptor();
        localDescriptor.jsonWriteProperties(ctx, bean);

      } else {
        jsonWriteProperties(ctx, bean);
      }

      ctx.pushPreviousState(prevState);
      ctx.appendObjectEnd();
    }
  }

  @SuppressWarnings("unchecked")
  private void jsonWriteProperties(WriteJsonContext ctx, EntityBean bean) {

    JsonWriteBeanVisitor<T> beanVisitor = (JsonWriteBeanVisitor<T>) ctx.getBeanVisitor();

    Set<String> props = ctx.getIncludeProperties();

    boolean explicitAllProps;
    if (props == null) {
      explicitAllProps = false;
    } else {
      explicitAllProps = props.contains("*");
      if (explicitAllProps || props.isEmpty()) {
        props = null;
      }
    }

    if (idProperty != null) {
      Object idValue = idProperty.getValue(bean);
      if (idValue != null) {
        if (props == null || props.contains(idProperty.getName())) {
          idProperty.jsonWrite(ctx, bean);
        }
      }
    }

    if (!explicitAllProps && props == null) {
      // just render the loaded properties
      props = ((EntityBean)bean)._ebean_getIntercept().getLoadedPropertyNames();
    }
    if (props != null) {
      // render only the appropriate properties (when not all properties)
      for (String prop : props) {
        BeanProperty p = getBeanProperty(prop);
        if (p != null && !p.isId()) {
          p.jsonWrite(ctx, bean);
        }
      }
    } else {
      if (explicitAllProps || !isReference(bean._ebean_getIntercept())) {
        // render all the properties and invoke lazy loading if required
        for (int j = 0; j < propertiesNonTransient.length; j++) {
          propertiesNonTransient[j].jsonWrite(ctx, bean);
        }
        for (int j = 0; j < propertiesTransient.length; j++) {
          propertiesTransient[j].jsonWrite(ctx, bean);
        }
      }
    }

    if (beanVisitor != null) {
      beanVisitor.visit((T) bean, ctx);
    }
  }

  @SuppressWarnings("unchecked")
  public T jsonReadBean(ReadJsonContext ctx, String path) {
    ReadBeanState beanState = jsonRead(ctx, path);
    if (beanState == null) {
      return null;
    } else {
      return (T) beanState.getBean();
    }
  }

  public ReadBeanState jsonRead(ReadJsonContext ctx, String path) {
    if (!ctx.readObjectBegin()) {
      // the object is null
      return null;
    }

    if (inheritInfo == null) {
      return jsonReadObject(ctx, path);

    } else {

      // check for the discriminator value to determine the correct sub type
      String discColumn = inheritInfo.getRoot().getDiscriminatorColumn();

      if (!ctx.readKeyNext()) {
        String msg = "Error reading inheritance discriminator - expected [" + discColumn + "] but no json key?";
        throw new TextException(msg);
      }
      
      String propName = ctx.getTokenKey();      
      String discValue;
      if (propName.equalsIgnoreCase(discColumn)) {
        discValue = ctx.readScalarValue(); 
        if (!ctx.readValueNext()) {
          // Expected to read a comma to setup for reading the real properties of the bean 
          String msg = "Error reading inheritance discriminator [" + discColumn + "]. Expected more json name values?";
          throw new TextException(msg);
        }
        
      } else {
        // Assume that the we are just reading using this bean type
        // Push the token key back so that it is re-read as it is one 
        // of the real properties of the bean itself
        ctx.pushTokenKey();
        discValue = inheritInfo.getDiscriminatorStringValue();
      }

      // determine the sub type for this particular json object
      InheritInfo localInheritInfo = inheritInfo.readType(discValue);
      BeanDescriptor<?> localDescriptor = localInheritInfo.getBeanDescriptor();
      return localDescriptor.jsonReadObject(ctx, path);
    }
  }
  
  private ReadBeanState jsonReadObject(ReadJsonContext ctx, String path) {

    EntityBean bean = createEntityBean();
    ctx.pushBean(bean, path, this);

    do {
      if (!ctx.readKeyNext()) {
        break;
      } else {
        // we read a property key ...
        String propName = ctx.getTokenKey();
        BeanProperty p = getBeanProperty(propName);
        if (p != null) {
          p.jsonRead(ctx, bean);
          ctx.setProperty(propName);
        } else {
          // unknown property key ...
          ctx.readUnmappedJson(propName);
        }

        if (!ctx.readValueNext()) {
          break;
        }
      }
    } while (true);

    return ctx.popBeanState();
  }

  public void flushPersistenceContextOnIterate(PersistenceContext persistenceContext) {
    persistenceContext.clear(beanType);
    for (int i = 0; i < propertiesMany.length; i++) {
      persistenceContext.clear(propertiesMany[i].getBeanDescriptor().getBeanType());
    }
  }
    
}
