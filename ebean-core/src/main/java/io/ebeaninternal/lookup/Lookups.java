package io.ebeaninternal.lookup;

import static java.util.stream.Collectors.toMap;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;
import java.util.ServiceLoader;

import io.ebean.core.type.LookupProvider;

public final class Lookups {

  private static final Map<String, Lookup> LOOKUP_MAP =
      ServiceLoader.load(LookupProvider.class).stream()
          .collect(toMap(p -> p.type().getModule().getName(), p -> p.get().provideLookup()));

  private static final Lookup LOOKUP = MethodHandles.publicLookup();

  public static Lookup getLookup(Class<?> type) {
    return LOOKUP_MAP.getOrDefault(type.getModule().getName(), LOOKUP);
  }
}
