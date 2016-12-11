package io.ebean.search;

/**
 * Text common terms query.
 * <p>
 * This maps to an ElasticSearch "common terms query".
 * </p>
 * <pre>{@code
 *
 *  TextCommonTerms options = new TextCommonTerms()
 *    .cutoffFrequency(0.001)
 *    .minShouldMatch("50%")
 *    .lowFreqOperatorAnd(true)
 *    .highFreqOperatorAnd(true);
 *
 *  List<Customer> customers = server.find(Customer.class)
 *    .text()
 *    .textCommonTerms("the brown", options)
 *    .findList();
 *
 * }</pre>
 * <pre>{@code
 *
 *   // ElasticSearch expression
 *
 *   "common": {
 *     "body": {
 *       "query": "the brown",
 *       "cutoff_frequency": 0.001,
 *       "low_freq_operator": "and",
 *       "high_freq_operator": "and",
 *       "minimum_should_match": "50%"
 *     }
 *   }
 *
 * }</pre>
 */
public class TextCommonTerms {

  protected double cutoffFrequency;

  protected boolean lowFreqOperatorAnd;
  protected boolean highFreqOperatorAnd;

  protected String minShouldMatch;
  protected String minShouldMatchLowFreq;
  protected String minShouldMatchHighFreq;

  /**
   * Set the cutoff frequency.
   */
  public TextCommonTerms cutoffFrequency(double cutoffFrequency) {
    this.cutoffFrequency = cutoffFrequency;
    return this;
  }

  /**
   * Set to true if low frequency terms should use AND operator.
   */
  public TextCommonTerms lowFreqOperatorAnd(boolean opAnd) {
    this.lowFreqOperatorAnd = opAnd;
    return this;
  }

  /**
   * Set to true if high frequency terms should use AND operator.
   */
  public TextCommonTerms highFreqOperatorAnd(boolean opAnd) {
    this.highFreqOperatorAnd = opAnd;
    return this;
  }

  /**
   * Set the minimum should match.
   */
  public TextCommonTerms minShouldMatch(String minShouldMatch) {
    this.minShouldMatch = minShouldMatch;
    return this;
  }

  /**
   * Set the minimum should match for low frequency terms.
   */
  public TextCommonTerms minShouldMatchLowFreq(String minShouldMatchLowFreq) {
    this.minShouldMatchLowFreq = minShouldMatchLowFreq;
    return this;
  }

  /**
   * Set the minimum should match for high frequency terms.
   */
  public TextCommonTerms minShouldMatchHighFreq(String minShouldMatchHighFreq) {
    this.minShouldMatchHighFreq = minShouldMatchHighFreq;
    return this;
  }

  /**
   * Return true if low freq should use the AND operator.
   */
  public boolean isLowFreqOperatorAnd() {
    return lowFreqOperatorAnd;
  }

  /**
   * Return true if high freq should use the AND operator.
   */
  public boolean isHighFreqOperatorAnd() {
    return highFreqOperatorAnd;
  }

  /**
   * Return the cutoff frequency.
   */
  public double getCutoffFrequency() {
    return cutoffFrequency;
  }

  /**
   * Return the minimum to match.
   */
  public String getMinShouldMatch() {
    return minShouldMatch;
  }

  /**
   * Return the minimum to match for high frequency.
   */
  public String getMinShouldMatchHighFreq() {
    return minShouldMatchHighFreq;
  }

  /**
   * Return the minimum to match for low frequency.
   */
  public String getMinShouldMatchLowFreq() {
    return minShouldMatchLowFreq;
  }

}
