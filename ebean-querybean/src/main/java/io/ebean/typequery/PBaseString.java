package io.ebean.typequery;

/**
 * Base for property types that store as String Varchar types.
 *
 * @param <R> the root query bean type
 */
public abstract class PBaseString<R, T> extends PBaseComparable<R, T> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  PBaseString(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  PBaseString(String name, R root, String prefix) {
    super(name, root, prefix);
  }

  /**
   * Deprecated migrate to eq().
   * <p>
   * Is equal to. The same as <code>eq</code> but uses the strong type as argument rather than String.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  @Deprecated
  public final R equalToType(T value) {
    expr().eq(_name, value);
    return _root;
  }

  /**
   * Deprecated migrate to ne().
   * <p>
   * Is not equal to. The same as <code>ne</code> but uses the strong type as argument rather than String.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  @Deprecated
  public final R notEqualToType(T value) {
    expr().ne(_name, value);
    return _root;
  }

  //  common string / expressions ------------

  /**
   * Equal to.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R eq(String value) {
    expr().eq(_name, value);
    return _root;
  }

  /**
   * Not equal to.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R ne(String value) {
    expr().ne(_name, value);
    return _root;
  }

  /**
   * Greater than.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R gt(String value) {
    expr().gt(_name, value);
    return _root;
  }

  /**
   * Greater than OR Null.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R gtOrNull(String value) {
    expr().gtOrNull(_name, value);
    return _root;
  }

  /**
   * Greater than or Equal to.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R ge(String value) {
    expr().ge(_name, value);
    return _root;
  }

  /**
   * Greater than or Equal to OR Null.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R geOrNull(String value) {
    expr().geOrNull(_name, value);
    return _root;
  }

  /**
   * Less than.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R lt(String value) {
    expr().lt(_name, value);
    return _root;
  }

  /**
   * Less than OR Null.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R ltOrNull(String value) {
    expr().ltOrNull(_name, value);
    return _root;
  }

  /**
   * Less than or Equal to.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R le(String value) {
    expr().le(_name, value);
    return _root;
  }

  /**
   * Less than or Equal to OR null.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R leOrNull(String value) {
    expr().leOrNull(_name, value);
    return _root;
  }

  /**
   * Greater or equal to lower value and strictly less than upper value.
   * <p>
   * This is generally preferable over Between for date and datetime types
   * as SQL Between is inclusive on the upper bound ({@code <= }) and generally
   * we need the upper bound to be exclusive ({@code < }).
   * </p>
   *
   * @param lower the lower bind value ({@code >= })
   * @param upper the upper bind value ({@code < })
   * @return the root query bean instance
   */
  public final R inRange(String lower, String upper) {
    expr().inRange(_name, lower, upper);
    return _root;
  }

  /**
   * Between lower and upper values.
   *
   * @param lower the lower bind value
   * @param upper the upper bind value
   * @return the root query bean instance
   */
  public final R between(String lower, String upper) {
    expr().between(_name, lower, upper);
    return _root;
  }

  /**
   * Case-insensitive is equal to.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public final R ieq(String value) {
    expr().ieq(_name, value);
    return _root;
  }

  /**
   * Case-insensitive is equal to.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public final R iequalTo(String value) {
    expr().ieq(_name, value);
    return _root;
  }

  /**
   * Like - include '%' and '_' placeholders as necessary.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public final R like(String value) {
    expr().like(_name, value);
    return _root;
  }

  /**
   * Starts with - uses a like with '%' wildcard added to the end.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public final R startsWith(String value) {
    expr().startsWith(_name, value);
    return _root;
  }

  /**
   * Ends with - uses a like with '%' wildcard added to the beginning.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public final R endsWith(String value) {
    expr().endsWith(_name, value);
    return _root;
  }

  /**
   * Contains - uses a like with '%' wildcard added to the beginning and end.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public final R contains(String value) {
    expr().contains(_name, value);
    return _root;
  }

  /**
   * Case-insensitive like.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public final R ilike(String value) {
    expr().ilike(_name, value);
    return _root;
  }

  /**
   * Case-insensitive starts with.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public final R istartsWith(String value) {
    expr().istartsWith(_name, value);
    return _root;
  }

  /**
   * Case-insensitive ends with.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public final R iendsWith(String value) {
    expr().iendsWith(_name, value);
    return _root;
  }

  /**
   * Case-insensitive contains.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public final R icontains(String value) {
    expr().icontains(_name, value);
    return _root;
  }

}
