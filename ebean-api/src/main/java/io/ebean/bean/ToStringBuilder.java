package io.ebean.bean;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

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
      sb.append(bean.getClass().getSimpleName()).append('@').append(counter).append('(');
    }
  }

  /**
   * Add a property as name value pair.
   */
  public void add(String name, Object value) {
    if (value != null && counter <= MAX) {
      if (value instanceof BeanCollection) {
        if (((BeanCollection<?>)value).isReference()) {
          // suppress unloaded bean collections
          return;
        }
      }
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
      sb.append(')');
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
    sb.append(name).append(':');
  }

  private void value(Object value) {
    if (counter > MAX) {
      return;
    }
    if (value instanceof ToStringAware) {
      if (value instanceof BeanCollection) {
        ((ToStringAware) value).toString(this);
      } else if (push(value)) {
        ((ToStringAware) value).toString(this);
      }
    } else if (value instanceof Collection) {
      addCollection((Collection<?>) value);
    } else if (value instanceof Map) {
      addMap(((Map<?, ?>) value));
    } else {
      String content = String.valueOf(value);
      if (content.length() > TRIM_LENGTH) {
        content = content.substring(0, TRIM_LENGTH) + " (trimmed)";
      }
      sb.append(content);
      if (sb.length() >= MAX_TOTAL_CONTENT) {
        sb.append(" ...");
        counter += MAX;
      }
    }
  }

  public void addMap(Map<?,?> map) {
    if (map == null || map.isEmpty()) {
      sb.append("{}");
    } else {
      boolean firstElement = true;
      sb.append('{');
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (firstElement) {
          firstElement = false;
        } else {
          sb.append(", ");
        }
        sb.append(entry.getKey()).append(':');
        value(entry.getValue());
        if (counter > MAX) {
          return;
        }
      }
      sb.append('}');
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
    boolean firstElement = true;
    sb.append('[');
    for (Object o : c) {
      if (firstElement) {
        firstElement = false;
      } else {
        sb.append(", ");
      }
      value(o);
      if (counter > MAX) {
        return;
      }
    }
    sb.append(']');
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
      sb.append(bean.getClass().getSimpleName()).append('@').append(idx);
      return false;
    }
    first = true;
    return true;
  }

}
