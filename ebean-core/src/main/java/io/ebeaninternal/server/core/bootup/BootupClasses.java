package io.ebeaninternal.server.core.bootup;

import io.ebean.annotation.DocStore;
import io.ebean.DatabaseBuilder;
import io.ebean.config.IdGenerator;
import io.ebean.config.ScalarTypeConverter;
import io.ebean.core.type.ScalarType;
import io.ebean.event.*;
import io.ebean.event.changelog.ChangeLogListener;
import io.ebean.event.changelog.ChangeLogPrepare;
import io.ebean.event.changelog.ChangeLogRegister;
import io.ebean.event.readaudit.ReadAuditLogger;
import io.ebean.event.readaudit.ReadAuditPrepare;
import io.ebean.plugin.CustomDeployParser;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.api.CoreLog;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;

/**
 * Interesting classes for a EbeanServer such as Embeddable, Entity,
 * ScalarTypes, Finders, Listeners and Controllers.
 */
public class BootupClasses implements Predicate<Class<?>> {

  private static final System.Logger log = CoreLog.internal;

  private final List<Class<?>> embeddableList = new ArrayList<>();
  private final List<Class<?>> entityList = new ArrayList<>();
  private final List<Class<? extends ScalarType<?>>> scalarTypeList = new ArrayList<>();
  private final List<Class<? extends ScalarTypeConverter<?, ?>>> scalarConverterList = new ArrayList<>();
  private final List<Class<? extends AttributeConverter<?, ?>>> attributeConverterList = new ArrayList<>();

  // The following objects are instantiated on first request
  // there is always a candidate list, that holds the class and an
  // instance list, that holds the instance. Once a class is instantiated
  // (or added) it will get removed from the candidate list
  private final List<Class<? extends IdGenerator>> idGeneratorCandidates = new ArrayList<>();
  private final List<Class<? extends BeanPersistController>> beanPersistControllerCandidates = new ArrayList<>();
  private final List<Class<? extends BeanPostLoad>> beanPostLoadCandidates = new ArrayList<>();
  private final List<Class<? extends BeanPostConstructListener>> beanPostConstructListenerCandidates = new ArrayList<>();
  private final List<Class<? extends BeanFindController>> beanFindControllerCandidates = new ArrayList<>();
  private final List<Class<? extends BeanPersistListener>> beanPersistListenerCandidates = new ArrayList<>();
  private final List<Class<? extends BeanQueryAdapter>> beanQueryAdapterCandidates = new ArrayList<>();
  private final List<Class<? extends ServerConfigStartup>> serverConfigStartupCandidates = new ArrayList<>();
  private final List<Class<? extends CustomDeployParser>> customDeployParserCandidates = new ArrayList<>();

  private final List<IdGenerator> idGeneratorInstances = new ArrayList<>();
  private final List<BeanPersistController> beanPersistControllerInstances = new ArrayList<>();
  private final List<BeanPostLoad> beanPostLoadInstances = new ArrayList<>();
  private final List<BeanPostConstructListener> beanPostConstructListenerInstances = new ArrayList<>();
  private final List<BeanFindController> beanFindControllerInstances = new ArrayList<>();
  private final List<BeanPersistListener> beanPersistListenerInstances = new ArrayList<>();
  private final List<BeanQueryAdapter> beanQueryAdapterInstances = new ArrayList<>();
  private final List<ServerConfigStartup> serverConfigStartupInstances = new ArrayList<>();
  private final List<CustomDeployParser> customDeployParserInstances = new ArrayList<>();

  // single objects
  private Class<? extends ChangeLogPrepare> changeLogPrepareClass;
  private Class<? extends ChangeLogListener> changeLogListenerClass;
  private Class<? extends ChangeLogRegister> changeLogRegisterClass;
  private Class<? extends ReadAuditPrepare> readAuditPrepareClass;
  private Class<? extends ReadAuditLogger> readAuditLoggerClass;

