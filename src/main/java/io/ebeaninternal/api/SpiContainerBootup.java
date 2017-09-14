package io.ebeaninternal.api;

/**
 * Plugin API available to invoke something prior to container bootup.
 * <p>
 * The initial intent is to provide a hook for 'docker-run' such that we can automatically ensure
 * we have a test DB docker container running and setup ready to go.
 * </p>
 */
public interface SpiContainerBootup {

  /**
   * Run something at bootup prior to the container starting.
   *
   * For example, start DB docker container(s).
   */
  void bootup();
}
