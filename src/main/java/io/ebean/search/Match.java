package io.ebean.search;

/**
 * Options for the text match expression.
 */
public class Match extends AbstractMatch {

  protected boolean phrase;

  protected boolean phrasePrefix;

  public Match() {
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
   * Use the AND operator (rather than OR).
   */
  public Match opAnd() {
    operatorAnd = true;
    return this;
  }

  /**
   * Use the OR operator (rather than AND).
   */
  public Match opOr() {
    operatorAnd = false;
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
   * Set the boost.
   */
  public Match boost(double boost) {
    this.boost = boost;
    return this;
  }

  /**
   * Set the rewrite to use.
   */
  public Match minShouldMatch(String minShouldMatch) {
    this.minShouldMatch = minShouldMatch;
    return this;
  }

  /**
   * Set the rewrite to use.
   */
  public Match rewrite(String rewrite) {
    this.rewrite = rewrite;
    return this;
  }

  /**
   * Return true if this is a phrase query.
   */
  public boolean isPhrase() {
    return phrase;
  }

  /**
   * Return true if this is a phrase prefix query.
   */
  public boolean isPhrasePrefix() {
    return phrasePrefix;
  }

}
