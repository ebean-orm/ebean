package io.ebeaninternal.server.deploy;

import io.avaje.applog.AppLog;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebean.cache.QueryCacheEntry;
import io.ebean.cache.ServerCache;
import io.ebeaninternal.api.*;
import io.ebeaninternal.api.TransactionEventTable.TableIUD;
import io.ebeaninternal.server.cache.*;
import io.ebeaninternal.server.core.CacheOptions;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;

import java.io.IOException;
import java.util.*;

import static java.lang.System.Logger.Level.*;

/**
 * Helper for BeanDescriptor that manages the bean, query and collection caches.
 *
 * @param <T> The entity bean type
 */
final class BeanDescriptorCacheHelp<T> {

  private static final System.Logger log = CoreLog.internal;

  private static final System.Logger queryLog = AppLog.getLogger("io.ebean.cache.QUERY");
  private static final System.Logger beanLog = AppLog.getLogger("io.ebean.cache.BEAN");
  private static final System.Logger manyLog = AppLog.getLogger("io.ebean.cache.COLL");
  private static final System.Logger natLog = AppLog.getLogger("io.ebean.cache.NATKEY");

  private final BeanDescriptor<T> desc;
  private final SpiCacheManager cacheManager;
  private final CacheOptions cacheOptions;
  /**
   * Flag indicating this bean has no relationships.
   */
  private final boolean cacheSharableBeans;
  private final boolean invalidateQueryCache;
  private final Class<?> beanType;
  private final String cacheName;
  private final BeanPropertyAssocOne<?>[] propertiesOneImported;
  private final String[] naturalKey;
  private final ServerCache beanCache;
  private final ServerCache naturalKeyCache;
  private final ServerCache queryCache;
  private final boolean noCaching;
  private final SpiCacheControl cacheControl;
  private final SpiCacheRegion cacheRegion;
  /**
   * Set to true if all persist changes need to notify the cache.
   */
  private boolean cacheNotifyOnAll;
  /**
   * Set to true if delete changes need to notify cache.
   */
  private boolean cacheNotifyOnDelete;

