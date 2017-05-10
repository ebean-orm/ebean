package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.el.ElPropertyDeploy;

import java.util.HashSet;
import java.util.Set;

/**
 * Converts logical property names to database columns with table alias.
 * <p>
 * In doing so it builds an 'includes' set which becomes the joins required to
 * support the properties parsed.
 * </p>
 */
public final class DeployPropertyParser extends DeployParser {


  private final BeanDescriptor<?> beanDescriptor;

  private final Set<String> includes = new HashSet<>();

  private final boolean distinct;

  public DeployPropertyParser(BeanDescriptor<?> beanDescriptor, boolean distinct) {
    this.beanDescriptor = beanDescriptor;
    this.distinct = distinct;
  }

  @Override
  public Set<String> getIncludes() {
    return includes;
  }

  @Override
  public String getDeployWord(String expression) {
    
    ElPropertyDeploy elProp;
    // we can't use the BeanFkProperty for distinct queries in combination with orderBy
    if (distinct) {
      elProp = beanDescriptor.getElGetValue(expression);
    } else {
      elProp = beanDescriptor.getElPropertyDeploy(expression);
    }
    
    if (elProp == null) {
      return null;
    } else {
      addIncludes(elProp.getElPrefix());
      return elProp.getElPlaceholder(encrypted);
    }
  }

  @Override
  public String convertWord() {
    String r = getDeployWord(word);
    return r == null ? word : r;
  }

  private void addIncludes(String prefix) {
    if (prefix != null) {
      includes.add(prefix);
    }
  }
}
