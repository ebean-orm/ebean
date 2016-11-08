package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.event.BeanPersistAdapter;
import com.avaje.ebean.event.BeanPersistRequest;
import com.avaje.ebean.event.BeanPostConstructListener;
import com.avaje.ebean.event.BeanPostLoad;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper that looks for methods annotated with lifecycle events and registers an adapter for them.
 * <p>
 * This includes PrePersist, PostPersist, PreUpdate, PostUpdate, PreRemove, PostRemove and PostLoad
 * lifecycle events.
 * </p>
 */
public class BeanLifecycleAdapterFactory {

  /**
   * Register a BeanPersistController for methods annotated with lifecycle events.
   */
  public void addLifecycleMethods(DeployBeanDescriptor<?> deployDesc) {

    Method[] methods = deployDesc.getBeanType().getMethods();

    // look for annotated methods
    MethodsHolder methodHolder = new MethodsHolder();
    for (Method m : methods) {
      methodHolder.checkMethod(m);
    }

    if (methodHolder.hasPersistMethods()) {
      // has pre/post persist annotated methods
      deployDesc.addPersistController(new PersistAdapter(new PersistMethodsHolder(methodHolder)));
    }

    if (!methodHolder.postLoads.isEmpty()) {
      // has postLoad methods
      deployDesc.addPostLoad(new PostLoadAdapter(methodHolder.postLoads));
    }
    if (!methodHolder.postConstructs.isEmpty()) {
      // has postConstruct methods
      deployDesc.addPostConstructListener(new PostConstructAdapter(methodHolder.postConstructs));
    }
  }

  /**
   * Holds Methods for the lifecycle events.s
   */
  private static class MethodsHolder {

    private boolean hasPersistMethods;
    private final List<Method> preInserts = new ArrayList<>();
    private final List<Method> postInserts = new ArrayList<>();
    private final List<Method> preUpdates = new ArrayList<>();
    private final List<Method> postUpdates = new ArrayList<>();
    private final List<Method> preDeletes = new ArrayList<>();
    private final List<Method> postDeletes = new ArrayList<>();
    private final List<Method> postLoads = new ArrayList<>();
    private final List<Method> postConstructs = new ArrayList<>();

    /**
     * Has one of the pre or post insert update delete annotated methods.
     */
    private boolean hasPersistMethods() {
      return hasPersistMethods;
    }

    /**
     * Check the method for all the annotations we are interested in.
     */
    private void checkMethod(Method method) {
      if (method.isAnnotationPresent(PrePersist.class)) {
        preInserts.add(method);
        hasPersistMethods = true;
      }
      if (method.isAnnotationPresent(PostPersist.class)) {
        postInserts.add(method);
        hasPersistMethods = true;
      }

      if (method.isAnnotationPresent(PreUpdate.class)) {
        preUpdates.add(method);
        hasPersistMethods = true;
      }
      if (method.isAnnotationPresent(PostUpdate.class)) {
        postUpdates.add(method);
        hasPersistMethods = true;
      }

      if (method.isAnnotationPresent(PreRemove.class)) {
        preDeletes.add(method);
        hasPersistMethods = true;
      }
      if (method.isAnnotationPresent(PostRemove.class)) {
        postDeletes.add(method);
        hasPersistMethods = true;
      }

      if (method.isAnnotationPresent(PostLoad.class)) {
        postLoads.add(method);
      }
      if (method.isAnnotationPresent(PostConstruct.class)) {
        postConstructs.add(method);
      }
    }
  }


  /**
   * Utility method to covert List of Method into array (because we care about performance here).
   */
  static Method[] toArray(List<Method> methodList) {
    return methodList.toArray(new Method[methodList.size()]);
  }

  /**
   * Holds Methods for the lifecycle events.s
   */
  private static class PersistMethodsHolder {

    private final Method[] preInserts;
    private final Method[] postInserts;
    private final Method[] preUpdates;
    private final Method[] postUpdates;
    private final Method[] preDeletes;
    private final Method[] postDeletes;

    PersistMethodsHolder(MethodsHolder methodsHolder) {
      this.preInserts = toArray(methodsHolder.preInserts);
      this.preUpdates = toArray(methodsHolder.preUpdates);
      this.preDeletes = toArray(methodsHolder.preDeletes);
      this.postInserts = toArray(methodsHolder.postInserts);
      this.postUpdates = toArray(methodsHolder.postUpdates);
      this.postDeletes = toArray(methodsHolder.postDeletes);
    }
  }

  /**
   * BeanPersistAdapter using reflection to invoke lifecycle methods.
   */
  private static class PersistAdapter extends BeanPersistAdapter {

    private final PersistMethodsHolder methodHolder;

    private PersistAdapter(PersistMethodsHolder methodHolder) {
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
      } catch (InvocationTargetException | IllegalAccessException e) {
        throw new PersistenceException("Error invoking lifecycle method", e);
      }
    }

    private void invoke(Method[] methods, BeanPersistRequest<?> request) {
      for (int i = 0; i < methods.length; i++) {
        invoke(methods[i], request.getBean());
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
  }

  /**
   * BeanPostLoad using reflection to invoke lifecycle methods.
   */
  private static class PostLoadAdapter implements BeanPostLoad {

    private final Method[] postLoadMethods;

    private PostLoadAdapter(List<Method> postLoadMethods) {
      this.postLoadMethods = toArray(postLoadMethods);
    }

    @Override
    public boolean isRegisterFor(Class<?> cls) {
      // Not used
      return false;
    }

    private void invoke(Method method, Object bean) {
      try {
        method.invoke(bean);
      } catch (InvocationTargetException | IllegalAccessException e) {
        throw new PersistenceException("Error invoking lifecycle method", e);
      }
    }

    @Override
    public void postLoad(Object bean) {
      for (int i = 0; i < postLoadMethods.length; i++) {
        invoke(postLoadMethods[i], bean);
      }
    }
  }

  /**
   * PostConstructAdapter using reflection to invoke lifecycle methods.
   */
  private static class PostConstructAdapter implements BeanPostConstructListener {

    private final Method[] postConstructMethods;

    private PostConstructAdapter(List<Method> postConstructMethods) {
      this.postConstructMethods = toArray(postConstructMethods);
    }

    @Override
    public boolean isRegisterFor(Class<?> cls) {
      // Not used
      return false;
    }

    private void invoke(Method method, Object bean) {
      try {
        method.invoke(bean);
      } catch (InvocationTargetException | IllegalAccessException e) {
        throw new PersistenceException("Error invoking lifecycle method", e);
      }
    }

    @Override
    public void postConstruct(Object bean) {
      for (int i = 0; i < postConstructMethods.length; i++) {
        invoke(postConstructMethods[i], bean);
      }
    }

    @Override
    public void autowire(Object bean) {
      // autowire is done by global PostConstructListener only
    }

    @Override
    public void postCreate(Object bean) {
      // postCreate is done by global PostConstructListener only
    }
  }
}
