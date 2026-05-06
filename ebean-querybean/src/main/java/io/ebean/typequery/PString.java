package io.ebean.typequery;

import org.jspecify.annotations.Nullable;

/**
 * String property.
 *
 * @param <R> the root query bean type
 */
public final class PString<R> extends PBaseComparable<R, String> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PString(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PString(String name, R root, String prefix) {
    super(name, root, prefix);
  }

  /**
   * Case insensitive is equal to.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public R ieq(String value) {
    expr().ieq(_name, value);
    return _root;
  }

  /**
   * Case insensitive is equal to.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public R iequalTo(String value) {
    expr().ieq(_name, value);
    return _root;
  }

  /**
   * Like - include '%' and '_' placeholders as necessary.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public R like(String value) {
    expr().like(_name, value);
    return _root;
  }

  /**
   * Is like if value is non-null and otherwise no expression is added to the query.
   *
   * @param value the value which can be null
   * @return the root query bean instance
   */
  public R likeIfPresent(@Nullable String value) {
    expr().likeIfPresent(_name, value);
    return _root;
  }

  /**
   * Starts with - uses a like with '%' wildcard added to the end.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public R startsWith(String value) {
    expr().startsWith(_name, value);
    return _root;
  }

  /**
   * Is starts with if value is non-null and otherwise no expression is added to the query.
   *
   * @param value the value which can be null
   * @return the root query bean instance
   */
  public R startsWithIfPresent(@Nullable String value) {
    expr().startsWithIfPresent(_name, value);
    return _root;
  }

  /**
   * Ends with - uses a like with '%' wildcard added to the beginning.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public R endsWith(String value) {
    expr().endsWith(_name, value);
    return _root;
  }

  /**
   * Contains - uses a like with '%' wildcard added to the beginning and end.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public R contains(String value) {
    expr().contains(_name, value);
    return _root;
  }

  /**
   * Is contains if value is non-null and otherwise no expression is added to the query.
   *
   * @param value the value which can be null
   * @return the root query bean instance
   */
  public R containsIfPresent(@Nullable String value) {
    expr().containsIfPresent(_name, value);
    return _root;
  }

  /**
   * Case insensitive like.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public R ilike(String value) {
    expr().ilike(_name, value);
    return _root;
  }

  /**
   * Is case-insensitive like if value is non-null and otherwise no expression is added to the query.
   *
   * @param value the value which can be null
   * @return the root query bean instance
   */
  public R ilikeIfPresent(@Nullable String value) {
    expr().ilikeIfPresent(_name, value);
    return _root;
  }

  /**
   * Case insensitive starts with.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public R istartsWith(String value) {
    expr().istartsWith(_name, value);
    return _root;
  }

  /**
   * Is case-insensitive starts with if value is non-null and otherwise no expression is added to the query.
   *
   * @param value the value which can be null
   * @return the root query bean instance
   */
  public R istartsWithIfPresent(@Nullable String value) {
    expr().istartsWithIfPresent(_name, value);
    return _root;
  }

  /**
   * Case insensitive ends with.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public R iendsWith(String value) {
    expr().iendsWith(_name, value);
    return _root;
  }

  /**
   * Case insensitive contains.
   *
   * @param value the equal to bind value
   * @return the root query bean instance
   */
  public R icontains(String value) {
    expr().icontains(_name, value);
    return _root;
  }

  /**
   * Is case-insensitive contains if value is non-null and otherwise no expression is added to the query.
   *
   * @param value the value which can be null
   * @return the root query bean instance
   */
  public R icontainsIfPresent(@Nullable String value) {
    expr().icontainsIfPresent(_name, value);
    return _root;
  }

}
