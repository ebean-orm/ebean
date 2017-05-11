package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.el.ElPropertyDeploy;

import java.util.Set;

/**
 * For updates converts logical property names to database columns and bean type to base table.
 */
public final class DeployUpdateParser extends DeployParser {

  private final BeanDescriptor<?> beanDescriptor;

  public DeployUpdateParser(BeanDescriptor<?> beanDescriptor) {
    this.beanDescriptor = beanDescriptor;
  }

  /**
   * Return null as not used for updates.
   */
  @Override
  public Set<String> getIncludes() {
    return null;
  }

  @Override
  public String convertWord() {

    String dbWord = getDeployWord(word);

    if (dbWord != null) {
      return dbWord;
    }
    // maybe tableAlias.propertyName
    return convertSubword(0, word, null);
  }

  private String convertSubword(int start, String currentWord, StringBuilder localBuffer) {
    while (true) {

      int dotPos = currentWord.indexOf('.', start);
      if (start == 0 && dotPos == -1) {
        return currentWord;
      }
      if (start == 0) {
        localBuffer = new StringBuilder();
      }
      if (dotPos == -1) {
        // no match...
        localBuffer.append(currentWord.substring(start));
        return localBuffer.toString();
      }

      // append up to the dot
      localBuffer.append(currentWord.substring(start, dotPos + 1));

      if (dotPos == currentWord.length() - 1) {
        // ends with a "." ???
        return localBuffer.toString();
      }

      // get the remainder after the dot
      start = dotPos + 1;
      String remainder = currentWord.substring(start, currentWord.length());

      //String dbWord = deployMap.get(remainder.toLowerCase());
      String dbWord = getDeployWord(remainder);
      if (dbWord != null) {
        // we have found a match for the remainder
        localBuffer.append(dbWord);
        return localBuffer.toString();
      } else {
        //

      }
    }
  }

  @Override
  public String getDeployWord(String expression) {

    if (expression.equalsIgnoreCase(beanDescriptor.getName())) {
      return beanDescriptor.getBaseTable();
    }

    ElPropertyDeploy elProp = beanDescriptor.getElPropertyDeploy(expression);
    if (elProp != null) {
      return elProp.getDbColumn();
    } else {
      return null;
    }
  }

}
