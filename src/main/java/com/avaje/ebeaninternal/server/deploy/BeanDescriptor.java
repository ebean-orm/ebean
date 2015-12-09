package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.OrderBy;
import com.avaje.ebean.PersistenceContextScope;
import com.avaje.ebean.Query;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.ValuePair;
import com.avaje.ebean.annotation.ConcurrencyMode;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebean.config.EncryptKey;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.IdGenerator;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebean.event.BeanFindController;
import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanPersistListener;
import com.avaje.ebean.event.BeanPostLoad;
import com.avaje.ebean.event.BeanQueryAdapter;
import com.avaje.ebean.event.changelog.BeanChange;
import com.avaje.ebean.event.changelog.ChangeLogFilter;
import com.avaje.ebean.event.changelog.ChangeType;
import com.avaje.ebean.event.readaudit.ReadAuditLogger;
import com.avaje.ebean.event.readaudit.ReadAuditPrepare;
import com.avaje.ebean.event.readaudit.ReadEvent;
import com.avaje.ebean.meta.MetaBeanInfo;
import com.avaje.ebean.meta.MetaQueryPlanStatistic;
import com.avaje.ebean.plugin.SpiBeanType;
import com.avaje.ebeaninternal.api.HashQueryPlan;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.SpiUpdatePlan;
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeaninternal.server.cache.CachedBeanData;
import com.avaje.ebeaninternal.server.core.CacheOptions;
import com.avaje.ebeaninternal.server.core.DefaultSqlUpdate;
import com.avaje.ebeaninternal.server.core.DiffHelp;
import com.avaje.ebeaninternal.server.core.InternString;
import com.avaje.ebeaninternal.server.core.PersistRequest;
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
import com.avaje.ebeaninternal.server.text.json.ReadJson;
import com.avaje.ebeaninternal.server.text.json.WriteJson;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.util.SortByClause;
import com.avaje.ebeaninternal.util.SortByClause.Property;
import com.avaje.ebeaninternal.util.SortByClauseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Describes Beans including their deployment information.
 */
public class BeanDescriptor<T> implements MetaBeanInfo, SpiBeanType<T> {

  private static final Logger logger = LoggerFactory.getLogger(BeanDescriptor.class);

  private final ConcurrentHashMap<Integer, SpiUpdatePlan> updatePlanCache = new ConcurrentHashMap<Integer, SpiUpdatePlan>();

  private final ConcurrentHashMap<HashQueryPlan, CQueryPlan> queryPlanCache = new ConcurrentHashMap<HashQueryPlan, CQueryPlan>();

  private final ConcurrentHashMap<String, ElPropertyValue> elCache = new ConcurrentHashMap<String, ElPropertyValue>();

  private final ConcurrentHashMap<String, ElPropertyDeploy> elDeployCache = new ConcurrentHashMap<String, ElPropertyDeploy>();

  private final ConcurrentHashMap<String, ElComparator<T>> comparatorCache = new ConcurrentHashMap<String, ElComparator<T>>();

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

  private final boolean idTypePlatformDefault;

  private final IdGenerator idGenerator;

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

  private final CompoundUniqueConstraint[] compoundUniqueConstraints;

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

  private final BeanProperty draft;

  private final BeanProperty draftDirty;

  /**
   * Map of BeanProperty Linked so as to preserve order.
   */
  private final LinkedHashMap<String, BeanProperty> propMap;

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
   * Intercept pre post on insert,update, and delete .
   */
  private volatile BeanPersistController persistController;

  private final BeanPostLoad beanPostLoad;

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
  
  /**
   * Properties that are initialised in the constructor need to be 'unloaded' to support partial object queries.
   */
  private final int[] unloadProperties;
  
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

  private final EntityBean prototypeEntityBean;
  
  private final IdBinder idBinder;

  private String idBinderInLHSSql;

  private String idBinderIdSql;

  private String deleteByIdSql;
  private String deleteByIdInSql;
  private String softDeleteByIdSql;
  private String softDeleteByIdInSql;

  private final String name;

  private final String baseTableAlias;

  /**
   * If true then only changed properties get updated.
   */
  private final boolean updateChangesOnly;

  private final boolean cacheSharableBeans;

  private final BeanDescriptorDraftHelp<T> draftHelp;
  private final BeanDescriptorCacheHelp<T> cacheHelp;
  private final BeanDescriptorJsonHelp<T> jsonHelp;
  
