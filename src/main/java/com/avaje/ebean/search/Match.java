package com.avaje.ebean.search;

/**
 * Options for the text match expression.
 */
public class Match {

  protected boolean and;

  protected double boost;

  protected String minShouldMatch;

  protected String zeroTerms;

  protected double cutoffFrequency;

  protected String analyzer;

  protected boolean phrase;

  protected boolean phrasePrefix;

  protected int maxExpansions;

  /**
   * Create and return Match options using AND operator.
   */
  public static Match AND() {
    return new Match().opAnd();
  }

  /**
   * Create and return Match options using OR operator.
   */
  public static Match OR() {
    return new Match().opOr();
  }

  /**
   * Use the AND operator (rather than OR).
   */
  public Match opAnd() {
    and = true;
    return this;
  }

  /**
   * Use the OR operator (rather than AND).
   */
  public Match opOr() {
    and = false;
    return this;
  }

  /**
   * Set the minimum should match value.
   */
  public Match minShouldMatch(String minShouldMatch) {
    this.minShouldMatch = minShouldMatch;
    return this;
  }

  /**
   * Set the boost.
   */
  public Match boost(double boost) {
    this.boost = boost;
    return this;
  }

  /**
   * Set this to be a "Phrase" type expression.
   */
  public Match phrase() {
    phrase = true;
    return this;
  }

  /**
   * Set this to be a "Phrase Prefix" type expression.
   */
  public Match phrasePrefix() {
    phrasePrefix = true;
    return this;
  }

  /**
   * Set the zero terms.
   */
  public Match zeroTerms(String zeroTerms) {
    this.zeroTerms = zeroTerms;
    return this;
  }

  /**
   * Set the cutoff frequency.
   */
  public Match cutoffFrequency(double cutoffFrequency) {
    this.cutoffFrequency = cutoffFrequency;
    return this;
  }

  /**
   * Set the max expansions (for phrase prefix only).
   */
  public Match maxExpansions(int maxExpansions) {
    this.maxExpansions = maxExpansions;
    return this;
  }

  /**
   * Set the Analyzer to use for this expression.
   */
  public Match analyzer(String analyzer) {
    this.analyzer = analyzer;
    return this;
  }

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

  public String getZeroTerms() {
    return zeroTerms;
  }

  public double getCutoffFrequency() {
    return cutoffFrequency;
  }

  public boolean isPhrase() {
    return phrase;
  }

  public boolean isPhrasePrefix() {
    return phrasePrefix;
  }

  public int getMaxExpansions() {
    return maxExpansions;
  }

  public String getAnalyzer() {
    return analyzer;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Match match = (Match) o;

    if (and != match.and) return false;
    if (Double.compare(match.boost, boost) != 0) return false;
    if (Double.compare(match.cutoffFrequency, cutoffFrequency) != 0) return false;
    if (phrase != match.phrase) return false;
    if (phrasePrefix != match.phrasePrefix) return false;
    if (maxExpansions != match.maxExpansions) return false;
    if (minShouldMatch != null ? !minShouldMatch.equals(match.minShouldMatch) : match.minShouldMatch != null)
      return false;
    if (zeroTerms != null ? !zeroTerms.equals(match.zeroTerms) : match.zeroTerms != null) return false;
    return analyzer != null ? analyzer.equals(match.analyzer) : match.analyzer == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = (and ? 1 : 0);
    temp = Double.doubleToLongBits(boost);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (minShouldMatch != null ? minShouldMatch.hashCode() : 0);
    result = 31 * result + (zeroTerms != null ? zeroTerms.hashCode() : 0);
    temp = Double.doubleToLongBits(cutoffFrequency);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (analyzer != null ? analyzer.hashCode() : 0);
    result = 31 * result + (phrase ? 1 : 0);
    result = 31 * result + (phrasePrefix ? 1 : 0);
    result = 31 * result + maxExpansions;
    return result;
  }
}
