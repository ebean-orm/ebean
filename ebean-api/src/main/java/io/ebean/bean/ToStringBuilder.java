package io.ebean.bean;

import java.util.Collection;
import java.util.IdentityHashMap;

/**
 * Helps build toString content taking into account recursion.
 * <p>
 * That is, it detects and handles the case where there are relationships that recurse
 * and would otherwise become an infinite loop (e.g. bidirectional parent child).
 */
public final class ToStringBuilder {

  /**
   * The max number of objects that we allow before stopping content being appended.
   */
  private static final int MAX = 100;

  /**
   * Max length of content in string form added for any given value.
   */
  private static final int TRIM_LENGTH = 500;

  /**
   * The max total content after which we stop content being appended.
   */
  private static final int MAX_TOTAL_CONTENT = 2000;

  private final IdentityHashMap<Object, Integer> id = new IdentityHashMap<>();
  private final StringBuilder sb = new StringBuilder(50);
  private boolean first = true;
  private int counter;

  @Override
  public String toString() {
    return sb.toString();
  }

  /**
   * Set of an object being added.
   */
  public void start(Object bean) {
    if (counter == 0) {
      id.putIfAbsent(bean, 0);
    }
    if (counter <= MAX) {
      sb.append(bean.getClass().getSimpleName()).append("@").append(counter).append("(");
    }
  }

  /**
   * Add a property as name value pair.
   */
  public void add(String name, Object value) {
    if (value != null && counter <= MAX) {
      key(name);
      value(value);
    }
  }

  /**
   * Add raw content.
   */
  public void addRaw(String content) {
    sb.append(content);
  }

  /**
   * End of an object.
   */
  public void end() {
    if (counter <= MAX) {
      sb.append(")");
    }
  }

  private void key(String name) {
    if (counter > MAX) {
      return;
    }
    if (first) {
      first = false;
    } else {
      sb.append(", ");
    }
    sb.append(name).append(":");
  }

  private void value(Object value) {
    if (counter > MAX) {
      return;
    }
    if (value instanceof ToStringAware) {
      if (push(value)) {
        ((ToStringAware) value).toString(this);
      }
    } else if (value instanceof Collection) {
      addCollection((Collection<?>) value);
    } else {
      String content = String.valueOf(value);
      if (content.length() > TRIM_LENGTH) {
        content = content.substring(0, TRIM_LENGTH) + " <trimmed>";
      }
      sb.append(content);
      if (sb.length() >= MAX_TOTAL_CONTENT) {
        sb.append(" ...");
        counter += MAX;
      }
    }
  }

  /**
   * Add a collection of values.
   */
  public void addCollection(Collection<?> c) {
    if (c == null || c.isEmpty()) {
      sb.append("[]");
      return;
    }
    int collectionPos = 0;
    sb.append("[");
    for (Object o : c) {
      if (collectionPos++ > 0) {
        sb.append(", ");
      }
      value(o);
      if (counter > MAX) {
        return;
      }
    }
    sb.append("]");
  }

  private boolean push(Object bean) {
    if (counter > MAX) {
      return false;
    }
    if (counter == MAX) {
      sb.append(" ...");
      counter++;
      return false;
    }
    Integer idx = id.putIfAbsent(bean, counter++);
    if (idx != null) {
      --counter;
      sb.append(bean.getClass().getSimpleName()).append("@").append(idx);
      return false;
    }
    first = true;
    return true;
  }

}
