package com.avaje.ebeaninternal.server.core;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.LoadBeanBuffer;
import com.avaje.ebeaninternal.api.LoadBeanRequest;
import com.avaje.ebeaninternal.api.LoadManyRequest;
import com.avaje.ebeaninternal.api.LoadManyBuffer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiQuery.Mode;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.ebeaninternal.server.transaction.DefaultPersistenceContext;

/**
 * Helper to handle lazy loading and refreshing of beans.
 */
public class DefaultBeanLoader {

  private static final Logger logger = LoggerFactory.getLogger(DefaultBeanLoader.class);

  private final DefaultServer server;

  protected DefaultBeanLoader(DefaultServer server) {
    this.server = server;
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

    LoadManyBuffer ctx = loadRequest.getLoadContext();
    BeanPropertyAssocMany<?> many = ctx.getBeanProperty();

    PersistenceContext pc = ctx.getPersistenceContext();

    ArrayList<Object> idList = new ArrayList<Object>(batchSize);

    for (int i = 0; i < batch.size(); i++) {
      BeanCollection<?> bc = batch.get(i);
      EntityBean ownerBean = bc.getOwnerBean();
      Object id = many.getParentId(ownerBean);
      idList.add(id);
    }
    int extraIds = batchSize - batch.size();
    if (extraIds > 0) {
      Object firstId = idList.get(0);
      for (int i = 0; i < extraIds; i++) {
        idList.add(firstId);
      }
    }

    BeanDescriptor<?> desc = ctx.getBeanDescriptor();

    SpiQuery<?> query = (SpiQuery<?>) server.createQuery(many.getTargetType());
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

    query.setLazyLoadForParents(idList, many);
    many.addWhereParentIdIn(query, idList);

    query.setPersistenceContext(pc);

    String mode = loadRequest.isLazy() ? "+lazy" : "+query";
    query.setLoadDescription(mode, loadRequest.getDescription());

    if (loadRequest.isLazy()) {
      // cascade the batch size (if set) for further lazy loading
      query.setLazyLoadBatchSize(loadRequest.getBatchSize());
    }

    // potentially changes the joins and selected properties
    ctx.configureQuery(query);

    if (loadRequest.isOnlyIds()) {
      // override to just select the Id values
      query.select(many.getTargetIdProperty());
    }

    server.findList(query, loadRequest.getTransaction());

    // check for BeanCollection's that where never processed
    // in the +query or +lazy load due to no rows (predicates)
    for (int i = 0; i < batch.size(); i++) {
      BeanCollection<?> bc = batch.get(i);
      if (bc.checkEmptyLazyLoad()) {
        if (logger.isDebugEnabled()) {
          logger.debug("BeanCollection after load was empty. Owner:" + batch.get(i).getOwnerBean());
        }
      } else if (loadRequest.isLoadCache()) {
        Object parentId = desc.getId(bc.getOwnerBean());
        desc.cacheManyPropPut(many, bc, parentId);
      }
    }

    // log the query (for testing secondary queries)
    loadRequest.logSecondaryQuery(query);
  }

  public void loadMany(BeanCollection<?> bc, boolean onlyIds) {

    EntityBean parentBean = bc.getOwnerBean();
    String propertyName = bc.getPropertyName();

    loadManyInternal(parentBean, propertyName, null, false, null, onlyIds);
  }

  public void refreshMany(EntityBean parentBean, String propertyName, Transaction t) {
    loadManyInternal(parentBean, propertyName, t, true, null, false);
  }

