package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a property as being excluded from history.
 * <p>
 * This means the property values are not maintained in the history table. Typically this
 * would be placed on relatively large properties (Clobs, Blobs, large varchar columns etc)
 * that are considered not interesting enough to maintain history on excluding them reduces
 * underlying database costs.
 * </p>
 * <p>
 * When placed on a ManyToMany this means that the intersection table does not have history
 * support.
 * </p>
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface HistoryExclude {

}
