/**
 * Annotation processor generating type-safe Ebean Query beans and DTO graph mappers.
 */
@GeneratePrism(io.ebean.annotation.DtoPath.class)
@GeneratePrism(io.ebean.annotation.DtoRef.class)
@GeneratePrism(io.ebean.annotation.DtoMapping.class)
@GeneratePrism(io.ebean.annotation.DtoConvert.class)
@GeneratePrism(io.ebean.annotation.DtoMixin.class)
package io.ebean.querybean.generator;

import io.avaje.prism.GeneratePrism;
