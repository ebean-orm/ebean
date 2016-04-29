package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeaninternal.server.cache.CacheChangeSet;
import com.avaje.ebeaninternal.server.cache.CachedBeanData;
import com.avaje.ebeaninternal.server.cache.CachedBeanDataFromBean;
import com.avaje.ebeaninternal.server.cache.CachedBeanDataToBean;
import com.avaje.ebeaninternal.server.cache.CachedManyIds;
import com.avaje.ebeaninternal.server.core.CacheOptions;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.querydefn.NaturalKeyBindParam;
import com.avaje.ebeaninternal.server.transaction.DefaultPersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper for BeanDescriptor that manages the bean, query and collection caches.
 * 
 * @param <T> The entity bean type
 */
final class BeanDescriptorCacheHelp<T> {

  private static final Logger queryLog = LoggerFactory.getLogger("org.avaje.ebean.cache.QUERY");
  private static final Logger beanLog = LoggerFactory.getLogger("org.avaje.ebean.cache.BEAN");
  private static final Logger manyLog = LoggerFactory.getLogger("org.avaje.ebean.cache.COLL");
  private static final Logger natLog = LoggerFactory.getLogger("org.avaje.ebean.cache.NATKEY");

  private final BeanDescriptor<T> desc;

  private final ServerCacheManager cacheManager;

  private final CacheOptions cacheOptions;

  /**
   * Flag indicating this bean has no relationships.
   */
  private final boolean cacheSharableBeans;

  private final Class<?> beanType;

  private final String cacheName;
  
  private final BeanPropertyAssocOne<?>[] propertiesOneImported;
  private final String naturalKeyProperty;

  private final ServerCache beanCache;
  private final ServerCache naturalKeyCache;
  private final ServerCache queryCache;

  BeanDescriptorCacheHelp(BeanDescriptor<T> desc, ServerCacheManager cacheManager, CacheOptions cacheOptions,
      boolean cacheSharableBeans, BeanPropertyAssocOne<?>[] propertiesOneImported) {

    this.desc = desc;
    this.beanType = desc.rootBeanType;
    this.cacheName = beanType.getSimpleName();
    this.cacheManager = cacheManager;
    this.cacheOptions = cacheOptions;
    this.cacheSharableBeans = cacheSharableBeans;
    this.propertiesOneImported = propertiesOneImported;
    this.naturalKeyProperty = cacheOptions.getNaturalKey();

    if (!cacheOptions.isEnableQueryCache()) {
      this.queryCache = null;
    } else {
      this.queryCache =  cacheManager.getQueryCache(beanType);
    }

    if (cacheOptions.isEnableBeanCache()) {
      this.beanCache = cacheManager.getBeanCache(beanType);
      if (cacheOptions.getNaturalKey() != null) {
        this.naturalKeyCache = cacheManager.getNaturalKeyCache(beanType);
      } else {
        this.naturalKeyCache = null;
      }
    } else {
      this.beanCache = null;
      this.naturalKeyCache = null;
    }
  }

  /**
   * Return true if there is currently query caching for this type of bean.
   */
  boolean isQueryCaching() {
    return queryCache != null;
  }

  /**
   * Return true if there is currently bean caching for this type of bean.
   */
  boolean isBeanCaching() {
    return beanCache != null;
  }

  /**
   * Return true if the persist request needs to notify the cache.
   */
  boolean isCacheNotify() {

    if (isBeanCaching() || isQueryCaching()) {
      return true;
    }
    for (int i = 0; i < propertiesOneImported.length; i++) {
      if (propertiesOneImported[i].getTargetDescriptor().isBeanCaching()) {
        return true;
      }
    }
    return false;
  }

  CacheOptions getCacheOptions() {
    return cacheOptions;
  }

  /**
   * Clear the query cache.
   */
  void queryCacheClear() {
    if (queryCache != null) {
      if (queryLog.isDebugEnabled()) {
        queryLog.debug("   CLEAR {}", cacheName);
      }
      queryCache.clear();
    }
  }

  /**
   * Add query cache clear to the changeSet.
   */
  void queryCacheClear(CacheChangeSet changeSet) {
    if (queryCache != null) {
      changeSet.addClearQuery(desc);
    }
  }

