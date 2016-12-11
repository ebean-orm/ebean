package io.ebean.search;

/**
 * Simple text query options.
 * <p>
 * This maps to an ElasticSearch "simple text query".
 * </p>
 * <pre>{@code
 *
 *  TextSimple options = new TextSimple()
 *       .analyzeWildcard(true)
 *       .fields("name")
 *       .lenient(true)
 *       .opAnd();
 *
 *   List<Customer> customers = server.find(Customer.class)
 *       .text()
 *       .textSimple("quick brown", options)
 *       .findList();
 *
 * }</pre>
 */
public class TextSimple {

  protected String[] fields;

  protected boolean operatorAnd;

  protected String analyzer;

  protected String flags;

  protected boolean lowercaseExpandedTerms = true;

  protected boolean analyzeWildcard;

  protected String locale;

  protected boolean lenient;

  protected String minShouldMatch;

  /**
   * Construct
   */
  public TextSimple() {
  }

  /**
   * Set the fields.
   */
  public TextSimple fields(String... fields) {
    this.fields = fields;
    return this;
  }

  /**
   * Use AND as the default operator.
   */
  public TextSimple opAnd() {
    this.operatorAnd = true;
    return this;
  }

  /**
   * Use OR as the default operator.
   */
  public TextSimple opOr() {
    this.operatorAnd = false;
    return this;
  }

  /**
   * Set the analyzer
   */
  public TextSimple analyzer(String analyzer) {
    this.analyzer = analyzer;
    return this;
  }

  /**
   * Set the flags.
   */
  public TextSimple flags(String flags) {
    this.flags = flags;
    return this;
  }


  /**
   * Set the false to not use lowercase expanded terms.
   */
  public TextSimple lowercaseExpandedTerms(boolean lowercaseExpandedTerms) {
    this.lowercaseExpandedTerms = lowercaseExpandedTerms;
    return this;
  }

  /**
   * Set to true to use analyze wildcard.
   */
  public TextSimple analyzeWildcard(boolean analyzeWildcard) {
    this.analyzeWildcard = analyzeWildcard;
    return this;
  }

  /**
   * Set the locale.
   */
  public TextSimple locale(String locale) {
    this.locale = locale;
    return this;
  }

  /**
   * Set the lenient mode.
   */
  public TextSimple lenient(boolean lenient) {
    this.lenient = lenient;
    return this;
  }

  /**
   * Set the minimum should match.
   */
  public TextSimple minShouldMatch(String minShouldMatch) {
    this.minShouldMatch = minShouldMatch;
    return this;
  }

  /**
   * Return lenient mode.
   */
  public boolean isLenient() {
    return lenient;
  }

  /**
   * Return true to analyse wildcard.
   */
  public boolean isAnalyzeWildcard() {
    return analyzeWildcard;
  }

  /**
   * Return lowercase expanded terms mode.
   */
  public boolean isLowercaseExpandedTerms() {
    return lowercaseExpandedTerms;
  }

  /**
   * Return true if the default operator should be AND.
   */
  public boolean isOperatorAnd() {
    return operatorAnd;
  }

  /**
   * Return the analyzer to use.
   */
  public String getAnalyzer() {
    return analyzer;
  }

  /**
   * Return the fields.
   */
  public String[] getFields() {
    return fields;
  }

  /**
   * Return the locale.
   */
  public String getLocale() {
    return locale;
  }

  /**
   * Return the flags.
   */
  public String getFlags() {
    return flags;
  }

  /**
   * Return the minimum should match.
   */
  public String getMinShouldMatch() {
    return minShouldMatch;
  }

}
