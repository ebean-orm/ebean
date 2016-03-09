package com.avaje.ebean.search;

/**
 * Options for the text match expression.
 */
public abstract class BaseMatch {

  protected boolean and;

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
  public boolean isAnd() {
    return and;
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

  protected boolean baseEquals(BaseMatch baseMatch) {

    if (and != baseMatch.and) return false;
    if (Double.compare(baseMatch.boost, boost) != 0) return false;
    if (maxExpansions != baseMatch.maxExpansions) return false;
    if (Double.compare(baseMatch.cutoffFrequency, cutoffFrequency) != 0) return false;
    if (prefixLength != baseMatch.prefixLength) return false;
    if (analyzer != null ? !analyzer.equals(baseMatch.analyzer) : baseMatch.analyzer != null) return false;
    if (minShouldMatch != null ? !minShouldMatch.equals(baseMatch.minShouldMatch) : baseMatch.minShouldMatch != null) return false;
    if (zeroTerms != null ? !zeroTerms.equals(baseMatch.zeroTerms) : baseMatch.zeroTerms != null) return false;
    if (fuzziness != null ? !fuzziness.equals(baseMatch.fuzziness) : baseMatch.fuzziness != null) return false;
    return rewrite != null ? rewrite.equals(baseMatch.rewrite) : baseMatch.rewrite == null;
  }

  protected int baseHashCode() {

    int result;
    long temp;
    result = (and ? 1 : 0);
    result = 31 * result + (analyzer != null ? analyzer.hashCode() : 0);
    temp = Double.doubleToLongBits(boost);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (minShouldMatch != null ? minShouldMatch.hashCode() : 0);
    result = 31 * result + maxExpansions;
    result = 31 * result + (zeroTerms != null ? zeroTerms.hashCode() : 0);
    temp = Double.doubleToLongBits(cutoffFrequency);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (fuzziness != null ? fuzziness.hashCode() : 0);
    result = 31 * result + prefixLength;
    result = 31 * result + (rewrite != null ? rewrite.hashCode() : 0);
    return result;
  }
}
