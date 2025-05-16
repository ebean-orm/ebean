package io.ebean.event;

import io.ebean.Database;
import io.ebean.Transaction;
import io.ebean.config.DatabaseConfig;

import java.util.List;

/**
 * A no operation implementation of BeanPersistController. Objects extending
 * this need to only override the methods they want to.
 * <p>
 * A BeanPersistAdapter is either found automatically via class path search or
 * can be added programmatically via
 * {@link DatabaseConfig#add(BeanPersistController)} or
 * {@link DatabaseConfig#setPersistControllers(java.util.List)}.
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

  @Override
  public void preDelete(List<BeanPersistRequest<?>> requests) {

  }

  /**
   * Returns true indicating normal processing should continue.
   */
  @Override
  public boolean preInsert(BeanPersistRequest<?> request) {
    return true;
  }

  @Override
  public void preInsert(List<BeanPersistRequest<?>> requests) {

  }

  /**
   * Returns true indicating normal processing should continue.
   */
  @Override
  public boolean preUpdate(BeanPersistRequest<?> request) {
    return true;
  }

  @Override
  public void preUpdate(List<BeanPersistRequest<?>> requests) {

  }

  /**
   * Returns true indicating normal processing should continue.
   */
  @Override
  public boolean preSoftDelete(BeanPersistRequest<?> request) {
    return true;
  }

  @Override
  public void preSoftDelete(List<BeanPersistRequest<?>> requests) {

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
   *
   * @deprecated Use {@link #preDelete(BeanDeleteIdsRequest)} instead.
   */
  @Override
  @Deprecated
  public void preDelete(BeanDeleteIdRequest request) {

  }

  @Deprecated
  private static class BeanDeleteIdRequestWrapper implements BeanDeleteIdRequest {
    private final BeanDeleteIdsRequest request;
    private Object id;

    public BeanDeleteIdRequestWrapper(BeanDeleteIdsRequest request) {
      this.request = request;
    }

    @Override
    public Database database() {
      return request.database();
    }

    @Override
    public Transaction transaction() {
      return request.transaction();
    }

    @Override
    public Class<?> beanType() {
      return request.beanType();
    }

    @Override
    public Object id() {
      return id;
    }
  }

  /**
   * Compatibility wrapper.
   */
  public void preDelete(BeanDeleteIdsRequest request) {
    BeanDeleteIdRequestWrapper wrapper = new BeanDeleteIdRequestWrapper(request);
    for (Object id : request.ids()) {
      wrapper.id = id;
      preDelete(wrapper);
    }
  }
}
