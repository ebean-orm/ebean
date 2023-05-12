package io.ebean;

import io.ebean.bean.PersistenceContext;
import io.ebean.plugin.Property;

/**
 * Merge options, when merging two beans. You can fine tune, how the merge should happen.
 * For example you can exclude some special properties or write your custom merge handler.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class BeanMergeOptions {

  /**
   * Interface to write your own merge handler.
   *
   * @param <T>
   */
  @FunctionalInterface
  public interface MergeHandler<T> {
    /**
     * The new <code>bean</code> and the <code>existing</code> is  passed. Together with <code>property</code>
     * and <code>path</code>, you can decide, if you want to continue with merge or not.
     */
    boolean mergeBeans(T bean, T existing, Property property, String path);

  }

  private PersistenceContext persistenceContext;

  private MergeHandler<?> mergeHandler;

  private boolean mergeId = true;

  private boolean mergeVersion = false;

  private boolean clearCollections = true;

  private boolean addExistingToPersistenceContext = true;

  /**
   * Return the persistence context, that is used during merge.
   * If no one is specified, the persistence context of the bean will be used
   */
  public PersistenceContext getPersistenceContext() {
    return persistenceContext;
  }

  /**
   * Sets the persistence context, that is used during merge.
   * * If no one is specified, the persistence context of the bean will be used
   */
  public void setPersistenceContext(PersistenceContext persistenceContext) {
    this.persistenceContext = persistenceContext;
  }

  /**
   * Returns the merge handler, if you want to do special handling for some properties.
   */
  public MergeHandler<?> getMergeHandler() {
    return mergeHandler;
  }

  /**
   * Sets the merge handler, if you want to do special handling for some properties.
   */
  public <T> void setMergeHandler(MergeHandler<T> mergeHandler) {
    this.mergeHandler = mergeHandler;
  }

  /**
   * Returns if we should merge the ID property (default=true).
   */
  public boolean isMergeId() {
    return mergeId;
  }

  /**
   * Should we merge the ID property (default=true).
   */
  public void setMergeId(boolean mergeId) {
    this.mergeId = mergeId;
  }

  /**
   * Returns if we should merge the version property (default=false).
   */
  public boolean isMergeVersion() {
    return mergeVersion;
  }

  /**
   * Should we merge the version property (default=false).
   */
  public void setMergeVersion(boolean mergeVersion) {
    this.mergeVersion = mergeVersion;
  }

  /**
   * Returns if we should clear/replace beanCollections (default=true).
   */
  public boolean isClearCollections() {
    return clearCollections;
  }

  /**
   * Should we clear/replace beanCollections (default=true).
   */
  public void setClearCollections(boolean clearCollections) {
    this.clearCollections = clearCollections;
  }

  /**
   * Returns if we should add existing beans to the persistenceContext (default=true).
   */
  public boolean isAddExistingToPersistenceContext() {
    return addExistingToPersistenceContext;
  }

  /**
   * Should we add existing beans to the persistenceContext (default=true).
   */
  public void setAddExistingToPersistenceContext(boolean addExistingToPersistenceContext) {
    this.addExistingToPersistenceContext = addExistingToPersistenceContext;
  }
}
