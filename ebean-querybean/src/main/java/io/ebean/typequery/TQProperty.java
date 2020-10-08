package io.ebean.typequery;

import io.ebean.ExpressionList;

/**
 * A property used in type query.
 *
 * @param <R> The type of the owning root bean
 */
public class TQProperty<R> {

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

  public String toString() {
    return _name;
  }

  /**
   * Internal method to return the underlying expression list.
   */
  protected ExpressionList<?> expr() {
    return ((TQRootBean) _root).peekExprList();
  }

  /**
   * Return the property name.
   */
  protected String propertyName() {
    return _name;
  }

  /**
   * Is null.
   */
  public R isNull() {
    expr().isNull(_name);
    return _root;
  }

  /**
   * Is not null.
   */
  public R isNotNull() {
    expr().isNotNull(_name);
    return _root;
  }

}
