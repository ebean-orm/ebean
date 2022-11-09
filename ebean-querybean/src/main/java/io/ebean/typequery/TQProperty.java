package io.ebean.typequery;

import io.ebean.ExpressionList;
import io.ebean.Query;

/**
 * A property used in type query.
 *
 * @param <R> The type of the owning root bean
 * @param <T> The property type
 */
public class TQProperty<R, T> implements Query.Property<T> {

  protected final String _name;
  protected final R _root;

  /**
   * Construct with a property name and root instance.
   *
   * @param name the name of the property
   * @param root the root query bean instance
   */
  public TQProperty(String name, R root) {
    this(name, root, null);
  }

  /**
   * Construct with additional path prefix.
   */
  public TQProperty(String name, R root, String prefix) {
    this._root = root;
    this._name = TQPath.add(prefix, name);
  }

  @Override
  public String toString() {
    return _name;
  }

  /**
   * Internal method to return the underlying expression list.
   */
  protected final ExpressionList<?> expr() {
    return ((TQRootBean<?, ?>) _root).peekExprList();
  }

  /**
   * Return the property name.
   */
  protected final String propertyName() {
    return _name;
  }

  /**
   * Is null.
   */
  public final R isNull() {
    expr().isNull(_name);
    return _root;
  }

  /**
   * Is not null.
   */
  public final R isNotNull() {
    expr().isNotNull(_name);
    return _root;
  }

}
