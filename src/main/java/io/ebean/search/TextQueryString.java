package io.ebean.search;

/**
 * Text query string options.
 * <p>
 * This maps to an ElasticSearch "query string query".
 * </p>
 * <pre>{@code
 *
 *  TextQueryString options = new TextQueryString()
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
 * <pre>{@code
 *
 *  // just use default options
 *  TextQueryString options = new TextQueryString();
 *
 *   List<Customer> customers = server.find(Customer.class)
 *       .text()
 *       .textSimple("quick brown", options)
 *       .findList();
 *
 * }</pre>
 */
public class TextQueryString {

  public static final int DEFAULT_FUZZY_MAX_EXPANSIONS = 50;

  protected final String[] fields;

  /**
   * Only used when multiple fields set.
   */
  protected boolean useDisMax = true;

  /**
   * Only used when multiple fields set.
   */
  protected double tieBreaker;

  protected String defaultField;

  protected boolean operatorAnd;

  protected String analyzer;

  protected boolean allowLeadingWildcard = true;

  protected boolean lowercaseExpandedTerms = true;

  protected int fuzzyMaxExpansions = DEFAULT_FUZZY_MAX_EXPANSIONS;

  protected String fuzziness;

  protected int fuzzyPrefixLength;

  protected double phraseSlop;

  protected double boost;

  protected boolean analyzeWildcard;

  protected boolean autoGeneratePhraseQueries;

  protected String minShouldMatch;

  protected boolean lenient;

  protected String locale;

  protected String timeZone;

  protected String rewrite;

  /**
   * Create with given fields.
   */
  public static TextQueryString fields(String... fields) {
    return new TextQueryString(fields);
  }

  /**
   * Construct with the fields to use.
   */
  public TextQueryString(String... fields) {
    this.fields = fields;
  }

  /**
   * Use the AND operator (rather than OR).
   */
  public TextQueryString opAnd() {
    this.operatorAnd = true;
    return this;
  }

  /**
   * Use the OR operator (rather than AND).
   */
  public TextQueryString opOr() {
    this.operatorAnd = false;
    return this;
  }

  /**
   * Set the locale.
   */
  public TextQueryString locale(String locale) {
    this.locale = locale;
    return this;
  }

  /**
   * Set lenient mode.
   */
  public TextQueryString lenient(boolean lenient) {
    this.lenient = lenient;
    return this;
  }

  /**
   * Set the minimum should match.
   */
  public TextQueryString minShouldMatch(String minShouldMatch) {
    this.minShouldMatch = minShouldMatch;
    return this;
  }

  /**
   * Set the analyzer.
   */
  public TextQueryString analyzer(String analyzer) {
    this.analyzer = analyzer;
    return this;
  }

  /**
   * Set useDisMax option (when multiple fields only).
   */
  public TextQueryString useDisMax(boolean useDisMax) {
    this.useDisMax = useDisMax;
    return this;
  }

  /**
   * Set tieBreaker option (when multiple fields only).
   */
  public TextQueryString tieBreaker(double tieBreaker) {
    this.tieBreaker = tieBreaker;
    return this;
  }

  /**
   * Set the default field.
   */
  public TextQueryString defaultField(String defaultField) {
    this.defaultField = defaultField;
    return this;
  }

  /**
   * Set allow leading wildcard mode.
   */
  public TextQueryString allowLeadingWildcard(boolean allowLeadingWildcard) {
    this.allowLeadingWildcard = allowLeadingWildcard;
    return this;
  }

  /**
   * Set lowercase expanded terms mode.
   */
  public TextQueryString lowercaseExpandedTerms(boolean lowercaseExpandedTerms) {
    this.lowercaseExpandedTerms = lowercaseExpandedTerms;
    return this;
  }

  /**
   * Set fuzzy max expansions.
   */
  public TextQueryString fuzzyMaxExpansions(int fuzzyMaxExpansions) {
    this.fuzzyMaxExpansions = fuzzyMaxExpansions;
    return this;
  }

