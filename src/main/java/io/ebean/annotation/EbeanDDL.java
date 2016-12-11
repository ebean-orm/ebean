package io.ebean.annotation;

import javax.validation.constraints.NotNull;

/**
 * special validation group for &#64;NotNull annotation to enforce <code>NOT NULL</code> generation on DDL.
 * Normally if you put the {@link NotNull} annotation on a property, Ebean will only generate a
 * <code>NOT NULL</code> in DDL if you do not change the validation-groups!
 */
public interface EbeanDDL {

}
