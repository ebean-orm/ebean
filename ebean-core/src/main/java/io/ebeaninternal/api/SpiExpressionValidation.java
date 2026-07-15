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
  private final LinkedHashSet<String> all = new LinkedHashSet<>();

  public SpiExpressionValidation(BeanType<?> desc) {
    this.desc = desc;
  }

  /**
   * Validate that the property expression (path) is valid.
   */
  public void validate(String propertyName) {
    all.add(propertyName);
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
   * Return the set of all property names visited during this validation, regardless of
   * whether they were considered valid against the bean type. Used to inspect the shape of
   * an expression (for example, to check whether it references any nested/associated path)
   * without needing a correctly-typed bean descriptor.
   */
  public Set<String> allProperties() {
    return all;
  }

}
