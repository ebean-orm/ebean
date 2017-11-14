package io.ebeaninternal.server.core;

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
public class DefaultBeanLoader {

  private static final Logger logger = LoggerFactory.getLogger(DefaultBeanLoader.class);

  private final DefaultServer server;

  private final boolean onIterateUseExtraTxn;

  protected DefaultBeanLoader(DefaultServer server) {
    this.server = server;
    this.onIterateUseExtraTxn = server.getDatabasePlatform().useExtraTransactionOnIterateSecondaryQueries();
  }

  /**
   * Return a batch size that might be less than the requestedBatchSize.
   * <p>
   * This means we can have large and variable requestedBatchSizes.
   * </p>
   * <p>
   * We want to restrict the number of different batch sizes as we want to
   * re-use the query plan cache and get DB statement re-use.
   * </p>
   */
  private int getBatchSize(int batchSize) {

    if (batchSize == 1) {
      // there is only one bean/collection to load
      return 1;
    }
    if (batchSize <= 5) {
      // anything less than 5 becomes 5
      return 5;
    }
    if (batchSize <= 10) {
      return 10;
    }
    if (batchSize <= 20) {
      return 20;
    }
    if (batchSize <= 50) {
      return 50;
    }
    if (batchSize <= 100) {
      return 100;
    }
    return batchSize;
  }

  public void refreshMany(EntityBean parentBean, String propertyName) {
    refreshMany(parentBean, propertyName, null);
  }

  public void loadMany(LoadManyRequest loadRequest) {

    List<BeanCollection<?>> batch = loadRequest.getBatch();

    int batchSize = getBatchSize(batch.size());

    SpiQuery<?> query = loadRequest.createQuery(server, batchSize);
    executeQuery(loadRequest, query);

    loadRequest.postLoad();
  }

  public void loadMany(BeanCollection<?> bc, boolean onlyIds) {

    EntityBean parentBean = bc.getOwnerBean();
    String propertyName = bc.getPropertyName();

    loadManyInternal(parentBean, propertyName, null, false, onlyIds);
  }

  public void refreshMany(EntityBean parentBean, String propertyName, Transaction t) {
    loadManyInternal(parentBean, propertyName, t, true, false);
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

    boolean useManyIdCache = beanCollection != null && parentDesc.isManyPropCaching();
    if (useManyIdCache) {
      Boolean readOnly = null;
      if (ebi.isReadOnly()) {
        readOnly = Boolean.TRUE;
      }
      if (parentDesc.cacheManyPropLoad(many, beanCollection, parentId, readOnly)) {
        return;
      }
    }

    SpiQuery<?> query = server.createQuery(parentDesc.getBeanType());

    if (refresh) {
      // populate a new collection
      BeanCollection<?> emptyCollection = many.createEmpty(parentBean);
      many.setValue(parentBean, emptyCollection);
      query.setLoadDescription("+refresh", null);
    } else {
      query.setLoadDescription("+lazy", null);
    }

    String idProperty = parentDesc.getIdBinder().getIdProperty();
    query.select(idProperty);

    if (onlyIds) {
      query.fetch(many.getName(), many.getTargetIdProperty());
    } else {
      query.fetch(many.getName());
    }
    if (filterMany != null) {
      query.setFilterMany(many.getName(), filterMany);
    }

    query.where().idEq(parentId);
    query.setUseCache(false);
    query.setMode(Mode.LAZYLOAD_MANY);
    query.setLazyLoadManyPath(many.getName());
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
  public void loadBean(LoadBeanRequest loadRequest) {

    List<EntityBeanIntercept> batch = loadRequest.getBatch();
    if (batch.isEmpty()) {
      throw new RuntimeException("Nothing in batch?");
    }

    int batchSize = getBatchSize(batch.size());

    List<Object> idList = loadRequest.getIdList(batchSize);
    if (idList.isEmpty()) {
      // everything was loaded from cache
      return;
    }

    SpiQuery<?> query = server.createQuery(loadRequest.getBeanType());
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
      SpiTransaction extraTxn = server.createQueryTransaction(query.getTenantId());
      try {
        return server.findList(query, extraTxn);
      } finally {
        extraTxn.end();
      }
    } else {
      return server.findList(query, loadRequest.getTransaction());
    }
  }

  public void refresh(EntityBean bean) {
    refreshBeanInternal(bean, SpiQuery.Mode.REFRESH_BEAN, -1);
  }

  public void loadBean(EntityBeanIntercept ebi) {
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
      int ownerIndex = ebi.getEmbeddedOwnerIndex();

      refreshBeanInternal(embeddedOwner, mode, ownerIndex);
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

    SpiQuery<?> query = server.createQuery(desc.getBeanType());
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
      query.setUseCache(false);
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
      String msg = "Bean not found during lazy load or refresh." + " id[" + id + "] type[" + desc.getBeanType() + "]";
      throw new EntityNotFoundException(msg);
    }

    desc.resetManyProperties(dbBean);

  }
}
