package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify the entity type maps to a document store (like ElasticSearch).
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocStore {

  /**
   * A unique Id used when queuing reindex events.
   */
  String queueId() default "";

  /**
   * The ElasticSearch index name. If left unspecified the short name of the bean type is used.
   */
  String indexName() default "";

  /**
   * The ElasticSearch index type. If left unspecified the short name of the bean type is used.
   */
  String indexType() default "";

  /**
   * Set to false to disable the "_all" index.
   */
  boolean enableAll() default true;

  /**
   * Set the refresh interval for the index.
   */
  String refreshInterval() default "";

  /**
   * The number of shards this index should use.
   */
  int shards() default 0;

  /**
   * The number of replicas this index should use.
   */
  int replicas() default 0;

  /**
   * Additional mapping that can be defined on the properties.
   */
  DocMapping[] mapping() default {};

  /**
   * Specify the behavior when bean Insert, Update, Delete events occur.
   */
  DocStoreMode persist() default DocStoreMode.DEFAULT;

  /**
   * Specify the behavior when bean Insert occurs.
   */
  DocStoreMode insert() default DocStoreMode.DEFAULT;

  /**
   * Specify the behavior when bean Update occurs.
   */
  DocStoreMode update() default DocStoreMode.DEFAULT;

  /**
   * Specify the behavior when bean Delete occurs.
   */
  DocStoreMode delete() default DocStoreMode.DEFAULT;

  /**
   * Specify to include only some properties in the doc store document.
   * <p>
   * If this is left as default then all scalar properties are included,
   * all @ManyToOne properties are included with just the nested id property
   * and no @OneToMany properties are included.
   * </p>
   * <p>
   * Note that typically DocStoreEmbedded is used on @ManyToOne and @OneToMany
   * properties to indicate what part of the nested document should be included.
   * </p>
   * <h3>Example:</h3>
   * <pre>{@code
   *
   * // only include the customer id and name
   * @DocStore(doc = "id,name")
   * @Entity @Table(name = "o_order")
   * public class Customer {
   *
   * }</pre>
   */
  String doc() default "";
}
