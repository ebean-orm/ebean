package io.ebeaninternal.server.deploy;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebean.cache.QueryCacheEntry;
import io.ebean.cache.ServerCache;
import io.ebeaninternal.api.BeanCacheResult;
import io.ebeaninternal.api.SpiCacheControl;
import io.ebeaninternal.api.SpiCacheRegion;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.TransactionEventTable.TableIUD;
import io.ebeaninternal.server.cache.CacheChangeSet;
import io.ebeaninternal.server.cache.CachedBeanData;
import io.ebeaninternal.server.cache.CachedBeanDataFromBean;
import io.ebeaninternal.server.cache.CachedBeanDataToBean;
import io.ebeaninternal.server.cache.CachedManyIds;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.core.CacheOptions;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper for BeanDescriptor that manages the bean, query and collection caches.
 *
 * @param <T> The entity bean type
 */
final class BeanDescriptorCacheHelp<T> {

  private static final Logger logger = LoggerFactory.getLogger(BeanDescriptorCacheHelp.class);

  private static final Logger queryLog = LoggerFactory.getLogger("io.ebean.cache.QUERY");
  private static final Logger beanLog = LoggerFactory.getLogger("io.ebean.cache.BEAN");
  private static final Logger manyLog = LoggerFactory.getLogger("io.ebean.cache.COLL");
  private static final Logger natLog = LoggerFactory.getLogger("io.ebean.cache.NATKEY");

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

