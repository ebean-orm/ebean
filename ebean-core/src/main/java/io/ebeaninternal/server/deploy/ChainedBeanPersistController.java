package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanDeleteIdRequest;
import io.ebean.event.BeanDeleteIdsRequest;
import io.ebean.event.BeanPersistController;
import io.ebean.event.BeanPersistRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Chains multiple BeanPersistController's together.
 * <p>
 * Used when multiple BeanPersistController register for the same bean type.
 */
public final class ChainedBeanPersistController implements BeanPersistController {

  private static final Sorter SORTER = new Sorter();

  private final List<BeanPersistController> list;
  private final BeanPersistController[] chain;

  /**
   * Construct adding 2 BeanPersistController's.
   */
  public ChainedBeanPersistController(BeanPersistController c1, BeanPersistController c2) {
    this(addList(c1, c2));
  }

  /**
   * Helper method used to create a list from 2 BeanPersistController's.
   */
  private static List<BeanPersistController> addList(BeanPersistController c1, BeanPersistController c2) {
    ArrayList<BeanPersistController> addList = new ArrayList<>(2);
    addList.add(c1);
    addList.add(c2);
    return addList;
  }

  /**
   * Construct given the list of BeanPersistController's.
   */
  public ChainedBeanPersistController(List<BeanPersistController> list) {
    this.list = list;
    BeanPersistController[] c = list.toArray(new BeanPersistController[0]);
    Arrays.sort(c, SORTER);
    this.chain = c;
  }

  /**
   * Return the size of the chain.
   */
  int size() {
    return chain.length;
  }

  /**
   * Register a new BeanPersistController and return the resulting chain.
   */
  public ChainedBeanPersistController register(BeanPersistController c) {
    if (list.contains(c)) {
      return this;
    } else {
      List<BeanPersistController> newList = new ArrayList<>(list);
      newList.add(c);
      return new ChainedBeanPersistController(newList);
    }
  }

  /**
   * De-register a BeanPersistController and return the resulting chain.
   */
  public ChainedBeanPersistController deregister(BeanPersistController c) {
    if (!list.contains(c)) {
      return this;
    } else {
      List<BeanPersistController> newList = new ArrayList<>(list);
      newList.remove(c);
      return new ChainedBeanPersistController(newList);
    }
  }

  /**
   * Always returns 0 (not used for this object).
   */
  @Override
  public int getExecutionOrder() {
    return 0;
  }

  /**
   * Always returns false (not used for this object).
   */
  @Override
  public boolean isRegisterFor(Class<?> cls) {
    return false;
  }

  @Override
  public void postDelete(BeanPersistRequest<?> request) {
    for (BeanPersistController controller : chain) {
      controller.postDelete(request);
    }
  }

  @Override
  public void postInsert(BeanPersistRequest<?> request) {
    for (BeanPersistController controller : chain) {
      controller.postInsert(request);
    }
  }

  @Override
  public void postUpdate(BeanPersistRequest<?> request) {
    for (BeanPersistController controller : chain) {
      controller.postUpdate(request);
    }
  }

  @Override
  public void postSoftDelete(BeanPersistRequest<?> request) {
    for (BeanPersistController controller : chain) {
      controller.postSoftDelete(request);
    }
  }

  @Override
  public boolean preDelete(BeanPersistRequest<?> request) {
    for (BeanPersistController controller : chain) {
      if (!controller.preDelete(request)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void preDelete(List<BeanPersistRequest<?>> requests) {
    for (BeanPersistController controller : chain) {
      controller.preDelete(requests);
    }
  }

  @Override
  public boolean preSoftDelete(BeanPersistRequest<?> request) {
    for (BeanPersistController controller : chain) {
      if (!controller.preSoftDelete(request)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void preSoftDelete(List<BeanPersistRequest<?>> requests) {
    for (BeanPersistController controller : chain) {
      controller.preSoftDelete(requests);
    }
  }

  @Override
  public void preDelete(BeanDeleteIdRequest request) {
    throw new AbstractMethodError();
  }

  @Override
  public void preDelete(BeanDeleteIdsRequest request) {
    for (BeanPersistController controller : chain) {
      controller.preDelete(request);
    }
  }

  @Override
  public boolean preInsert(BeanPersistRequest<?> request) {
    for (BeanPersistController controller : chain) {
      if (!controller.preInsert(request)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void preInsert(List<BeanPersistRequest<?>> requests) {
    for (BeanPersistController controller : chain) {
      controller.preInsert(requests);
    }
  }

  @Override
  public boolean preUpdate(BeanPersistRequest<?> request) {
    for (BeanPersistController controller : chain) {
      if (!controller.preUpdate(request)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void preUpdate(List<BeanPersistRequest<?>> requests) {
    for (BeanPersistController controller : chain) {
      controller.preUpdate(requests);
    }
  }

  /**
   * Helper to order the BeanPersistController's in a chain.
   */
  private static final class Sorter implements Comparator<BeanPersistController> {
    @Override
    public int compare(BeanPersistController o1, BeanPersistController o2) {
      return Integer.compare(o1.getExecutionOrder(), o2.getExecutionOrder());
    }
  }
}