  /**
   * Get a query result from the query cache.
   */
  @SuppressWarnings("unchecked")
  BeanCollection<T> queryCacheGet(Object id) {
    if (queryCache == null) {
      throw new IllegalStateException("No query cache enabled on " + desc + ". Need explicit @Cache(enableQueryCache=true)");
    }
    BeanCollection<T> list = (BeanCollection<T>) queryCache.get(id);
    if (queryLog.isDebugEnabled()) {
      if (list == null) {
        queryLog.debug("   GET {}({}) - cache miss", cacheName, id);
      } else {
        queryLog.debug("   GET {}({}) - hit", cacheName, id);
      }
    }
    return list;
  }

  /**
   * Put a query result into the query cache.
   */
  void queryCachePut(Object id, BeanCollection<T> query) {
    if (queryCache == null) {
      throw new IllegalStateException("No query cache enabled on " + desc + ". Need explicit @Cache(enableQueryCache=true)");
    }
    if (queryLog.isDebugEnabled()) {
      queryLog.debug("   PUT {}({})", cacheName, id);
    }
    queryCache.put(id, query);
  }


  void manyPropRemove(String propertyName, Object parentId) {
    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, propertyName);
    if (manyLog.isTraceEnabled()) {
      manyLog.trace("   REMOVE {}({}).{}", cacheName, parentId, propertyName);
    }
    collectionIdsCache.remove(parentId);
  }

  void manyPropClear(String propertyName) {
    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, propertyName);
    if (manyLog.isDebugEnabled()) {
      manyLog.debug("   CLEAR {}(*).{} ", cacheName, propertyName);
    }
    collectionIdsCache.clear();
  }

  /**
   * Return the CachedManyIds for a given bean many property. Returns null if not in the cache.
   */
  private CachedManyIds manyPropGet(Object parentId, String propertyName) {
    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, propertyName);
    CachedManyIds entry = (CachedManyIds) collectionIdsCache.get(parentId);
    if (entry == null) {
      if (manyLog.isTraceEnabled()) {
        manyLog.trace("   GET {}({}).{} - cache miss", cacheName, parentId, propertyName);
      }
    } else if (manyLog.isDebugEnabled()) {
      manyLog.debug("   GET {}({}).{} - hit", cacheName, parentId, propertyName);      
    }
    return entry;
  }

  /**
   * Try to load the bean collection from cache return true if successful.
   */
  boolean manyPropLoad(BeanPropertyAssocMany<?> many, BeanCollection<?> bc, Object parentId, Boolean readOnly) {

    CachedManyIds entry = manyPropGet(parentId, many.getName());
    if (entry == null) {
      // not in cache so return unsuccessful
      return false;
    }
    
    Object ownerBean = bc.getOwnerBean();
    EntityBeanIntercept ebi = ((EntityBean) ownerBean)._ebean_getIntercept();
    PersistenceContext persistenceContext = ebi.getPersistenceContext();

    BeanDescriptor<?> targetDescriptor = many.getTargetDescriptor();

    List<Object> idList = entry.getIdList();
    bc.checkEmptyLazyLoad();
    for (int i = 0; i < idList.size(); i++) {
      Object id = idList.get(i);
      Object refBean = targetDescriptor.createReference(readOnly, id, persistenceContext);
      many.add(bc, (EntityBean) refBean);
    }
    return true;
  }

  /**
   * Put the beanCollection into the cache.
   */
  void manyPropPut(BeanPropertyAssocMany<?> many, Object details, Object parentId) {

    CachedManyIds entry = createManyIds(many, details);
    cachePutManyIds(parentId, many.getName(), entry);
  }

  void cachePutManyIds(Object parentId, String manyName, CachedManyIds entry) {

    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, manyName);
    if (manyLog.isDebugEnabled()) {
      manyLog.debug("   PUT {}({}).{} - ids:{}", cacheName, parentId, manyName, entry);
    }
    collectionIdsCache.put(parentId, entry);
  }

  private CachedManyIds createManyIds(BeanPropertyAssocMany<?> many, Object details) {

    BeanDescriptor<?> targetDescriptor = many.getTargetDescriptor();

    List<Object> idList = new ArrayList<Object>();
    Collection<?> actualDetails = BeanCollectionUtil.getActualEntries(details);
    for (Object bean : actualDetails) {
      idList.add(targetDescriptor.getId((EntityBean) bean));
    }
    return new CachedManyIds(idList);
  }

  /**
   * Find the bean using the natural key lookup if available.
   */
  Object naturalKeyIdLookup(SpiQuery<T> query) {

    if (!isNaturalKeyCaching(query.isUseBeanCache())) {
      // no natural key caching for this query
      return null;
    }

    // check if it is a find by unique id (using the natural key)
    NaturalKeyBindParam keyBindParam = query.getNaturalKeyBindParam();
    if (keyBindParam == null || !isNaturalKey(keyBindParam.getName())) {
      // query is not appropriate 
      return null;
    }

    // try to lookup the id using the natural key
    Object id = naturalKeyCache.get(keyBindParam.getValue());
    if (natLog.isTraceEnabled() && id != null) {
      natLog.trace(" LOOKUP {}({}) - id:{}", cacheName, keyBindParam.getValue(), id);
    }
    return id;
  }

  private boolean isNaturalKeyCaching(Boolean queryUseCache) {
    return naturalKeyCache != null && (queryUseCache == null || queryUseCache);
  }

  private boolean isNaturalKey(String propName) {
    return propName != null && propName.equals(cacheOptions.getNaturalKey());
  }

  /**
   * For a bean built from the cache this sets up its persistence context for future lazy loading etc.
   */
  private void setupContext(Object bean, PersistenceContext context) {
    if (context == null) {
      context = new DefaultPersistenceContext();
    }

    // Not using a loadContext for beans coming out of L2 cache
    // so that means no batch lazy loading for these beans
    EntityBean entityBean = (EntityBean)bean;
    EntityBeanIntercept ebi = entityBean._ebean_getIntercept();
    ebi.setPersistenceContext(context);
    Object id = desc.getId(entityBean);
    desc.contextPut(context, id, bean);
  }
  
  /**
   * Return the beanCache creating it if necessary.
   */
  private ServerCache getBeanCache() {
    if (beanCache == null) {
      throw new IllegalStateException("No bean cache enabled for " + desc + ". Add the @Cache annotation.");
    }
    return beanCache;
  }
  
  /**
   * Clear the bean cache.
   */
  private void beanCacheClear() {
    if (beanCache != null) {
      if (beanLog.isDebugEnabled()) {
        beanLog.debug("   CLEAR {}", cacheName);
      }
      beanCache.clear();
    }
  }

  CachedBeanData beanExtractData(BeanDescriptor<?> targetDesc, EntityBean bean) {
    return CachedBeanDataFromBean.extract(targetDesc, bean);
  }

  /**
   * Put a bean into the bean cache.
   */
  void beanCachePut(EntityBean bean) {

    if (desc.inheritInfo != null) {
      desc.descOf(bean.getClass()).cacheBeanPutDirect(bean);
    } else {
      beanCachePutDirect(bean);
    }
  }

  /**
   * Put the bean into the bean cache.
   */
  void beanCachePutDirect(EntityBean bean) {

    CachedBeanData beanData = beanExtractData(desc, bean);

    Object id = desc.getId(bean);
    if (beanLog.isDebugEnabled()) {
      beanLog.debug("   PUT {}({}) data:{}", cacheName, id, beanData);
    }
    getBeanCache().put(id, beanData);

    if (naturalKeyProperty != null) {
      Object naturalKey = beanData.getData(naturalKeyProperty);
      if (naturalKey != null) {
        if (natLog.isDebugEnabled()) {
          natLog.debug(" PUT {}({}, {})", cacheName, naturalKey, id);
        }
        naturalKeyCache.put(naturalKey, id);
      }
    }
  }

  CachedBeanData beanCacheGetData(Object id) {
    return (CachedBeanData) getBeanCache().get(id);
  }
  
  T beanCacheGet(Object id, Boolean readOnly, PersistenceContext context) {
    T bean = beanCacheGetInternal(id, readOnly, context);
    if (bean != null) {
      setupContext(bean, context);
    }
    return bean;
  }
  
  /**
   * Return a bean from the bean cache.
   */
  @SuppressWarnings("unchecked")
  private T beanCacheGetInternal(Object id, Boolean readOnly, PersistenceContext context) {

    CachedBeanData data = (CachedBeanData) getBeanCache().get(id);
    if (data == null) {
      if (beanLog.isTraceEnabled()) {
        beanLog.trace("   GET {}({}) - cache miss", cacheName, id);
      }
      return null;
    }
    if (cacheSharableBeans && !Boolean.FALSE.equals(readOnly)) {
      Object bean = data.getSharableBean();
      if (bean != null) {
        if (beanLog.isTraceEnabled()) {
          beanLog.trace("   GET {}({}) - hit shared bean", cacheName, id);
        }
        if (desc.isReadAuditing()) {
          desc.readAuditBean("l2", "", bean);
        }
        return (T) bean;
      }
    }

    return (T)loadBean(id, readOnly, data, context);
  }

  /**
   * Load the entity bean taking into account inheritance.
   */
  private EntityBean loadBean(Object id, Boolean readOnly, CachedBeanData data, PersistenceContext context) {

    String discValue = data.getDiscValue();
    if (discValue == null) {
      return loadBeanDirect(id, readOnly, data, context);
    } else {
      return rootDescriptor(discValue).cacheBeanLoadDirect(id, readOnly, data, context);
    }
  }

  /**
   * Return the root BeanDescriptor for inheritance.
   */
  private BeanDescriptor<?> rootDescriptor(String discValue) {
    return desc.inheritInfo.readType(discValue).desc();
  }

  /**
   * Load the entity bean from cache data given this is the root bean type.
   */
  EntityBean loadBeanDirect(Object id, Boolean readOnly, CachedBeanData data, PersistenceContext context) {

    if (context == null) {
      context = new DefaultPersistenceContext();
    }

    EntityBean bean = desc.createEntityBean();
    id = desc.convertSetId(id, bean);
    CachedBeanDataToBean.load(desc, bean, data, context);

    EntityBeanIntercept ebi = bean._ebean_getIntercept();

    // Not using a loadContext for beans coming out of L2 cache
    // so that means no batch lazy loading for these beans
    ebi.setBeanLoader(desc.getEbeanServer());
    if (Boolean.TRUE.equals(readOnly)) {
      ebi.setReadOnly(true);
    }
    ebi.setPersistenceContext(context);
    desc.contextPut(context, id, bean);

    if (beanLog.isTraceEnabled()) {
      beanLog.trace("   GET {}({}) - hit", cacheName, id);
    }
    if (desc.isReadAuditing()) {
      desc.readAuditBean("l2", "", bean);
    }
    return bean;
  }

  /**
   * Load the embedded bean checking for inheritance.
   */
  EntityBean embeddedBeanLoad(CachedBeanData data, PersistenceContext context) {

    String discValue = data.getDiscValue();
    if (discValue == null) {
      return embeddedBeanLoadDirect(data, context);
    } else {
      return rootDescriptor(discValue).cacheEmbeddedBeanLoadDirect(data, context);
    }
  }

  /**
   * Load the embedded bean given this is the bean type.
   */
  EntityBean embeddedBeanLoadDirect(CachedBeanData data, PersistenceContext context) {
    EntityBean bean = desc.createEntityBean();
    CachedBeanDataToBean.load(desc, bean, data, context);
    return bean;
  }

  /**
   * Remove a bean from the cache given its Id.
   */
  void beanCacheRemove(Object id) {
    if (beanCache != null) {
      if (beanLog.isDebugEnabled()) {
        beanLog.debug("   REMOVE {}({})", cacheName, id);
      }
      beanCache.remove(id);
    }
    for (int i = 0; i < propertiesOneImported.length; i++) {
      propertiesOneImported[i].cacheClear();
    }
  }

  /**
   * Returns true if it managed to populate/load the bean from the cache.
   */
  boolean beanCacheLoad(EntityBean bean, EntityBeanIntercept ebi, Object id, PersistenceContext context) {

    CachedBeanData cacheData = (CachedBeanData) getBeanCache().get(id);
    if (cacheData == null) {
      if (beanLog.isTraceEnabled()) {
        beanLog.trace("   LOAD {}({}) - cache miss", cacheName, id);
      }
      return false;
    }
    int lazyLoadProperty = ebi.getLazyLoadPropertyIndex();
    if (lazyLoadProperty > -1 && !cacheData.isLoaded(ebi.getLazyLoadProperty())) {
      if (beanLog.isTraceEnabled()) {
        beanLog.trace("   LOAD {}({}) - cache miss on property({})", cacheName, id, ebi.getLazyLoadProperty());
      }
      return false;
    }

    CachedBeanDataToBean.load(desc, bean, cacheData, context);
    if (beanLog.isDebugEnabled()) {
      beanLog.debug("   LOAD {}({}) - hit", cacheName, id);
    }
    return true;
  }

  /**
   * Add appropriate cache changes to support delete.
   */
  void handleDelete(Object id, CacheChangeSet changeSet) {
    if (beanCache != null) {
      changeSet.addBeanRemove(desc, id);
    }
    cacheDeleteImported(true, null, changeSet);
  }

  /**
   * Add appropriate cache changes to support delete.
   */
  void handleDelete(Object id, PersistRequestBean<T> deleteRequest, CacheChangeSet changeSet) {
    queryCacheClear(changeSet);
    if (beanCache != null) {
      changeSet.addBeanRemove(desc, id);
    }
    cacheDeleteImported(true, deleteRequest.getEntityBean(), changeSet);
  }

  /**
   * Add appropriate cache changes to support insert.
   */
  void handleInsert(PersistRequestBean<T> insertRequest, CacheChangeSet changeSet) {
    queryCacheClear(changeSet);
    cacheDeleteImported(false, insertRequest.getEntityBean(), changeSet);
    changeSet.addBeanInsert(desc.getBaseTable());
  }

  private void cacheDeleteImported(boolean clear, EntityBean entityBean, CacheChangeSet changeSet) {
    for (int i = 0; i < propertiesOneImported.length; i++) {
      propertiesOneImported[i].cacheDelete(clear, entityBean, changeSet);
    }
  }

  /**
   * Add appropriate changes to support update.
   */
  void handleUpdate(Object id, PersistRequestBean<T> updateRequest, CacheChangeSet changeSet) {

    queryCacheClear(changeSet);

    List<BeanPropertyAssocMany<?>> manyCollections = updateRequest.getUpdatedManyCollections();
    if (manyCollections != null) {
      for (int i = 0; i < manyCollections.size(); i++) {
        BeanPropertyAssocMany<?> many = manyCollections.get(i);
        Object details = many.getValue(updateRequest.getEntityBean());
        CachedManyIds entry = createManyIds(many, details);
        changeSet.addManyPut(desc, many.getName(), id, entry);
      }
    }
    
    // check if the bean itself was updated
    if (!updateRequest.isUpdatedManysOnly()) {

      boolean updateNaturalKey = false;

      Map<String, Object> changes = new LinkedHashMap<String, Object>();
      EntityBean bean = updateRequest.getEntityBean();
      boolean[] dirtyProperties = updateRequest.getDirtyProperties();
      for (int i = 0; i < dirtyProperties.length; i++) {
        if (dirtyProperties[i]) {
          BeanProperty property = desc.propertiesIndex[i];
          if (property.isCacheDataInclude()) {
            Object val = property.getCacheDataValue(bean);
            changes.put(property.getName(), val);
            if (property.isNaturalKey()) {
              updateNaturalKey = true;
              changeSet.addNaturalKeyPut(desc, id, val);
            }
          }
        }
      }

      changeSet.addBeanUpdate(desc, id, changes, updateNaturalKey, updateRequest.getVersion());
    }
  }

  /**
   * Invalidate parts of cache due to SqlUpdate or external modification etc.
   */
  void handleBulkUpdate(TableIUD tableIUD) {
    // inserts don't invalidate the bean cache
    if (tableIUD.isUpdateOrDelete()) {
      beanCacheClear();
    }
    // any change invalidates the query cache
    queryCacheClear();
  }

  void cacheNaturalKeyPut(Object id, Object newKey) {
    if (newKey != null) {
      naturalKeyCache.put(newKey, id);
    }
  }

  /**
   * Apply changes to the bean cache entry.
   */
  void cacheBeanUpdate(Object id, Map<String, Object> changes, boolean updateNaturalKey, long version) {

    ServerCache cache = getBeanCache();
    CachedBeanData existingData = (CachedBeanData) cache.get(id);
    if (existingData != null) {
      long currentVersion = existingData.getVersion();
      if (version > 0 && version < currentVersion) {
        if (beanLog.isDebugEnabled()) {
          beanLog.debug("   REMOVE {}({}) - version conflict old:{} new:{}", cacheName, id, currentVersion, version);
        }
        cache.remove(id);
      } else {
        if (version == 0) {
          version = currentVersion;
        }
        CachedBeanData newData = existingData.update(changes, version);
        if (beanLog.isDebugEnabled()) {
          beanLog.debug("   UPDATE {}({})  changes:{}", cacheName, id, changes);
        }
        cache.put(id, newData);
      }

      if (updateNaturalKey) {
        Object oldKey = existingData.getData(naturalKeyProperty);
        if (oldKey != null) {
          if (natLog.isDebugEnabled()) {
            natLog.debug(".. update {} REMOVE({}) - old key for ({})", cacheName, oldKey, id);
          }
          naturalKeyCache.remove(oldKey);
        }
      }
    }
  }

}
