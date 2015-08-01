package com.avaje.ebeaninternal.server.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.avaje.ebean.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.config.CompoundType;
import com.avaje.ebean.config.ScalarTypeConverter;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.util.ClassPathSearchMatcher;

/**
 * Interesting classes for a EbeanServer such as Embeddable, Entity,
 * ScalarTypes, Finders, Listeners and Controllers.
 */
public class BootupClasses implements ClassPathSearchMatcher {

  private static final Logger logger = LoggerFactory.getLogger(BootupClasses.class);

  private final List<Class<?>> embeddableList = new ArrayList<Class<?>>();

  private final List<Class<?>> entityList = new ArrayList<Class<?>>();

  private final List<Class<?>> scalarTypeList = new ArrayList<Class<?>>();

  private final List<Class<?>> scalarConverterList = new ArrayList<Class<?>>();

  private final List<Class<?>> compoundTypeList = new ArrayList<Class<?>>();

  private final List<Class<?>> beanControllerList = new ArrayList<Class<?>>();

  private final List<Class<?>> transactionEventListenerList = new ArrayList<Class<?>>();

  private final List<Class<?>> beanFindControllerList = new ArrayList<Class<?>>();
  private final List<Class<?>> beanQueryAdapterList = new ArrayList<Class<?>>();

  private final List<Class<?>> beanListenerList = new ArrayList<Class<?>>();

  private final List<Class<?>> serverConfigStartupList = new ArrayList<Class<?>>();
  private final List<ServerConfigStartup> serverConfigStartupInstances = new ArrayList<ServerConfigStartup>();

  private final List<BeanFindController> findControllerInstances = new ArrayList<BeanFindController>();
  private final List<BeanPersistController> persistControllerInstances = new ArrayList<BeanPersistController>();
  private final List<BeanPersistListener> persistListenerInstances = new ArrayList<BeanPersistListener>();
  private final List<BeanQueryAdapter> queryAdapterInstances = new ArrayList<BeanQueryAdapter>();
  private final List<TransactionEventListener> transactionEventListenerInstances = new ArrayList<TransactionEventListener>();

  public BootupClasses() {
  }

  public BootupClasses(List<Class<?>> list) {
    if (list != null) {
      for (Class<?> cls : list) {
        isMatch(cls);
      }
    }
  }

  /**
   * Run any ServerConfigStartup listeners.
   */
  public void runServerConfigStartup(ServerConfig serverConfig) {

    for (Class<?> cls : serverConfigStartupList) {
      try {
        ServerConfigStartup newInstance = (ServerConfigStartup) cls.newInstance();
        newInstance.onStart(serverConfig);
      } catch (Exception e) {
        // assume that the desired behavior is to fail - add your own try catch if needed
        throw new IllegalStateException("Error running ServerConfigStartup " + cls, e);
      }
    }
    for (ServerConfigStartup startup : serverConfigStartupInstances) {
      try {
        startup.onStart(serverConfig);
      } catch (Exception e) {
        // assume that the desired behavior is to fail - add your own try catch if needed
        throw new IllegalStateException("Error running ServerConfigStartup " + startup.getClass(), e);
      }
    }
  }

  public void addQueryAdapters(List<BeanQueryAdapter> queryAdapterInstances) {
    if (queryAdapterInstances != null) {
      for (BeanQueryAdapter a : queryAdapterInstances) {
        this.queryAdapterInstances.add(a);
        // don't automatically instantiate
        this.beanQueryAdapterList.remove(a.getClass());
      }
    }
  }

  /**
   * Add BeanPersistController instances.
   */
  public void addPersistControllers(List<BeanPersistController> beanControllerInstances) {
    if (beanControllerInstances != null) {
      for (BeanPersistController c : beanControllerInstances) {
        this.persistControllerInstances.add(c);
        // don't automatically instantiate
        this.beanControllerList.remove(c.getClass());
      }
    }
  }

  /**
   * Add BeanFindController instances.
   */
  public void addFindControllers(List<BeanFindController> findControllers) {
    if (findControllers != null) {
      for (BeanFindController c : findControllers) {
        this.findControllerInstances.add(c);
        // don't automatically instantiate
        this.beanFindControllerList.remove(c.getClass());
      }
    }
  }

  /**
   * Add TransactionEventListeners instances.
   */
  public void addTransactionEventListeners(List<TransactionEventListener> transactionEventListeners) {
    if (transactionEventListeners != null) {
      for (TransactionEventListener c : transactionEventListeners) {
        this.transactionEventListenerInstances.add(c);
        // don't automatically instantiate
        this.transactionEventListenerList.remove(c.getClass());
      }
    }
  }

  public void addPersistListeners(List<BeanPersistListener> listenerInstances) {
    if (listenerInstances != null) {
      for (BeanPersistListener l : listenerInstances) {
        this.persistListenerInstances.add(l);
        // don't automatically instantiate
        this.beanListenerList.remove(l.getClass());
      }
    }
  }

  public void addServerConfigStartup(List<ServerConfigStartup> startupInstances) {
    if (startupInstances != null) {
      for (ServerConfigStartup l : startupInstances) {
        this.serverConfigStartupInstances.add(l);
        // don't automatically instantiate
        this.serverConfigStartupList.remove(l.getClass());
      }
    }
  }

