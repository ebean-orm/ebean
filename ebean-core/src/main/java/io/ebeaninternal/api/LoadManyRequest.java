package io.ebeaninternal.api;

import io.ebean.CacheMode;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.server.core.BindPadding;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

/**
 * Request for loading Associated Many Beans.
 */
public final class LoadManyRequest extends LoadRequest {

  private static final System.Logger log = CoreLog.log;
  private final LoadManyBuffer loadContext;
  private final boolean onlyIds;
  private final boolean loadCache;
  private final BeanCollection<?> originCollection;
  private boolean originIncluded;

  /**
   * Construct for lazy loading.
   */
  public LoadManyRequest(LoadManyBuffer loadContext, boolean onlyIds, boolean loadCache, BeanCollection<?> originCollection) {
    super(null, true);
    this.loadContext = loadContext;
    this.onlyIds = onlyIds;
    this.loadCache = loadCache;
    this.originCollection = originCollection;
  }

  /**
   * Construct for secondary query.
   */
  public LoadManyRequest(LoadManyBuffer loadContext, OrmQueryRequest<?> parentRequest) {
    super(parentRequest, false);
    this.loadContext = loadContext;
    this.onlyIds = false;
    this.loadCache = false;
    this.originCollection = null;
  }

  @Override
  public Class<?> beanType() {
    return loadContext.descriptor().type();
  }

  public String description() {
    return loadContext.fullPath();
  }

  private List<Object> parentIdList(SpiEbeanServer server, BeanPropertyAssocMany<?> many, PersistenceContext pc) {
    final var idList = new ArrayList<>(loadContext.size());
    final var descriptor = many.descriptor();
    for (int i = 0; i < loadContext.size(); i++) {
      final BeanCollection<?> bc = loadContext.get(i);
      if (bc != null) {
        final var parent = bc.owner();
        final var parentId = descriptor.getId(parent);
        idList.add(parentId);
        bc.setLoader(server); // don't use the load buffer again
        if (lazy) {
          descriptor.contextPutIfAbsent(pc, parentId, parent);
          if (!originIncluded && bc == originCollection) {
            originIncluded = true;
          }
        }
      }
    }
    if (originCollection != null && !originIncluded) {
      CoreLog.log.log(INFO, "Batch lazy loading including origin collection - size:{0}", idList.size());
      idList.add(many.parentId(originCollection.owner()));
      originCollection.setLoader(server); // don't use the load buffer again
    }
    if (descriptor.isPadInExpression()) {
      BindPadding.padIds(idList);
    }
    return idList;
  }

  private BeanPropertyAssocMany<?> many() {
    return loadContext.beanProperty();
  }

  public SpiQuery<?> createQuery(SpiEbeanServer server) {
    BeanPropertyAssocMany<?> many = many();
    SpiQuery<?> query = many.newLoadManyQuery(server, onlyIds);
    query.usingTransaction(transaction);
    final var pc = loadContext.persistenceContext();
    many.addWhereParentIdIn(query, parentIdList(server, many, pc), loadContext.isUseDocStore());
    query.setPersistenceContext(pc);
    query.setLoadDescription(lazy ? "lazy" : "query", description());
    if (lazy) {
      query.setLazyLoadBatchSize(loadContext.batchSize());
    } else {
      query.setBeanCacheMode(CacheMode.OFF);
    }
    // potentially changes the joins, selected properties, cache mode
    loadContext.configureQuery(query);

    return query;
  }

  /**
   * After the query execution check for empty collections and load L2 cache if desired.
   */
  public void postLoad() {
    BeanDescriptor<?> desc = loadContext.descriptor();
    BeanPropertyAssocMany<?> many = many();
    // check for BeanCollection's that where never processed
    // in the +query or +lazy load due to no rows (predicates)
    for (int i = 0; i < loadContext.size(); i++) {
      BeanCollection<?> bc = loadContext.get(i);
      if (bc != null) {
        if (bc.checkEmptyLazyLoad()) {
          if (log.isLoggable(DEBUG)) {
            EntityBean ownerBean = bc.owner();
            Object parentId = desc.getId(ownerBean);
            log.log(DEBUG, "BeanCollection after lazy load was empty. type:{0} id:{1} owner:{2}", ownerBean.getClass().getName(), parentId, ownerBean);
          }
        } else if (loadCache && many.isUseCache()) {
          desc.cacheManyPropPut(many, bc, desc.cacheKeyForBean(bc.owner()));
        }
      }
    }
  }
}
