package io.ebeaninternal.server.core;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class ServiceUtil {

  public static <S> S service(Class<S> cls) {
    ServiceLoader<S> load = ServiceLoader.load(cls);
    Iterator<S> serviceInstances = load.iterator();
    return serviceInstances.hasNext() ? serviceInstances.next() : null;
  }
}