  BeanDescriptorCacheHelp(BeanDescriptor<T> desc, SpiCacheManager cacheManager, CacheOptions cacheOptions,
                          boolean cacheSharableBeans, BeanPropertyAssocOne<?>[] propertiesOneImported) {
    this.desc = desc;
    this.beanType = desc.rootBeanType;
    this.cacheName = beanType.getSimpleName();
    this.cacheManager = cacheManager;
    this.cacheOptions = cacheOptions;
    this.invalidateQueryCache = cacheOptions.isInvalidateQueryCache();
    this.cacheSharableBeans = cacheSharableBeans;
    this.propertiesOneImported = propertiesOneImported;
    this.naturalKey = cacheOptions.getNaturalKey();
    if (!cacheOptions.isEnableQueryCache()) {
      this.queryCache = null;
    } else {
      this.queryCache = cacheManager.getQueryCache(beanType);
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
    this.noCaching = (beanCache == null && queryCache == null);
    if (noCaching) {
      this.cacheControl = DCacheControlNone.INSTANCE;
      this.cacheRegion = (invalidateQueryCache) ? cacheManager.getRegion(cacheOptions.getRegion()) : DCacheRegionNone.INSTANCE;
    } else {
      this.cacheRegion = cacheManager.getRegion(cacheOptions.getRegion());
      this.cacheControl = new DCacheControl(cacheRegion, (beanCache != null), (naturalKeyCache != null), (queryCache != null));
    }
  }

  /**
   * Derive the cache notify flags.
   */
  void deriveNotifyFlags() {
    cacheNotifyOnAll = (invalidateQueryCache || beanCache != null || queryCache != null);
    cacheNotifyOnDelete = !cacheNotifyOnAll && isNotifyOnDeletes();
    if (log.isLoggable(DEBUG)) {
      if (cacheNotifyOnAll || cacheNotifyOnDelete) {
        String notifyMode = cacheNotifyOnAll ? "All" : "Delete";
        log.log(DEBUG, "l2 caching on {0} - beanCaching:{1} queryCaching:{2} notifyMode:{3} ",
          desc.fullName(), isBeanCaching(), isQueryCaching(), notifyMode);
      }
    }
  }

  /**
   * Return true if there is an imported bi-directional relationship to a bea
   * that does have bean caching enabled.
   */
  private boolean isNotifyOnDeletes() {
    for (BeanPropertyAssocOne<?> imported : propertiesOneImported) {
      if (imported.isCacheNotifyRelationship()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return true if the persist request needs to notify the cache.
   */
  boolean isCacheNotify(PersistRequest.Type type) {
    return cacheRegion.isEnabled()
      && (cacheNotifyOnAll || cacheNotifyOnDelete && (type == PersistRequest.Type.DELETE || type == PersistRequest.Type.DELETE_PERMANENT));
  }

  /**
   * Return true if there is currently query caching for this type of bean.
   */
  boolean isQueryCaching() {
    return cacheControl.isQueryCaching();
  }

  /**
   * Return true if there is currently bean caching for this type of bean.
   */
  boolean isBeanCaching() {
    return cacheControl.isBeanCaching();
  }

  /**
   * Return true if there is natural key caching for this type of bean.
   */
  boolean isNaturalKeyCaching() {
    return cacheControl.isNaturalKeyCaching();
  }

  /**
   * Return true if there is bean or query caching on this type.
   */
  boolean isCaching() {
    return cacheControl.isCaching();
  }

  /**
   * Return the natural key properties.
   */
  String[] getNaturalKey() {
    return naturalKey;
  }

  CacheOptions getCacheOptions() {
    return cacheOptions;
  }

  /**
   * Clear the query cache.
   */
  void queryCacheClear() {
    if (queryCache != null) {
      if (queryLog.isLoggable(DEBUG)) {
        queryLog.log(DEBUG, "   CLEAR {0}", cacheName);
      }
      queryCache.clear();
    }
  }

  /**
   * Add query cache clear to the changeSet.
   */
  private void queryCacheClear(CacheChangeSet changeSet) {
    if (queryCache != null) {
      changeSet.addClearQuery(desc);
    }
  }

  /**
   * Get a query result from the query cache.
   */
  Object queryCacheGet(Object id) {
    if (queryCache == null) {
      throw new IllegalStateException("No query cache enabled on " + desc + ". Need explicit @Cache(enableQueryCache=true)");
    }
    Object queryResult = queryCache.get(id);
    if (queryLog.isLoggable(DEBUG)) {
      if (queryResult == null) {
        queryLog.log(DEBUG, "   GET {0}({1}) - cache miss", cacheName, id);
      } else {
        queryLog.log(DEBUG, "   GET {0}({1}) - hit", cacheName, id);
      }
    }
    return queryResult;
  }

  /**
   * Put a query result into the query cache.
   */
  void queryCachePut(Object id, QueryCacheEntry entry) {
    if (queryCache == null) {
      throw new IllegalStateException("No query cache enabled on " + desc + ". Need explicit @Cache(enableQueryCache=true)");
    }
    if (queryLog.isLoggable(DEBUG)) {
      queryLog.log(DEBUG, "   PUT {0}({1})", cacheName, id);
    }
    queryCache.put(id, entry);
  }

  void manyPropRemove(String propertyName, String parentKey) {
    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, propertyName);
    if (manyLog.isLoggable(TRACE)) {
      manyLog.log(TRACE, "   REMOVE {0}({1}).{2}", cacheName, parentKey, propertyName);
    }
    collectionIdsCache.remove(parentKey);
  }

  void manyPropClear(String propertyName) {
    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, propertyName);
    if (manyLog.isLoggable(DEBUG)) {
      manyLog.log(DEBUG, "   CLEAR {0}(*).{1} ", cacheName, propertyName);
    }
    collectionIdsCache.clear();
  }

  /**
   * Return the CachedManyIds for a given bean many property. Returns null if not in the cache.
   */
  private CachedManyIds manyPropGet(String propertyName, String parentKey) {
    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, propertyName);
    CachedManyIds entry = (CachedManyIds) collectionIdsCache.get(parentKey);
    if (entry == null) {
      if (manyLog.isLoggable(TRACE)) {
        manyLog.log(TRACE, "   GET {0}({1}).{2} - cache miss", cacheName, parentKey, propertyName);
      }
    } else if (manyLog.isLoggable(DEBUG)) {
      manyLog.log(DEBUG, "   GET {0}({1}).{2} - hit", cacheName, parentKey, propertyName);
    }
    return entry;
  }

  /**
   * Try to load the bean collection from cache return true if successful.
   */
  boolean manyPropLoad(BeanPropertyAssocMany<?> many, BeanCollection<?> bc, String parentKey) {
    if (many.isElementCollection()) {
      // held as part of the bean cache so skip
      return false;
    }
    CachedManyIds entry = manyPropGet(many.name(), parentKey);
    if (entry == null) {
      // not in cache so return unsuccessful
      return false;
    }
    EntityBean ownerBean = bc.owner();
    EntityBeanIntercept ebi = ownerBean._ebean_getIntercept();
    PersistenceContext persistenceContext = ebi.persistenceContext();
    BeanDescriptor<?> targetDescriptor = many.targetDescriptor();

    List<Object> idList = entry.getIdList();
    bc.checkEmptyLazyLoad();
    int i = 0;
    for (Object id : idList) {
      final EntityBean ref = targetDescriptor.createReference(persistenceContext, id);
      if (many.hasOrderColumn()) {
        ref._ebean_getIntercept().setSortOrder(++i);
      }
      many.add(bc, ref);
    }
    return true;
  }

  /**
   * Put the beanCollection into the cache.
   */
  void manyPropPut(BeanPropertyAssocMany<?> many, Object details, String parentKey) {
    if (many.isElementCollection()) {
      CachedBeanData data = (CachedBeanData) beanCache.get(parentKey);
      if (data != null) {
        try {
          // add as JSON to bean cache
          String asJson = many.jsonWriteCollection(details);
          Map<String, Object> changes = new HashMap<>();
          changes.put(many.name(), asJson);

          CachedBeanData newData = data.update(changes, data.getVersion());
          if (beanLog.isLoggable(DEBUG)) {
            beanLog.log(DEBUG, "   UPDATE {0}({1})  changes:{2}", cacheName, parentKey, changes);
          }
          beanCache.put(parentKey, newData);
        } catch (IOException e) {
          log.log(ERROR, "Error updating L2 cache", e);
        }
      }
    } else {
      CachedManyIds entry = createManyIds(many, details);
      if (entry != null) {
        cachePutManyIds(many.name(), parentKey, entry);
      }
    }
  }

  void cachePutManyIds(String manyName, String parentKey, CachedManyIds entry) {
    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, manyName);
    if (manyLog.isLoggable(DEBUG)) {
      manyLog.log(DEBUG, "   PUT {0}({1}).{2} - ids:{3}", cacheName, parentKey, manyName, entry);
    }
    collectionIdsCache.put(parentKey, entry);
  }

