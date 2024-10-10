package io.ebeaninternal.server.core;

import io.ebean.CacheMode;
import io.ebean.ExpressionList;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.*;
import io.ebeaninternal.api.SpiQuery.Mode;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Helper to handle lazy loading and refreshing of beans.
 */
final class DefaultBeanLoader {

  private static final System.Logger log = CoreLog.internal;

  private final DefaultServer server;
  private final boolean onIterateUseExtraTxn;

  DefaultBeanLoader(DefaultServer server) {
    this.server = server;
    this.onIterateUseExtraTxn = server.databasePlatform().useExtraTransactionOnIterateSecondaryQueries();
  }

  void loadMany(LoadManyRequest loadRequest) {
    executeQuery(loadRequest, loadRequest.createQuery(server));
    loadRequest.postLoad();
  }

  void loadMany(BeanCollection<?> bc, boolean onlyIds) {
    loadManyInternal(bc.owner(), bc.propertyName(), false, onlyIds);
  }

  void refreshMany(EntityBean parentBean, String propertyName) {
    loadManyInternal(parentBean, propertyName, true, false);
  }

  private void loadManyInternal(EntityBean parentBean, String propertyName, boolean refresh, boolean onlyIds) {
    EntityBeanIntercept ebi = parentBean._ebean_getIntercept();
    PersistenceContext pc = ebi.persistenceContext();
    BeanDescriptor<?> parentDesc = server.descriptor(parentBean.getClass());
    BeanPropertyAssocMany<?> many = (BeanPropertyAssocMany<?>) parentDesc.beanProperty(propertyName);
    BeanCollection<?> beanCollection = null;
    ExpressionList<?> filterMany = null;

    Object currentValue = many.getValue(parentBean);
    if (currentValue instanceof BeanCollection<?>) {
      beanCollection = (BeanCollection<?>) currentValue;
      filterMany = beanCollection.filterMany();
    }

    Object parentId = parentDesc.getId(parentBean);
    if (pc == null) {
      pc = new DefaultPersistenceContext();
    }
    parentDesc.contextPutIfAbsent(pc, parentId, parentBean);
    boolean useManyIdCache = beanCollection != null && parentDesc.isManyPropCaching() && many.isUseCache();
    if (useManyIdCache) {
      Boolean readOnly = null;
      if (ebi.isReadOnly()) {
        readOnly = Boolean.TRUE;
      }
      final String parentKey = parentDesc.cacheKey(parentId);
      if (parentDesc.cacheManyPropLoad(many, beanCollection, parentKey, readOnly)) {
        return;
      }
    }

    SpiQuery<?> query = server.createQuery(parentDesc.type());
    if (refresh) {
      // populate a new collection
      BeanCollection<?> emptyCollection = many.createEmpty(parentBean);
      many.setValue(parentBean, emptyCollection);
      query.setLoadDescription("refresh", null);
    } else {
      query.setLoadDescription("lazy", null);
    }

    query.select(parentDesc.idBinder().idSelect());
    if (onlyIds) {
      query.fetch(many.name(), many.targetIdProperty());
    } else {
      query.fetch(many.name());
    }
    if (filterMany != null) {
      query.setFilterMany(many.name(), filterMany);
    }

    query.where().idEq(parentId);
    query.setBeanCacheMode(CacheMode.OFF);
    query.setMode(Mode.LAZYLOAD_MANY);
    query.setLazyLoadManyPath(many.name());
    query.setPersistenceContext(pc);
    if (ebi.isReadOnly()) {
      query.setReadOnly(true);
    }
    if (many.hasOrderColumn()) {
      query.orderBy(many.path() + "." +  many.fetchOrderBy());
    }

    server.findOne(query);
    if (beanCollection != null) {
      if (beanCollection.checkEmptyLazyLoad()) {
        if (log.isLoggable(DEBUG)) {
          log.log(DEBUG, "BeanCollection after load was empty. Owner:{0}", beanCollection.owner());
        }
      } else if (useManyIdCache) {
        final String parentKey = parentDesc.cacheKey(parentId);
        parentDesc.cacheManyPropPut(many, beanCollection, parentKey);
      }
    }
  }

