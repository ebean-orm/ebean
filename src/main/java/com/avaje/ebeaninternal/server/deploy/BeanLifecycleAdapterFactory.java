package com.avaje.ebeaninternal.server.deploy;

import java.lang.reflect.Method;
import java.util.Set;

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
 * This includes PrePerist, PostPerist, PreUpdate, PostUpdate, PreRemove, PostRemove and PostLoad
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

    MethodHolder methodHolder = new MethodHolder();

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
  private static class MethodHolder {

    private boolean hasListener;
    private Method preInsert;
    private Method postInsert;
    private Method preUpdate;
    private Method postUpdate;
    private Method preDelete;
    private Method postDelete;
    private Method postLoad;

    private boolean hasListener() {
      return hasListener;
    }

    private void checkMethod(Method method) {
      if (method.isAnnotationPresent(PrePersist.class)) {
        preInsert = method;
        hasListener = true;
      }
      if (method.isAnnotationPresent(PostPersist.class)) {
        postInsert = method;
        hasListener = true;
      }

      if (method.isAnnotationPresent(PreUpdate.class)) {
        preUpdate = method;
        hasListener = true;
      }
      if (method.isAnnotationPresent(PostUpdate.class)) {
        postUpdate = method;
        hasListener = true;
      }

      if (method.isAnnotationPresent(PreRemove.class)) {
        preDelete = method;
        hasListener = true;
      }
      if (method.isAnnotationPresent(PostRemove.class)) {
        postDelete = method;
        hasListener = true;
      }

      if (method.isAnnotationPresent(PostLoad.class)) {
        postLoad = method;
        hasListener = true;
      }

    }
  }

  /**
   * BeanPersistAdapter using reflection to invoke lifecycle methods.
   */
  private static class Adapter extends BeanPersistAdapter {

    private final MethodHolder methodHolder;

    private Adapter(MethodHolder methodHolder) {
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
      } catch (Exception e) {
        logger.error("Error invoking lifecycle adapter", e);
      }
    }

    private void invoke(Method method, BeanPersistRequest<?> request) {
      invoke(method, request.getBean());
    }

    @Override
    public boolean preDelete(BeanPersistRequest<?> request) {
      if (methodHolder.preDelete != null) {
        invoke(methodHolder.preDelete, request);
      }
      return true;
    }

    @Override
    public boolean preInsert(BeanPersistRequest<?> request) {
      if (methodHolder.preInsert != null) {
        invoke(methodHolder.preInsert, request);
      }
      return true;
    }

    @Override
    public boolean preUpdate(BeanPersistRequest<?> request) {
      if (methodHolder.preUpdate != null) {
        invoke(methodHolder.preUpdate, request);
      }
      return true;
    }

    @Override
    public void postDelete(BeanPersistRequest<?> request) {
      if (methodHolder.postDelete != null) {
        invoke(methodHolder.postDelete, request);
      }
    }

    @Override
    public void postInsert(BeanPersistRequest<?> request) {
      if (methodHolder.postInsert != null) {
        invoke(methodHolder.postInsert, request);
      }
    }

    @Override
    public void postUpdate(BeanPersistRequest<?> request) {
      if (methodHolder.postUpdate != null) {
        invoke(methodHolder.postUpdate, request);
      }
    }

    @Override
    public void postLoad(Object bean, Set<String> includedProperties) {
      if (methodHolder.postLoad != null) {
        invoke(methodHolder.postLoad, bean);
      }
    }

  }
}
