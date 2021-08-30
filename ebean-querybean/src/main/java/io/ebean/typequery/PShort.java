package io.ebean.typequery;

/**
 * Short property.
 *
 * @param <R> the root query bean type
 */
public final class PShort<R> extends PBaseNumber<R, Short> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PShort(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PShort(String name, R root, String prefix) {
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
   * short selectedFlags = BwFlags.HAS_BULK + BwFlags.HAS_SIZE;
   * short mask = BwFlags.HAS_SIZE; // Only Size flag set
   *
   * bitwiseAnd(selectedFlags, mask)
   *
   * }</pre>
   *
   * @param flags        The flags we are looking for
   */
  public R bitwiseAnd(short flags, short mask) {
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
  public R bitwiseAll(short flags) {
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
  public R bitwiseAny(short flags) {
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
  public R bitwiseNot(short flags) {
    expr().bitwiseNot(_name, flags);
    return _root;
  }
}
