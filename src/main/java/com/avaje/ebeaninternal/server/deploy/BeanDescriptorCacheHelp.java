package com.avaje.ebeaninternal.server.deploy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeaninternal.server.cache.CachedBeanData;
import com.avaje.ebeaninternal.server.cache.CachedBeanDataFromBean;
import com.avaje.ebeaninternal.server.cache.CachedBeanDataToBean;
import com.avaje.ebeaninternal.server.cache.CachedBeanDataUpdate;
import com.avaje.ebeaninternal.server.cache.CachedManyIds;
import com.avaje.ebeaninternal.server.core.CacheOptions;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.querydefn.NaturalKeyBindParam;
import com.avaje.ebeaninternal.server.transaction.DefaultPersistenceContext;

/**
 * Helper for BeanDescriptor that manages the bean, query and collection caches.
 * 
 * @param <T> The entity bean type
 */
public final class BeanDescriptorCacheHelp<T> {

  public static final Logger queryLog = LoggerFactory.getLogger("org.avaje.ebean.cache.QUERY");
  public static final Logger beanLog = LoggerFactory.getLogger("org.avaje.ebean.cache.BEAN");
  public static final Logger manyLog = LoggerFactory.getLogger("org.avaje.ebean.cache.COLL");
  public static final Logger natLog = LoggerFactory.getLogger("org.avaje.ebean.cache.NATKEY");
  
  
  private final BeanDescriptor<T> desc;

  private final ServerCacheManager cacheManager;

  private final CacheOptions cacheOptions;

  /**
   * Flag indicating this bean has no relationships.
   */
  private final boolean cacheSharableBeans;

  private final Class<T> beanType;

  private final String cacheName;
  
  private final BeanPropertyAssocOne<?>[] propertiesOneImported;

  private ServerCache beanCache;
  private ServerCache naturalKeyCache;
  private ServerCache queryCache;

  public BeanDescriptorCacheHelp(BeanDescriptor<T> desc, ServerCacheManager cacheManager, CacheOptions cacheOptions,
      boolean cacheSharableBeans, BeanPropertyAssocOne<?>[] propertiesOneImported) {

    this.desc = desc;
    this.beanType = desc.getBeanType();
    this.cacheName = beanType.getSimpleName();
    this.cacheManager = cacheManager;
    this.cacheOptions = cacheOptions;
    this.cacheSharableBeans = cacheSharableBeans;
    this.propertiesOneImported = propertiesOneImported;
  }

  /**
   * Initialise the cache once the server has started.
   */
  public void initialise() {
    if (cacheOptions.isUseNaturalKeyCache()) {
      this.naturalKeyCache = cacheManager.getNaturalKeyCache(beanType);
    }
    if (cacheOptions.isUseCache()) {
      this.beanCache = cacheManager.getBeanCache(beanType);
    }
  }

  /**
   * Execute the warming cache query (if defined) and load the cache.
   */
  public void runCacheWarming(EbeanServer ebeanServer) {
    if (cacheOptions == null) {
      return;
    }
    String warmingQuery = cacheOptions.getWarmingQuery();
    if (warmingQuery != null && warmingQuery.trim().length() > 0) {
      Query<?> query = ebeanServer.createQuery(beanType, warmingQuery);
      query.setUseCache(true);
      query.setReadOnly(true);
      query.setLoadBeanCache(true);
      List<?> list = query.findList();
      if (beanLog.isInfoEnabled()) {
        beanLog.info("Loaded {} cache with [{}] beans", cacheName, list.size());
      }
    }
  }

  public void setUseCache(boolean useCache) {
    if (useCache) {
      getBeanCache();
    } else {
      beanCacheClear();
      beanCache = null;
    }
  }

  /**
   * Return true if there is currently query caching for this type of bean.
   */
  public boolean isQueryCaching() {
    return queryCache != null;
  }

  /**
   * Return true if there is currently bean caching for this type of bean.
   */
  public boolean isBeanCaching() {
    return beanCache != null;
  }

