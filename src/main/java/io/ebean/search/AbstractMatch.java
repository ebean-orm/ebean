package io.ebean.search;

/**
 * Options for the text match and multi match expressions.
 */
public abstract class AbstractMatch {

  protected boolean operatorAnd;

  protected String analyzer;

  protected double boost;

  protected String minShouldMatch;

  protected int maxExpansions;

  protected String zeroTerms;

  protected double cutoffFrequency;

  protected String fuzziness;

  protected int prefixLength;

  protected String rewrite;

  /**
   * Return true if using the AND operator otherwise using the OR operator.
   */
  public boolean isOperatorAnd() {
    return operatorAnd;
  }

  /**
   * Return the boost.
   */
  public double getBoost() {
    return boost;
  }

  /**
   * Return the minimum should match.
   */
  public String getMinShouldMatch() {
    return minShouldMatch;
  }

  /**
   * Return the zero terms option.
   */
  public String getZeroTerms() {
    return zeroTerms;
  }

  /**
   * Return the cutoff frequency.
   */
  public double getCutoffFrequency() {
    return cutoffFrequency;
  }

  /**
   * Return the max expansions.
   */
  public int getMaxExpansions() {
    return maxExpansions;
  }

  /**
   * Return the analyzer.
   */
  public String getAnalyzer() {
    return analyzer;
  }

  /**
   * Return the fuzziness.
   */
  public String getFuzziness() {
    return fuzziness;
  }

  /**
   * Return the prefix length.
   */
  public int getPrefixLength() {
    return prefixLength;
  }

  /**
   * Return the rewrite option.
   */
  public String getRewrite() {
    return rewrite;
  }

}
