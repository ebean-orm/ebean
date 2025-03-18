package io.ebean;

import java.util.Map;
import java.util.Set;

/**
 * Provides access to the internal state of an entity bean.
 */
public interface BeanState {

  /**
   * Return true if this is a lazy loading reference bean.
   * <p>
   * If so the this bean only holds the Id property and will invoke lazy loading
   * if any other property is get or set.
   * </p>
   */
  boolean isReference();

  /**
   * Return true if the bean is new (and not yet saved).
   */
  boolean isNew();

  /**
   * Return true if the bean is new or dirty (and probably needs to be saved).
   */
  boolean isNewOrDirty();

  /**
   * Return true if the bean has been changed but not yet saved.
   */
  boolean isDirty();

  /**
   * This can be called with true to disable lazy loading on the bean.
   */
  void setDisableLazyLoad(boolean disableLazyLoading);

  /**
   * Return true if the bean has lazy loading disabled.
   */
  boolean isDisableLazyLoad();

  /**
   * Set the loaded state of the property given it's name.
   * <p>
   * Typically this would be used to set the loaded state of a property
   * to false to ensure that the specific property is excluded from a
   * stateless update.
   * </p>
   * <pre>{@code
   *
   *   // populate a bean via say JSON
   *   User user = ...;
   *
   *   // set loaded state on the email property to false so that
   *   // the email property is not included in a stateless update
   *   DB.beanState(user).setPropertyLoaded("email", false);
   *
   *   user.update();
   *
   * }</pre>
   * <p>
   * This will throw an IllegalArgumentException if the property is unknown.
   */
  void setPropertyLoaded(String propertyName, boolean loaded);

  /**
   * For partially populated beans returns the properties that are loaded on the
   * bean.
   * <p>
   * Accessing another property will cause lazy loading to occur.
   */
  Set<String> loadedProps();

  /**
   * Return the set of changed properties.
   */
  Set<String> changedProps();

  /**
   * Return a map of the updated properties and their new and old values.
   */
  Map<String, ValuePair> dirtyValues();

  /**
   * Return true if the bean is readOnly.
   * <p>
   * If a setter is called on a readOnly bean it will throw an exception.
   */
  boolean isUnmodifiable();

  /**
   * Advanced - Used to programmatically build a partially or fully loaded
   * entity bean. First create an entity bean via
   * {@link Database#createEntityBean(Class)}, then populate its properties
   * and then call this method specifying which properties where loaded or null
   * for a fully loaded entity bean.
   */
  void setLoaded();

  /**
   * Reset the bean putting it into NEW state such that a save() results in an insert.
   */
  void resetForInsert();

  /**
   * Returns a map with load errors.
   */
  Map<String, Exception> loadErrors();

  /**
   * Return the sort order value for an order column.
   */
  int sortOrder();

}