    if (logger.isDebugEnabled()) {
      if (cacheNotifyOnAll || cacheNotifyOnDelete) {
        String notifyMode = cacheNotifyOnAll ? "All" : "Delete";
        logger.debug("l2 caching on {} - beanCaching:{} queryCaching:{} notifyMode:{} ",
          desc.getFullName(), isBeanCaching(), isQueryCaching(), notifyMode);
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
      if (queryLog.isDebugEnabled()) {
        queryLog.debug("   CLEAR {}", cacheName);
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
    if (queryLog.isDebugEnabled()) {
      if (queryResult == null) {
        queryLog.debug("   GET {}({}) - cache miss", cacheName, id);
      } else {
        queryLog.debug("   GET {}({}) - hit", cacheName, id);
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
    if (queryLog.isDebugEnabled()) {
      queryLog.debug("   PUT {}({})", cacheName, id);
    }
    queryCache.put(id, entry);
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

    if (many.isElementCollection()) {
      // held as part of the bean cache so skip
      return false;
    }

    CachedManyIds entry = manyPropGet(parentId, many.getName());
    if (entry == null) {
      // not in cache so return unsuccessful
      return false;
    }

    EntityBean ownerBean = bc.getOwnerBean();
    EntityBeanIntercept ebi = ownerBean._ebean_getIntercept();
    PersistenceContext persistenceContext = ebi.getPersistenceContext();

    BeanDescriptor<?> targetDescriptor = many.getTargetDescriptor();

    List<Object> idList = entry.getIdList();
    bc.checkEmptyLazyLoad();
    for (Object id : idList) {
      Object refBean = targetDescriptor.createReference(readOnly, false, id, persistenceContext);
      many.add(bc, (EntityBean) refBean);
    }
    return true;
  }

  /**
   * Put the beanCollection into the cache.
   */
  void manyPropPut(BeanPropertyAssocMany<?> many, Object details, Object parentId) {

    if (many.isElementCollection()) {
      CachedBeanData data = (CachedBeanData) beanCache.get(parentId);
      if (data != null) {
        try {
          // add as JSON to bean cache
          String asJson = many.jsonWriteCollection(details);
          Map<String, Object> changes = new HashMap<>();
          changes.put(many.getName(), asJson);

          CachedBeanData newData = data.update(changes, data.getVersion());
          if (beanLog.isDebugEnabled()) {
            beanLog.debug("   UPDATE {}({})  changes:{}", cacheName, parentId, changes);
          }
          beanCache.put(parentId, newData);
        } catch (IOException e) {
          logger.error("Error updating L2 cache", e);
        }
      }
    } else {
      CachedManyIds entry = createManyIds(many, details);
      if (entry != null) {
        cachePutManyIds(parentId, many.getName(), entry);
      }
    }
  }

  void cachePutManyIds(Object parentId, String manyName, CachedManyIds entry) {

    ServerCache collectionIdsCache = cacheManager.getCollectionIdsCache(beanType, manyName);
    if (manyLog.isDebugEnabled()) {
      manyLog.debug("   PUT {}({}).{} - ids:{}", cacheName, parentId, manyName, entry);
    }
    collectionIdsCache.put(parentId, entry);
  }

  private CachedManyIds createManyIds(BeanPropertyAssocMany<?> many, Object details) {

    Collection<?> actualDetails = BeanCollectionUtil.getActualDetails(details);
    if (actualDetails == null) {
      return null;
    }

    BeanDescriptor<?> targetDescriptor = many.getTargetDescriptor();
    List<Object> idList = new ArrayList<>(actualDetails.size());
    for (Object bean : actualDetails) {
      idList.add(targetDescriptor.getId((EntityBean) bean));
    }
    return new CachedManyIds(idList);
  }

  /**
   * Hit the bean cache with the given ids returning the hits.
   */
  BeanCacheResult<T> cacheIdLookup(PersistenceContext context, Collection<?> ids) {

    Set<Object> keys = new HashSet<>(ids.size());
    for (Object id : ids) {
      keys.add(desc.cacheKey(id));
    }

    Map<Object, Object> beanDataMap = beanCache.getAll(keys);
    if (beanLog.isTraceEnabled()) {
      beanLog.trace("   GET MANY {}({}) - hits:{}", cacheName, ids, beanDataMap.keySet());
    }

    BeanCacheResult<T> result = new BeanCacheResult<>();
    for (Map.Entry<Object, Object> entry : beanDataMap.entrySet()) {
      CachedBeanData cachedBeanData = (CachedBeanData) entry.getValue();
      T bean = convertToBean(entry.getKey(), false, context, cachedBeanData);
      result.add(bean, desc.getBeanId(bean));
    }

    return result;
  }

  /**
   * Use natural keys to hit the bean cache and return resulting hits.
   */
  BeanCacheResult<T> naturalKeyLookup(PersistenceContext context, Set<Object> keys) {

    if (context == null) {
      context = new DefaultPersistenceContext();
    }

    // naturalKey -> Id map
    Map<Object, Object> naturalKeyMap = naturalKeyCache.getAll(keys);

    if (natLog.isTraceEnabled()) {
      natLog.trace(" LOOKUP Many {}({}) - hits:{}", cacheName, keys, naturalKeyMap);
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
    if (beanLog.isTraceEnabled()) {
      beanLog.trace("   GET MANY {}({}) - hits:{}", cacheName, ids, beanDataMap.keySet());
    }
    // process the hits into beans etc
    for (Map.Entry<Object, Object> entry : beanDataMap.entrySet()) {

      Object id = entry.getKey();
      CachedBeanData cachedBeanData = (CachedBeanData) entry.getValue();

      T bean = convertToBean(id, false, context, cachedBeanData);
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
      if (beanLog.isDebugEnabled()) {
        beanLog.debug("   CLEAR {}", cacheName);
      }
      beanCache.clear();
    }
  }

  CachedBeanData beanExtractData(BeanDescriptor<?> targetDesc, EntityBean bean) {
    return CachedBeanDataFromBean.extract(targetDesc, bean);
  }

  void beanPutAll(Collection<EntityBean> beans) {
    if (desc.inheritInfo != null) {
      Class<?> aClass = theClassOf(beans);
      // check if all beans have the same class
      for (EntityBean bean : beans) {
        if (!bean.getClass().equals(aClass)) {
          aClass = null;
          break;
        }
      }
      if (aClass == null) {
        // there are different bean types in the collection, so we add one by one to the cache
        for (EntityBean bean : beans) {
          desc.descOf(bean.getClass()).cacheBeanPutDirect(bean);
        }
      } else {
        desc.descOf(aClass).cacheBeanPutAllDirect(beans);
      }
    } else {
      beanCachePutAllDirect(beans);
    }
  }

  private Class<?> theClassOf(Collection<EntityBean> beans) {
    if (beans instanceof List) {
      return ((List<?>) beans).get(0).getClass();
    }
    return beans.iterator().next().getClass();
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
    if (beanLog.isDebugEnabled()) {
      beanLog.debug("   PUT ALL {}({})", cacheName, map.keySet());
    }
    getBeanCache().putAll(map);

    if (natKeys != null && !natKeys.isEmpty()) {
      if (natLog.isDebugEnabled()) {
        natLog.debug(" PUT ALL {}({}, {})", cacheName, naturalKey, natKeys.keySet());
      }
      naturalKeyCache.putAll(natKeys);
    }
  }

  /**
   * Put the bean into the bean cache.
   */
  void beanCachePutDirect(EntityBean bean) {

    CachedBeanData beanData = beanExtractData(desc, bean);

    String key = desc.cacheKeyForBean(bean);
    if (beanLog.isDebugEnabled()) {
      beanLog.debug("   PUT {}({}) data:{}", cacheName, key, beanData);
    }
    getBeanCache().put(key, beanData);

    if (naturalKey != null) {
      String naturalKey = calculateNaturalKey(beanData);
      if (naturalKey != null) {
        if (natLog.isDebugEnabled()) {
          natLog.debug(" PUT {}({}, {})", cacheName, naturalKey, key);
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
      sb.append(val).append(";");
    }
    return sb.toString();
  }

  CachedBeanData beanCacheGetData(String key) {
    return (CachedBeanData) getBeanCache().get(key);
  }

  T beanCacheGet(String key, Boolean readOnly, PersistenceContext context) {
    T bean = beanCacheGetInternal(key, readOnly, context);
    if (bean != null) {
      setupContext(bean, context);
    }
    return bean;
  }

  /**
   * Return a bean from the bean cache.
   */
  private T beanCacheGetInternal(String key, Boolean readOnly, PersistenceContext context) {

    CachedBeanData data = (CachedBeanData) getBeanCache().get(key);
    if (data == null) {
      if (beanLog.isTraceEnabled()) {
        beanLog.trace("   GET {}({}) - cache miss", cacheName, key);
      }
      return null;
    }
    if (beanLog.isTraceEnabled()) {
      beanLog.trace("   GET {}({}) - hit", cacheName, key);
    }
    return convertToBean(key, readOnly, context, data);
  }

  @SuppressWarnings("unchecked")
  private T convertToBean(Object id, Boolean readOnly, PersistenceContext context, CachedBeanData data) {
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

    return (T) loadBean(id, readOnly, data, context);
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
  void beanCacheApplyInvalidate(Collection<String> keys) {
    if (beanCache != null) {
      if (beanLog.isDebugEnabled()) {
        beanLog.debug("   REMOVE {}({})", cacheName, keys);
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
  Set<EntityBeanIntercept> beanCacheLoadAll(List<EntityBeanIntercept> list, PersistenceContext context, int lazyLoadProperty, String propertyName) {

    Map<Object, EntityBeanIntercept> ebis = new HashMap<>();
    for (EntityBeanIntercept ebi : list) {
      ebis.put(desc.cacheKeyForBean(ebi.getOwner()), ebi);
    }


    Map<Object, Object> hits = getBeanCache().getAll(ebis.keySet());

    if (beanLog.isTraceEnabled()) {
      beanLog.trace("   LOAD ALL {}({}) - got hits ({})", cacheName, ebis.keySet(), hits.size());
    }

    Set<EntityBeanIntercept> loaded = new HashSet<>();

    Iterator<Map.Entry<Object, Object>> iterator = hits.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<Object, Object> hit = iterator.next();

      Object key = hit.getKey();
      EntityBeanIntercept ebi = ebis.remove(key);
      CachedBeanData cacheData = (CachedBeanData) hit.getValue();

      if (lazyLoadProperty > -1 && !cacheData.isLoaded(propertyName)) {
        if (beanLog.isTraceEnabled()) {
          beanLog.trace("   LOAD {}({}) - cache miss on property({})", cacheName, key, propertyName);
        }
        iterator.remove();

      } else {
        CachedBeanDataToBean.load(desc, ebi.getOwner(), cacheData, context);
        loaded.add(ebi);
        if (beanLog.isDebugEnabled()) {
          beanLog.debug("   LOAD {}({}) - hit", cacheName, key);
        }
      }
    }

    if (!ebis.isEmpty() && beanLog.isTraceEnabled()) {
      beanLog.trace("   LOAD {}({}) - cache miss", cacheName, ebis.keySet());
    }

    return loaded;
  }

  /**
   * Returns true if it managed to populate/load the single bean from the cache.
   */
  boolean beanCacheLoad(EntityBean bean, EntityBeanIntercept ebi, String key, PersistenceContext context) {

    CachedBeanData cacheData = (CachedBeanData) getBeanCache().get(key);
    if (cacheData == null) {
      if (beanLog.isTraceEnabled()) {
        beanLog.trace("   LOAD {}({}) - cache miss", cacheName, key);
      }
      return false;
    }
    int lazyLoadProperty = ebi.getLazyLoadPropertyIndex();
    if (lazyLoadProperty > -1 && !cacheData.isLoaded(ebi.getLazyLoadProperty())) {
      if (beanLog.isTraceEnabled()) {
        beanLog.trace("   LOAD {}({}) - cache miss on property({})", cacheName, key, ebi.getLazyLoadProperty());
      }
      return false;
    }

    CachedBeanDataToBean.load(desc, bean, cacheData, context);
    if (beanLog.isDebugEnabled()) {
      beanLog.debug("   LOAD {}({}) - hit", cacheName, key);
    }
    return true;
  }

  void cacheUpdateQuery(boolean update, SpiTransaction transaction) {
    if (invalidateQueryCache || cacheNotifyOnAll || (!update && cacheNotifyOnDelete)) {
      transaction.getEvent().add(desc.getBaseTable(), false, update, !update);
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
      cacheDeleteImported(true, deleteRequest.getEntityBean(), changeSet);
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
      cacheDeleteImported(false, insertRequest.getEntityBean(), changeSet);
      changeSet.addBeanInsert(desc.getBaseTable());
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
      List<BeanPropertyAssocMany<?>> manyCollections = updateRequest.getUpdatedManyCollections();
      if (manyCollections != null) {
        for (BeanPropertyAssocMany<?> many : manyCollections) {
          if (!many.isElementCollection()) {
            Object details = many.getValue(updateRequest.getEntityBean());
            CachedManyIds entry = createManyIds(many, details);
            if (entry != null) {
              changeSet.addManyPut(desc, many.getName(), id, entry);
            }
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
        if (beanLog.isDebugEnabled()) {
          beanLog.debug("   REMOVE {}({}) - version conflict old:{} new:{}", cacheName, key, currentVersion, version);
        }
        cache.remove(key);
      } else {
        if (version == 0) {
          version = currentVersion;
        }
        CachedBeanData newData = existingData.update(changes, version);
        if (beanLog.isDebugEnabled()) {
          beanLog.debug("   UPDATE {}({})  changes:{}", cacheName, key, changes);
        }
        cache.put(key, newData);
      }

      if (updateNaturalKey) {
        Object oldKey = calculateNaturalKey(existingData);
        if (oldKey != null) {
          if (natLog.isDebugEnabled()) {
            natLog.debug(".. update {} REMOVE({}) - old key for ({})", cacheName, oldKey, key);
          }
          naturalKeyCache.remove(oldKey);
        }
      }
    }
  }

}
