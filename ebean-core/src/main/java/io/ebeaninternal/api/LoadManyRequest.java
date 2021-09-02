package io.ebeaninternal.api;

import io.ebean.CacheMode;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.BindPadding;
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
public final class LoadManyRequest extends LoadRequest {

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
  public Class<?> beanType() {
    return loadContext.getBeanDescriptor().getBeanType();
  }

  public String description() {
    return loadContext.getFullPath();
  }

  private List<Object> parentIdList(SpiEbeanServer server) {
    List<Object> idList = new ArrayList<>();
    BeanPropertyAssocMany<?> many = many();
    for (BeanCollection<?> bc : batch) {
      idList.add(many.getParentId(bc.getOwnerBean()));
      bc.setLoader(server); // don't use the load buffer again
    }
    if (many.getTargetDescriptor().isPadInExpression()) {
      BindPadding.padIds(idList);
    }
    return idList;
  }

  private BeanPropertyAssocMany<?> many() {
    return loadContext.getBeanProperty();
  }

  public SpiQuery<?> createQuery(SpiEbeanServer server) {
    BeanPropertyAssocMany<?> many = many();
    SpiQuery<?> query = many.newQuery(server);
    String orderBy = many.getLazyFetchOrderBy();
    if (orderBy != null) {
      query.order(orderBy);
    }
    String extraWhere = many.getExtraWhere();
    if (extraWhere != null) {
      // replace special ${ta} placeholder with the base table alias
      // which is always t0 and add the extra where clause
      query.where().raw(extraWhere.replace("${ta}", "t0").replace("${mta}", "int_"));
    }
    query.setLazyLoadForParents(many);
    many.addWhereParentIdIn(query, parentIdList(server), loadContext.isUseDocStore());
    query.setPersistenceContext(loadContext.getPersistenceContext());
    query.setLoadDescription(lazy ? "+lazy" : "+query", description());
    if (lazy) {
      query.setLazyLoadBatchSize(loadContext.getBatchSize());
    } else {
      query.setBeanCacheMode(CacheMode.OFF);
    }
    // potentially changes the joins, selected properties, cache mode
    loadContext.configureQuery(query);
    if (onlyIds) {
      // lazy loading invoked via clear() and removeAll()
      query.select(many.getTargetIdProperty());
    }
    return query;
  }

  /**
   * After the query execution check for empty collections and load L2 cache if desired.
   */
  public void postLoad() {
    BeanDescriptor<?> desc = loadContext.getBeanDescriptor();
    BeanPropertyAssocMany<?> many = many();
    // check for BeanCollection's that where never processed
    // in the +query or +lazy load due to no rows (predicates)
    for (BeanCollection<?> bc : batch) {
      if (bc.checkEmptyLazyLoad()) {
        if (logger.isDebugEnabled()) {
          EntityBean ownerBean = bc.getOwnerBean();
          Object parentId = desc.getId(ownerBean);
          logger.debug("BeanCollection after lazy load was empty. type:" + ownerBean.getClass().getName() + " id:" + parentId + " owner:" + ownerBean);
        }
      } else if (loadCache && many.isUseCache()) {
        desc.cacheManyPropPut(many, bc, desc.getId(bc.getOwnerBean()));
      }
    }
  }
}
