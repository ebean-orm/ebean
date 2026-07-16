package io.ebean;

import io.ebean.config.DtoMapperRegister;
import jakarta.persistence.PersistenceException;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads all generated {@link DtoMapperRegister} implementations (via {@code ServiceLoader},
 * mirroring how {@code EntityClassRegister} is discovered) once, and resolves the {@link
 * DtoMapper} for a given (source, dto) pair, or by the generated mapper's own concrete type, on
 * request.
 * <p>
 * Has no dependency on {@link Database} - it can be constructed independently, before (or
 * without) a {@code Database} existing at all, e.g. as a DI-managed singleton constructed
 * alongside the rest of an application's dependency graph. If you want the exact same instance
 * (and hence the exact same underlying mapper instances) shared between {@code query.mapTo(...)}
 * and your own application code, construct it yourself and register it via {@code
 * DatabaseBuilder.putServiceObject(DtoMapperManager.class, myManager)} before building the {@code
 * Database} - it is then used instead of a Database-internal default instance.
 * <p>
 * Resolved mappers are cached so that repeated lookups only ever pay the cost of iterating the
 * generated registers and constructing the mapper (and its nested mapper/{@code FetchGroup}
 * graph) once - after that, every lookup is a single hash-map hit regardless of how many entity/
 * DTO pairs are registered.
 */
public final class DtoMapperManager {

  private final List<DtoMapperRegister> registers;
  private final ConcurrentHashMap<MapperKey, DtoMapper<?, ?>> pairCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Class<?>, Object> typeCache = new ConcurrentHashMap<>();

  public DtoMapperManager() {
    this.registers = load();
  }

  private static List<DtoMapperRegister> load() {
    List<DtoMapperRegister> result = new ArrayList<>();
    for (DtoMapperRegister register : ServiceLoader.load(DtoMapperRegister.class)) {
      result.add(register);
    }
    return result;
  }

  /**
   * Return the {@link DtoMapper} for the given (source, dto) pair.
   *
   * @throws PersistenceException if no generated mapper is registered for that pair.
   */
  @SuppressWarnings("unchecked")
  public <S, D> DtoMapper<S, D> mapperFor(Class<S> sourceType, Class<D> dtoType) {
    return (DtoMapper<S, D>) pairCache.computeIfAbsent(new MapperKey(sourceType, dtoType), this::resolve);
  }

  /**
   * Return the generated mapper instance of the given concrete mapper type - e.g. {@code
   * manager.get(CustomerDtoMapper.class)} - typically used to resolve a mapper instance for
   * dependency injection into application code (e.g. an avaje-inject {@code @Factory} bean
   * method).
   *
   * @throws PersistenceException if no generated mapper of that type is registered.
   */
  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> mapperType) {
    return (T) typeCache.computeIfAbsent(mapperType, this::resolveByType);
  }

  private DtoMapper<?, ?> resolve(MapperKey key) {
    for (DtoMapperRegister register : registers) {
      DtoMapper<?, ?> mapper = register.mapperFor(key.sourceType, key.dtoType);
      if (mapper != null) {
        return mapper;
      }
    }
    throw new PersistenceException("No DtoMapper registered mapping " + key.sourceType + " -> " + key.dtoType
      + " - check @DtoMapping(source = " + key.sourceType.getSimpleName() + ".class, target = "
      + key.dtoType.getSimpleName() + ".class) is declared on a package-info.java processed by querybean-generator");
  }

  private Object resolveByType(Class<?> mapperType) {
    for (DtoMapperRegister register : registers) {
      Object mapper = register.mapperOfType(mapperType);
      if (mapper != null) {
        return mapper;
      }
    }
    throw new PersistenceException("No DtoMapper of type " + mapperType.getName() + " registered"
      + " - check a @DtoMapping(...) pair generating " + mapperType.getSimpleName()
      + " is declared on a package-info.java processed by querybean-generator");
  }

  /**
   * Cache key pairing the source entity type and target DTO type.
   */
  private static final class MapperKey {

    private final Class<?> sourceType;
    private final Class<?> dtoType;

    MapperKey(Class<?> sourceType, Class<?> dtoType) {
      this.sourceType = sourceType;
      this.dtoType = dtoType;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof MapperKey)) {
        return false;
      }
      MapperKey other = (MapperKey) o;
      return sourceType == other.sourceType && dtoType == other.dtoType;
    }

    @Override
    public int hashCode() {
      return 31 * sourceType.hashCode() + dtoType.hashCode();
    }
  }
}
