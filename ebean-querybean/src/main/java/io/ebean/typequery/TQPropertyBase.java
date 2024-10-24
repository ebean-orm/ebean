package io.ebean.typequery;

/**
 * Base scalar property.
 *
 * @param <R> The type of the owning root bean
 * @param <T> The property type
 */
public abstract class TQPropertyBase<R, T> extends TQProperty<R, T> {

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
  public final R asc() {
    expr().orderBy().asc(_name);
    return _root;
  }

  /**
   * Order by descending on this property.
   */
  public final R desc() {
    expr().orderBy().desc(_name);
    return _root;
  }

}
