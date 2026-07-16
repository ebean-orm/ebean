package io.ebean;

/**
 * Runtime helpers used by generated {@link DtoMapper} implementations to safely resolve a
 * primitive-typed DTO field whose value is derived from a multi-hop {@code @DtoPath} that
 * traverses a nullable intermediate relation.
 * <p>
 * A {@code null}-guarded getter-chain (e.g. {@code source.getOrganisation() == null ? null :
 * source.getOrganisation().getId()}) always types as the boxed wrapper (since one branch is the
 * {@code null} literal). When the DTO's target field is a primitive (e.g. {@code long
 * organisationId}), passing that boxed expression to the constructor auto-unboxes it - which
 * throws a raw, unhelpful {@link NullPointerException} if the relation really is {@code null}.
 * <p>
 * These methods give the generated mapper a choice, controlled by {@code @DtoPath#failOnNull()}:
 * default to the primitive's zero-equivalent value ({@code orZero} methods, the default), or
 * throw a clear, descriptive exception naming the offending property path ({@code require}
 * methods, opted into via {@code failOnNull = true}).
 *
 * @see io.ebean.annotation.DtoPath
 */
public final class DtoMapperSupport {

  private DtoMapperSupport() {
  }

  /** Return {@code 0} if {@code value} is {@code null}, otherwise its unboxed value. */
  public static long orZero(Long value) {
    return value == null ? 0L : value;
  }

  /** Return {@code 0} if {@code value} is {@code null}, otherwise its unboxed value. */
  public static int orZero(Integer value) {
    return value == null ? 0 : value;
  }

  /** Return {@code 0} if {@code value} is {@code null}, otherwise its unboxed value. */
  public static short orZero(Short value) {
    return value == null ? 0 : value;
  }

  /** Return {@code 0} if {@code value} is {@code null}, otherwise its unboxed value. */
  public static byte orZero(Byte value) {
    return value == null ? 0 : value;
  }

  /** Return {@code 0.0} if {@code value} is {@code null}, otherwise its unboxed value. */
  public static double orZero(Double value) {
    return value == null ? 0.0 : value;
  }

  /** Return {@code 0.0f} if {@code value} is {@code null}, otherwise its unboxed value. */
  public static float orZero(Float value) {
    return value == null ? 0.0f : value;
  }

  /** Return {@code false} if {@code value} is {@code null}, otherwise its unboxed value. */
  public static boolean orZero(Boolean value) {
    return value != null && value;
  }

  /** Return {@code '\u0000'} if {@code value} is {@code null}, otherwise its unboxed value. */
  public static char orZero(Character value) {
    return value == null ? '\u0000' : value;
  }

  /** Return the unboxed value, or throw if {@code value} is {@code null}. */
  public static long require(Long value, String path) {
    if (value == null) {
      throw failure(path);
    }
    return value;
  }

  /** Return the unboxed value, or throw if {@code value} is {@code null}. */
  public static int require(Integer value, String path) {
    if (value == null) {
      throw failure(path);
    }
    return value;
  }

  /** Return the unboxed value, or throw if {@code value} is {@code null}. */
  public static short require(Short value, String path) {
    if (value == null) {
      throw failure(path);
    }
    return value;
  }

  /** Return the unboxed value, or throw if {@code value} is {@code null}. */
  public static byte require(Byte value, String path) {
    if (value == null) {
      throw failure(path);
    }
    return value;
  }

  /** Return the unboxed value, or throw if {@code value} is {@code null}. */
  public static double require(Double value, String path) {
    if (value == null) {
      throw failure(path);
    }
    return value;
  }

  /** Return the unboxed value, or throw if {@code value} is {@code null}. */
  public static float require(Float value, String path) {
    if (value == null) {
      throw failure(path);
    }
    return value;
  }

  /** Return the unboxed value, or throw if {@code value} is {@code null}. */
  public static boolean require(Boolean value, String path) {
    if (value == null) {
      throw failure(path);
    }
    return value;
  }

  /** Return the unboxed value, or throw if {@code value} is {@code null}. */
  public static char require(Character value, String path) {
    if (value == null) {
      throw failure(path);
    }
    return value;
  }

  private static IllegalStateException failure(String path) {
    return new IllegalStateException(
      "@DtoPath(\"" + path + "\") resolved to null via a nullable intermediate relation, but the"
        + " target DTO field is primitive and failOnNull=true - either handle the null case in"
        + " source data, use a boxed wrapper type for the DTO field, or remove failOnNull to"
        + " default to the primitive's zero-equivalent value instead.");
  }
}
