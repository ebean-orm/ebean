package io.ebeaninternal.server.deploy;

public final class BeanEmbeddedMeta {

  private final BeanProperty[] properties;

  BeanEmbeddedMeta(BeanProperty[] properties) {
    this.properties = properties;
  }

  /**
   * Return the properties with over ridden mapping information.
   */
  public BeanProperty[] getProperties() {
    return properties;
  }

}
