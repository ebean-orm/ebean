package io.ebeaninternal.api;

import io.ebean.CacheMode;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.api.SpiQuery.Mode;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.*;

/**
 * Request for loading ManyToOne and OneToOne relationships.
 */
public final class LoadBeanRequest extends LoadRequest {

  private final Set<EntityBeanIntercept> batch;
  private final LoadBeanBuffer loadBuffer;
  private final String lazyLoadProperty;
  private final boolean loadCache;
  private final boolean alreadyLoaded;

  /**
   * Construct for lazy load request.
   */
  public LoadBeanRequest(LoadBeanBuffer loadBuffer, EntityBeanIntercept ebi, boolean loadCache) {
    this(loadBuffer, null, true, ebi.lazyLoadProperty(), ebi.isLoaded(), loadCache || ebi.isLoadedFromCache());
  }

  /**
   * Construct for secondary query.
   */
  public LoadBeanRequest(LoadBeanBuffer loadBuffer, OrmQueryRequest<?> parentRequest) {
    this(loadBuffer, parentRequest, false, null, false, false);
  }

  private LoadBeanRequest(LoadBeanBuffer loadBuffer, OrmQueryRequest<?> parentRequest, boolean lazy,
                          String lazyLoadProperty, boolean alreadyLoaded, boolean loadCache) {
    super(parentRequest, lazy);
    this.loadBuffer = loadBuffer;
    this.batch = loadBuffer.batch();
    this.lazyLoadProperty = lazyLoadProperty;
    this.alreadyLoaded = alreadyLoaded;
    this.loadCache = loadCache;
  }

  @Override
  public Class<?> beanType() {
    return loadBuffer.descriptor().type();
  }

  public String description() {
    return loadBuffer.fullPath();
  }

  /**
   * Return the batch of beans to actually load.
   */
  public Set<EntityBeanIntercept> batch() {
    return batch;
  }

  /**
   * Return the list of Id values for the beans in the lazy load buffer.
   */
  public List<Object> ids() {
    final List<Object> idList = new ArrayList<>(batch.size());
    final BeanDescriptor<?> desc = loadBuffer.descriptor();
    for (EntityBeanIntercept ebi : batch) {
      idList.add(desc.getId(ebi.owner()));
    }
    return idList;
  }

  /**
   * Configure the query for lazy loading execution.
   */
  public void configureQuery(SpiQuery<?> query, List<Object> idList) {
    query.setMode(Mode.LAZYLOAD_BEAN);
    query.setPersistenceContext(loadBuffer.persistenceContext());
    query.setLoadDescription(lazy ? "+lazy" : "+query", description());
    if (lazy) {
      query.setLazyLoadBatchSize(loadBuffer.batchSize());
      if (alreadyLoaded) {
        query.setBeanCacheMode(CacheMode.OFF);
      }
    } else {
      query.setBeanCacheMode(CacheMode.OFF);
    }
    loadBuffer.configureQuery(query, lazyLoadProperty);
    if (loadCache) {
      query.setBeanCacheMode(CacheMode.PUT);
    }
    if (idList.size() == 1) {
      query.where().idEq(idList.get(0));
    } else {
      query.where().idIn(idList);
    }
  }

  /**
   * Load the beans into the L2 cache if that is requested and check for load failures due to deletes.
   */
  public Result postLoad(List<?> list) {
    Set<Object> loadedIds = new HashSet<>();
    BeanDescriptor<?> desc = loadBuffer.descriptor();
    // collect Ids and maybe load bean cache
    for (Object bean : list) {
      loadedIds.add(desc.id(bean));
    }
    if (loadCache) {
      desc.cacheBeanPutAll(list);
    }
    if (lazyLoadProperty != null) {
      List<EntityBeanIntercept> missed = new ArrayList<>();
      Set<Object> missedIds = new HashSet<>();
      for (EntityBeanIntercept ebi : batch) {
        // check if the underlying row in DB was deleted. Mark the bean as 'failed' if
        // necessary but allow processing to continue until it is accessed by client code
        Object id = desc.getId(ebi.owner());
        if (!loadedIds.contains(id)) {
          // assume this is logically deleted (hence not found)
          desc.markAsDeleted(ebi.owner());
          missedIds.add(id);
          missed.add(ebi);
        }
      }
      return new Result(loadedIds, missedIds, missed);
    }
    return EMPTY_RESULT;
  }

  static final Result EMPTY_RESULT = new Result(Collections.emptySet(),Collections.emptySet(), Collections.emptyList());

  public static class Result {

    private final Set<Object> loadedIds;
    private final Set<Object> missedIds;
    private final List<EntityBeanIntercept> missed;

    Result(Set<Object> loadedIds, Set<Object> missedIds, List<EntityBeanIntercept> missed) {
      this.loadedIds = loadedIds;
      this.missedIds = missedIds;
      this.missed = missed;
    }

    public boolean markedDeleted() {
      return !missedIds.isEmpty();
    }

    public Set<Object> missedIds() {
      return missedIds;
    }

    public Set<Object> loadedIds() {
      return loadedIds;
    }

    public List<EntityBeanIntercept> missed() {
      return missed;
    }
  }
}
