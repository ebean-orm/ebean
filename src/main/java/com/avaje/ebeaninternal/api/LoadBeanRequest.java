package com.avaje.ebeaninternal.api;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for loading ManyToOne and OneToOne relationships.
 */
public class LoadBeanRequest extends LoadRequest {

  private static final Logger logger = LoggerFactory.getLogger(LoadBeanRequest.class);

  private final List<EntityBeanIntercept> batch;

  private final LoadBeanBuffer loadBuffer;

  private final int lazyLoadPropertyIndex;

  private final String lazyLoadProperty;

  private final boolean loadCache;

  /**
   * Construct for lazy load request.
   */
  public LoadBeanRequest(LoadBeanBuffer LoadBuffer, int lazyLoadPropertyIndex, String lazyLoadProperty, boolean loadCache) {
    this(LoadBuffer, null, true, lazyLoadPropertyIndex, lazyLoadProperty, loadCache);
  }

  /**
   * Construct for secondary query.
   */
  public LoadBeanRequest(LoadBeanBuffer LoadBuffer, OrmQueryRequest<?> parentRequest) {
    this(LoadBuffer, parentRequest, false, -1, null, false);
  }

  private LoadBeanRequest(LoadBeanBuffer loadBuffer, OrmQueryRequest<?> parentRequest, boolean lazy,
                          int lazyLoadPropertyIndex, String lazyLoadProperty, boolean loadCache) {

    super(parentRequest, lazy);
    this.loadBuffer = loadBuffer;
    this.batch = loadBuffer.getBatch();
    this.lazyLoadPropertyIndex = lazyLoadPropertyIndex;
    this.lazyLoadProperty = lazyLoadProperty;
    this.loadCache = loadCache;
  }

  @Override
  public Class<?> getBeanType() {
    return loadBuffer.getBeanDescriptor().getBeanType();
  }

  public boolean isLoadCache() {
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
  public LoadBeanBuffer getLoadContext() {
    return loadBuffer;
  }

  /**
   * Return the property that invoked the lazy loading.
   */
  public String getLazyLoadProperty() {
    return lazyLoadProperty;
  }

  public int getBatchSize() {
    return getLoadContext().getBatchSize();
  }

  /**
   * Return the list of Id values for the beans in the lazy load buffer.
   */
  public List<Object> getIdList(int batchSize) {

    ArrayList<Object> idList = new ArrayList<Object>(batchSize);

    BeanDescriptor<?> desc = loadBuffer.getBeanDescriptor();
    for (int i = 0; i < batch.size(); i++) {
      EntityBeanIntercept ebi = batch.get(i);
      EntityBean bean = ebi.getOwner();
      idList.add(desc.getId(bean));
    }

    if (!idList.isEmpty()) {
      int extraIds = batchSize - batch.size();
      if (extraIds > 0) {
        // for performance make up the Id's to the batch size
        // so we get the same query (for Ebean and the db)
        Object firstId = idList.get(0);
        for (int i = 0; i < extraIds; i++) {
          // just add the first Id again
          idList.add(firstId);
        }
      }
    }

    return idList;
  }

  /**
   * Configure the query for lazy loading execution.
   */
  public void configureQuery(SpiQuery<?> query) {

    query.setMode(SpiQuery.Mode.LAZYLOAD_BEAN);
    query.setPersistenceContext(loadBuffer.getPersistenceContext());

    String mode = isLazy() ? "+lazy" : "+query";
    query.setLoadDescription(mode, getDescription());

    if (isLazy()) {
      // cascade the batch size (if set) for further lazy loading
      query.setLazyLoadBatchSize(getBatchSize());
    }

    loadBuffer.configureQuery(query, getLazyLoadProperty());
  }

  /**
   * Load the beans into the L2 cache if that is requested and check for load failures due to deletes.
   */
  public void postLoad(List<?> list) {

    if (isLoadCache()) {
      BeanDescriptor<?> desc = loadBuffer.getBeanDescriptor();
      for (int i = 0; i < list.size(); i++) {
        desc.cacheBeanPutData((EntityBean) list.get(i));
      }
    }

    if (lazyLoadPropertyIndex > -1) {
      // this is a lazy loading query so check for lazy loading failure (due to deleted rows)
      for (int i = 0; i < batch.size(); i++) {
        // check if the underlying row in DB was deleted. Mark the bean as 'failed' if
        // necessary but allow processing to continue until it is accessed by client code
        EntityBeanIntercept ebi = batch.get(i);
        // all beans in the batch should have this property loaded now
        if (ebi.isLazyLoadFailure(lazyLoadPropertyIndex)) {
          BeanDescriptor<?> desc = loadBuffer.getBeanDescriptor();
          Object beanId = desc.getId(ebi.getOwner());
          ebi.setOwnerId(beanId);
          logger.info("Lazy loading unsuccessful for type:" + desc.getName() + " id:" + beanId + " - expecting when bean has been deleted");
        }
      }
    }
  }
}
