package io.ebeaninternal.server.cache;

import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * List of changes to be applied to L2 cache.
 */
public class CacheChangeSet {

  private final List<CacheChange> entries = new ArrayList<>();

  private final Set<BeanDescriptor<?>> queryCaches = new HashSet<>();

  private final Map<ManyKey, ManyChange> manyChangeMap = new HashMap<>();

  /**
   * Set of "base tables" modified used to invalidate entities based on views.
   */
  private final Set<String> viewInvalidation = new HashSet<>();

  private final boolean viewEntityInvalidation;

  /**
   * Construct specifying if we also need to process invalidation for entities based on views.
   */
  public CacheChangeSet(boolean viewEntityInvalidation) {
    this.viewEntityInvalidation = viewEntityInvalidation;
  }

  /**
   * Apply the changes to the L2 cache except entity/view invalidation.
   * <p>
   * Return the set of table changes to process invalidation for entities based on views.
   */
  public Set<String> apply() {
    for (BeanDescriptor<?> entry : queryCaches) {
      entry.clearQueryCache();
    }
    for (CacheChange entry : entries) {
      entry.apply();
    }
    for (CacheChange entry : manyChangeMap.values()) {
      entry.apply();
    }
    return viewInvalidation;
  }

  /**
   * Add an entry to clear a query cache.
   */
  public void addClearQuery(BeanDescriptor<?> descriptor) {
    queryCaches.add(descriptor);
  }

  /**
   * Add many property clear.
   */
  public <T> void addManyClear(BeanDescriptor<T> desc, String manyProperty) {
    many(desc, manyProperty).setClear();
  }

  /**
   * Add many property remove.
   */
  public <T> void addManyRemove(BeanDescriptor<T> desc, String manyProperty, Object parentId) {
    many(desc, manyProperty).addRemove(parentId);
  }

  /**
   * Add many property put.
   */
  public <T> void addManyPut(BeanDescriptor<T> desc, String manyProperty, Object parentId, CachedManyIds entry) {
    many(desc, manyProperty).addPut(parentId, entry);
  }

  /**
   * On bean insert register table for view based entity invalidation.
   */
  public void addBeanInsert(String baseTable) {
    if (viewEntityInvalidation) {
      viewInvalidation.add(baseTable);
    }
  }

  /**
   * Remove a bean from the cache.
   */
  public <T> void addBeanRemove(BeanDescriptor<T> desc, Object id) {
    entries.add(new CacheChangeBeanRemove(desc, id));
    if (viewEntityInvalidation) {
      viewInvalidation.add(desc.getBaseTable());
    }
  }

  /**
   * Update a bean entry.
   */
  public <T> void addBeanUpdate(BeanDescriptor<T> desc, Object id, Map<String, Object> changes, boolean updateNaturalKey, long version) {
    entries.add(new CacheChangeBeanUpdate(desc, id, changes, updateNaturalKey, version));
    if (viewEntityInvalidation) {
      viewInvalidation.add(desc.getBaseTable());
    }
  }

  /**
   * Update a natural key.
   */
  public <T> void addNaturalKeyPut(BeanDescriptor<T> desc, Object id, Object val) {
    entries.add(new CacheChangeNaturalKeyPut(desc, id, val));
  }

  /**
   * Return the ManyChange for the given descriptor and property manyProperty.
   */
  private ManyChange many(BeanDescriptor<?> desc, String manyProperty) {
    ManyKey key = new ManyKey(desc, manyProperty);
    return manyChangeMap.computeIfAbsent(key, ManyChange::new);
  }

  /**
   * Changes for a specific many property.
   */
  private static class ManyChange implements CacheChange {

    final ManyKey key;

    final List<Object> removes = new ArrayList<>();

    final Map<Object, CachedManyIds> puts = new LinkedHashMap<>();

    boolean clear;

    ManyChange(ManyKey key) {
      this.key = key;
    }

    /**
     * Clear all entries.
     */
    void setClear() {
      this.clear = true;
      removes.clear();
    }

    /**
     * Remove entry for the given parentId.
     */
    void addRemove(Object parentId) {
      if (!clear) {
        removes.add(parentId);
      }
    }

    /**
     * Put entry for the given parentId.
     */
    void addPut(Object parentId, CachedManyIds entry) {
      puts.put(parentId, entry);
    }

    @Override
    public void apply() {
      if (clear) {
        key.cacheClear();
      } else {
        for (Map.Entry<Object, CachedManyIds> entry : puts.entrySet()) {
          key.cachePut(entry.getKey(), entry.getValue());
        }
        for (Object parentId : removes) {
          key.cacheRemove(parentId);
        }
      }
    }
  }

  /**
   * Key for changes on a many property.
   */
  private static class ManyKey {

    private final BeanDescriptor<?> desc;

    private final String manyProperty;

    ManyKey(BeanDescriptor<?> desc, String manyProperty) {
      this.desc = desc;
      this.manyProperty = manyProperty;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ManyKey manyKey = (ManyKey) o;
      return desc.equals(manyKey.desc) && manyProperty.equals(manyKey.manyProperty);
    }

    @Override
    public int hashCode() {
      return 92821 * desc.hashCode() + manyProperty.hashCode();
    }

    void cacheClear() {
      desc.cacheManyPropClear(manyProperty);
    }

    void cachePut(Object parentId, CachedManyIds entry) {
      desc.cacheManyPropPut(manyProperty, parentId, entry);
    }

    void cacheRemove(Object parentId) {
      desc.cacheManyPropRemove(manyProperty, parentId);
    }
  }
}
