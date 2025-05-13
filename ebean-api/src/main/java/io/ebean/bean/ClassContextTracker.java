package io.ebean.bean;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Interface for tracking the number of entities in <code>PersistenceContext</code>.
 */
public interface ClassContextTracker {

  /**
   * Get the threshold for <code>rootType</code>.
   */
  int getThreshold(Class<?> rootType);

  /**
   * Do logging for <code>rootType</code> with current number of <code>size</code> elements and <code>threshold</code>.
   */
  int log(Class<?> rootType, int size, int threshold);

  ClassContextTracker INSTANCE = createInstance();

  static ClassContextTracker createInstance() {

    Iterator<ClassContextTracker> loader = ServiceLoader.load(ClassContextTracker.class).iterator();
    if (loader.hasNext()) {
      return loader.next();
    }
    return new ClassContextTracker() {
      @Override
      public int getThreshold(Class<?> rootType) {
        return -1;
      }

      @Override
      public int log(Class<?> rootType, int size, int threshold) {
        return -1;
      }
    };
  }

}
