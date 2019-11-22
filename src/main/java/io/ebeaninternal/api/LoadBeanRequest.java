package io.ebeaninternal.api;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Request for loading ManyToOne and OneToOne relationships.
 */
public class LoadBeanRequest extends LoadRequest {

  private final List<EntityBeanIntercept> batch;

  private final LoadBeanBuffer loadBuffer;

  private final String lazyLoadProperty;

  private final boolean loadCache;

  private boolean loadedFromCache;

  /**
   * Construct for lazy load request.
   */
  public LoadBeanRequest(LoadBeanBuffer LoadBuffer, EntityBeanIntercept ebi, boolean loadCache) {
    this(LoadBuffer, null, true, ebi.getLazyLoadProperty(), loadCache);
    this.loadedFromCache = ebi.isLoadedFromCache();
  }

  /**
   * Construct for secondary query.
   */
  public LoadBeanRequest(LoadBeanBuffer LoadBuffer, OrmQueryRequest<?> parentRequest) {
    this(LoadBuffer, parentRequest, false, null, false);
  }

  private LoadBeanRequest(LoadBeanBuffer loadBuffer, OrmQueryRequest<?> parentRequest, boolean lazy,
                          String lazyLoadProperty, boolean loadCache) {

    super(parentRequest, lazy);
    this.loadBuffer = loadBuffer;
    this.batch = loadBuffer.getBatch();
    this.lazyLoadProperty = lazyLoadProperty;
    this.loadCache = loadCache;
  }

  @Override
  public Class<?> getBeanType() {
    return loadBuffer.getBeanDescriptor().getBeanType();
  }

  /**
   * Return true if the beans invoking lazy loading were previously loaded from cache.
   */
  public boolean isLoadedFromCache() {
    return loadedFromCache;
  }

  private boolean isLoadCache() {
    return loadCache;
  }

  public String getDescription() {
    return "path:" + loadBuffer.getFullPath() + " batch:" + batch.size();
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

    query.setMode(SpiQuery.Mode.LAZYLOAD_BEAN);
    query.setPersistenceContext(loadBuffer.getPersistenceContext());

    String mode = isLazy() ? "+lazy" : "+query";
    query.setLoadDescription(mode, getDescription());

    if (isLazy()) {
      // cascade the batch size (if set) for further lazy loading
      query.setLazyLoadBatchSize(getBatchSize());
    }

    loadBuffer.configureQuery(query, lazyLoadProperty);

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
    for (Object aList : list) {
      EntityBean loadedBean = (EntityBean) aList;
      loadedIds.add(desc.getId(loadedBean));
    }
    if (isLoadCache()) {
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