  /**
   * Return true if the persist request needs to notify the cache.
   */
  public boolean isCacheNotify() {

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

  public CacheOptions getCacheOptions() {
    return cacheOptions;
  }

  /**
   * Clear the query cache.
   */
  public void queryCacheClear() {
    if (queryCache != null) {
      if (queryLog.isDebugEnabled()) {
        queryLog.debug("   CLEAR {}", cacheName);
      }
      queryCache.clear();
    }
  }


  /**
   * Get a query result from the query cache.
   */
  @SuppressWarnings("unchecked")
  public BeanCollection<T> queryCacheGet(Object id) {
    if (queryCache == null) {
      return null;
    } else {
      return (BeanCollection<T>) queryCache.get(id);
    }
  }

  /**
   * Put a query result into the query cache.
   */
  public void queryCachePut(Object id, BeanCollection<T> query) {
    if (queryCache == null) {
      queryCache = cacheManager.getQueryCache(beanType);
    }
    if (queryLog.isDebugEnabled()) {
      queryLog.debug("   PUT {} {}", cacheName, id);
    }
    queryCache.put(id, query);
  }


  public void manyPropRemove(Object parentId, String propertyName) {
    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, propertyName);
    if (manyLog.isDebugEnabled()) {
      manyLog.debug("   REMOVE {}({}).{}", cacheName, parentId, propertyName);
    }
    collectionIdsCache.remove(parentId);
  }

