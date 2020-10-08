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

  private static final String JOIN = "join";

  private static final String FROM = "from";

  private final BeanDescriptor<?> beanDescriptor;

  private final Set<String> includes = new HashSet<>();

  private boolean catchFirst;

  private ElPropertyDeploy firstProp;

  DeployPropertyParser(BeanDescriptor<?> beanDescriptor) {
    this.beanDescriptor = beanDescriptor;
  }

  /**
   * Set to true to catch the first property.
   */
  public DeployPropertyParser setCatchFirst(boolean catchFirst) {
    this.catchFirst = catchFirst;
    return this;
  }

  /**
   * Return the first property found by the parser.
   */
  public ElPropertyDeploy getFirstProp() {
    return firstProp;
  }

  @Override
  public Set<String> getIncludes() {
    return includes;
  }

  /**
   * Skip if in raw sql expression with from tableName or join tableName.
   */
  @Override
  protected boolean skipWordConvert() {
    return FROM.equalsIgnoreCase(priorWord) || JOIN.equalsIgnoreCase(priorWord);
  }

  @Override
  public String getDeployWord(String expression) {
    ElPropertyDeploy elProp = beanDescriptor.getElPropertyDeploy(expression);
    if (elProp == null) {
      return null;
    } else {
      if (catchFirst && firstProp == null) {
        firstProp = elProp;
      }
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
