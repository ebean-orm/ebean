package com.avaje.ebeaninternal.server.type;

import java.util.Set;

/**
 * Wraps a Set for the purposes of detecting modifications.
 */
public class ModifyAwareSet<E> extends ModifyAwareCollection<E> implements Set<E> {

  /**
   * Create with an Owner that is notified of modifications.
   */
  public ModifyAwareSet(ModifyAwareOwner owner, Set<E> s) {
    super(owner, s);
  }
  
}
