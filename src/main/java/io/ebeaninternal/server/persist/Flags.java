package io.ebeaninternal.server.persist;

/**
 * Flags used in persistence.
 * <p>
 * Allows passing of flag state when recursively persisting.
 */
public final class Flags {

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
   * Indicates Normal insert or update (not forced).
   */
  public static final int NORMAL = 0x00000010;

  /**
   * No flags set.
   */
  public static final int ZERO = 0;

  public static final int PUBLISH_RECURSE = PUBLISH + RECURSE;

  private static final int PUBLISH_MERGE_NORMAL = PUBLISH + MERGE + NORMAL;

  private static final int INSERT_NORMAL = INSERT + NORMAL;

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
   * Return true if part of a Merge or Publish or Normal (bean state matches persist).
   */
  public static boolean isPublishMergeOrNormal(int state) {
    return (state & PUBLISH_MERGE_NORMAL) != 0;
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
    return set(state, INSERT);
  }

  /**
   * Insert flag and normal in that bean is in NEW state (for insert).
   */
  public static int setInsertNormal(int state) {
    return set(state, INSERT_NORMAL);
  }

  /**
   * Parent was not inserted.
   */
  public static int setUpdate(int state) {
    return unset(state, INSERT);
  }

  /**
   * Not Insert and normal in that bean is in LOADED state (for update).
   */
  public static int setUpdateNormal(int state) {
    state &= ~INSERT;
    state |= NORMAL;
    return state;
  }

  /**
   * Set Recurse flag.
   */
  public static int setRecurse(int state) {
    return set(state, RECURSE);
  }

  public static int unsetRecuse(int state) {
    return unset(state, RECURSE);
  }

  /**
   * Set Publish flag.
   */
  public static int setPublish(int state) {
    return set(state, PUBLISH);
  }

  public static int unsetPublish(int state) {
    return unset(state, PUBLISH);
  }

  /**
   * Set Merge flag.
   */
  public static int setMerge(int state) {
    return set(state, MERGE);
  }

  public static int unsetMerge(int state) {
    return unset(state, MERGE);
  }

  private static int set(int state, int flag) {
    return (state |= flag);
  }

  private static int unset(int state, int flag) {
    return state &= ~flag;
  }
}
