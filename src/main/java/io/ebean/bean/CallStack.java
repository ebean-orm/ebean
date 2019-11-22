package io.ebean.bean;

import java.io.Serializable;
import java.util.Arrays;

import static io.ebean.util.EncodeB64.enc;

/**
 * Represent the call stack (stack trace elements).
 * <p>
 * Used with a query to identify a CallStackQuery for AutoTune automatic query
 * tuning.
 * </p>
 * <p>
 * This is used so that a single query called from different methods can be
 * tuned for each different call stack.
 * </p>
 * <p>
 * Note the call stack is trimmed to remove the common ebean internal elements.
 * </p>
 */
public final class CallStack implements Serializable, CallOrigin {

  private static final long serialVersionUID = -8590644046907438579L;

  private static final String NEWLINE = "\n";

  private final String zeroHash;
  private final String pathHash;
  private final StackTraceElement[] callStack;
  private final int hc;

  public CallStack(StackTraceElement[] callStack, int zeroHash, int pathHash) {
    this.callStack = callStack;
    this.zeroHash = enc(zeroHash);
    this.pathHash = enc(pathHash);
    this.hc = computeHashCode();
  }

  private int computeHashCode() {
    int hc = 0;
    for (StackTraceElement element : callStack) {
      hc = 92821 * hc + element.hashCode();
    }
    return hc;
  }

  @Override
  public String toString() {
    return zeroHash + ":" + pathHash + ":" + callStack[0];
  }

  @Override
  public int hashCode() {
    return hc;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof CallStack)) {
      return false;
    }
    CallStack e = (CallStack) obj;
    return Arrays.equals(callStack, e.callStack);
  }

  /**
   * Return the first element of the call stack.
   */
  @Override
  public String getTopElement() {
    return callStack[0].toString();
  }

  /**
   * Return the call stack lines appended with the given newLine string.
   */
  @Override
  public String getFullDescription() {
    StringBuilder sb = new StringBuilder(400);
    for (int i = 0; i < callStack.length; i++) {
      if (i > 0) {
        sb.append(NEWLINE);
      }
      sb.append(callStack[i].toString());
    }
    return sb.toString();
  }

  @Override
  public String getOriginKey(int queryHash) {
    return enc(queryHash) + "." + zeroHash + "." + pathHash;
  }

}