  private CachedManyIds createManyIds(BeanPropertyAssocMany<?> many, Object details) {
    Collection<?> actualDetails = BeanCollectionUtil.getActualDetails(details);
    if (actualDetails == null) {
      return null;
    }

    BeanDescriptor<?> targetDescriptor = many.targetDescriptor();
    List<Object> idList = new ArrayList<>(actualDetails.size());
    for (Object bean : actualDetails) {
      idList.add(targetDescriptor.id(bean));
    }
    return new CachedManyIds(idList);
  }

  /**
   * Hit the bean cache with the given ids returning the hits.
   */
  BeanCacheResult<T> cacheIdLookup(PersistenceContext context, boolean unmodifiable, Collection<?> ids) {
    Set<Object> keys = new HashSet<>(ids.size());
    for (Object id : ids) {
      keys.add(desc.cacheKey(id));
    }
    if (ids.isEmpty()) {
      return new BeanCacheResult<>();
    }
    Map<Object, Object> beanDataMap = beanCache.getAll(keys);
    if (beanLog.isLoggable(TRACE)) {
      beanLog.log(TRACE, "   MGET {0}({1}) - hits:{2}", cacheName, ids, beanDataMap.keySet());
    }
    BeanCacheResult<T> result = new BeanCacheResult<>();
    for (Map.Entry<Object, Object> entry : beanDataMap.entrySet()) {
      CachedBeanData cachedBeanData = (CachedBeanData) entry.getValue();
      T bean = convertToBean(entry.getKey(), unmodifiable, context, cachedBeanData);
      result.add(bean, desc.id(bean));
    }
    return result;
  }

