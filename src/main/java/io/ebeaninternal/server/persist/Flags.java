package io.ebeaninternal.server.persist;

/**
 * Flags used in persistence.
 * <p>
 * Allows passing of flag state when recursively persisting.
 */
public class Flags {

  /**
   * Indicates the bean is being inserted.
   */
  public static final int INSERT = 0x00000001;

  /**
   * Indicates persist cascade.
   */
  public static final int RECURSE = 0x00000002;

  /**
   * Indicates Publish mode.
   */
  public static final int PUBLISH = 0x00000004;

  /**
   * Indicates Merge mode.
   */
  public static final int MERGE = 0x00000008;

  /**
   * No flags set.
   */
  public static final int ZERO = 0;

  public static final int PUBLISH_RECURSE = PUBLISH + RECURSE;

  private static final int PUBLISH_MERGE = PUBLISH + MERGE;

  /**
   * Return true if the bean is being inserted.
   */
  public static boolean isInsert(int state) {
    return isSet(state, INSERT);
  }

  /**
   * Return true if persist cascading.
   */
  public static boolean isRecurse(int state) {
    return isSet(state, RECURSE);
  }

  /**
   * Return true if part of a Publish.
   */
  public static boolean isPublish(int state) {
    return isSet(state, PUBLISH);
  }

  /**
   * Return true if part of a Merge.
   */
  public static boolean isMerge(int state) {
    return isSet(state, PUBLISH);
  }

  /**
   * Return true if part of a Merge or Publish.
   */
  public static boolean isPublishOrMerge(long state) {
    return (state & PUBLISH_MERGE) != 0;
  }

  /**
   * Return true if the given flag is set.
   */
  public static boolean isSet(int state, int flag) {
    return (state & flag) == flag;
  }

  /**
   * Set Insert flag.
   */
  public static int setInsert(int state) {
    return set(state, INSERT, true);
  }

  /**
   * Parent was not inserted.
   */
  public static int unsetInsert(int state) {
    return set(state, INSERT, false);
  }

  /**
   * Set Recurse flag.
   */
  public static int setRecurse(int state) {
    return set(state, RECURSE, true);
  }

  public static int unsetRecuse(int state) {
    return set(state, RECURSE, false);
  }

  /**
   * Set Publish flag.
   */
  public static int setPublish(int state) {
    return set(state, PUBLISH, true);
  }

  public static int unsetPublish(int state) {
    return set(state, PUBLISH, false);
  }

  /**
   * Set Merge flag.
   */
  public static int setMerge(int state) {
    return set(state, MERGE, true);
  }

  public static int unsetMerge(int state) {
    return set(state, MERGE, false);
  }

  private static int set(int state, int flag, boolean setFlag) {
    if (setFlag) {
      return (state |= flag);
    } else {
      return state &= ~flag;
    }
  }
}
