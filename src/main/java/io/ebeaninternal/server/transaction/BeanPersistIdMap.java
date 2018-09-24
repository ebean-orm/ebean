package io.ebeaninternal.server.transaction;

import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Organises the individual bean persist requests by type.
 */
public final class BeanPersistIdMap {

  private final Map<String, BeanPersistIds> beanMap = new LinkedHashMap<>();

  @Override
  public String toString() {
    return beanMap.toString();
  }

  public boolean isEmpty() {
    return beanMap.isEmpty();
  }

  public Collection<BeanPersistIds> values() {
    return beanMap.values();
  }

  /**
   * Add a Insert Update or Delete payload.
   */
  public void add(BeanDescriptor<?> desc, PersistRequest.Type type, Object id) {

    BeanPersistIds r = getPersistIds(desc);
    r.addId(type, id);
  }

  private BeanPersistIds getPersistIds(BeanDescriptor<?> desc) {
    String beanType = desc.getFullName();
    return beanMap.computeIfAbsent(beanType, k -> new BeanPersistIds(desc));
  }

}