  /**
   * Use natural keys to hit the bean cache and return resulting hits.
   */
  BeanCacheResult<T> naturalKeyLookup(PersistenceContext context, boolean unmodifiable, Set<Object> keys) {
    if (context == null) {
      context = new DefaultPersistenceContext();
    }

    // naturalKey -> Id map
    Map<Object, Object> naturalKeyMap = naturalKeyCache.getAll(keys);
    if (natLog.isLoggable(TRACE)) {
      natLog.log(TRACE, " MLOOKUP {0}({1}) - hits:{2}", cacheName, keys, naturalKeyMap);
    }

    BeanCacheResult<T> result = new BeanCacheResult<>();
    if (naturalKeyMap.isEmpty()) {
      return result;
    }

    // create reverse id -> natural key map
    Map<Object, Object> reverseMap = new HashMap<>();
    for (Map.Entry<Object, Object> entry : naturalKeyMap.entrySet()) {
      reverseMap.put(entry.getValue(), entry.getKey());
    }

    Set<Object> ids = new HashSet<>(naturalKeyMap.values());
    Map<Object, Object> beanDataMap = beanCache.getAll(ids);
    if (beanLog.isLoggable(TRACE)) {
      beanLog.log(TRACE, "   MGET {0}({1}) - hits:{2}", cacheName, ids, beanDataMap.keySet());
    }
    // process the hits into beans etc
    for (Map.Entry<Object, Object> entry : beanDataMap.entrySet()) {
      Object id = entry.getKey();
      CachedBeanData cachedBeanData = (CachedBeanData) entry.getValue();
      T bean = convertToBean(id, unmodifiable, context, cachedBeanData);
      Object naturalKey = reverseMap.get(id);
      result.add(bean, naturalKey);
    }
    return result;
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
    EntityBean entityBean = (EntityBean) bean;
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
  void beanCacheClear() {
    if (beanCache != null) {
      if (beanLog.isLoggable(DEBUG)) {
        beanLog.log(DEBUG, "   CLEAR {0}", cacheName);
      }
      beanCache.clear();
    }
  }

  CachedBeanData beanExtractData(BeanDescriptor<?> targetDesc, EntityBean bean) {
    return CachedBeanDataFromBean.extract(targetDesc, bean);
  }

  void beanPutAll(Collection<EntityBean> beans) {
    beanCachePutAllDirect(beans);
  }

  void beanCachePutAllDirect(Collection<EntityBean> beans) {
    Map<Object, Object> natKeys = null;
    if (naturalKey != null) {
      natKeys = new LinkedHashMap<>();
    }

    Map<Object, Object> map = new LinkedHashMap<>();
    for (EntityBean bean : beans) {
      CachedBeanData beanData = beanExtractData(desc, bean);
      String key = desc.cacheKeyForBean(bean);
      map.put(key, beanData);
      if (naturalKey != null) {
        Object naturalKey = calculateNaturalKey(beanData);
        if (naturalKey != null) {
          natKeys.put(naturalKey, key);
        }
      }
    }
    if (beanLog.isLoggable(DEBUG)) {
      beanLog.log(DEBUG, "   MPUT {0}({1})", cacheName, map.keySet());
    }
    getBeanCache().putAll(map);

    if (natKeys != null && !natKeys.isEmpty()) {
      if (natLog.isLoggable(DEBUG)) {
        natLog.log(DEBUG, " MPUT {0}({1}, {2})", cacheName, Arrays.toString(naturalKey), natKeys.keySet());
      }
      naturalKeyCache.putAll(natKeys);
    }
  }

  /**
   * Put a bean into the bean cache.
   */
  void beanCachePut(EntityBean bean) {
    beanCachePutDirect(bean);
  }

  /**
   * Put the bean into the bean cache.
   */
  void beanCachePutDirect(EntityBean bean) {
    CachedBeanData beanData = beanExtractData(desc, bean);
    String key = desc.cacheKeyForBean(bean);
    if (beanLog.isLoggable(DEBUG)) {
      beanLog.log(DEBUG, "   PUT {0}({1}) data:{2}", cacheName, key, beanData);
    }
    getBeanCache().put(key, beanData);
    if (naturalKey != null) {
      String naturalKey = calculateNaturalKey(beanData);
      if (naturalKey != null) {
        if (natLog.isLoggable(DEBUG)) {
          natLog.log(DEBUG, " PUT {0}({1}, {2})", cacheName, naturalKey, key);
        }
        naturalKeyCache.put(naturalKey, key);
      }
    }
  }

  private String calculateNaturalKey(CachedBeanData beanData) {
    if (naturalKey.length == 1) {
      Object data = beanData.getData(naturalKey[0]);
      return (data == null) ? null : data.toString();
    }
    StringBuilder sb = new StringBuilder();
    for (String key : naturalKey) {
      Object val = beanData.getData(key);
      if (val == null) {
        return null;
      }
      sb.append(val).append(';');
    }
    return sb.toString();
  }

  CachedBeanData beanCacheGetData(String key) {
    return (CachedBeanData) getBeanCache().get(key);
  }

  T beanCacheGet(String key, boolean unmodifiable, PersistenceContext context) {
    T bean = beanCacheGetInternal(key, unmodifiable, context);
    if (bean != null && !unmodifiable) {
      setupContext(bean, context);
    }
    return bean;
  }

  /**
   * Return a bean from the bean cache.
   */
  private T beanCacheGetInternal(String key, boolean unmodifiable, PersistenceContext context) {
    CachedBeanData data = (CachedBeanData) getBeanCache().get(key);
    if (data == null) {
      if (beanLog.isLoggable(TRACE)) {
        beanLog.log(TRACE, "   GET {0}({1}) - cache miss", cacheName, key);
      }
      return null;
    }
    if (beanLog.isLoggable(TRACE)) {
      beanLog.log(TRACE, "   GET {0}({1}) - hit", cacheName, key);
    }
    return convertToBean(key, unmodifiable, context, data);
  }

  @SuppressWarnings("unchecked")
  private T convertToBean(Object id, boolean unmodifiable, PersistenceContext context, CachedBeanData data) {
    if (cacheSharableBeans && unmodifiable) {
      Object bean = data.getSharableBean();
      if (bean != null) {
        if (beanLog.isLoggable(TRACE)) {
          beanLog.log(TRACE, "   GET {0}({1}) - hit shared bean", cacheName, id);
        }
        return (T) bean;
      }
    }
    return (T) loadBean(id, unmodifiable, data, context);
  }

  /**
   * Load the entity bean taking into account inheritance.
   */
  private EntityBean loadBean(Object id, boolean unmodifiable, CachedBeanData data, PersistenceContext context) {
    return loadBeanDirect(id, unmodifiable, data, context);
  }

  /**
   * Load the entity bean from cache data given this is the root bean type.
   */
  EntityBean loadBeanDirect(Object id, boolean unmodifiable, CachedBeanData data, PersistenceContext context) {
    id = desc.convertId(id);
    EntityBean bean = context == null ? null : (EntityBean) desc.contextGet(context, id);;
    if (bean == null) {
      bean = desc.createEntityBean2(unmodifiable);
      desc.setId(id, bean);
      if (!unmodifiable) {
        if (context == null) {
          context = new DefaultPersistenceContext();
        }
        desc.contextPut(context, id, bean);
        EntityBeanIntercept ebi = bean._ebean_getIntercept();
        ebi.setPersistenceContext(context);
        // Not using loadContext here so no batch lazy loading for these beans
        ebi.setBeanLoader(desc.l2BeanLoader());
      }
    }

    CachedBeanDataToBean.load(desc, bean, data, context);
    return bean;
  }

  /**
   * Load the embedded bean checking for inheritance.
   */
  EntityBean embeddedBeanLoad(CachedBeanData data, PersistenceContext context) {
    return embeddedBeanLoadDirect(data, context);
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
  void beanCacheApplyInvalidate(Collection<String> keys) {
    if (beanCache != null) {
      if (beanLog.isLoggable(DEBUG)) {
        beanLog.log(DEBUG, "   MREMOVE {0}({1})", cacheName, keys);
      }
      beanCache.removeAll(new HashSet<>(keys));
    }
    for (BeanPropertyAssocOne<?> imported : propertiesOneImported) {
      imported.cacheClear();
    }
  }

  /**
   * Load a batch of entities from L2 bean cache checking the lazy loaded property is loaded.
   */
  Set<EntityBeanIntercept> beanCacheLoadAll(Set<EntityBeanIntercept> batch, PersistenceContext context, int lazyLoadProperty, String propertyName) {
    Map<Object, EntityBeanIntercept> ebis = new HashMap<>();
    for (EntityBeanIntercept ebi : batch) {
      ebis.put(desc.cacheKeyForBean(ebi.owner()), ebi);
    }

    Map<Object, Object> hits = getBeanCache().getAll(ebis.keySet());
    if (beanLog.isLoggable(TRACE)) {
      beanLog.log(TRACE, "   MLOAD {0}({1}) - got hits ({2})", cacheName, ebis.keySet(), hits.size());
    }

    Set<EntityBeanIntercept> loaded = new HashSet<>();
    for (Map.Entry<Object, Object> hit : hits.entrySet()) {
      Object key = hit.getKey();
      EntityBeanIntercept ebi = ebis.remove(key);
      CachedBeanData cacheData = (CachedBeanData) hit.getValue();

      if (lazyLoadProperty > -1 && !cacheData.isLoaded(propertyName)) {
        if (beanLog.isLoggable(TRACE)) {
          beanLog.log(TRACE, "   load {0}({1}) - cache miss on property({2})", cacheName, key, propertyName);
        }
      } else {
        CachedBeanDataToBean.load(desc, ebi.owner(), cacheData, context);
        loaded.add(ebi);
        if (beanLog.isLoggable(DEBUG)) {
          beanLog.log(DEBUG, "   load {0}({1}) - hit", cacheName, key);
        }
      }
    }
    if (!ebis.isEmpty() && beanLog.isLoggable(TRACE)) {
      beanLog.log(TRACE, "   load {0}({1}) - cache miss", cacheName, ebis.keySet());
    }
    return loaded;
  }

  /**
   * Returns true if it managed to populate/load the single bean from the cache.
   */
  boolean beanCacheLoad(EntityBean bean, EntityBeanIntercept ebi, String key, PersistenceContext context) {
    CachedBeanData cacheData = (CachedBeanData) getBeanCache().get(key);
    if (cacheData == null) {
      if (beanLog.isLoggable(TRACE)) {
        beanLog.log(TRACE, "   LOAD {0}({1}) - cache miss", cacheName, key);
      }
      return false;
    }
    int lazyLoadProperty = ebi.lazyLoadPropertyIndex();
    if (lazyLoadProperty > -1 && !cacheData.isLoaded(ebi.lazyLoadProperty())) {
      if (beanLog.isLoggable(TRACE)) {
        beanLog.log(TRACE, "   LOAD {0}({1}) - cache miss on property({2})", cacheName, key, ebi.lazyLoadProperty());
      }
      return false;
    }
    CachedBeanDataToBean.load(desc, bean, cacheData, context);
    if (beanLog.isLoggable(DEBUG)) {
      beanLog.log(DEBUG, "   LOAD {0}({1}) - hit", cacheName, key);
    }
    return true;
  }

  void cacheUpdateQuery(boolean update, SpiTransaction transaction) {
    if (invalidateQueryCache || cacheNotifyOnAll || (!update && cacheNotifyOnDelete)) {
      transaction.event().add(desc.baseTable(), false, update, !update);
    }
  }

  /**
   * Add appropriate cache changes to support delete by id.
   */
  void persistDeleteIds(Collection<Object> ids, CacheChangeSet changeSet) {
    if (invalidateQueryCache) {
      changeSet.addInvalidate(desc);
    } else {
      queryCacheClear(changeSet);
      if (beanCache != null) {
        changeSet.addBeanRemoveMany(desc, ids);
      }
      cacheDeleteImported(true, null, changeSet);
    }
  }

  /**
   * Add appropriate cache changes to support delete bean.
   */
  void persistDelete(Object id, PersistRequestBean<T> deleteRequest, CacheChangeSet changeSet) {
    if (invalidateQueryCache) {
      changeSet.addInvalidate(desc);
    } else {
      queryCacheClear(changeSet);
      if (beanCache != null) {
        changeSet.addBeanRemove(desc, id);
      }
      cacheDeleteImported(true, deleteRequest.entityBean(), changeSet);
    }
  }

  /**
   * Add appropriate cache changes to support insert.
   */
  void persistInsert(PersistRequestBean<T> insertRequest, CacheChangeSet changeSet) {
    if (invalidateQueryCache) {
      changeSet.addInvalidate(desc);
    } else {
      queryCacheClear(changeSet);
      cacheDeleteImported(false, insertRequest.entityBean(), changeSet);
      changeSet.addBeanInsert(desc.baseTable());
    }
  }

  private void cacheDeleteImported(boolean clear, EntityBean entityBean, CacheChangeSet changeSet) {
    for (BeanPropertyAssocOne<?> imported : propertiesOneImported) {
      imported.cacheDelete(clear, entityBean, changeSet);
    }
  }

  /**
   * Add appropriate changes to support update.
   */
  void persistUpdate(Object id, PersistRequestBean<T> updateRequest, CacheChangeSet changeSet) {
    if (invalidateQueryCache) {
      changeSet.addInvalidate(desc);

    } else {
      queryCacheClear(changeSet);
      if (beanCache == null) {
        // query caching only
        return;
      }
      List<BeanPropertyAssocMany<?>> manyCollections = updateRequest.updatedManyForL2Cache();
      if (manyCollections != null) {
        for (BeanPropertyAssocMany<?> many : manyCollections) {
          Object details = many.getValue(updateRequest.entityBean());
          CachedManyIds entry = createManyIds(many, details);
          if (entry != null) {
            final String parentKey = desc.cacheKey(id);
            changeSet.addManyPut(desc, many.name(), parentKey, entry);
          }
        }
      }
      updateRequest.addBeanUpdate(changeSet);
    }
  }

  /**
   * Invalidate parts of cache due to SqlUpdate or external modification etc.
   */
  void persistTableIUD(TableIUD tableIUD, CacheChangeSet changeSet) {
    if (invalidateQueryCache) {
      changeSet.addInvalidate(desc);
      return;
    }
    if (noCaching) {
      return;
    }
    changeSet.addClearQuery(desc);
    // inserts don't invalidate the bean cache
    if (tableIUD.isUpdateOrDelete()) {
      changeSet.addClearBean(desc);
    }
    // any change invalidates the collection IDs cache
    for (BeanPropertyAssocOne<?> imported : propertiesOneImported) {
      imported.cacheClear(changeSet);
    }
  }

  void cacheNaturalKeyPut(String key, String newKey) {
    if (newKey != null) {
      naturalKeyCache.put(newKey, key);
    }
  }

  /**
   * Apply changes to the bean cache entry.
   */
  void cacheBeanUpdate(String key, Map<String, Object> changes, boolean updateNaturalKey, long version) {
    ServerCache cache = getBeanCache();
    CachedBeanData existingData = (CachedBeanData) cache.get(key);
    if (existingData != null) {
      long currentVersion = existingData.getVersion();
      if (version > 0 && version < currentVersion) {
        if (beanLog.isLoggable(DEBUG)) {
          beanLog.log(DEBUG, "   REMOVE {0}({1}) - version conflict old:{2} new:{3}", cacheName, key, currentVersion, version);
        }
        cache.remove(key);
      } else {
        if (version == 0) {
          version = currentVersion;
        }
        CachedBeanData newData = existingData.update(changes, version);
        if (beanLog.isLoggable(DEBUG)) {
          beanLog.log(DEBUG, "   UPDATE {0}({1})  changes:{2}", cacheName, key, changes);
        }
        cache.put(key, newData);
      }
      if (updateNaturalKey) {
        Object oldKey = calculateNaturalKey(existingData);
        if (oldKey != null) {
          if (natLog.isLoggable(DEBUG)) {
            natLog.log(DEBUG, ".. update {0} REMOVE({1}) - old key for ({2})", cacheName, oldKey, key);
          }
          naturalKeyCache.remove(oldKey);
        }
      }
    }
  }

}
