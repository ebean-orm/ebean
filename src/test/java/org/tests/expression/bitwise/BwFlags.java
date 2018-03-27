package org.tests.expression.bitwise;

public class BwFlags {

  public static final int NOTHING = 0;

  /**
   * Indicates the bean is being inserted.
   */
  public static final int HAS_COLOUR = 0x00000001;

  /**
   * Indicates persist cascade.
   */
  public static final int HAS_SIZE = 0x00000002;

  /**
   * Indicates Publish mode.
   */
  public static final int HAS_BULK = 0x00000004;

  /**
   * Return true if part of a Merge or Publish.
   */
  public static boolean isColour(long state) {
    return (state & HAS_COLOUR) != 0;
  }

  /**
   * Return true if the given flag is set.
   */
  public static boolean isSet(int state, int flag) {
    return (state & flag) == flag;
  }

  public static int setHasColour(int state) {
    return set(state, HAS_COLOUR, true);
  }

  /**
   * Parent was not inserted.
   */
  public static int unsetHasColour(int state) {
    return set(state, HAS_COLOUR, false);
  }

  /**
   * Set Recurse flag.
   */
  public static int setHasSize(int state) {
    return set(state, HAS_SIZE, true);
  }

  public static int unsetHasSize(int state) {
    return set(state, HAS_SIZE, false);
  }

  /**
   * Set Publish flag.
   */
  public static int setHasBulk(int state) {
    return set(state, HAS_BULK, true);
  }

  public static int unsetHasBulk(int state) {
    return set(state, HAS_BULK, false);
  }


  private static int set(int state, int flag, boolean setFlag) {
    if (setFlag) {
      return (state |= flag);
    } else {
      return state &= ~flag;
    }
  }

}
