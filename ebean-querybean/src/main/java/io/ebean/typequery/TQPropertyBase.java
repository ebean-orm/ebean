package io.ebean.typequery;

/**
 * Base scalar property.
 *
 * @param <R> The type of the owning root bean
 */
public class TQPropertyBase<R> extends TQProperty<R> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name the name of the property
   * @param root the root query bean instance
   */
  public TQPropertyBase(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public TQPropertyBase(String name, R root, String prefix) {
    super(name, root, prefix);
  }

  /**
   * Order by ascending on this property.
   */
  public R asc() {
    expr().order().asc(_name);
    return _root;
  }

  /**
   * Order by descending on this property.
   */
  public R desc() {
    expr().order().desc(_name);
    return _root;
  }

}