  private ChangeLogPrepare changeLogPrepare;
  private ChangeLogListener changeLogListener;
  private ChangeLogRegister changeLogRegister;
  private ReadAuditPrepare readAuditPrepare;
  private ReadAuditLogger readAuditLogger;

  public BootupClasses() {
  }

  public BootupClasses(Set<Class<?>> classes) {
    if (classes != null) {
      for (Class<?> cls : classes) {
        test(cls);
      }
    }
  }

  /**
   * Run any ServerConfigStartup listeners.
   */
  public void runServerConfigStartup(DatabaseBuilder config) {
    for (Class<?> cls : serverConfigStartupCandidates) {
      try {
        ServerConfigStartup newInstance = (ServerConfigStartup) cls.getDeclaredConstructor().newInstance();
        newInstance.onStart(config);
      } catch (Exception e) {
        // assume that the desired behavior is to fail - add your own try catch if needed
        throw new IllegalStateException("Error running ServerConfigStartup " + cls, e);
      }
    }
    for (ServerConfigStartup startup : serverConfigStartupInstances) {
      try {
        startup.onStart(config);
      } catch (Exception e) {
        // assume that the desired behavior is to fail - add your own try catch if needed
        throw new IllegalStateException("Error running ServerConfigStartup " + startup.getClass(), e);
      }
    }
  }

  /**
   * Adds the list <code>toAdd</code> to <code>instances</code> and removes any pending
   * candidate, to prevent duplicate instantiation.
   */
  private <T> void add(List<T> toAdd, List<T> instances, List<Class<? extends T>> candidates) {
    if (toAdd != null) {
      for (T obj : toAdd) {
        instances.add(obj);
        // don't automatically instantiate
        candidates.remove(obj.getClass());
      }
    }
  }

  /**
   * Add IdGenerator instances (registered explicitly with the ServerConfig).
   */
  public void addIdGenerators(List<IdGenerator> idGenerators) {
    add(idGenerators, idGeneratorInstances, idGeneratorCandidates);
  }

  /**
   * Add BeanPersistController instances.
   */
  public void addPersistControllers(List<BeanPersistController> beanControllers) {
    add(beanControllers, beanPersistControllerInstances, beanPersistControllerCandidates);
  }

  /**
   * Add BeanPostLoad instances.
   */
  public void addPostLoaders(List<BeanPostLoad> postLoaders) {
    add(postLoaders, beanPostLoadInstances, beanPostLoadCandidates);
  }

  /**
   * Add BeanPostConstructListener instances.
   */
  public void addPostConstructListeners(List<BeanPostConstructListener> postConstructListener) {
    add(postConstructListener, beanPostConstructListenerInstances, beanPostConstructListenerCandidates);
  }

  /**
   * Add BeanFindController instances.
   */
  public void addFindControllers(List<BeanFindController> findControllers) {
    add(findControllers, beanFindControllerInstances, beanFindControllerCandidates);
  }

  public void addPersistListeners(List<BeanPersistListener> listenerInstances) {
    add(listenerInstances, beanPersistListenerInstances, beanPersistListenerCandidates);
  }

  public void addQueryAdapters(List<BeanQueryAdapter> queryAdapters) {
    add(queryAdapters, beanQueryAdapterInstances, beanQueryAdapterCandidates);
  }

  public void addServerConfigStartup(List<ServerConfigStartup> startupInstances) {
    add(startupInstances, serverConfigStartupInstances, serverConfigStartupCandidates);
  }

  public void addCustomDeployParser(List<CustomDeployParser> customDeployParser) {
    add(customDeployParser, customDeployParserInstances, customDeployParserCandidates);
  }

