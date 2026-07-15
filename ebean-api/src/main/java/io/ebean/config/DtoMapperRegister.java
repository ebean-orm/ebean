package io.ebean.config;

import io.ebean.DtoMapper;

/**
 * Loads and returns the {@link DtoMapper} to use for a given DTO type, generated per-module by
 * the querybean-generator annotation processor for each {@code io.ebean.annotation.DtoMapping}
 * registered pair.
 * <p>
 * Implementations resolve purely via literal {@code Class} comparisons (no reflection,
 * {@code Class.forName}, or {@code MethodHandles}) - safe under GraalVM native-image with zero
 * additional reachability metadata - mirroring {@link EntityClassRegister}.
 */
public interface DtoMapperRegister {

  /**
   * Return the mapper for the given DTO type, or {@code null} if this register has no mapper for
   * that type.
   */
  <SOURCE,TARGET> DtoMapper<SOURCE, TARGET> mapperFor(Class<SOURCE> sourceType, Class<TARGET> targetType);

  /**
   * Return the mapper instance of the given concrete generated mapper type, or {@code null} if
   * this register has no mapper of that type.
   * <p>
   * An alternative to {@link #mapperFor(Class, Class)} for looking up a mapper by its own class
   * (e.g. {@code CustomerDtoMapper.class}) rather than by its (source, target) pair - typically
   * used to resolve a mapper instance for dependency injection into application code.
   */
  <T> T mapperOfType(Class<T> mapperType);
}
