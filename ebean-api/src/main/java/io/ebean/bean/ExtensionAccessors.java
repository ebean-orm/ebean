package io.ebean.bean;

import io.ebean.bean.extend.EntityExtension;
import io.ebean.bean.extend.ExtendableBean;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Each ExtendableBean has one static member defined as
 * <pre>
 * private static ExtensionAccessors _ebean_extension_accessors =
 *   new ExtensionAccessors(thisClass._ebeanProps, superClass._ebean_extension_accessors | null)
 * </pre>
 * The ExtensionAccessors class is used to compute the additional space, that has to be reserved
 * in the descriptor and the virtual properties, that will be added to the bean descriptor.
 * The following class structure:
 * <pre>
 *   &#64Entity
 *   class Base extends ExtendableBean {
 *     String prop0;
 *     String prop1;
 *     String prop2;
 *   }
 *   &#64EntityExtends(Base.class)
 *   class Ext1 {
 *     String prop3;
 *     String prop4;
 *   }
 *   &#64EntityExtends(Base.class)
 *   class Ext2 {
 *     String prop5;
 *     String prop6;
 *   }
 * </pre>
 * will create an EntityBeanIntercept for "Base" holding up to 7 fields. Writing to fields 0..2 with ebi.setValue will modify
 * the fields in Base, the r/w accesses to fields 3..4 are routed to Ext1 and 5..6 to Ext2.
 * <p>
 * Note about offset and index:
 * </p>
 * <p>
 * When you have subclasses (<code>class Child extends Base</code>) the extensions have all the same index in the parent and in
 * the subclass, but may have different offsets, as the Child-class will provide additional fields.
 * </p>
 *
 * @author Roland Praml, FOCONIS AG
 */
public class ExtensionAccessors implements Iterable<ExtensionAccessor> {

  /**
   * Default extension info for beans, that have no extension.
   */
  public static ExtensionAccessors NONE = new ExtensionAccessors();

  /**
   * The start offset specifies the offset where the first extension will start
   */
  private final int startOffset;

  /**
   * The entries.
   */
  private List<ExtensionAccessor> accessors = new ArrayList<>();

  /**
   * If we inherit from a class that has extensions, we have to inherit also all extensions from here
   */
  private final ExtensionAccessors parent;

  /**
   * The total property length of all extensions. This will be initialized once and cannot be changed any more
   */
  private volatile int propertyLength = -1;

  /**
   * The offsets where the extensions will start for effective binary search.
   */
  private int[] offsets;

  /**
   * Lock for synchronizing the initialization.
   */
  private static final Lock lock = new ReentrantLock();

  /**
   * Constructor for <code>ExtensionInfo.NONE</code>.
   */
  private ExtensionAccessors() {
    this.startOffset = Integer.MAX_VALUE;
    this.propertyLength = 0;
    this.parent = null;
  }

  /**
   * Called from enhancer. Each entity has a static field initialized with
   * <code>_ebean_extensions = new ExtensonInfo(thisClass._ebeanProps, superClass._ebean_extensions | null)</code>
   */
  public ExtensionAccessors(String[] props, ExtensionAccessors parent) {
    this.startOffset = props.length;
    this.parent = parent;
  }

  /**
   * Called from enhancer. Each class annotated with {@link EntityExtension} will show up here.
   *
   * @param prototype instance of the class that is annotated with {@link EntityExtension}
   */
  public ExtensionAccessor add(EntityBean prototype) {
    if (propertyLength != -1) {
      throw new UnsupportedOperationException("The extension is already in use and cannot be extended anymore");
    }
    Entry entry = new Entry(prototype);
    lock.lock();
    try {
      accessors.add(entry);
    } finally {
      lock.unlock();
    }
    return entry;
  }

  /**
   * returns how many extensions are registered.
   */
  public int size() {
    init();
    return accessors.size();
  }

  /**
   * Returns the additional properties, that have been added by extensions.
   */
  public int getPropertyLength() {
    init();
    return propertyLength;
  }

  /**
   * Copies parent extensions and initializes the offsets. This will be done once only.
   */
  private void init() {
    if (propertyLength != -1) {
      return;
    }
    lock.lock();
    try {
      if (propertyLength != -1) {
        return;
      }
      // sort the accessors, so they are "stable"
      if (parent != null) {
        parent.init();
        accessors.sort(Comparator.comparing(e -> e.getType().getName()));
        if (accessors.isEmpty()) {
          accessors = parent.accessors;
        } else {
          accessors.addAll(0, parent.accessors);
        }
      }
      int length = 0;
      offsets = new int[accessors.size()];
      for (int i = 0; i < accessors.size(); i++) {
        Entry entry = (Entry) accessors.get(i);
        entry.index = i;
        offsets[i] = startOffset + length;
        length += entry.getProperties().length;
      }
      propertyLength = length;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Returns the offset of this extension accessor.
   * Note: The offset may vary on subclasses
   */
  int getOffset(ExtensionAccessor accessor) {
    return offsets[accessor.getIndex()];
  }

  /**
   * Finds the accessor for a given property. If the propertyIndex is lower than startOffset, no accessor will be returned,
   * as this means that we try to access a property in the base-entity.
   */
  ExtensionAccessor findAccessor(int propertyIndex) {
    init();
    if (propertyIndex < startOffset) {
      return null;
    }
    int pos = Arrays.binarySearch(offsets, propertyIndex);
    if (pos == -1) {
      return null;
    }
    if (pos < 0) {
      pos = -2 - pos;
    }
    return accessors.get(pos);
  }

  @Override
  public Iterator<ExtensionAccessor> iterator() {
    init();
    return accessors.iterator();
  }

  /**
   * Invoked by enhancer.
   */
  public EntityBean createInstance(ExtensionAccessor accessor, EntityBean base) {
    int offset = getOffset(accessor);
    return ((Entry) accessor).createInstance(offset, base);
  }

  static class Entry implements ExtensionAccessor {
    private int index;
    private final EntityBean prototype;

    private Entry(EntityBean prototype) {
      this.prototype = prototype;
    }

    @Override
    public String[] getProperties() {
      return prototype._ebean_getPropertyNames();
    }

    @Override
    public int getIndex() {
      return index;
    }

    @Override
    public Class<?> getType() {
      return prototype.getClass();
    }

    EntityBean createInstance(int offset, EntityBean base) {
      return (EntityBean) prototype._ebean_newExtendedInstance(offset, base);
    }

    @Override
    public EntityBean getExtension(ExtendableBean bean) {
      EntityBean eb = (EntityBean) bean;
      return eb._ebean_getExtension(Entry.this);
    }
  }

  /**
   * Reads the extension accessors for a given class. If the provided type is not an ExtenadableBean, the
   * <code>ExtensionAccessors.NONE</code> is returned.
   */
  public static ExtensionAccessors read(Class<?> type) {
    if (ExtendableBean.class.isAssignableFrom(type)) {
      try {
        return (ExtensionAccessors) type.getField("_ebean_extension_accessors").get(null);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException("Could not read extension info from " + type, e);
      }
    }
    return ExtensionAccessors.NONE;
  }
}
