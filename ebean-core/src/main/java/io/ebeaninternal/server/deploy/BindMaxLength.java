package io.ebeaninternal.server.deploy;

import io.ebean.core.type.InputStreamInfo;

import java.nio.charset.StandardCharsets;

/**
 * The max length check on bind values.
 */
public interface BindMaxLength {

  /**
   * Return a UTF8 based implementation.
   */
  static BindMaxLength ofUtf8() {
    return new UTF8();
  }

  /**
   * Return a standard implementation.
   */
  static BindMaxLength ofStandard() {
    return new Standard();
  }

  /**
   * Return the length of the object.
   */
  long length(int dbLength, Object obj);

  /**
   * Length check based on UTF8 bytes.
   */
  final class UTF8 implements BindMaxLength {

    @Override
    public long length(int dbLength, Object obj) {
      if (obj instanceof String) {
        String s = (String) obj;
        int stringLength = s.length();
        if (stringLength > dbLength) {
          return stringLength;
        } else if (stringLength * 4 <= dbLength) {
          return -1;
        } else {
          return s.getBytes(StandardCharsets.UTF_8).length;
        }

      } else if (obj instanceof byte[]) {
        return ((byte[]) obj).length;
      } else if (obj instanceof InputStreamInfo) {
        return ((InputStreamInfo) obj).length();
      } else {
        return -1;
      }
    }
  }

  /**
   * Standard string length implementation.
   */
  final class Standard implements BindMaxLength {

    @Override
    public long length(int dbLength, Object obj) {
      if (obj instanceof String) {
        return ((String) obj).length();
      } else if (obj instanceof byte[]) {
        return ((byte[]) obj).length;
      } else if (obj instanceof InputStreamInfo) {
        return ((InputStreamInfo) obj).length();
      } else {
        return -1;
      }
    }
  }
}
