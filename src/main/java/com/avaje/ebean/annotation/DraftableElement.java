package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate an entity bean that has 'draftable' support but it not a 'top level'
 * (or root level) bean but instead child related to another @Draftable entity bean.
 * <p>
 * Relationships to @DraftableElements (@OneToMany, @ManyToMany etc) are automatically
 * deemed to have Cascade.ALL for save and delete (as well as orphan removal mode).
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DraftableElement {

}
