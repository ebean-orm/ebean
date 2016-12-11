package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate an entity bean that has 'draftable' support.
 * <p>
 * This means that a second set of tables is created to hold draft versions of
 * the rows and that these can then be published which effectively copies/transfers
 * the values from the 'draft' table to the 'live' table.
 * </p>
 * <p>
 * Ebean Query supports 'find as draft' which builds the resulting object graph using
 * the draft tables. This object graph is typically edited, approved in some application
 * specific manor and then published.
 * </p>
 * <p>
 * EbeanServer has a publish method which transfers/copies the draft object graph to
 * the 'live' tables.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Draftable {

}
