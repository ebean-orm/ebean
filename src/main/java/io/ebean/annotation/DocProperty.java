package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify the entity type maps to a document store (like ElasticSearch).
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocProperty {

  /**
   * Set this to true to indicate that this property should be un-analysed.
   */
  boolean code() default false;

  /**
   * Set this to true to get an additional un-analysed 'raw' field to use for sorting etc.
   */
  boolean sortable() default false;

  /**
   * Set to true to have the property additionally stored separately from _source.
   */
  boolean store() default false;

  /**
   * Set a boost value specific to this property.
   */
  float boost() default 1;

  /**
   * Set a value to use instead of null.
   */
  String nullValue() default "";

  /**
   * Set this to false to exclude this from the _all property.
   */
  boolean includeInAll() default true;

  /**
   * The analyzer to use.
   */
  String analyzer() default "";

  /**
   * The analyzer to use for searches.
   */
  String searchAnalyzer() default "";

  /**
   * The index options for this property.
   */
  Option options() default Option.DEFAULT;

  /**
   * Set this to false such that doc values are not stored separately for this property.
   */
  boolean docValues() default true;

  /**
   * Set a copyTo field.
   */
  String copyTo() default "";

  /**
   * Set to false to disable the field from indexing, it will only be get/set in _source.
   */
  boolean enabled() default true;

  /**
   * Set to false such that norms are not stored.
   */
  boolean norms() default true;

  /**
   * Index options for a property.
   */
  enum Option {

    /**
     * Only index the doc number.
     */
    DOCS,

    /**
     * Doc number and term frequencies are indexed.
     */
    FREQS,

    /**
     * Doc number, term frequencies and term positions are indexed.
     */
    POSITIONS,

    /**
     * Doc number, term frequencies, term positions and start/end offsets are indexed.
     */
    OFFSETS,

    /**
     * Use the default which means analysed string properties use POSITIONS as the default and all other types use DOCS.
     */
    DEFAULT
  }
}
