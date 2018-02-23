package io.ebean.event;

import io.ebean.config.ServerConfig;

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

  @Override
  public abstract boolean isRegisterFor(Class<?> cls);

  /**
   * Returns 10 - override this to control the order in which
   * BeanPersistController's are executed when there is multiple of them
   * registered for a given entity type (class).
   */
  @Override
  public int getExecutionOrder() {
    return 10;
  }

  /**
   * Returns true indicating normal processing should continue.
   */
  @Override
  public boolean preDelete(BeanPersistRequest<?> request) {
    return true;
  }

  /**
   * Returns true indicating normal processing should continue.
   */
  @Override
  public boolean preInsert(BeanPersistRequest<?> request) {
    return true;
  }

  /**
   * Returns true indicating normal processing should continue.
   */
  @Override
  public boolean preUpdate(BeanPersistRequest<?> request) {
    return true;
  }

  /**
   * Returns true indicating normal processing should continue.
   */
  @Override
  public boolean preSoftDelete(BeanPersistRequest<?> request) {
    return true;
  }

  /**
   * Does nothing by default.
   */
  @Override
  public void postDelete(BeanPersistRequest<?> request) {
  }

  /**
   * Does nothing by default.
   */
  @Override
  public void postInsert(BeanPersistRequest<?> request) {
  }

  /**
   * Does nothing by default.
   */
  @Override
  public void postUpdate(BeanPersistRequest<?> request) {
  }

  /**
   * Does nothing by default.
   */
  @Override
  public void postSoftDelete(BeanPersistRequest<?> request) {
  }

  /**
   * Does nothing by default.
   */
  @Override
  public void preDelete(BeanDeleteIdRequest request) {

  }
}
