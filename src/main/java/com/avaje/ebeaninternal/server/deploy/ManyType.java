package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebeaninternal.api.SpiQuery;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Represents the type of a OneToMany or ManyToMany property.
 */
public class ManyType {

  public static final ManyType JAVA_LIST = new ManyType(Underlying.LIST);
  public static final ManyType JAVA_SET = new ManyType(Underlying.SET);
  public static final ManyType JAVA_MAP = new ManyType(Underlying.MAP);

  public enum Underlying {

    LIST(List.class),
    SET(Set.class),
    MAP(null);

    Class<? extends Collection> type;

    Underlying(Class<? extends Collection> type) {
      this.type = type;
    }
  }

  private final SpiQuery.Type queryType;

  private final Underlying underlying;


  public ManyType(Underlying underlying) {
    this.underlying = underlying;
    switch (underlying) {
      case LIST:
        queryType = SpiQuery.Type.LIST;
        break;
      case SET:
        queryType = SpiQuery.Type.SET;
        break;

      default:
        queryType = SpiQuery.Type.MAP;
        break;
    }
  }

  public boolean isMap() {
    return Underlying.MAP.equals(underlying);
  }

  /**
   * Return the matching Query type.
   */
  public SpiQuery.Type getQueryType() {
    return queryType;
  }

  /**
   * Return the underlying type.
   */
  public Underlying getUnderlying() {
    return underlying;
  }

  /**
   * Returns List.class or Set.class and null for Map.
   * Not intended to be called for maps.
   */
  public Class<? extends Collection> getCollectionType() {
    return underlying.type;
  }
}
