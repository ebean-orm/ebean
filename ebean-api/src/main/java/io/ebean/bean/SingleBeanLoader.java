package io.ebean.bean;

import io.ebean.Database;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BeanLoader used when single beans are loaded (which is usually not ideal / N+1).
 */
public abstract class SingleBeanLoader implements BeanLoader {

  private final ReentrantLock lock = new ReentrantLock();

  protected final Database database;

  SingleBeanLoader(Database database) {
    this.database = database;
  }

  @Override
  public String getName() {
    return database.getName();
  }

  @Override
  public Lock lock() {
    lock.lock();
    return lock;
  }

  /**
   * Single bean lazy loaded when bean from L2 cache.
   */
  public static class L2 extends SingleBeanLoader {
    public L2(Database database) {
      super(database);
    }

    @Override
    public void loadBean(EntityBeanIntercept ebi) {
      database.getPluginApi().loadBeanL2(ebi);
    }
  }

  /**
   * Single bean lazy loaded when a reference bean.
   */
  public static class Ref extends SingleBeanLoader {
    public Ref(Database database) {
      super(database);
    }

    @Override
    public void loadBean(EntityBeanIntercept ebi) {
      database.getPluginApi().loadBeanRef(ebi);
    }
  }

  /**
   * Single bean lazy loaded when a reference bean.
   */
  public static class Dflt extends SingleBeanLoader {
    public Dflt(Database database) {
      super(database);
    }

    @Override
    public void loadBean(EntityBeanIntercept ebi) {
      database.getPluginApi().loadBean(ebi);
    }
  }
}
