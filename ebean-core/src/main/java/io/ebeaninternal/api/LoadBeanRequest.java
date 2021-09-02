package io.ebeaninternal.api;

import io.ebean.CacheMode;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.api.SpiQuery.Mode;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Request for loading ManyToOne and OneToOne relationships.
 */
public final class LoadBeanRequest extends LoadRequest {

  private final List<EntityBeanIntercept> batch;
  private final LoadBeanBuffer loadBuffer;
  private final String lazyLoadProperty;
  private final boolean loadCache;
  private final boolean alreadyLoaded;

  /**
   * Construct for lazy load request.
   */
  public LoadBeanRequest(LoadBeanBuffer loadBuffer, EntityBeanIntercept ebi, boolean loadCache) {
    this(loadBuffer, null, true, ebi.getLazyLoadProperty(), ebi.isLoaded(), loadCache || ebi.isLoadedFromCache());
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
    this.batch = loadBuffer.getBatch();
    this.lazyLoadProperty = lazyLoadProperty;
    this.alreadyLoaded = alreadyLoaded;
    this.loadCache = loadCache;
  }

  @Override
  public Class<?> getBeanType() {
    return loadBuffer.getBeanDescriptor().getBeanType();
  }

  public String description() {
    return loadBuffer.getFullPath();
  }

  /**
   * Return the batch of beans to actually load.
   */
  public List<EntityBeanIntercept> getBatch() {
    return batch;
  }

  /**
   * Return the load context.
   */
  private LoadBeanBuffer getLoadContext() {
    return loadBuffer;
  }

  public int getBatchSize() {
    return getLoadContext().getBatchSize();
  }

  /**
   * Return the list of Id values for the beans in the lazy load buffer.
   */
  public List<Object> getIdList() {
    List<Object> idList = new ArrayList<>();
    BeanDescriptor<?> desc = loadBuffer.getBeanDescriptor();
    for (EntityBeanIntercept ebi : batch) {
      idList.add(desc.getId(ebi.getOwner()));
    }
    return idList;
  }

  /**
   * Configure the query for lazy loading execution.
   */
  public void configureQuery(SpiQuery<?> query, List<Object> idList) {
    query.setMode(Mode.LAZYLOAD_BEAN);
    query.setPersistenceContext(loadBuffer.getPersistenceContext());
    query.setLoadDescription(lazy ? "+lazy" : "+query", description());
    if (lazy) {
      query.setLazyLoadBatchSize(getBatchSize());
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
  public void postLoad(List<?> list) {
    Set<Object> loadedIds = new HashSet<>();
    BeanDescriptor<?> desc = loadBuffer.getBeanDescriptor();
    // collect Ids and maybe load bean cache
    for (Object bean : list) {
      loadedIds.add(desc.beanId(bean));
    }
    if (loadCache) {
      desc.cacheBeanPutAll(list);
    }
    if (lazyLoadProperty != null) {
      for (EntityBeanIntercept ebi : batch) {
        // check if the underlying row in DB was deleted. Mark the bean as 'failed' if
        // necessary but allow processing to continue until it is accessed by client code
        Object id = desc.getId(ebi.getOwner());
        if (!loadedIds.contains(id)) {
          // assume this is logically deleted (hence not found)
          desc.markAsDeleted(ebi.getOwner());
        }
      }
    }
  }
}