  /**
   * Set fuzziness.
   */
  public TextQueryString fuzziness(String fuzziness) {
    this.fuzziness = fuzziness;
    return this;
  }

  /**
   * Set the fuzzy prefix length.
   */
  public TextQueryString fuzzyPrefixLength(int fuzzyPrefixLength) {
    this.fuzzyPrefixLength = fuzzyPrefixLength;
    return this;
  }

  /**
   * Set the phrase slop.
   */
  public TextQueryString phraseSlop(double phraseSlop) {
    this.phraseSlop = phraseSlop;
    return this;
  }

  /**
   * Set the boost.
   */
  public TextQueryString boost(double boost) {
    this.boost = boost;
    return this;
  }

  /**
   * Set the analyze wildcard mode.
   */
  public TextQueryString analyzeWildcard(boolean analyzeWildcard) {
    this.analyzeWildcard = analyzeWildcard;
    return this;
  }

  /**
   * Set the auto generate phrase queries mode.
   */
  public TextQueryString autoGeneratePhraseQueries(boolean autoGeneratePhraseQueries) {
    this.autoGeneratePhraseQueries = autoGeneratePhraseQueries;
    return this;
  }

  /**
   * Set the time zone.
   */
  public TextQueryString timeZone(String timeZone) {
    this.timeZone = timeZone;
    return this;
  }

  /**
   * Set the rewrite option.
   */
  public TextQueryString rewrite(String rewrite) {
    this.rewrite = rewrite;
    return this;
  }

  /**
   * Return the rewrite option.
   */
  public String getRewrite() {
    return rewrite;
  }

  /**
   * Return the fields.
   */
  public String[] getFields() {
    return fields;
  }

  /**
   * Return true if AND is the default operator.
   */
  public boolean isOperatorAnd() {
    return operatorAnd;
  }

  /**
   * Return the analyzer.
   */
  public String getAnalyzer() {
    return analyzer;
  }

  /**
   * Return the locale.
   */
  public String getLocale() {
    return locale;
  }

  /**
   * Return lenient mode.
   */
  public boolean isLenient() {
    return lenient;
  }

  /**
   * Return the minimum should match.
   */
  public String getMinShouldMatch() {
    return minShouldMatch;
  }

  /**
   * Return the useDixMax mode.
   */
  public boolean isUseDisMax() {
    return useDisMax;
  }

  /**
   * Return the tie breaker.
   */
  public double getTieBreaker() {
    return tieBreaker;
  }

  /**
   * Return the default field.
   */
  public String getDefaultField() {
    return defaultField;
  }

  /**
   * Return the allow leading wildcard mode.
   */
  public boolean isAllowLeadingWildcard() {
    return allowLeadingWildcard;
  }

  /**
   * Return the lowercase expanded terms mode.
   */
  public boolean isLowercaseExpandedTerms() {
    return lowercaseExpandedTerms;
  }

  /**
   * Return the fuzzy max expansions.
   */
  public int getFuzzyMaxExpansions() {
    return fuzzyMaxExpansions;
  }

  /**
   * Return the fuzziness.
   */
  public String getFuzziness() {
    return fuzziness;
  }

  /**
   * Return the fuzzy prefix length.
   */
  public int getFuzzyPrefixLength() {
    return fuzzyPrefixLength;
  }

  /**
   * Return the phrase slop.
   */
  public double getPhraseSlop() {
    return phraseSlop;
  }

  /**
   * Return the analyze wildcard mode.
   */
  public boolean isAnalyzeWildcard() {
    return analyzeWildcard;
  }

  /**
   * Return the boost.
   */
  public double getBoost() {
    return boost;
  }

  /**
   * Return the auto generate phase queries mode.
   */
  public boolean isAutoGeneratePhraseQueries() {
    return autoGeneratePhraseQueries;
  }

  /**
   * Return the time zone.
   */
  public String getTimeZone() {
    return timeZone;
  }

}
