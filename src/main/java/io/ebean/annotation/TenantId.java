package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a property as mapping to the "Tenant Id" when using Partition based multi-tenancy support.
 * <p>
 * TenantId properties are automatically considered - not null and insert only.
 * </p>
 * <p>
 * Ebean automatically populates the Tenant Id value via a CurrentTenantIdProvider implementation that is
 * registered with Ebean via ServerConfig.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TenantId {

}