  public void addChangeLogInstances(DatabaseBuilder.Settings config) {
    readAuditPrepare = config.getReadAuditPrepare();
    readAuditLogger = config.getReadAuditLogger();
    changeLogPrepare = config.getChangeLogPrepare();
    changeLogListener = config.getChangeLogListener();
    changeLogRegister = config.getChangeLogRegister();
    // if not already set create the implementations found
    // via classpath scanning
    if (readAuditPrepare == null && readAuditPrepareClass != null) {
      readAuditPrepare = create(readAuditPrepareClass, false);
    }
    if (readAuditLogger == null && readAuditLoggerClass != null) {
      readAuditLogger = create(readAuditLoggerClass, false);
    }
    if (changeLogPrepare == null && changeLogPrepareClass != null) {
      changeLogPrepare = create(changeLogPrepareClass, false);
    }
    if (changeLogListener == null && changeLogListenerClass != null) {
      changeLogListener = create(changeLogListenerClass, false);
    }
    if (changeLogRegister == null && changeLogRegisterClass != null) {
      changeLogRegister = create(changeLogRegisterClass, false);
    }
  }

  /**
   * Create an instance using the default constructor returning null if there
   * is no default constructor (implying the class was not intended to be instantiated
   * automatically via classpath scanning.
   * <p>
   * Use logOnException = true to log the error and carry on.
   */
  private <T> T create(Class<T> cls, boolean logOnException) {
    try {
      return cls.getConstructor().newInstance();
    } catch (NoSuchMethodException e) {
      log.log(DEBUG, "Ignore/expected - no default constructor: {0}", e.getMessage());
      return null;

    } catch (Exception e) {
      if (logOnException) {
        // not expected but we log and carry on
        log.log(ERROR, "Error creating " + cls, e);
        return null;

      } else {
        // ok, stop the bus
        throw new IllegalStateException("Error creating " + cls, e);
      }
    }
  }