  /**
   * Load a batch of beans for +query or +lazy loading.
   */
  void loadBean(LoadBeanRequest loadRequest) {
    if (loadRequest.checkEmpty()) {
      throw new RuntimeException("Nothing in batch?");
    }
    final SpiQuery<?> query = loadRequest.createQuery(server);
    loadRequest.postLoad(executeQuery(loadRequest, query));
  }

  /**
   * Execute the lazy load query taking into account MySql transaction oddness.
   */
  private List<?> executeQuery(LoadRequest loadRequest, SpiQuery<?> query) {
    if (onIterateUseExtraTxn && loadRequest.isParentFindIterate()) {
      // MySql - we need a different transaction to execute the secondary query
      SpiTransaction extraTxn = server.createReadOnlyTransaction(query.tenantId(), query.isUseMaster());
      try {
        query.usingTransaction(extraTxn);
        return server.findList(query);
      } finally {
        extraTxn.end();
      }
    } else {
      return server.findList(query);
    }
  }

  public void refresh(EntityBean bean) {
    refreshBeanInternal(bean, SpiQuery.Mode.REFRESH_BEAN, -1);
  }

  void loadBean(EntityBeanIntercept ebi) {
    refreshBeanInternal(ebi.owner(), SpiQuery.Mode.LAZYLOAD_BEAN, -1);
  }

  private void refreshBeanInternal(EntityBean bean, SpiQuery.Mode mode, int embeddedOwnerIndex) {
    EntityBeanIntercept ebi = bean._ebean_getIntercept();
    PersistenceContext pc = ebi.persistenceContext();
    if (Mode.REFRESH_BEAN == mode) {
      // need a new PersistenceContext for REFRESH
      pc = null;
    }
    BeanDescriptor<?> desc = server.descriptor(bean.getClass());
    if (EntityType.EMBEDDED == desc.entityType()) {
      // lazy loading on an embedded bean property
      EntityBean embeddedOwner = (EntityBean) ebi.embeddedOwner();
      refreshBeanInternal(embeddedOwner, mode, ebi.embeddedOwnerIndex());
    }
    Object id = desc.getId(bean);
    if (pc == null) {
      // a reference with no existing persistenceContext
      pc = new DefaultPersistenceContext();
      desc.contextPut(pc, id, bean);
      ebi.setPersistenceContext(pc);
    }
    boolean draft = desc.isDraftInstance(bean);
    if (embeddedOwnerIndex == -1) {
      if (desc.lazyLoadMany(ebi)) {
        return;
      }
      if (!draft && Mode.LAZYLOAD_BEAN == mode && desc.isBeanCaching()) {
        // lazy loading and the bean cache is active
        if (desc.cacheBeanLoad(bean, ebi, id, pc)) {
          return;
        }
      }
    }
    SpiQuery<?> query = server.createQuery(desc.type());
    query.setLazyLoadProperty(ebi.lazyLoadProperty());
    if (draft) {
      query.asDraft();
    } else if (mode == SpiQuery.Mode.LAZYLOAD_BEAN && desc.isSoftDelete()) {
      query.setIncludeSoftDeletes();
    }
    if (embeddedOwnerIndex > -1) {
      query.select(ebi.property(embeddedOwnerIndex));
    }
    // don't collect AutoTune usage profiling information
    // as we just copy the data out of these fetched beans
    // and put the data into the original bean
    query.setUsageProfiling(false);
    query.setPersistenceContext(pc);
    query.setMode(mode);
    query.setId(id);
    if (embeddedOwnerIndex > -1 || mode == Mode.REFRESH_BEAN) {
      // make sure the query doesn't use the cache
      query.setBeanCacheMode(CacheMode.OFF);
    }
    if (ebi.isReadOnly()) {
      query.setReadOnly(true);
    }
    if (Mode.REFRESH_BEAN == mode) {
      // explicitly state to load all properties on REFRESH.
      // Lobs default to fetch lazy so this forces lobs to be
      // included in a 'refresh' query
      query.select("*");
    }

    Object dbBean = query.findOne();
    if (dbBean == null) {
      throw new EntityNotFoundException("Bean not found during lazy load or refresh. Id:" + id + " type:" + desc.type());
    }
    desc.resetManyProperties(dbBean);
  }
}