  public List<BeanQueryAdapter> getBeanQueryAdapters() {
    // add class registered BeanQueryAdapter to the
    // already created instances
    for (Class<?> cls : beanQueryAdapterList) {
      try {
        BeanQueryAdapter newInstance = (BeanQueryAdapter) cls.newInstance();
        queryAdapterInstances.add(newInstance);
      } catch (Exception e) {
        String msg = "Error creating BeanQueryAdapter " + cls;
        logger.error(msg, e);
      }
    }

    return queryAdapterInstances;
  }

  public List<BeanFindController> getBeanFindControllers() {
    // add class registered BeanFindController to the
    // list of created instances
    for (Class<?> cls : beanFindControllerList) {
      try {
        BeanFindController newInstance = (BeanFindController) cls.newInstance();
        findControllerInstances.add(newInstance);
      } catch (Exception e) {
        String msg = "Error creating BeanPersistController " + cls;
        logger.error(msg, e);
      }
    }

    return findControllerInstances;
  }

  public List<BeanPersistListener> getBeanPersistListeners() {
    // add class registered BeanPersistController to the
    // already created instances
    for (Class<?> cls : beanListenerList) {
      try {
        BeanPersistListener newInstance = (BeanPersistListener) cls.newInstance();
        persistListenerInstances.add(newInstance);
      } catch (Exception e) {
        String msg = "Error creating BeanPersistController " + cls;
        logger.error(msg, e);
      }
    }

    return persistListenerInstances;
  }

  public List<BeanPersistController> getBeanPersistControllers() {
    // add class registered BeanPersistController to the
    // already created instances
    for (Class<?> cls : beanControllerList) {
      try {
        BeanPersistController newInstance = (BeanPersistController) cls.newInstance();
        persistControllerInstances.add(newInstance);
      } catch (Exception e) {
        String msg = "Error creating BeanPersistController " + cls;
        logger.error(msg, e);
      }
    }

    return persistControllerInstances;
  }

  public List<TransactionEventListener> getTransactionEventListeners() {
    // add class registered TransactionEventListener to the
    // already created instances
    for (Class<?> cls : transactionEventListenerList) {
      try {
        TransactionEventListener newInstance = (TransactionEventListener) cls.newInstance();
        transactionEventListenerInstances.add(newInstance);
      } catch (Exception e) {
        String msg = "Error creating TransactionEventListener " + cls;
        logger.error(msg, e);
      }
    }

    return transactionEventListenerInstances;
  }

  /**
   * Return the list of Embeddable classes.
   */
  public List<Class<?>> getEmbeddables() {
    return embeddableList;
  }

  /**
   * Return the list of entity classes.
   */
  public List<Class<?>> getEntities() {
    return entityList;
  }

  /**
   * Return the list of ScalarTypes found.
   */
  public List<Class<?>> getScalarTypes() {
    return scalarTypeList;
  }

  /**
   * Return the list of ScalarConverters found.
   */
  public List<Class<?>> getScalarConverters() {
    return scalarConverterList;
  }

  /**
   * Return the list of ScalarConverters found.
   */
  public List<Class<?>> getCompoundTypes() {
    return compoundTypeList;
  }

  public boolean isMatch(Class<?> cls) {

    if (isEmbeddable(cls)) {
      embeddableList.add(cls);

    } else if (isEntity(cls)) {
      entityList.add(cls);

    } else {
      return isInterestingInterface(cls);
    }

    return true;
  }

  /**
   * Look for interesting interfaces.
   * <p>
   * This includes ScalarType, BeanController, BeanFinder and BeanListener.
   * </p>
   */
  private boolean isInterestingInterface(Class<?> cls) {

    if (Modifier.isAbstract(cls.getModifiers())) {
      // do not include abstract classes as we can
      // not instantiate them
      return false;
    }
    boolean interesting = false;

    if (BeanPersistController.class.isAssignableFrom(cls)) {
      beanControllerList.add(cls);
      interesting = true;
    }

    if (TransactionEventListener.class.isAssignableFrom(cls)) {
      transactionEventListenerList.add(cls);
      interesting = true;
    }

    if (ScalarType.class.isAssignableFrom(cls)) {
      scalarTypeList.add(cls);
      interesting = true;
    }

    if (ScalarTypeConverter.class.isAssignableFrom(cls)) {
      scalarConverterList.add(cls);
      interesting = true;
    }

    if (CompoundType.class.isAssignableFrom(cls)) {
      compoundTypeList.add(cls);
      interesting = true;
    }

    if (BeanFindController.class.isAssignableFrom(cls)) {
      beanFindControllerList.add(cls);
      interesting = true;
    }

    if (BeanPersistListener.class.isAssignableFrom(cls)) {
      beanListenerList.add(cls);
      interesting = true;
    }

    if (BeanQueryAdapter.class.isAssignableFrom(cls)) {
      beanQueryAdapterList.add(cls);
      interesting = true;
    }

    if (ServerConfigStartup.class.isAssignableFrom(cls)) {
      serverConfigStartupList.add(cls);
      interesting = true;
    }

    return interesting;
  }

  private boolean isEntity(Class<?> cls) {

    Annotation ann = cls.getAnnotation(Entity.class);
    if (ann != null) {
      return true;
    }
    ann = cls.getAnnotation(Table.class);
    return ann != null;
  }

  private boolean isEmbeddable(Class<?> cls) {

    Annotation ann = cls.getAnnotation(Embeddable.class);
    return ann != null;
  }
}
