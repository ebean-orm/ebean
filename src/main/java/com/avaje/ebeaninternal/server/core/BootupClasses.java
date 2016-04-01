package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.config.CompoundType;
import com.avaje.ebean.config.ScalarTypeConverter;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.event.BeanFindController;
import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanPersistListener;
import com.avaje.ebean.event.BeanPostLoad;
import com.avaje.ebean.event.BeanQueryAdapter;
import com.avaje.ebean.event.ServerConfigStartup;
import com.avaje.ebean.event.TransactionEventListener;
import com.avaje.ebean.event.changelog.ChangeLogListener;
import com.avaje.ebean.event.changelog.ChangeLogPrepare;
import com.avaje.ebean.event.changelog.ChangeLogRegister;
import com.avaje.ebean.event.readaudit.ReadAuditLogger;
import com.avaje.ebean.event.readaudit.ReadAuditPrepare;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.util.ClassPathSearchMatcher;
import org.avaje.classpath.scanner.ClassFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Interesting classes for a EbeanServer such as Embeddable, Entity,
 * ScalarTypes, Finders, Listeners and Controllers.
 */
public class BootupClasses implements ClassPathSearchMatcher, ClassFilter {

  private static final Logger logger = LoggerFactory.getLogger(BootupClasses.class);

  private final List<Class<?>> embeddableList = new ArrayList<Class<?>>();

  private final List<Class<?>> entityList = new ArrayList<Class<?>>();

  private final List<Class<?>> scalarTypeList = new ArrayList<Class<?>>();

  private final List<Class<?>> scalarConverterList = new ArrayList<Class<?>>();

  private final List<Class<?>> compoundTypeList = new ArrayList<Class<?>>();

  private final List<Class<?>> beanControllerList = new ArrayList<Class<?>>();

  private final List<Class<?>> beanPostLoadList = new ArrayList<Class<?>>();

  private final List<Class<?>> transactionEventListenerList = new ArrayList<Class<?>>();

  private final List<Class<?>> beanFindControllerList = new ArrayList<Class<?>>();
  private final List<Class<?>> beanQueryAdapterList = new ArrayList<Class<?>>();

  private final List<Class<?>> beanListenerList = new ArrayList<Class<?>>();

  private final List<Class<?>> serverConfigStartupList = new ArrayList<Class<?>>();
  private final List<ServerConfigStartup> serverConfigStartupInstances = new ArrayList<ServerConfigStartup>();

  private final List<BeanFindController> findControllerInstances = new ArrayList<BeanFindController>();
  private final List<BeanPersistController> persistControllerInstances = new ArrayList<BeanPersistController>();
  private final List<BeanPostLoad> beanPostLoadInstances = new ArrayList<BeanPostLoad>();
  private final List<BeanPersistListener> persistListenerInstances = new ArrayList<BeanPersistListener>();
  private final List<BeanQueryAdapter> queryAdapterInstances = new ArrayList<BeanQueryAdapter>();
  private final List<TransactionEventListener> transactionEventListenerInstances = new ArrayList<TransactionEventListener>();

  private Class<?> changeLogPrepareClass;
  private Class<?> changeLogListenerClass;
  private Class<?> changeLogRegisterClass;
  private Class<?> readAuditPrepareClass;
  private Class<?> readAuditLoggerClass;

