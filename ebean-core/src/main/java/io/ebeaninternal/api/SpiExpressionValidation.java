package io.ebeaninternal.api;

import io.ebean.plugin.BeanType;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Property expression validation request for a given root bean type.
 */
public final class SpiExpressionValidation {

  private final BeanType<?> desc;
  private final LinkedHashSet<String> unknown = new LinkedHashSet<>();
  private boolean nestedProperty;

  public SpiExpressionValidation(BeanType<?> desc) {
    this.desc = desc;
  }

  /**
   * Validate that the property expression (path) is valid.
   */
  public void validate(String propertyName) {
    if (!nestedProperty && propertyName.indexOf('.') > -1) {
      nestedProperty = true;
    }
    if (!desc.isValidExpression(propertyName)) {
      unknown.add(propertyName);
    }
  }

  /**
   * Return the set of properties considered as having unknown paths.
   */
  public Set<String> unknownProperties() {
    return unknown;
  }

  /**
   * Return true if any property visited during this validation referenced a nested/associated
   * path (i.e. contained a '.'). Used to inspect the shape of an expression without needing a
   * correctly-typed bean descriptor.
   */
  public boolean hasNestedProperty() {
    return nestedProperty;
  }

}
