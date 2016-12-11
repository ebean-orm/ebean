package io.ebean.search;

/**
 * Options for the text match expression.
 */
public class MultiMatch extends AbstractMatch {

  /**
   * The MultiMatch type.
   */
  public enum Type {
    BEST_FIELDS,
    MOST_FIELDS,
    CROSS_FIELDS,
    PHRASE,
    PHRASE_PREFIX
  }

  protected final String[] fields;

  protected Type type = Type.BEST_FIELDS;

  protected double tieBreaker;

  /**
   * Create with the given fields.
   */
  public static MultiMatch fields(String... fields) {
    return new MultiMatch(fields);
  }

  /**
   * Construct with a set of fields.
   */
  public MultiMatch(String... fields) {
    this.fields = fields;
  }

  /**
   * Set the type of query.
   */
  public MultiMatch type(Type type) {
    this.type = type;
    return this;
  }

  /**
   * Set the tieBreaker to use.
   */
  public MultiMatch tieBreaker(double tieBreaker) {
    this.tieBreaker = tieBreaker;
    return this;
  }

  /**
   * Use the AND operator (rather than OR).
   */
  public MultiMatch opAnd() {
    operatorAnd = true;
    return this;
  }

  /**
   * Use the OR operator (rather than AND).
   */
  public MultiMatch opOr() {
    operatorAnd = false;
    return this;
  }

  /**
   * Set the minimum should match value.
   */
  public MultiMatch minShouldMatch(String minShouldMatch) {
    this.minShouldMatch = minShouldMatch;
    return this;
  }

  /**
   * Set the boost.
   */
  public MultiMatch boost(double boost) {
    this.boost = boost;
    return this;
  }

  /**
   * Set the zero terms.
   */
  public MultiMatch zeroTerms(String zeroTerms) {
    this.zeroTerms = zeroTerms;
    return this;
  }

  /**
   * Set the cutoff frequency.
   */
  public MultiMatch cutoffFrequency(double cutoffFrequency) {
    this.cutoffFrequency = cutoffFrequency;
    return this;
  }

  /**
   * Set the max expansions (for phrase prefix only).
   */
  public MultiMatch maxExpansions(int maxExpansions) {
    this.maxExpansions = maxExpansions;
    return this;
  }

  /**
   * Set the Analyzer to use for this expression.
   */
  public MultiMatch analyzer(String analyzer) {
    this.analyzer = analyzer;
    return this;
  }

  /**
   * Set the rewrite to use.
   */
  public MultiMatch rewrite(String rewrite) {
    this.rewrite = rewrite;
    return this;
  }

  /**
   * Return the type.
   */
  public Type getType() {
    return type;
  }

  /**
   * Return the fields to search.
   */
  public String[] getFields() {
    return fields;
  }

  /**
   * Return the tie breaker.
   */
  public double getTieBreaker() {
    return tieBreaker;
  }

}
