package io.ebeaninternal.server.core;

import io.ebean.CacheMode;
import io.ebean.ExpressionList;
import io.ebean.Transaction;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.LoadBeanRequest;
import io.ebeaninternal.api.LoadManyRequest;
import io.ebeaninternal.api.LoadRequest;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiQuery.Mode;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * Helper to handle lazy loading and refreshing of beans.
 */
final class DefaultBeanLoader {

  private static final Logger logger = LoggerFactory.getLogger(DefaultBeanLoader.class);

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
    loadManyInternal(bc.getOwnerBean(), bc.getPropertyName(), null, false, onlyIds);
  }

  void refreshMany(EntityBean parentBean, String propertyName) {
    loadManyInternal(parentBean, propertyName, null, true, false);
  }

  private void loadManyInternal(EntityBean parentBean, String propertyName, Transaction t, boolean refresh, boolean onlyIds) {
    EntityBeanIntercept ebi = parentBean._ebean_getIntercept();
    PersistenceContext pc = ebi.getPersistenceContext();
    BeanDescriptor<?> parentDesc = server.getBeanDescriptor(parentBean.getClass());
    BeanPropertyAssocMany<?> many = (BeanPropertyAssocMany<?>) parentDesc.getBeanProperty(propertyName);
    BeanCollection<?> beanCollection = null;
    ExpressionList<?> filterMany = null;

    Object currentValue = many.getValue(parentBean);
    if (currentValue instanceof BeanCollection<?>) {
      beanCollection = (BeanCollection<?>) currentValue;
      filterMany = beanCollection.getFilterMany();
    }

    Object parentId = parentDesc.getId(parentBean);
    if (pc == null) {
      pc = new DefaultPersistenceContext();
      parentDesc.contextPut(pc, parentId, parentBean);
    }
    boolean useManyIdCache = beanCollection != null && parentDesc.isManyPropCaching() && many.isUseCache();
    if (useManyIdCache) {
      Boolean readOnly = null;
      if (ebi.isReadOnly()) {
        readOnly = Boolean.TRUE;
      }
      if (parentDesc.cacheManyPropLoad(many, beanCollection, parentId, readOnly)) {
        return;
      }
    }

    SpiQuery<?> query = server.createQuery(parentDesc.type());
    if (refresh) {
      // populate a new collection
      BeanCollection<?> emptyCollection = many.createEmpty(parentBean);
      many.setValue(parentBean, emptyCollection);
      query.setLoadDescription("+refresh", null);
    } else {
      query.setLoadDescription("+lazy", null);
    }

    query.select(parentDesc.getIdBinder().getIdProperty());
    if (onlyIds) {
      query.fetch(many.name(), many.getTargetIdProperty());
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

    server.findOne(query, t);
    if (beanCollection != null) {
      if (beanCollection.checkEmptyLazyLoad()) {
        if (logger.isDebugEnabled()) {
          logger.debug("BeanCollection after load was empty. Owner:" + beanCollection.getOwnerBean());
        }
      } else if (useManyIdCache) {
        parentDesc.cacheManyPropPut(many, beanCollection, parentId);
      }
    }
  }

  /**
   * Load a batch of beans for +query or +lazy loading.
   */
  void loadBean(LoadBeanRequest loadRequest) {
    List<EntityBeanIntercept> batch = loadRequest.batch();
    if (batch.isEmpty()) {
      throw new RuntimeException("Nothing in batch?");
    }

    List<Object> idList = loadRequest.getIdList();
    if (idList.isEmpty()) {
      // everything was loaded from cache
      return;
    }

    SpiQuery<?> query = server.createQuery(loadRequest.beanType());
    loadRequest.configureQuery(query, idList);
    List<?> list = executeQuery(loadRequest, query);
    loadRequest.postLoad(list);
  }

  /**
   * Execute the lazy load query taking into account MySql transaction oddness.
   */
  private List<?> executeQuery(LoadRequest loadRequest, SpiQuery<?> query) {
    if (onIterateUseExtraTxn && loadRequest.isParentFindIterate()) {
      // MySql - we need a different transaction to execute the secondary query
      SpiTransaction extraTxn = server.createReadOnlyTransaction(query.getTenantId());
      try {
        return server.findList(query, extraTxn);
      } finally {
        extraTxn.end();
      }
    } else {
      return server.findList(query, loadRequest.transaction());
    }
  }

  public void refresh(EntityBean bean) {
    refreshBeanInternal(bean, SpiQuery.Mode.REFRESH_BEAN, -1);
  }

  void loadBean(EntityBeanIntercept ebi) {
    refreshBeanInternal(ebi.getOwner(), SpiQuery.Mode.LAZYLOAD_BEAN, -1);
  }

  private void refreshBeanInternal(EntityBean bean, SpiQuery.Mode mode, int embeddedOwnerIndex) {
    EntityBeanIntercept ebi = bean._ebean_getIntercept();
    PersistenceContext pc = ebi.getPersistenceContext();
    if (Mode.REFRESH_BEAN == mode) {
      // need a new PersistenceContext for REFRESH
      pc = null;
    }

    BeanDescriptor<?> desc = server.getBeanDescriptor(bean.getClass());
    if (EntityType.EMBEDDED == desc.getEntityType()) {
      // lazy loading on an embedded bean property
      EntityBean embeddedOwner = (EntityBean) ebi.getEmbeddedOwner();
      refreshBeanInternal(embeddedOwner, mode, ebi.getEmbeddedOwnerIndex());
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
    query.setLazyLoadProperty(ebi.getLazyLoadProperty());
    if (draft) {
      query.asDraft();
    }

    if (embeddedOwnerIndex > -1) {
      query.select(ebi.getProperty(embeddedOwnerIndex));
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
      throw new EntityNotFoundException("Bean not found during lazy load or refresh." + " id[" + id + "] type[" + desc.type() + "]");
    }
    desc.resetManyProperties(dbBean);
  }
}
