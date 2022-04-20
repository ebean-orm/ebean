package io.ebean.bean;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Collects profile information for a bean (or reference/proxy bean) at a given node.
 * <p>
 * The node identifies the location of the bean in the object graph.
 */
public final class NodeUsageCollector {

  private final static Cleaner cleaner = Cleaner.create();

  public static final class State implements Runnable {
    private final WeakReference<NodeUsageListener> managerRef;
    /**
     * The properties used at this profile point.
     */
    private final Set<String> used = new LinkedHashSet<>();
    /**
     * The point in the object graph for a specific query and call stack point.
     */
    private final ObjectGraphNode node;
    /**
     * set to true if the bean is modified (setter called)
     */
    private boolean modified;

    /**
     * The property that cause a reference to lazy load.
     */
    private String loadProperty;

    private State(ObjectGraphNode node, WeakReference<NodeUsageListener> managerRef) {
      this.node = node;
      this.managerRef = managerRef;
    }

    @Override
    public String toString() {
      return node + " read:" + used + " modified:" + modified;
    }

    @Override
    public void run() {
      NodeUsageListener manager = managerRef.get();
      if (manager != null) {
        manager.collectNodeUsage(this);
      }
    }

    /**
     * Return true if no properties where used.
     */
    public boolean isEmpty() {
      return used.isEmpty();
    }

    /**
     * Return the associated node which identifies the location in the object
     * graph of the bean/reference.
     */
    public ObjectGraphNode node() {
      return node;
    }

    /**
     * Return the set of used properties.
     */
    public Set<String> used() {
      return used;
    }

    /**
     * Return true if the bean was modified by a setter.
     */
    public boolean isModified() {
      return modified;
    }
  }

  private final State state;

  public NodeUsageCollector(ObjectGraphNode node, WeakReference<NodeUsageListener> managerRef) {
    this.state = new State(node, managerRef);
    cleaner.register(this, state);
  }

  /**
   * Return the underlying state.
   */
  public State state() {
    return state;
  }

  /**
   * The bean has been modified by a setter method.
   */
  public void setModified() {
    state.modified = true;
  }

  /**
   * Add the name of a property that has been used.
   */
  public void addUsed(String property) {
    state.used.add(property);
  }

  /**
   * The property that invoked a lazy load.
   */
  public void setLoadProperty(String loadProperty) {
    state.loadProperty = loadProperty;
  }

  @Override
  public String toString() {
    return state.toString();
  }
}