  private void loadManyInternal(EntityBean parentBean, String propertyName, Transaction t, boolean refresh,
                                ObjectGraphNode node, boolean onlyIds) {

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
      pc.put(parentId, parentBean);
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

    SpiQuery<?> query = (SpiQuery<?>) server.createQuery(parentDesc.getBeanType());

    if (refresh) {
      // populate a new collection
      BeanCollection<?> emptyCollection = many.createEmpty(parentBean);
      many.setValue(parentBean, emptyCollection);
      query.setLoadDescription("+refresh", null);
    } else {
      query.setLoadDescription("+lazy", null);
    }

    if (node != null) {
      // so we can hook back to the root query
      query.setParentNode(node);
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

    server.findUnique(query, t);

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

    LoadBeanBuffer ctx = loadRequest.getLoadContext();
    BeanDescriptor<?> desc = ctx.getBeanDescriptor();

    Class<?> beanType = desc.getBeanType();

    EntityBeanIntercept[] ebis = batch.toArray(new EntityBeanIntercept[batch.size()]);
    ArrayList<Object> idList = new ArrayList<Object>(batchSize);

    for (int i = 0; i < batch.size(); i++) {
      EntityBeanIntercept ebi = batch.get(i);
      EntityBean bean = ebi.getOwner();
      Object id = desc.getId(bean);
      idList.add(id);
    }

    if (idList.isEmpty()) {
      // everything was loaded from cache
      return;
    }

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

    PersistenceContext persistenceContext = ctx.getPersistenceContext();

    SpiQuery<?> query = (SpiQuery<?>) server.createQuery(beanType);

    query.setMode(Mode.LAZYLOAD_BEAN);
    query.setPersistenceContext(persistenceContext);

    String mode = loadRequest.isLazy() ? "+lazy" : "+query";
    query.setLoadDescription(mode, loadRequest.getDescription());

    if (loadRequest.isLazy()) {
      // cascade the batch size (if set) for further lazy loading
      query.setLazyLoadBatchSize(loadRequest.getBatchSize());
    }

    ctx.configureQuery(query, loadRequest.getLazyLoadProperty());

    // make sure the query doesn't use the cache
    // query.setUseCache(false);
    if (idList.size() == 1) {
      query.where().idEq(idList.get(0));
    } else {
      query.where().idIn(idList);
    }

    List<?> list = server.findList(query, loadRequest.getTransaction());

    if (loadRequest.isLoadCache()) {
      for (int i = 0; i < list.size(); i++) {
        desc.cacheBeanPutData((EntityBean) list.get(i));
      }
    }

    for (int i = 0; i < ebis.length; i++) {
      // Check if the underlying row in DB was deleted. Mark this bean as 'failed' if
      // necessary but allow processing to continue until it is accessed by client code
      ebis[i].checkLazyLoadFailure();
    }

    // log the query (for testing secondary queries)
    loadRequest.logSecondaryQuery(query);
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
      pc.put(id, bean);
      ebi.setPersistenceContext(pc);
    }

    if (embeddedOwnerIndex == -1) {
      if (SpiQuery.Mode.LAZYLOAD_BEAN.equals(mode) && desc.isBeanCaching()) {
        // lazy loading and the bean cache is active 
        if (desc.cacheBeanLoad(bean, ebi, id)) {
          return;
        }
      }
      if (desc.lazyLoadMany(ebi)) {
        return;
      }
    }

    SpiQuery<?> query = (SpiQuery<?>) server.createQuery(desc.getBeanType());
    query.setLazyLoadProperty(ebi.getLazyLoadProperty());

    if (embeddedOwnerIndex > -1) {
      String embeddedBeanPropertyName = ebi.getProperty(embeddedOwnerIndex);
      query.select("id," + embeddedBeanPropertyName);
    }

    // don't collect autoFetch usage profiling information
    // as we just copy the data out of these fetched beans
    // and put the data into the original bean
    query.setUsageProfiling(false);
    query.setPersistenceContext(pc);

    query.setMode(mode);
    query.setId(id);

    if (embeddedOwnerIndex > -1 || mode.equals(SpiQuery.Mode.REFRESH_BEAN)) {
      // make sure the query doesn't use the cache
      query.setUseCache(false);
    }

    if (ebi.isReadOnly()) {
      query.setReadOnly(true);
    }

    if (SpiQuery.Mode.REFRESH_BEAN.equals(mode)) {
      // explicitly state to load all properties on REFRESH.
      // Lobs default to fetch lazy so this forces lobs to be 
      // included in a 'refresh' query
      query.select("*");
    }

    Object dbBean = query.findUnique();
    if (dbBean == null) {
      String msg = "Bean not found during lazy load or refresh." + " id[" + id + "] type[" + desc.getBeanType() + "]";
      throw new EntityNotFoundException(msg);
    }

    desc.resetManyProperties(dbBean);

  }
}
