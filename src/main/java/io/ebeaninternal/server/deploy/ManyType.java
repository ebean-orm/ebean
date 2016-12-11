package io.ebeaninternal.server.deploy;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Represents the type of a OneToMany or ManyToMany property.
 */
public enum ManyType {

  LIST(false, List.class),
  SET(false, Set.class),
  MAP(true, null);


  private final boolean map;

  private final Class<? extends Collection> type;

  ManyType(boolean map, Class<? extends Collection> type) {
    this.map = map;
    this.type = type;
  }

  public boolean isMap() {
    return map;
  }

  /**
   * Returns List.class or Set.class and null for Map.
   * Not intended to be called for maps.
   */
  public Class<? extends Collection> getCollectionType() {
    return type;
  }
}
