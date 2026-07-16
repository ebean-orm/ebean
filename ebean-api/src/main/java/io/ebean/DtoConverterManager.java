package io.ebean;

import jakarta.persistence.PersistenceException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static bridge registering custom {@code @DtoConvert} converter instances so generated DTO
 * mappers can reach them.
 * <p>
 * Generated mappers (see {@code query.mapTo(SomeDto.class)}) are wired via {@code ServiceLoader}
 * as plain, no-arg-constructed, compile-time singletons (mirroring how entity/query-bean
 * registration already works) - they have no way to reach a dependency-injection container, or
 * any particular {@code Database} instance, at construction time. When a
 * {@code @DtoConvert(value = ConverterType.class, method = "...")} property's converter is an
 * <b>instance</b> method (as opposed to a {@code static} one, which is called directly with no
 * registration needed at all), the generated mapper resolves it via {@link #get(Class)} - so the
 * application must register an instance here, typically one already built by its own DI
 * container, <b>before</b> building the {@code Database}:
 * <pre>{@code
 * AES256Cipher cipher = ...; // already DI-constructed
 * DtoConverterManager.put(DriverConversions.class, new DriverConversionsImpl(cipher));
 *
 * Database db = DatabaseFactory.create(...); // generated mappers resolve converters from here
 * }</pre>
 * <p>
 * This is a deliberate, narrowly-scoped exception to preferring dependency injection over static
 * mutable state - it exists solely to bridge an already-DI-constructed singleton into
 * {@code ServiceLoader}-discovered, no-arg-constructed generated code, which cannot otherwise
 * reach a DI container or a specific {@code Database} instance. {@link #get(Class)} throws
 * immediately if nothing was registered for the given type, so a missing/late registration fails
 * fast at {@code Database} build time (a generated mapper's eager field initializer) rather than
 * lazily on first use.
 */
public final class DtoConverterManager {

  private static final Map<Class<?>, Object> converters = new ConcurrentHashMap<>();

  private DtoConverterManager() {
  }

  /**
   * Register a converter instance for the given type - must be called before the
   * {@code Database} using it is built.
   */
  public static <T> void put(Class<T> type, T instance) {
    converters.put(type, instance);
  }

  /**
   * Return the registered converter instance for the given type.
   *
   * @throws PersistenceException if no instance was registered for {@code type}.
   */
  @SuppressWarnings("unchecked")
  public static <T> T get(Class<T> type) {
    T instance = (T) converters.get(type);
    if (instance == null) {
      throw new PersistenceException("No " + type.getName() + " registered - call "
        + "DtoConverterManager.put(" + type.getSimpleName() + ".class, ...) before starting the Database");
    }
    return instance;
  }
}
