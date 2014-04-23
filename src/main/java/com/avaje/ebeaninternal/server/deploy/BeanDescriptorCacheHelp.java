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
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeaninternal.server.cache.CachedBeanData;
import com.avaje.ebeaninternal.server.cache.CachedBeanDataFromBean;
import com.avaje.ebeaninternal.server.cache.CachedBeanDataToBean;
import com.avaje.ebeaninternal.server.cache.CachedBeanDataUpdate;
import com.avaje.ebeaninternal.server.cache.CachedManyIds;
import com.avaje.ebeaninternal.server.core.CacheOptions;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;

/**
 * Helper for BeanDescriptor that manages the bean, query and collection caches.
 * 
 * @param <T> The entity bean type
 */
public final class BeanDescriptorCacheHelp<T> {

  private static final Logger logger = LoggerFactory.getLogger(BeanDescriptorCacheHelp.class);

  private final BeanDescriptor<T> desc;

  private final ServerCacheManager cacheManager;

  private final CacheOptions cacheOptions;

  /**
   * Flag indicating this bean has no relationships.
   */
  private final boolean cacheSharableBeans;

  private final Class<T> beanType;

  private final BeanPropertyAssocOne<?>[] propertiesOneImported;

  private ServerCache beanCache;
  private ServerCache naturalKeyCache;
  private ServerCache queryCache;

  public BeanDescriptorCacheHelp(BeanDescriptor<T> desc, ServerCacheManager cacheManager, CacheOptions cacheOptions,
      boolean cacheSharableBeans, BeanPropertyAssocOne<?>[] propertiesOneImported) {

    this.desc = desc;
    this.beanType = desc.getBeanType();
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
      if (logger.isInfoEnabled()) {
        String msg = "Loaded " + beanType + " cache with [" + list.size() + "] beans";
        logger.info(msg);
      }
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
    queryCache.put(id, query);
  }


  public void manyPropRemove(Object parentId, String propertyName) {
    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, propertyName);
    collectionIdsCache.remove(parentId);
  }

  public void manyPropClear(String propertyName) {
    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, propertyName);
    collectionIdsCache.clear();
  }

  /**
   * Return the CachedManyIds for a given bean many property. Returns null if not in the cache.
   */
  public CachedManyIds manyPropGet(Object parentId, String propertyName) {
    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, propertyName);
    return (CachedManyIds) collectionIdsCache.get(parentId);
  }

  /**
   * Put the CachedManyIds into the cache.
   */
  public void manyPropPutEntry(Object parentId, String propertyName, CachedManyIds ids) {
    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, propertyName);
    collectionIdsCache.put(parentId, ids);
  }

  /**
   * Try to load the bean collection from cache return true if successful.
   */
  public boolean manyPropLoad(BeanPropertyAssocMany<?> many, BeanCollection<?> bc, Object parentId, Boolean readOnly) {

    CachedManyIds ids = manyPropGet(parentId, many.getName());
    if (ids == null) {
      // not in cache so return unsuccessful
      return false;
    }

    Object ownerBean = bc.getOwnerBean();
    EntityBeanIntercept ebi = ((EntityBean) ownerBean)._ebean_getIntercept();
    PersistenceContext persistenceContext = ebi.getPersistenceContext();

    BeanDescriptor<?> targetDescriptor = many.getTargetDescriptor();

    List<Object> idList = ids.getIdList();
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
  public void manyPropPut(BeanPropertyAssocMany<?> many, BeanCollection<?> bc, Object parentId) {

    BeanDescriptor<?> targetDescriptor = many.getTargetDescriptor();
    ArrayList<Object> idList = new ArrayList<Object>();

    // get the underlying collection of beans (in the List, Set or Map)
    Collection<?> actualDetails = bc.getActualDetails();
    for (Object bean : actualDetails) {
      // Collect the id values
      idList.add(targetDescriptor.getId((EntityBean) bean));
    }
    CachedManyIds ids = new CachedManyIds(idList);
    manyPropPutEntry(parentId, many.getName(), ids);
  }


  public boolean isNaturalKey(String propName) {
    return propName != null && propName.equals(cacheOptions.getNaturalKey());
  }

  public Object naturalKeyLookup(Object uniqueKeyValue) {
    if (naturalKeyCache != null) {
      return naturalKeyCache.get(uniqueKeyValue);
    }
    return null;
  }

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
      beanCache.clear();
    }
  }

  /**
   * Put a bean into the bean cache.
   */
  public void beanCachePut(EntityBean bean) {

    CachedBeanData beanData = CachedBeanDataFromBean.extract(desc, bean);

    Object id = desc.getId(bean);
    getBeanCache().put(id, beanData);
    if (beanData.isNaturalKeyUpdate() && naturalKeyCache != null) {
      Object naturalKey = beanData.getNaturalKey();
      if (naturalKey != null) {
        naturalKeyCache.put(naturalKey, id);
      }
    }
  }

  public CachedBeanData beanCacheGetData(Object id) {
    return (CachedBeanData) getBeanCache().get(id);
  }
  
  /**
   * Return a bean from the bean cache.
   */
  @SuppressWarnings("unchecked")
  public T beanCacheGet(Object id, Boolean readOnly) {

    CachedBeanData d = (CachedBeanData) getBeanCache().get(id);
    if (d == null) {
      return null;
    }
    if (cacheSharableBeans && !Boolean.FALSE.equals(readOnly)) {
      Object bean = d.getSharableBean();
      if (bean != null) {
        return (T) bean;
      }
    }

    EntityBean bean = desc.createBean();
    desc.convertSetId(id, bean);
    if (Boolean.TRUE.equals(readOnly)) {
      bean._ebean_getIntercept().setReadOnly(true);
    }

    CachedBeanDataToBean.load(desc, bean, d);
    return (T) bean;
  }


  /**
   * Remove a bean from the cache given its Id.
   */
  public void beanCacheRemove(Object id) {
    if (beanCache != null) {
      beanCache.remove(id);
    }
    for (int i = 0; i < propertiesOneImported.length; i++) {
      propertiesOneImported[i].cacheClear();
    }
  }

  public boolean beanCacheLoad(EntityBean bean, EntityBeanIntercept ebi, Object id) {

    CachedBeanData cacheData = (CachedBeanData) getBeanCache().get(id);
    if (cacheData == null) {
      return false;
    }
    int lazyLoadProperty = ebi.getLazyLoadPropertyIndex();
    if (lazyLoadProperty > -1 && !cacheData.containsProperty(lazyLoadProperty)) {
      return false;
    }

    CachedBeanDataToBean.load(desc, bean, cacheData);
    return true;
  }
  
  /**
   * Remove a bean from the cache given its Id.
   */
  public void handleDelete(Object id, PersistRequestBean<T> deleteRequest) {
    if (queryCache != null) {
      queryCache.clear();
    }
    if (beanCache != null) {
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
      queryCache.clear();
    }

    ServerCache cache = getBeanCache();
    CachedBeanData existingData = (CachedBeanData) cache.get(id);
    if (existingData != null) {
      CachedBeanData newData = CachedBeanDataUpdate.update(desc, existingData, updateRequest.getEntityBean());
      cache.put(id, newData);
      if (newData.isNaturalKeyUpdate() && naturalKeyCache != null) {
        
        Object oldKey = newData.getOldNaturalKey();
        Object newKey = newData.getNaturalKey();
        if (oldKey != null) {
          naturalKeyCache.remove(oldKey);
        }
        if (newKey != null) {
          naturalKeyCache.put(newKey, id);
        }
      }
    }
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
