package io.ebean.typequery;

/**
 * Long property.
 *
 * @param <R> the root query bean type
 */
public class PLong<R> extends PBaseNumber<R, Long> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PLong(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PLong(String name, R root, String prefix) {
    super(name, root, prefix);
  }

  /**
   * Add bitwise AND expression of the given bit flags to compare with the match/mask.
   * <p>
   * <pre>{@code
   *
   * // Flags Bulk + Size = Size
   * // ... meaning Bulk is not set and Size is set
   *
   * long selectedFlags = BwFlags.HAS_BULK + BwFlags.HAS_SIZE;
   * long mask = BwFlags.HAS_SIZE; // Only Size flag set
   *
   * bitwiseAnd(selectedFlags, mask)
   *
   * }</pre>
   *
   * @param flags        The flags we are looking for
   */
  public R bitwiseAnd(long flags, long mask) {
    expr().bitwiseAnd(_name, flags, mask);
    return _root;
  }

  /**
   * Add expression for ALL of the given bit flags to be set.
   * <pre>{@code
   *
   * bitwiseAll(BwFlags.HAS_BULK + BwFlags.HAS_COLOUR)
   *
   * }</pre>
   *
   * @param flags        The flags we are looking for
   */
  public R bitwiseAll(long flags) {
    expr().bitwiseAll(_name, flags);
    return _root;
  }

  /**
   * Add expression for ANY of the given bit flags to be set.
   * <pre>{@code
   *
   * bitwiseAny(BwFlags.HAS_BULK + BwFlags.HAS_COLOUR)
   *
   * }</pre>
   *
   * @param flags        The flags we are looking for
   */
  public R bitwiseAny(long flags) {
    expr().bitwiseAny(_name, flags);
    return _root;
  }

  /**
   * Add expression for the given bit flags to be NOT set.
   * <pre>{@code
   *
   * bitwiseNot(BwFlags.HAS_COLOUR)
   *
   * }</pre>
   *
   * @param flags        The flags we are looking for
   */
  public R bitwiseNot(long flags) {
    expr().bitwiseNot(_name, flags);
    return _root;
  }
}
