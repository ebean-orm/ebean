package com.avaje.ebeaninternal.server.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.common.BeanList;
import com.avaje.ebean.common.BeanMap;
import com.avaje.ebean.common.BeanSet;
import com.avaje.ebeaninternal.api.SpiQuery;

/**
 * Creates the BeanCollections.
 * <p>
 * Creates the BeanSet BeanMap and BeanList objects.
 * </p>
 */
public class BeanCollectionFactory {

  private static final int defaultListInitialCapacity = 20;
  private static final int defaultSetInitialCapacity = 32;
  private static final int defaultMapInitialCapacity = 32;

  /**
   * Create a BeanCollection for the given parameters.
   */
  public static BeanCollection<?> create(SpiQuery.Type manyType) {

    switch (manyType) {
      case MAP:
        return new BeanMap<>(new LinkedHashMap<>(defaultMapInitialCapacity));
      case LIST:
        return new BeanList<>(new ArrayList<>(defaultListInitialCapacity));
      case SET:
        return new BeanSet<>(new LinkedHashSet<>(defaultSetInitialCapacity));

      default:
        throw new RuntimeException("Invalid Arg " + manyType);
    }
  }

}
