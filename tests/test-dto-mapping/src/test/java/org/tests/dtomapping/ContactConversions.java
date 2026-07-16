package org.tests.dtomapping;

/**
 * Static, dependency-free scalar conversions - the {@code @DtoConvert} static-dispatch case
 * (requirement r13): no registration needed, the generated mapper calls
 * {@code ContactConversions.toActive(...)} directly.
 */
public final class ContactConversions {

  private ContactConversions() {
  }

  public static boolean toActive(Short status) {
    return status != null && status != 0;
  }
}