  /**
   * Create the instance if it has a default constructor and add it to the list of instances.
   * It clears the list of classes afterwards, so that each class in the given list is
   * instantiated only once
   */
  private <T> List<T> createAdd(List<T> instances, List<Class<? extends T>> candidates) {
    for (Class<? extends T> cls : candidates) {
      T newInstance = create(cls, true);
      if (newInstance != null) {
        instances.add(newInstance);
      }
    }
    candidates.clear(); // important, clear class list!
    return instances;
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

  public List<IdGenerator> getIdGenerators() {
    return createAdd(idGeneratorInstances, idGeneratorCandidates);
  }

  public List<BeanPersistController> getBeanPersistControllers() {
    return createAdd(beanPersistControllerInstances, beanPersistControllerCandidates);
  }

  public List<BeanPostLoad> getBeanPostLoaders() {
    return createAdd(beanPostLoadInstances, beanPostLoadCandidates);
  }

  public List<BeanPostConstructListener> getBeanPostConstructoListeners() {
    return createAdd(beanPostConstructListenerInstances, beanPostConstructListenerCandidates);
  }

  public List<BeanFindController> getBeanFindControllers() {
    return createAdd(beanFindControllerInstances, beanFindControllerCandidates);
  }

  public List<BeanPersistListener> getBeanPersistListeners() {
    return createAdd(beanPersistListenerInstances, beanPersistListenerCandidates);
  }

  public List<BeanQueryAdapter> getBeanQueryAdapters() {
    return createAdd(beanQueryAdapterInstances, beanQueryAdapterCandidates);
  }

  public List<CustomDeployParser> getCustomDeployParsers() {
    return createAdd(customDeployParserInstances, customDeployParserCandidates);
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
  public List<Class<? extends ScalarType<?>>> getScalarTypes() {
    return scalarTypeList;
  }

  /**
   * Return the list of ScalarConverters found.
   */
  public List<Class<? extends ScalarTypeConverter<?, ?>>> getScalarConverters() {
    return scalarConverterList;
  }

  /**
   * Return the list of AttributeConverters found.
   */
  public List<Class<? extends AttributeConverter<?, ?>>> getAttributeConverters() {
    return attributeConverterList;
  }

  @Override
  public boolean test(Class<?> cls) {
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
  @SuppressWarnings("unchecked")
  private boolean isInterestingInterface(Class<?> cls) {
    if (Modifier.isAbstract(cls.getModifiers())
      || !(Modifier.isPublic(cls.getModifiers()) || Modifier.isProtected(cls.getModifiers()))) {
      // do not include abstract and non pupbic/protected classes as we can
      // not instantiate them
      return false;
    }
    boolean interesting = false;

    // Types
    if (ScalarType.class.isAssignableFrom(cls)) {
      scalarTypeList.add((Class<? extends ScalarType<?>>) cls);
      interesting = true;
    }

    if (ScalarTypeConverter.class.isAssignableFrom(cls)) {
      scalarConverterList.add((Class<? extends ScalarTypeConverter<?, ?>>) cls);
      interesting = true;
    }

    if (AttributeConverter.class.isAssignableFrom(cls)) {
      attributeConverterList.add((Class<? extends AttributeConverter<?, ?>>) cls);
      interesting = true;
    }

    if (IdGenerator.class.isAssignableFrom(cls)) {
      idGeneratorCandidates.add((Class<? extends IdGenerator>) cls);
      interesting = true;
    }

    // "Candidates"
    if (BeanPersistController.class.isAssignableFrom(cls)) {
      beanPersistControllerCandidates.add((Class<? extends BeanPersistController>) cls);
      interesting = true;
    }

    if (BeanPostLoad.class.isAssignableFrom(cls)) {
      beanPostLoadCandidates.add((Class<? extends BeanPostLoad>) cls);
      interesting = true;
    }

    if (BeanPostConstructListener.class.isAssignableFrom(cls)) {
      beanPostConstructListenerCandidates.add((Class<? extends BeanPostConstructListener>) cls);
      interesting = true;
    }

    if (BeanFindController.class.isAssignableFrom(cls)) {
      beanFindControllerCandidates.add((Class<? extends BeanFindController>) cls);
      interesting = true;
    }

    if (BeanPersistListener.class.isAssignableFrom(cls)) {
      beanPersistListenerCandidates.add((Class<? extends BeanPersistListener>) cls);
      interesting = true;
    }

    if (BeanQueryAdapter.class.isAssignableFrom(cls)) {
      beanQueryAdapterCandidates.add((Class<? extends BeanQueryAdapter>) cls);
      interesting = true;
    }

    if (ServerConfigStartup.class.isAssignableFrom(cls)) {
      serverConfigStartupCandidates.add((Class<? extends ServerConfigStartup>) cls);
      interesting = true;
    }

    if (CustomDeployParser.class.isAssignableFrom(cls)) {
      customDeployParserCandidates.add((Class<? extends CustomDeployParser>) cls);
      interesting = true;
    }

    // single instances, last assigned wins
    if (ChangeLogListener.class.isAssignableFrom(cls)) {
      changeLogListenerClass = (Class<? extends ChangeLogListener>) cls;
      interesting = true;
    }

    if (ChangeLogRegister.class.isAssignableFrom(cls)) {
      changeLogRegisterClass = (Class<? extends ChangeLogRegister>) cls;
      interesting = true;
    }

    if (ChangeLogPrepare.class.isAssignableFrom(cls)) {
      changeLogPrepareClass = (Class<? extends ChangeLogPrepare>) cls;
      interesting = true;
    }

    if (ReadAuditPrepare.class.isAssignableFrom(cls)) {
      readAuditPrepareClass = (Class<? extends ReadAuditPrepare>) cls;
      interesting = true;
    }
    if (ReadAuditLogger.class.isAssignableFrom(cls)) {
      readAuditLoggerClass = (Class<? extends ReadAuditLogger>) cls;
      interesting = true;
    }

    return interesting;
  }

  private boolean isEntity(Class<?> cls) {
    return has(cls, Entity.class) || has(cls, Table.class) || has(cls, DocStore.class);
  }

  private boolean isEmbeddable(Class<?> cls) {
    return has(cls, Embeddable.class);
  }

  /**
   * Returns true if this class has the annotation (or meta annotation). Does not search recursively.
   */
  private boolean has(Class<?> cls, Class<? extends Annotation> ann) {
    return AnnotationUtil.has(cls, ann);
  }
}