  private final String defaultSelectClause;
  private final Set<String> defaultSelectClauseSet;

  private SpiEbeanServer ebeanServer;

  /**
   * Construct the BeanDescriptor.
   */
  public BeanDescriptor(BeanDescriptorMap owner, DeployBeanDescriptor<T> deploy) {

    this.owner = owner;
    this.serverName = owner.getServerName();
    this.entityType = deploy.getEntityType();
    this.properties = deploy.getProperties();
    this.propertyCount = this.properties.length;
    this.name = InternString.intern(deploy.getName());
    this.baseTableAlias = "t0";
    this.fullName = InternString.intern(deploy.getFullName());

    this.beanType = deploy.getBeanType();
    this.prototypeEntityBean = createPrototypeEntityBean(beanType);
    
    this.namedQueries = deploy.getNamedQueries();
    this.namedUpdates = deploy.getNamedUpdates();

    this.inheritInfo = deploy.getInheritInfo();

    this.beanFinder = deploy.getBeanFinder();
    this.persistController = deploy.getPersistController();
    this.persistListener = deploy.getPersistListener();
    this.beanPostLoad = deploy.getPostLoad();
    this.queryAdapter = deploy.getQueryAdapter();
    this.changeLogFilter = deploy.getChangeLogFilter();

    this.defaultSelectClause = deploy.getDefaultSelectClause();
    this.defaultSelectClauseSet = deploy.parseDefaultSelectClause(defaultSelectClause);

    this.idType = deploy.getIdType();
    this.idTypePlatformDefault = deploy.isIdTypePlatformDefault();
    this.idGenerator = deploy.getIdGenerator();
    this.sequenceName = deploy.getSequenceName();
    this.sequenceInitialValue = deploy.getSequenceInitialValue();
    this.sequenceAllocationSize = deploy.getSequenceAllocationSize();
    this.selectLastInsertedId = deploy.getSelectLastInsertedId();
    this.concurrencyMode = deploy.getConcurrencyMode();
    this.updateChangesOnly = deploy.isUpdateChangesOnly();
    this.compoundUniqueConstraints = deploy.getCompoundUniqueConstraints();

    this.readAuditing = deploy.isReadAuditing();
    this.draftable = deploy.isDraftable();
    this.draftableElement = deploy.isDraftableElement();
    this.historySupport = deploy.isHistorySupport();
    this.draftTable = deploy.getDraftTable();
    this.baseTable = InternString.intern(deploy.getBaseTable());
    this.baseTableAsOf = deploy.getBaseTableAsOf();
    this.baseTableVersionsBetween = deploy.getBaseTableVersionsBetween();
    this.dbComment = deploy.getDbComment();
    this.autoTunable = EntityType.ORM.equals(entityType) && (beanFinder == null);

    // helper object used to derive lists of properties
    DeployBeanPropertyLists listHelper = new DeployBeanPropertyLists(owner, this, deploy);

    this.softDeleteProperty = listHelper.getSoftDeleteProperty();
    this.softDelete = (softDeleteProperty != null);
    this.idProperty = listHelper.getId();
    this.versionProperty = listHelper.getVersionProperty();
    this.draft = listHelper.getDraft();
    this.draftDirty = listHelper.getDraftDirty();
    this.propMap = listHelper.getPropertyMap();
    this.propertiesTransient = listHelper.getTransients();
    this.propertiesNonTransient = listHelper.getNonTransients();
    this.propertiesBaseScalar = listHelper.getBaseScalar();
    this.propertiesBaseCompound = listHelper.getBaseCompound();
    this.propertiesEmbedded = listHelper.getEmbedded();
    this.propertiesLocal = listHelper.getLocal();
    this.propertiesMutable = listHelper.getMutable();
    this.unidirectional = listHelper.getUnidirectional();
    this.propertiesOne = listHelper.getOnes();
    //this.propertiesOneExported = listHelper.getOneExported();
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

    this.derivedTableJoins = listHelper.getTableJoin();

    boolean noRelationships = propertiesOne.length + propertiesMany.length == 0;
    
    this.cacheSharableBeans = noRelationships && deploy.getCacheOptions().isReadOnly();
    this.cacheHelp = new BeanDescriptorCacheHelp<T>(this, owner.getCacheManager(), deploy.getCacheOptions(), cacheSharableBeans, propertiesOneImported);
    this.jsonHelp = new BeanDescriptorJsonHelp<T>(this);
    this.draftHelp = new BeanDescriptorDraftHelp<T>(this);
    
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
      return (EntityBean) beanType.newInstance();
    } catch (Exception e) {
      throw new IllegalStateException("Error trying to create the prototypeEntityBean for "+beanType, e);
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
    for (int i = 0; i < propertiesMany.length; i++) {
      // used for creating lazy loading lists etc
      propertiesMany[i].setLoader(ebeanServer);
    }
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
   * @param withHistoryTables map populated if @History is supported on this entity bean
   */
  public void initialiseId(Map<String, String> withHistoryTables, Map<String,String> draftTables) {

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
   * @param asOfTableMap the map of base tables to associated 'with history' tables
   * @param asOfViewSuffix the suffix added to the table name to derive the 'with history' view name
   * @param draftTableMap the map of base tables to associated 'draft' tables.
   */
  public void initialiseOther(Map<String, String> asOfTableMap, String asOfViewSuffix, Map<String, String> draftTableMap) {

    for (int i = 0; i < propertiesManyToMany.length; i++) {
      // register associated draft table for M2M intersection
      propertiesManyToMany[i].registerDraftIntersectionTable(draftTableMap);
    }

    if (historySupport) {
      // history support on this bean so check all associated intersection tables
      // and if they are not excluded register the associated 'with history' table
      for (int i = 0; i < propertiesManyToMany.length; i++) {
        // register associated history table for M2M intersection
        if (!propertiesManyToMany[i].isExcludedFromHistory()) {
          TableJoin intersectionTableJoin = propertiesManyToMany[i].getIntersectionTableJoin();
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

    if (softDelete) {
      softDeleteByIdSql = "update " + baseTable + " set " + getSoftDeleteDbSet() + " where " + idEqualsSql;
      softDeleteByIdInSql = "update " + baseTable + " set " + getSoftDeleteDbSet() + " where " + idBinderInLHSSqlNoAlias + " ";
    } else {
      softDeleteByIdSql = null;
      softDeleteByIdInSql = null;
    }

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

  /**
   * Return true if this request should be included in the change log.
   */
  public BeanChange getChangeLogBean(PersistRequestBean<T> request) {

    if (changeLogFilter == null) {
      return null;
    }
    PersistRequest.Type type = request.getType();
    switch (type) {
      case INSERT:
        return changeLogFilter.includeInsert(request) ? insertBeanChange(request): null;
      case UPDATE:
      case SOFT_DELETE:
        return changeLogFilter.includeUpdate(request) ? updateBeanChange(request): null;
      case DELETE:
        return changeLogFilter.includeDelete(request) ? deleteBeanChange(request) :null;
      default:
        throw new IllegalStateException("Unhandled request type " + type);
    }
  }

  /**
   * Return the bean change for a delete.
   */
  @SuppressWarnings("unchecked")
  private BeanChange deleteBeanChange(PersistRequestBean<T> request) {
    return newBeanChange(request.getBeanId(), ChangeType.DELETE, Collections.EMPTY_MAP);
  }

  /**
   * Return the bean change for an update.
   */
  private BeanChange updateBeanChange(PersistRequestBean<T> request) {
    return newBeanChange(request.getBeanId(), ChangeType.UPDATE, diffFlatten(request.getEntityBeanIntercept().getDirtyValues()));
  }

  /**
   * Return the bean change for an insert.
   */
  private BeanChange insertBeanChange(PersistRequestBean<T> request) {
    return newBeanChange(request.getBeanId(), ChangeType.INSERT, diffForInsert(request.getEntityBean()));
  }

  private BeanChange newBeanChange(Object id, ChangeType changeType, Map<String, ValuePair> values) {
    return new BeanChange(getBaseTable(), id, changeType, values);
  }

  /**
   * Initialise the cache once the server has started.
   */
  public void cacheInitialise() {
    cacheHelp.initialise();
  }

  public SqlUpdate deleteById(Object id, List<Object> idList, boolean softDelete) {
    if (id != null) {
      return deleteById(id, softDelete);
    } else {
      return deleteByIdList(idList, softDelete);
    }
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
    for (int i = 0; i < idList.size(); i++) {
      idBinder.bindId(delete, idList.get(i));
    }
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
    elDeployCache.put(fkey.getName(), fkey);
  }

  public void initialiseFkeys() {
    for (int i = 0; i < propertiesOneImported.length; i++) {
      propertiesOneImported[i].addFkey();
    }
  }

  public boolean calculateUseCache(Boolean queryUseCache) {
    return (queryUseCache != null) ? queryUseCache : isBeanCaching();
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
   * Set the bean caching on or off.
   */
  public void setUseCache(boolean useCache) {
    cacheHelp.setUseCache(useCache);
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
  public boolean isCacheNotify(boolean publish) {
    if (draftable && !publish) {
      // no caching when editing draft beans
      return false;
    }
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

  public void cacheBeanPut(T bean) {
    cacheBeanPutData((EntityBean) bean);
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

  public void cacheHandleInsert(PersistRequestBean<T> insertRequest) {
    cacheHelp.handleInsert(insertRequest);
  }

  /**
   * Update the cached bean data.
   */
  public void cacheHandleUpdate(Object id, PersistRequestBean<T> updateRequest) {
    cacheHelp.handleUpdate(id, updateRequest);
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
   *
   * This implies cascade delete does not continue depth wise and that this is no
   * associated L2 bean caching.
   */
  public boolean isDeleteByStatement() {
    return deleteRecurseSkippable && !isBeanCaching();
  }

  /**
   * Find a property annotated with @WhenCreated or @CreatedTimestamp.
   */
  public BeanProperty findWhenCreatedProperty() {

    for (int i = 0; i < propertiesBaseScalar.length; i++) {
      if (propertiesBaseScalar[i].isGeneratedWhenCreated()) {
        return propertiesBaseScalar[i];
      }
    }
    return null;
  }

  /**
   * Find a property annotated with @WhenModified or @UpdatedTimestamp.
   */
  public BeanProperty findWhenModifiedProperty() {

    for (int i = 0; i < propertiesBaseScalar.length; i++) {
      if (propertiesBaseScalar[i].isGeneratedWhenModified()) {
        return propertiesBaseScalar[i];
      }
    }
    return null;
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

  /**
   * Return a named query.
   */
  public DeployNamedQuery getNamedQuery(String name) {
    return namedQueries.get(name);
  }

  public void addNamedQuery(DeployNamedQuery deployNamedQuery) {
    namedQueries.put(deployNamedQuery.getName(), deployNamedQuery);
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

      convertSetId(id, eb);

      EntityBeanIntercept ebi = eb._ebean_getIntercept();
      ebi.setBeanLoader(ebeanServer);

      // Note: not creating proxies for many's...
      ebi.setReference(idPropertyIndex);

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
   * Return bean class name.
   */
  public String getDescriptorId() {
    return fullName;
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
    return propMap.get(propName);
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
      if (fk != null && fk instanceof BeanFkeyProperty) {
        // propertyDeploy chain for foreign key column
        return ((BeanFkeyProperty)fk).create(chain.getExpression(), chain.isContainsMany());
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
      chain.setContainsMany();
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

  /**
   * Reset the many properties to empty state ready for reloading.
   */
  public void resetManyProperties(Object dbBean) {

    EntityBean bean = (EntityBean)dbBean;
    for (int i = 0; i < propertiesMany.length; i++) {
      propertiesMany[i].resetMany(bean);
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

  /**
   * Return true if this is an embedded bean.
   */
  public boolean isEmbedded() {
    return EntityType.EMBEDDED.equals(entityType);
  }

  /**
   * Return the compound unique constraints.
   */
  public CompoundUniqueConstraint[] getCompoundUniqueConstraints() {
    return compoundUniqueConstraints;
  }

  /**
   * Return the beanListener.
   */
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
  @SuppressWarnings("unchecked")
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
   * Return the DB comment for the base table.
   */
  public String getDbComment() {
    return dbComment;
  }

  /**
   * Return the base table. Only properties mapped to the base table are by
   * default persisted.
   */
  public String getBaseTable() {
    return baseTable;
  }

  /**
   * Return the base table to use given the query temporal mode.
   */
  public String getBaseTable(SpiQuery.TemporalMode mode) {
    switch (mode) {
      case DRAFT: return draftTable;
      case VERSIONS: return baseTableVersionsBetween;
      case AS_OF: return baseTableAsOf;
        default: return baseTable;
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
   * Return true if the bean is considered a 'draft' instance.
   */
  public boolean isDraftInstance(EntityBean entityBean) {
    if (draft != null) {
      return Boolean.TRUE == draft.getValue(entityBean);
    }
    // no draft property - so just ignore the check / return true
    return true;
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

  /**
   * Return a collection of all BeanProperty. This includes transient properties.
   */
  public Collection<BeanProperty> propertiesAll() {
    return propMap.values();
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
   * Set the embedded owner on any embedded bean properties.
   */
  public void setEmbeddedOwner(EntityBean bean) {
    for (int i = 0; i < propertiesEmbedded.length; i++) {
      propertiesEmbedded[i].setEmbeddedOwner(bean);
    }
  }

  public BeanProperty getIdProperty() {
    return idProperty;
  }

  /**
   * Return true if this bean should be inserted rather than updated.
   * 
   * @param ebi
   *          The entity bean intercept
   * @param insertMode
   *          true if the 'root request' was an insert rather than an update
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
   * Check for mutable scalar types and mark as dirty if necessary.
   */
  public void checkMutableProperties(EntityBeanIntercept ebi) {
    for (int i = 0; i < propertiesMutable.length; i++) {
      BeanProperty beanProperty = propertiesMutable[i];
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
   * Flatten the diff that comes from the entity bean intercept.
   */
  Map<String, ValuePair> diffFlatten(Map<String, ValuePair> diff) {
    return DiffHelp.flatten(diff, this);
  }

  /**
   * Return a map of the differences between a and b.
   * <p>
   * A and B must be of the same type. B can be null, in which case the 'dirty
   * diff' of a is returned.
   * </p>
   * <p>
   * This intentionally does not include as OneToMany or ManyToMany properties.
   * </p>
   */
  public Map<String, ValuePair> diffForInsert(EntityBean newBean) {

    Map<String, ValuePair> map = new LinkedHashMap<String, ValuePair>();
    diffForInsert(null, map, newBean);
    return map;
  }

  /**
   * Populate the diff for inserts with flattened non-null property values.
   */
  public void diffForInsert(String prefix, Map<String, ValuePair> map, EntityBean newBean) {
    for (int i = 0; i < propertiesBaseScalar.length; i++) {
      propertiesBaseScalar[i].diffForInsert(prefix, map, newBean);
    }
    for (int i = 0; i < propertiesOne.length; i++) {
      propertiesOne[i].diffForInsert(prefix, map, newBean);
    }
    for (int i = 0; i < propertiesEmbedded.length; i++) {
      propertiesEmbedded[i].diffForInsert(prefix, map, newBean);
    }
  }

  /**
   * Return the diff comparing the bean values.
   */
  public Map<String, ValuePair> diff(EntityBean newBean, EntityBean oldBean) {
    Map<String, ValuePair> map = new LinkedHashMap<String, ValuePair>();
    diff(null, map, newBean, oldBean);
    return map;
  }

  /**
   * Populate the diff for updates with flattened non-null property values.
   */
  public void diff(String prefix, Map<String, ValuePair> map, EntityBean newBean, EntityBean oldBean) {

    for (int i = 0; i < propertiesBaseScalar.length; i++) {
      propertiesBaseScalar[i].diff(prefix, map, newBean, oldBean);
    }
    for (int i = 0; i < propertiesOne.length; i++) {
      propertiesOne[i].diff(prefix, map, newBean, oldBean);
    }
    for (int i = 0; i < propertiesEmbedded.length; i++) {
      propertiesEmbedded[i].diff(prefix, map, newBean, oldBean);
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

    if (idProperty != null) {
      OrderBy<T> orderBy = query.getOrderBy();
      if (orderBy == null || orderBy.isEmpty()) {
        query.order().asc(idProperty.getName());
      } else if (!orderBy.containsProperty(idProperty.getName())){
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

  public void jsonWrite(WriteJson writeJson, EntityBean bean) throws IOException {
    jsonHelp.jsonWrite(writeJson, bean, null);
  }  
  
  public void jsonWrite(WriteJson writeJson, EntityBean bean, String key) throws IOException {
    jsonHelp.jsonWrite(writeJson, bean, key);
  }

  protected void jsonWriteProperties(WriteJson writeJson, EntityBean bean) throws IOException {
    jsonHelp.jsonWriteProperties(writeJson, bean);
  }
    
  public T jsonRead(ReadJson jsonRead, String path) throws IOException {
    return jsonHelp.jsonRead(jsonRead, path);
  }
  
  protected T jsonReadObject(ReadJson jsonRead, String path) throws IOException {
    return jsonHelp.jsonReadObject(jsonRead, path);
  }
}
