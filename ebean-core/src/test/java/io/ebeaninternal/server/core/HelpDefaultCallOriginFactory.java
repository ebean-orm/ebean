package io.ebeaninternal.server.core;

/**
 * Make DefaultCallOriginFactory accessible to tests.
 */
public class HelpDefaultCallOriginFactory {

  public static CallOriginFactory create(int maxStack) {
    return new DefaultCallOriginFactory(maxStack);
  }
}
