package io.ebean.bean;

/**
 * A type that can participate in building toString content with ToStringBuilder.
 */
public interface ToStringAware {

  /**
   * Append to the ToStringBuilder.
   */
  void toString(ToStringBuilder builder);
}
