package io.ebeaninternal.api;

import io.ebean.plugin.BeanType;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Property expression validation request for a given root bean type.
 */
public class SpiExpressionValidation {

  private final BeanType<?> desc;

  private final LinkedHashSet<String> unknown = new LinkedHashSet<>();

  public SpiExpressionValidation(BeanType<?> desc) {
    this.desc = desc;
  }

  /**
   * Validate that the property expression (path) is valid.
   */
  public void validate(String propertyName) {
    if (!desc.isValidExpression(propertyName)) {
      unknown.add(propertyName);
    }
  }

  /**
   * Return the set of properties considered as having unknown paths.
   */
  public Set<String> getUnknownProperties() {
    return unknown;
  }

}