  public void manyPropClear(String propertyName) {
    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, propertyName);
    if (manyLog.isDebugEnabled()) {
      manyLog.debug("   CLEAR {}(*).{} ", cacheName, propertyName);
    }
    collectionIdsCache.clear();
  }

  /**
   * Return the CachedManyIds for a given bean many property. Returns null if not in the cache.
   */
  public CachedManyIds manyPropGet(Object parentId, String propertyName) {
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
  public boolean manyPropLoad(BeanPropertyAssocMany<?> many, BeanCollection<?> bc, Object parentId, Boolean readOnly) {

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
      Object refBean = targetDescriptor.createReference(readOnly, id);
      EntityBeanIntercept refEbi = ((EntityBean) refBean)._ebean_getIntercept();

      many.add(bc, (EntityBean) refBean);
      persistenceContext.put(id, refBean);
      refEbi.setPersistenceContext(persistenceContext);
    }
    return true;
  }

  /**
   * Put the beanCollection into the cache.
   */
  public void manyPropPut(BeanPropertyAssocMany<?> many, Object details, Object parentId) {
  
    BeanDescriptor<?> targetDescriptor = many.getTargetDescriptor();
    ArrayList<Object> idList = new ArrayList<Object>();

    // get the underlying collection of beans (in the List, Set or Map)
    Collection<?> actualDetails = BeanCollectionUtil.getActualEntries(details);
    
    for (Object bean : actualDetails) {
      // Collect the id values
      idList.add(targetDescriptor.getId((EntityBean) bean));
    }
    
    CachedManyIds entry = new CachedManyIds(idList);
    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, many.getName());
    if (manyLog.isDebugEnabled()) {
      manyLog.debug("   PUT {}({}).{} - ids:{}", cacheName, parentId, many.getName(), entry);
    }
    collectionIdsCache.put(parentId, entry);
  }



  public T naturalKeyLookup(SpiQuery<T> query, SpiTransaction t) {
    
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
    if (natLog.isTraceEnabled()) {
      natLog.trace(" LOOKUP {}({}) - id:{}", cacheName, keyBindParam.getValue(), id);
    }
    if (id == null) {
      return null;
    }
    
    // try looking up into the bean cache using the id
    T cacheBean = beanCacheGetInternal(id, query.isReadOnly());
    if (cacheBean != null) {   
      setupContext(cacheBean, query, getPersistenceContext(t));
    }
    return cacheBean;  
  }

  private PersistenceContext getPersistenceContext(SpiTransaction t) {
    PersistenceContext context = null;
    if (t == null) {
      t = desc.getEbeanServer().getCurrentServerTransaction();
    }
    if (t != null) {
      context = t.getPersistenceContext();
    }
    return context;
  }

  private boolean isNaturalKeyCaching(Boolean queryUseCache) {
    return naturalKeyCache != null && (queryUseCache == null || queryUseCache.booleanValue());
  }

  private boolean isNaturalKey(String propName) {
    return propName != null && propName.equals(cacheOptions.getNaturalKey());
  }



  /**
   * For a bean built from the cache this sets up its persistence context for future lazy loading etc.
   */
  private void setupContext(Object bean, SpiQuery<T> query, PersistenceContext context) {
    if (context == null) {
      context = new DefaultPersistenceContext();
    }

    // Not using a loadContext for beans coming out of L2 cache
    // so that means no batch lazy loading for these beans
    EntityBean entityBean = (EntityBean)bean;
    EntityBeanIntercept ebi = entityBean._ebean_getIntercept();
    ebi.setPersistenceContext(context);
    Object id = desc.getId(entityBean);
    context.put(id, bean);

  }
  
  /**
   * Return the beanCache creating it if necessary.
   */
  private ServerCache getBeanCache() {
    if (beanCache == null) {
      beanCache = cacheManager.getBeanCache(beanType);
    }
    return beanCache;
  }
  
  /**
   * Clear the bean cache.
   */
  public void beanCacheClear() {
    if (beanCache != null) {
      if (beanLog.isDebugEnabled()) {
        beanLog.debug("   CLEAR {}", cacheName);
      }
      beanCache.clear();
    }
  }

  public CachedBeanData beanExtractData(EntityBean bean) {
    return CachedBeanDataFromBean.extract(desc, bean);
  }

  public void beanLoadData(EntityBean bean, CachedBeanData data) {
    CachedBeanDataToBean.load(desc, bean, data);
  }
  
  /**
   * Put a bean into the bean cache.
   */
  public void beanCachePut(EntityBean bean) {

    CachedBeanData beanData = beanExtractData(bean);

    Object id = desc.getId(bean);
    if (beanLog.isDebugEnabled()) {
      beanLog.debug("   PUT {}({})", cacheName, id);
    }
    getBeanCache().put(id, beanData);
    
    if (beanData.isNaturalKeyUpdate() && naturalKeyCache != null) {
      Object naturalKey = beanData.getNaturalKey();
      if (naturalKey != null) {
        if (natLog.isDebugEnabled()) {
          natLog.debug(" PUT {}({}, {})", cacheName, naturalKey, id);
        }
        naturalKeyCache.put(naturalKey, id);
      }
    }
  }

  public CachedBeanData beanCacheGetData(Object id) {
    return (CachedBeanData) getBeanCache().get(id);
  }
  
  public T beanCacheGet(SpiQuery<T> query, PersistenceContext context) {
    T bean = beanCacheGetInternal(query.getId(), query.isReadOnly());
    if (bean != null) {
      setupContext(bean, query, context);
    }
    return bean;
  }
  
  /**
   * Return a bean from the bean cache.
   */
  @SuppressWarnings("unchecked")
  private T beanCacheGetInternal(Object id, Boolean readOnly) {

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
        return (T) bean;
      }
    }

    EntityBean bean = desc.createEntityBean();
    desc.convertSetId(id, bean);
    EntityBeanIntercept ebi = bean._ebean_getIntercept();
    ebi.setBeanLoader(desc.getEbeanServer());
    
    if (Boolean.TRUE.equals(readOnly)) {
      ebi.setReadOnly(true);
    }

    beanLoadData(bean, data);
    
    if (beanLog.isTraceEnabled()) {
      beanLog.trace("   GET {}({}) - hit", cacheName, id);
    }
    return (T) bean;
  }

  /**
   * Remove a bean from the cache given its Id.
   */
  public void beanCacheRemove(Object id) {
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
  public boolean beanCacheLoad(EntityBean bean, EntityBeanIntercept ebi, Object id) {

    CachedBeanData cacheData = (CachedBeanData) getBeanCache().get(id);
    if (cacheData == null) {
      if (beanLog.isTraceEnabled()) {
        beanLog.trace("   LOAD {}({}) - cache miss", cacheName, id);
      }
      return false;
    }
    int lazyLoadProperty = ebi.getLazyLoadPropertyIndex();
    if (lazyLoadProperty > -1 && !cacheData.isLoaded(lazyLoadProperty)) {
      if (beanLog.isTraceEnabled()) {
        beanLog.trace("   LOAD {}({}) - cache miss on property", cacheName, id);
      }
      return false;
    }

    CachedBeanDataToBean.load(desc, bean, cacheData);
    if (beanLog.isDebugEnabled()) {
      beanLog.debug("   LOAD {}({}) - hit", cacheName, id);
    }
    return true;
  }
  
  /**
   * Remove a bean from the cache given its Id.
   */
  public void handleDelete(Object id, PersistRequestBean<T> deleteRequest) {
    if (queryCache != null) {
      if (queryLog.isDebugEnabled()) {
        queryLog.debug("   CLEAR {}(*) - delete trigger", cacheName);
      }
      queryCache.clear();
    }
    if (beanCache != null) {
      if (beanLog.isDebugEnabled()) {
        beanLog.debug("   REMOVE {}({})", cacheName, id);
      }
      beanCache.remove(id);
    }
    for (int i = 0; i < propertiesOneImported.length; i++) {
      BeanPropertyAssocMany<?> many = propertiesOneImported[i].getRelationshipProperty();
      if (many != null) {
        propertiesOneImported[i].cacheDelete(true, deleteRequest.getEntityBean());
      }
    }
  }

  public void handleInsert(Object id, PersistRequestBean<T> insertRequest) {
    if (queryCache != null) {
      if (queryLog.isDebugEnabled()) {
        queryLog.debug("   CLEAR {}(*) - insert trigger", cacheName);
      }
      queryCache.clear();
    }
    for (int i = 0; i < propertiesOneImported.length; i++) {
      propertiesOneImported[i].cacheDelete(false, insertRequest.getEntityBean());
    }
  }

  /**
   * Update the cached bean data.
   */
  public void handleUpdate(Object id, PersistRequestBean<T> updateRequest) {

    if (queryCache != null) {
      if (queryLog.isDebugEnabled()) {
        queryLog.debug("   CLEAR {}(*) - update trigger", cacheName);
      }
      queryCache.clear();
    }

    List<BeanPropertyAssocMany<?>> manyCollections = updateRequest.getUpdatedManyCollections();
    if (manyCollections != null) {
      // clear the appropriate manyProp caches first
      for (int i = 0; i < manyCollections.size(); i++) {
        manyPropRemove(id, manyCollections.get(i).getName());  
      }
    }
    
    // check if the bean itself was updated
    if (!updateRequest.isUpdatedManysOnly()) {
      
      // update the bean cache entry if it exists
      ServerCache cache = getBeanCache();
      CachedBeanData existingData = (CachedBeanData) cache.get(id);
      if (existingData != null) {
        
        if (isCachedDataTooOld(existingData)) {
          // just remove the entry from the cache
          if (beanLog.isDebugEnabled()) {
            beanLog.debug("   REMOVE {}({}) - entry too old", cacheName, id);
          }
          cache.remove(id);
          
        } else {
          // Update the cache data with the changes from our update
          CachedBeanData newData = CachedBeanDataUpdate.update(desc, existingData, updateRequest.getEntityBean());
          if (beanLog.isDebugEnabled()) {
            beanLog.debug("   UPDATE {}({})", cacheName, id);
          }
          cache.put(id, newData);
          if (newData.isNaturalKeyUpdate() && naturalKeyCache != null) {
            
            Object oldKey = newData.getOldNaturalKey();
            Object newKey = newData.getNaturalKey();
            if (natLog.isDebugEnabled()) {
              natLog.debug(".. update {} PUT({}, {}) REMOVE({})", cacheName, newKey, id, oldKey);
            }
    
            if (oldKey != null) {
              naturalKeyCache.remove(oldKey);
            }
            if (newKey != null) {
              naturalKeyCache.put(newKey, id);
            }
          }
        }
      }
    }
    
    if (manyCollections != null) {
      for (int i = 0; i < manyCollections.size(); i++) {
        BeanPropertyAssocMany<?> many = manyCollections.get(i);
        Object manyValue = many.getValue(updateRequest.getEntityBean());
        manyPropPut(many, manyValue, id);
      }
    }
    
  }

  private boolean isCachedDataTooOld(CachedBeanData existingData) {
    return cacheOptions.isTooOldInMillis(System.currentTimeMillis() - existingData.getWhenCreated());
  }

  /**
   * Invalidate parts of cache due to SqlUpdate or external modification etc.
   */
  public void handleBulkUpdate(TableIUD tableIUD) {
    // inserts don't invalidate the bean cache
    if (tableIUD.isUpdateOrDelete()) {
      beanCacheClear();
    }
    // any change invalidates the query cache
    queryCacheClear();
  }
}
