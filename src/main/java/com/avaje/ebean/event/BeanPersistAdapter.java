package com.avaje.ebean.event;

import java.util.Set;

import com.avaje.ebean.config.ServerConfig;

/**
 * A no operation implementation of BeanPersistController. Objects extending
 * this need to only override the methods they want to.
 * <p>
 * A BeanPersistAdapter is either found automatically via class path search or
 * can be added programmatically via
 * {@link ServerConfig#add(BeanPersistController)} or
 * {@link ServerConfig#setPersistControllers(java.util.List)}.
 * </p>
 */
public abstract class BeanPersistAdapter implements BeanPersistController {

  public abstract boolean isRegisterFor(Class<?> cls);

  /**
   * Returns 10 - override this to control the order in which
   * BeanPersistController's are executed when there is multiple of them
   * registered for a given entity type (class).
   */
  public int getExecutionOrder() {
    return 10;
  }

  /**
   * Returns true indicating normal processing should continue.
   */
  public boolean preDelete(BeanPersistRequest<?> request) {
    return true;
  }

  /**
   * Returns true indicating normal processing should continue.
   */
  public boolean preInsert(BeanPersistRequest<?> request) {
    return true;
  }

  /**
   * Returns true indicating normal processing should continue.
   */
  public boolean preUpdate(BeanPersistRequest<?> request) {
    return true;
  }

  /**
   * Does nothing by default.
   */
  public void postDelete(BeanPersistRequest<?> request) {
  }

  /**
   * Does nothing by default.
   */
  public void postInsert(BeanPersistRequest<?> request) {
  }

  /**
   * Does nothing by default.
   */
  public void postUpdate(BeanPersistRequest<?> request) {
  }

}
