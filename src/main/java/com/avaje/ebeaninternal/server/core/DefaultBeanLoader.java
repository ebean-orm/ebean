package com.avaje.ebeaninternal.server.core;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.LoadBeanContext;
import com.avaje.ebeaninternal.api.LoadBeanRequest;
import com.avaje.ebeaninternal.api.LoadManyContext;
import com.avaje.ebeaninternal.api.LoadManyRequest;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiQuery.Mode;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.transaction.DefaultPersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to handle lazy loading and refreshing of beans.
 * 
 * @author rbygrave
 */
public class DefaultBeanLoader {

  private static final Logger logger = LoggerFactory.getLogger(DefaultBeanLoader.class);

  private final DebugLazyLoad debugLazyLoad;

  private final DefaultServer server;

  protected DefaultBeanLoader(DefaultServer server, DebugLazyLoad debugLazyLoad) {
    this.server = server;
    this.debugLazyLoad = debugLazyLoad;
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
  private int getBatchSize(int batchListSize, int requestedBatchSize) {
    if (batchListSize == requestedBatchSize) {
      return batchListSize;
    }
    if (batchListSize == 1) {
      // there is only one bean/collection to load
      return 1;
    }
    if (requestedBatchSize <= 5) {
      // anything less than 5 becomes 5
      return 5;
    }
    if (batchListSize <= 10 || requestedBatchSize <= 10) {
      // 10 or less to load
      // ... or we wanted a batch size between 6 and 10
      return 10;
    }
    if (batchListSize <= 20 || requestedBatchSize <= 20) {
      // 20 or less to load
      // ... or we wanted a batch size between 11 and 20
      return 20;
    }
    if (batchListSize <= 50) {
      return 50;
    }
    return requestedBatchSize;
  }
	
	public void refreshMany(Object parentBean, String propertyName) {
		refreshMany(parentBean, propertyName, null);
	}

  public void loadMany(LoadManyRequest loadRequest) {

    List<BeanCollection<?>> batch = loadRequest.getBatch();

    int batchSize = getBatchSize(batch.size(), loadRequest.getBatchSize());

    LoadManyContext ctx = loadRequest.getLoadContext();
    BeanPropertyAssocMany<?> many = ctx.getBeanProperty();

    PersistenceContext pc = ctx.getPersistenceContext();

    ArrayList<Object> idList = new ArrayList<Object>(batchSize);

    for (int i = 0; i < batch.size(); i++) {
      BeanCollection<?> bc = batch.get(i);
      Object ownerBean = bc.getOwnerBean();
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

    String idProperty = desc.getIdBinder().getIdProperty();

    SpiQuery<?> query = (SpiQuery<?>) server.createQuery(desc.getBeanType());
    query.setMode(Mode.LAZYLOAD_MANY);
    query.setLazyLoadManyPath(many.getName());
    query.setPersistenceContext(pc);
    query.select(idProperty);
    query.fetch(many.getName());

    if (idList.size() == 1) {
      query.where().idEq(idList.get(0));
    } else {
      query.where().idIn(idList);
    }

    String mode = loadRequest.isLazy() ? "+lazy" : "+query";
    query.setLoadDescription(mode, loadRequest.getDescription());

    // potentially changes the joins and selected properties
    ctx.configureQuery(query);

    if (loadRequest.isOnlyIds()) {
      // override to just select the Id values
      query.fetch(many.getName(), many.getTargetIdProperty());
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
        desc.cachePutMany(many, bc, parentId);
      }
    }
  }

  public void loadMany(BeanCollection<?> bc, LoadManyContext ctx, boolean onlyIds) {

    Object parentBean = bc.getOwnerBean();
    String propertyName = bc.getPropertyName();

    ObjectGraphNode node = ctx == null ? null : ctx.getObjectGraphNode();

    loadManyInternal(parentBean, propertyName, null, false, node, onlyIds);

    if (server.getAdminLogging().isDebugLazyLoad()) {

      Class<?> cls = parentBean.getClass();
      BeanDescriptor<?> desc = server.getBeanDescriptor(cls);
      BeanPropertyAssocMany<?> many = (BeanPropertyAssocMany<?>) desc.getBeanProperty(propertyName);

      StackTraceElement cause = debugLazyLoad.getStackTraceElement(cls);

      String msg = "debug.lazyLoad " + many.getManyType() + " [" + desc + "][" + propertyName + "]";
      if (cause != null) {
        msg += " at: " + cause;
      }
      System.err.println(msg);
    }
  }

	public void refreshMany(Object parentBean, String propertyName, Transaction t) {
		loadManyInternal(parentBean, propertyName, t, true, null, false);
	}

	private void loadManyInternal(Object parentBean, String propertyName, Transaction t, boolean refresh, ObjectGraphNode node, boolean onlyIds) {

    boolean vanilla = (parentBean instanceof EntityBean == false);

    EntityBeanIntercept ebi = null;
    PersistenceContext pc = null;
    BeanCollection<?> beanCollection = null;
    ExpressionList<?> filterMany = null;

    if (!vanilla) {
      ebi = ((EntityBean) parentBean)._ebean_getIntercept();
      pc = ebi.getPersistenceContext();
    }

    BeanDescriptor<?> parentDesc = server.getBeanDescriptor(parentBean.getClass());
    BeanPropertyAssocMany<?> many = (BeanPropertyAssocMany<?>) parentDesc.getBeanProperty(propertyName);

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

    boolean useManyIdCache = !vanilla && beanCollection != null && parentDesc.cacheIsUseManyId();
    if (useManyIdCache) {
      Boolean readOnly = null;
      if (ebi != null && ebi.isReadOnly()) {
        readOnly = Boolean.TRUE;
      }
      if (parentDesc.cacheLoadMany(many, beanCollection, parentId, readOnly, false)) {
        return;
      }
    }

    SpiQuery<?> query = (SpiQuery<?>) server.createQuery(parentDesc.getBeanType());

    if (refresh) {
      // populate a new collection
      Object emptyCollection = many.createEmpty(vanilla);
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
    query.setVanillaMode(vanilla);

    if (ebi != null) {
      if (ebi.isReadOnly()) {
        query.setReadOnly(true);
      }
    }

    server.findUnique(query, t);

    if (beanCollection != null) {
      if (beanCollection.checkEmptyLazyLoad()) {
        if (logger.isDebugEnabled()) {
          logger.debug("BeanCollection after load was empty. Owner:" + beanCollection.getOwnerBean());
        }
      } else if (useManyIdCache) {
        parentDesc.cachePutMany(many, beanCollection, parentId);
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

    int batchSize = getBatchSize(batch.size(), loadRequest.getBatchSize());

    LoadBeanContext ctx = loadRequest.getLoadContext();
    BeanDescriptor<?> desc = ctx.getBeanDescriptor();

    Class<?> beanType = desc.getBeanType();

    EntityBeanIntercept[] ebis = batch.toArray(new EntityBeanIntercept[batch.size()]);
    ArrayList<Object> idList = new ArrayList<Object>(batchSize);

    for (int i = 0; i < batch.size(); i++) {
      EntityBeanIntercept ebi = batch.get(i);
      Object bean = ebi.getOwner();
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

    // query the database
    for (int i = 0; i < ebis.length; i++) {
      Object parentBean = ebis[i].getParentBean();
      if (parentBean != null) {
        // Special case for OneToOne
        BeanDescriptor<?> parentDesc = server.getBeanDescriptor(parentBean.getClass());
        Object parentId = parentDesc.getId(parentBean);
        persistenceContext.put(parentId, parentBean);
      }
    }

    SpiQuery<?> query = (SpiQuery<?>) server.createQuery(beanType);

    query.setMode(Mode.LAZYLOAD_BEAN);
    query.setPersistenceContext(persistenceContext);

    String mode = loadRequest.isLazy() ? "+lazy" : "+query";
    query.setLoadDescription(mode, loadRequest.getDescription());

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
        desc.cachePutBeanData(list.get(i));
      }
    }

    for (int i = 0; i < ebis.length; i++) {
      if (ebis[i].isReference()) {
        // The underlying row in DB was deleted. Mark this bean as 'failed'
        // but allow processing to continue until it is accessed by client code
        ebis[i].setLazyLoadFailure();
      }
    }

  }

	public void refresh(Object bean) {
		refreshBeanInternal(bean, SpiQuery.Mode.REFRESH_BEAN);
	}
	
	public void loadBean(EntityBeanIntercept ebi) {
		refreshBeanInternal(ebi.getOwner(), SpiQuery.Mode.LAZYLOAD_BEAN);
	}

  private void refreshBeanInternal(Object bean, SpiQuery.Mode mode) {

    boolean vanilla = (bean instanceof EntityBean == false);

    EntityBeanIntercept ebi = null;
    PersistenceContext pc = null;

    if (!vanilla) {
      ebi = ((EntityBean) bean)._ebean_getIntercept();
      pc = ebi.getPersistenceContext();
    }

    BeanDescriptor<?> desc = server.getBeanDescriptor(bean.getClass());
    Object id = desc.getId(bean);

    if (pc == null) {
      // a reference with no existing persistenceContext
      pc = new DefaultPersistenceContext();
      pc.put(id, bean);
      if (ebi != null) {
        ebi.setPersistenceContext(pc);
      }
    }

    if (ebi != null) {
      if (SpiQuery.Mode.LAZYLOAD_BEAN.equals(mode) && desc.isBeanCaching()) {
        // lazy loading and the bean cache is active 
        if (desc.loadFromCache(bean, ebi, id)) {
          return;
        }
      }
      if (desc.lazyLoadMany(ebi)) {
        return;
      }
    }

    SpiQuery<?> query = (SpiQuery<?>) server.createQuery(desc.getBeanType());
    if (ebi != null) {
      Object parentBean = ebi.getParentBean();
      if (parentBean != null) {
        // Special case for OneToOne
        BeanDescriptor<?> parentDesc = server.getBeanDescriptor(parentBean.getClass());
        Object parentId = parentDesc.getId(parentBean);
        pc.putIfAbsent(parentId, parentBean);
      }

      query.setLazyLoadProperty(ebi.getLazyLoadProperty());
    }

    // don't collect autoFetch usage profiling information
    // as we just copy the data out of these fetched beans
    // and put the data into the original bean
    query.setUsageProfiling(false);
    query.setPersistenceContext(pc);

    query.setMode(mode);
    query.setId(id);
    // make sure the query doesn't use the cache
    if (mode.equals(SpiQuery.Mode.REFRESH_BEAN)) {
      query.setUseCache(false);
    }
    query.setVanillaMode(vanilla);

    if (ebi != null && ebi.isReadOnly()) {
      query.setReadOnly(true);
    }

    Object dbBean = query.findUnique();
    if (dbBean == null) {
      String msg = "Bean not found during lazy load or refresh." + " id[" + id + "] type[" + desc.getBeanType() + "]";
      throw new EntityNotFoundException(msg);
    }

  }
}
