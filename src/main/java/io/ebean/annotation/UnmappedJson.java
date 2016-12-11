package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Map property on a bean that unmapped JSON properties go into.
 * <p>
 * This property is considered transient (not mapped to a DB column) unlike <code>@DbJson</code> but will be
 * written to JSON output (unless it also annotated with <code>@JsonIgnore</code>).
 * </p>
 * <p>
 * Being JSON read and written means that in a document store only (ElasticSearch only) case it can hold
 * all top level unmapped properties.
 * </p>
 * <h3>Example:</h3>
 * <pre>{@code
 *
 * @UnmappedJson
 * Map<String,Object> unmapped;
 *
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UnmappedJson {

}
