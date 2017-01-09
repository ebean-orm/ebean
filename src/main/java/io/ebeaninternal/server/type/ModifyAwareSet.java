package io.ebeaninternal.server.type;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Wraps a Set for the purposes of detecting modifications.
 */
public class ModifyAwareSet<E> implements Set<E>, ModifyAwareOwner {

  private static final long serialVersionUID = 1;

  protected final ModifyAwareOwner owner;

  protected final Set<E> set;

  /**
   * Create as top level with it's own ModifyAwareOwner instance wrapping the given Set.
   */
  public ModifyAwareSet(Set<E> underlying) {
    this(new ModifyAwareFlag(), underlying);
  }

  /**
   * Create with an Owner that is notified of modifications.
   */
  public ModifyAwareSet(ModifyAwareOwner owner, Set<E> underlying) {
    this.owner = owner;
    this.set = underlying;
  }

  @Override
  public boolean isMarkedDirty() {
    return owner.isMarkedDirty();
  }

  @Override
  public void markAsModified() {
    owner.markAsModified();
  }

  @Override
  public void resetMarkedDirty() {
    owner.resetMarkedDirty();
  }


  public String toString() {
    return set.toString();
  }

  public boolean add(E o) {
    if (set.add(o)) {
      owner.markAsModified();
      return true;
    }
    return false;
  }

  public boolean addAll(Collection<? extends E> collection) {
    boolean changed = false;
    for (E o : collection) {
      if (set.add(o)) {
        owner.markAsModified();
        changed = true;
      }
    }
    return changed;
  }

  public void clear() {
    if (!set.isEmpty()) {
      owner.markAsModified();
    }
    set.clear();
  }

  public boolean contains(Object o) {
    return set.contains(o);
  }

  public boolean containsAll(Collection<?> collection) {
    return set.containsAll(collection);
  }

  public boolean isEmpty() {
    return set.isEmpty();
  }

  public Iterator<E> iterator() {
    return new ModifyAwareIterator<>(owner, set.iterator());
  }

  public boolean remove(Object o) {
    if (set.remove(o)) {
      owner.markAsModified();
      return true;
    }
    return false;
  }

  public boolean removeAll(Collection<?> collection) {
    boolean changed = false;
    for (Object element : collection) {
      if (set.remove(element)) {
        owner.markAsModified();
        changed = true;
      }
    }
    return changed;
  }

  public boolean retainAll(Collection<?> collection) {
    boolean changed = false;
    Iterator<?> it = set.iterator();
    while (it.hasNext()) {
      Object o = it.next();
      if (!collection.contains(o)) {
        it.remove();
        owner.markAsModified();
        changed = true;
      }
    }
    return changed;
  }

  public int size() {
    return set.size();
  }

  public Object[] toArray() {
    return set.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return set.toArray(a);
  }
}
