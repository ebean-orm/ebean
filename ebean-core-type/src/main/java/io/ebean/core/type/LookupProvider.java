package io.ebean.core.type;

import java.lang.invoke.MethodHandles.Lookup;

/** Provides a Lookup instance for accessing entity/dto fields. */
public interface LookupProvider {

  Lookup provideLookup();

}
