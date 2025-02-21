package io.ebean.plugin;

import static java.util.stream.Collectors.toMap;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.ServiceLoader;

import io.ebean.config.LookupProvider;

/**
 * Provides Lookup instances using potentially module specific Lookups.
 */
public final class Lookups {

  private static final Map<String, Lookup> LOOKUP_MAP =
      ServiceLoader.load(LookupProvider.class).stream()
          .collect(toMap(p -> p.type().getModule().getName(), p -> p.get().provideLookup()));

  private static final Lookup DEFAULT_LOOKUP = MethodHandles.publicLookup();

  private static final MethodType VOID = MethodType.methodType(void.class);

  /**
   * Return a Lookup ideally for the module associated with the given type.
   */
  public static Lookup getLookup(Class<?> type) {
    return LOOKUP_MAP.getOrDefault(type.getModule().getName(), DEFAULT_LOOKUP);
  }

  /**
   * Find the default constructor and return a new instance for the given type
   * potentially using a module specific Lookup instance.
   */
  @SuppressWarnings("unchecked")
  public static <T> T newDefaultInstance(Class<?> type) throws Throwable {
    return (T) getLookup(type).findConstructor(type, VOID).invoke();
  }
}
