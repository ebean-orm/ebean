package io.ebeaninternal.api;

import java.util.ArrayList;
import java.util.List;

/**
 * The results of bean cache hit.
 */
public class BeanCacheResult<T> {

  private List<Entry<T>> list = new ArrayList<>();

  /**
   * Add an entry.
   */
  public void add(T bean, Object key) {
    list.add(new Entry<>(bean, key));
  }

  /**
   * Return the hits.
   */
  public List<Entry<T>> hits() {
    return list;
  }

  /**
   * Bean and cache key pair.
   */
  static class Entry<T> {

    private final T bean;
    private final Object key;

    public Entry(T bean, Object key) {
      this.bean = bean;
      this.key = key;
    }

    /**
     * Return the natural key or id value.
     */
    public Object getKey() {
      return key;
    }

    /**
     * Return the bean.
     */
    public T getBean() {
      return bean;
    }
  }
}
