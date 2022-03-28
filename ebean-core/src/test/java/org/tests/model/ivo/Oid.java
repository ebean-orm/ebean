package org.tests.model.ivo;


public class Oid<T> {

  private final long value;

  public Oid(long value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public long getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return (int) (value ^ (value >>> 32));
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Oid<?>) {
      return ((Oid<?>) o).value == value;
    }
    return false;
  }

  public static Oid<?> valueOf(String s) {
    return new Oid<>(Integer.valueOf(s));
  }

  public static Oid<?> valueOf(long i) {
    return new Oid<>(i);
  }

  public static <T> Oid<T> valueOf(Class<T> cls, String s) {
    Integer v = Integer.valueOf(s);
    return new Oid<>(v);
  }
}
