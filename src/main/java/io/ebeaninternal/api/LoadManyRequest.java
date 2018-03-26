package io.ebeaninternal.api;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.util.StringHelper;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for loading Associated Many Beans.
 */
public class LoadManyRequest extends LoadRequest {

  private static final Logger logger = LoggerFactory.getLogger(LoadManyRequest.class);

  private final List<BeanCollection<?>> batch;

  private final LoadManyBuffer loadContext;

  private final boolean onlyIds;

  private final boolean loadCache;

  /**
   * Construct for lazy loading.
   */
  public LoadManyRequest(LoadManyBuffer loadContext, boolean onlyIds, boolean loadCache) {
    this(loadContext, null, true, onlyIds, loadCache);
  }

  /**
   * Construct for secondary query.
   */
  public LoadManyRequest(LoadManyBuffer loadContext, OrmQueryRequest<?> parentRequest) {
    this(loadContext, parentRequest, false, false, false);
  }

  private LoadManyRequest(LoadManyBuffer loadContext, OrmQueryRequest<?> parentRequest, boolean lazy, boolean onlyIds, boolean loadCache) {
    super(parentRequest, lazy);
    this.loadContext = loadContext;
    this.batch = loadContext.getBatch();
    this.onlyIds = onlyIds;
    this.loadCache = loadCache;
  }

  @Override
  public Class<?> getBeanType() {
    return loadContext.getBeanDescriptor().getBeanType();
  }

  public String getDescription() {
    return "path:" + loadContext.getFullPath() + " size:" + batch.size();
  }

  /**
   * Return the batch of collections to actually load.
   */
  public List<BeanCollection<?>> getBatch() {
    return batch;
  }

  /**
   * Return the load context.
   */
  public LoadManyBuffer getLoadContext() {
    return loadContext;
  }

  /**
   * Return true if lazy loading should only load the id values.
   * <p>
   * This for use when lazy loading is invoked on methods such as clear() and removeAll() where it
   * generally makes sense to only fetch the Id values as the other property information is not
   * used.
   * </p>
   */
  public boolean isOnlyIds() {
    return onlyIds;
  }

  /**
   * Return true if we should load the Collection ids into the cache.
   */
  public boolean isLoadCache() {
    return loadCache;
  }

  /**
   * Return the batch size used for this load context.
   */
  public int getBatchSize() {
    return loadContext.getBatchSize();
  }

  private List<Object> getParentIdList(int batchSize) {

    ArrayList<Object> idList = new ArrayList<>(batchSize);

    BeanPropertyAssocMany<?> many = getMany();
    for (BeanCollection<?> bc : batch) {
      idList.add(many.getParentId(bc.getOwnerBean()));
    }
    if (!many.getTargetDescriptor().isMultiValueIdSupported()) {
      int extraIds = batchSize - batch.size();
      if (extraIds > 0) {
        Object firstId = idList.get(0);
        for (int i = 0; i < extraIds; i++) {
          idList.add(firstId);
        }
      }
    }

    return idList;
  }

  private BeanPropertyAssocMany<?> getMany() {
    return loadContext.getBeanProperty();
  }

  public SpiQuery<?> createQuery(SpiEbeanServer server, int batchSize) {

    BeanPropertyAssocMany<?> many = getMany();

    SpiQuery<?> query = many.newQuery(server);
    String orderBy = many.getLazyFetchOrderBy();
    if (orderBy != null) {
      query.orderBy(orderBy);
    }

    String extraWhere = many.getExtraWhere();
    if (extraWhere != null) {
      // replace special ${ta} placeholder with the base table alias
      // which is always t0 and add the extra where clause
      String ew = StringHelper.replaceString(extraWhere, "${ta}", "t0");
      query.where().raw(ew);
    }

    query.setLazyLoadForParents(many);

    List<Object> idList = getParentIdList(batchSize);
    many.addWhereParentIdIn(query, idList, loadContext.isUseDocStore());

    query.setPersistenceContext(loadContext.getPersistenceContext());

    String mode = isLazy() ? "+lazy" : "+query";
    query.setLoadDescription(mode, getDescription());

    if (isLazy()) {
      // cascade the batch size (if set) for further lazy loading
      query.setLazyLoadBatchSize(getBatchSize());
    }

    // potentially changes the joins and selected properties
    loadContext.configureQuery(query);

    if (isOnlyIds()) {
      // override to just select the Id values
      query.select(many.getTargetIdProperty());
    }

    return query;
  }

  /**
   * After the query execution check for empty collections and load L2 cache if desired.
   */
  public void postLoad() {

    BeanDescriptor<?> desc = loadContext.getBeanDescriptor();
    BeanPropertyAssocMany<?> many = getMany();

    // check for BeanCollection's that where never processed
    // in the +query or +lazy load due to no rows (predicates)
    for (BeanCollection<?> bc : batch) {
      if (bc.checkEmptyLazyLoad()) {
        if (logger.isDebugEnabled()) {
          EntityBean ownerBean = bc.getOwnerBean();
          Object parentId = desc.getId(ownerBean);
          logger.debug("BeanCollection after lazy load was empty. type:" + ownerBean.getClass().getName() + " id:" + parentId + " owner:" + ownerBean);
        }
      } else if (isLoadCache()) {
        Object parentId = desc.getId(bc.getOwnerBean());
        desc.cacheManyPropPut(many, bc, parentId);
      }
    }

  }
}
