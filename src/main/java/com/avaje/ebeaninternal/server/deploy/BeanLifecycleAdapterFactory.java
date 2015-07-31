package com.avaje.ebeaninternal.server.deploy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.event.BeanPersistAdapter;
import com.avaje.ebean.event.BeanPersistRequest;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

/**
 * Helper that looks for methods annotated with lifecycle events and registers an adapter for them.
 * <p>
 * This includes PrePersist, PostPersist, PreUpdate, PostUpdate, PreRemove, PostRemove and PostLoad
 * lifecycle events.
 * </p>
 */
public class BeanLifecycleAdapterFactory {

  private static final Logger logger = LoggerFactory.getLogger(BeanLifecycleAdapterFactory.class);

  /**
   * Register a BeanPersistController for methods annotated with lifecycle events.
   */
  public void addLifecycleMethods(DeployBeanDescriptor<?> deployDesc) {

    Method[] methods = deployDesc.getBeanType().getMethods();

    MethodsHolder methodHolder = new MethodsHolder();

    for (Method m : methods) {
      methodHolder.checkMethod(m);
    }

    if (methodHolder.hasListener()) {
      deployDesc.addPersistController(new Adapter(methodHolder));
    }
  }

  /**
   * Holds Methods for the lifecycle events.s
   */
  private static class MethodsHolder {

    private boolean hasListener;
    private final List<Method> preInserts = new ArrayList<Method>();
    private final List<Method> postInserts = new ArrayList<Method>();
    private final List<Method> preUpdates = new ArrayList<Method>();
    private final List<Method> postUpdates = new ArrayList<Method>();
    private final List<Method> preDeletes = new ArrayList<Method>();
    private final List<Method> postDeletes = new ArrayList<Method>();
    private final List<Method> postLoads = new ArrayList<Method>();

    private boolean hasListener() {
      return hasListener;
    }

    private void checkMethod(Method method) {
      if (method.isAnnotationPresent(PrePersist.class)) {
        preInserts.add(method);
        hasListener = true;
      }
      if (method.isAnnotationPresent(PostPersist.class)) {
        postInserts.add(method);
        hasListener = true;
      }

      if (method.isAnnotationPresent(PreUpdate.class)) {
        preUpdates.add(method);
        hasListener = true;
      }
      if (method.isAnnotationPresent(PostUpdate.class)) {
        postUpdates.add(method);
        hasListener = true;
      }

      if (method.isAnnotationPresent(PreRemove.class)) {
        preDeletes.add(method);
        hasListener = true;
      }
      if (method.isAnnotationPresent(PostRemove.class)) {
        postDeletes.add(method);
        hasListener = true;
      }

      if (method.isAnnotationPresent(PostLoad.class)) {
        postLoads.add(method);
        hasListener = true;
      }
    }
  }

  /**
   * BeanPersistAdapter using reflection to invoke lifecycle methods.
   */
  private static class Adapter extends BeanPersistAdapter {

    private final MethodsHolder methodHolder;

    private Adapter(MethodsHolder methodHolder) {
      this.methodHolder = methodHolder;
    }

    @Override
    public boolean isRegisterFor(Class<?> cls) {
      // Not used
      return false;
    }

    private void invoke(Method method, Object bean) {
      try {
        method.invoke(bean);
      } catch (InvocationTargetException e) {
        throw new PersistenceException("Error invoking lifecycle method", e);
      } catch (IllegalAccessException e) {
        throw new PersistenceException("Error invoking lifecycle method", e);
      }
    }

    private void invoke(List<Method> methods, BeanPersistRequest<?> request) {
      if (methods.isEmpty()) return;
      for (Method method : methods) {
        invoke(method, request.getBean());
      }
    }

    @Override
    public boolean preDelete(BeanPersistRequest<?> request) {
      invoke(methodHolder.preDeletes, request);
      return true;
    }

    @Override
    public boolean preInsert(BeanPersistRequest<?> request) {
      invoke(methodHolder.preInserts, request);
      return true;
    }

    @Override
    public boolean preUpdate(BeanPersistRequest<?> request) {
      invoke(methodHolder.preUpdates, request);
      return true;
    }

    @Override
    public void postDelete(BeanPersistRequest<?> request) {
      invoke(methodHolder.postDeletes, request);
    }

    @Override
    public void postInsert(BeanPersistRequest<?> request) {
      invoke(methodHolder.postInserts, request);
    }

    @Override
    public void postUpdate(BeanPersistRequest<?> request) {
      invoke(methodHolder.postUpdates, request);
    }

    @Override
    public void postLoad(Object bean, Set<String> includedProperties) {
      if (methodHolder.postLoads.isEmpty()) return;
      for (Method method : methodHolder.postLoads) {
        invoke(method, bean);
      }
    }
  }
}