  private ChangeLogPrepare changeLogPrepare;
  private ChangeLogListener changeLogListener;
  private ChangeLogRegister changeLogRegister;
  private ReadAuditPrepare readAuditPrepare;
  private ReadAuditLogger readAuditLogger;

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
   * Add BeanPostLoad instances.
   */
  public void addPostLoaders(List<BeanPostLoad> postLoadInstances) {
    if (postLoadInstances != null) {
      for (BeanPostLoad c : postLoadInstances) {
        this.beanPostLoadInstances.add(c);
        // don't automatically instantiate
        this.beanPostLoadList.remove(c.getClass());
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

  public void addChangeLogInstances(ServerConfig serverConfig) {

    readAuditPrepare = serverConfig.getReadAuditPrepare();
    readAuditLogger = serverConfig.getReadAuditLogger();

    if (readAuditPrepare == null && readAuditPrepareClass != null) {
      readAuditPrepare = (ReadAuditPrepare)create(readAuditPrepareClass, false);
    }
    if (readAuditLogger == null && readAuditLoggerClass != null) {
      readAuditLogger = (ReadAuditLogger)create(readAuditLoggerClass, false);
    }

    changeLogListener = serverConfig.getChangeLogListener();
    changeLogRegister = serverConfig.getChangeLogRegister();
    changeLogPrepare = serverConfig.getChangeLogPrepare();

    // if not already set create the implementations found
    // via classpath scanning
    if (changeLogPrepare == null && changeLogPrepareClass != null) {
      changeLogPrepare = (ChangeLogPrepare)create(changeLogPrepareClass, false);
    }
    if (changeLogListener == null && changeLogListenerClass != null) {
      changeLogListener = (ChangeLogListener)create(changeLogListenerClass, false);
    }
    if (changeLogRegister == null && changeLogRegisterClass != null) {
      changeLogRegister = (ChangeLogRegister)create(changeLogRegisterClass, false);
    }
  }

  /**
   * Create an instance using the default constructor returning null if there
   * is no default constructor (implying the class was not intended to be instantiated
   * automatically via classpath scanning.
   * <p>
   * Use logOnException = true to log the error and carry on.
   */
  private Object create(Class<?> cls, boolean logOnException) {
    try {
      // instantiate via found class
      Constructor constructor = cls.getConstructor();
      return constructor.newInstance();

    } catch (NoSuchMethodException e) {
      logger.debug("Ignore/expected - no default constructor", e);
      return null;

    } catch (Exception e) {
      if (logOnException) {
        // not expected but we log and carry on
        logger.error("Error creating " + cls, e);
        return null;

      } else {
        // ok, stop the bus
        throw new IllegalStateException("Error creating " + cls, e);
      }
    }
  }

  /**
   * Create the instance if it has a default constructor and add it to the list of instances.
   */
  @SuppressWarnings(value = "unchecked")
  private <T> void createAdd(Class<?> cls, List<T> instances) {
    Object newInstance = create(cls, true);
    if (newInstance != null) {
      instances.add((T)newInstance);
    }
  }

  public ChangeLogPrepare getChangeLogPrepare() {
    return changeLogPrepare;
  }

  public ChangeLogListener getChangeLogListener() {
    return changeLogListener;
  }

  public ChangeLogRegister getChangeLogRegister() {
    return changeLogRegister;
  }

  public ReadAuditPrepare getReadAuditPrepare() {
    return readAuditPrepare;
  }

  public ReadAuditLogger getReadAuditLogger() {
    return readAuditLogger;
  }

  public List<BeanQueryAdapter> getBeanQueryAdapters() {
    // add class registered BeanQueryAdapter to the already created instances
    for (Class<?> cls : beanQueryAdapterList) {
      createAdd(cls, queryAdapterInstances);
    }
    return queryAdapterInstances;
  }

  public List<BeanFindController> getBeanFindControllers() {
    // add class registered BeanFindController to the list of created instances
    for (Class<?> cls : beanFindControllerList) {
      createAdd(cls, findControllerInstances);
    }
    return findControllerInstances;
  }

  public List<BeanPersistListener> getBeanPersistListeners() {
    // add class registered BeanPersistController to the already created instances
    for (Class<?> cls : beanListenerList) {
      createAdd(cls, persistListenerInstances);
    }
    return persistListenerInstances;
  }

  public List<BeanPersistController> getBeanPersistControllers() {
    // add class registered BeanPersistController to the already created instances
    for (Class<?> cls : beanControllerList) {
      createAdd(cls, persistControllerInstances);
    }
    return persistControllerInstances;
  }

  public List<BeanPostLoad> getBeanPostLoaders() {
    // add class registered BeanPostLoad to the already created instances
    for (Class<?> cls : beanPostLoadList) {
      createAdd(cls, beanPostLoadInstances);
    }
    return beanPostLoadInstances;
  }

  public List<TransactionEventListener> getTransactionEventListeners() {
    // add class registered TransactionEventListener to the already created instances
    for (Class<?> cls : transactionEventListenerList) {
      createAdd(cls, transactionEventListenerInstances);
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

  @Override
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

    if (BeanPostLoad.class.isAssignableFrom(cls)) {
      beanPostLoadList.add(cls);
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

    if (ChangeLogListener.class.isAssignableFrom(cls)) {
      changeLogListenerClass = cls;
      interesting = true;
    }

    if (ChangeLogRegister.class.isAssignableFrom(cls)) {
      changeLogRegisterClass = cls;
      interesting = true;
    }

    if (ChangeLogPrepare.class.isAssignableFrom(cls)) {
      changeLogPrepareClass = cls;
      interesting = true;
    }

    if (ReadAuditPrepare.class.isAssignableFrom(cls)) {
      readAuditPrepareClass = cls;
      interesting = true;
    }
    if (ReadAuditLogger.class.isAssignableFrom(cls)) {
      readAuditLoggerClass = cls;
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
