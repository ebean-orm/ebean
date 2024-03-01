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
        return utf8String(dbLength, s);
      } else if (obj instanceof byte[]) {
        return ((byte[]) obj).length;
      } else if (obj instanceof InputStreamInfo) {
        return ((InputStreamInfo) obj).length();
      } else if ("org.postgresql.util.PGobject".equals(obj.getClass().getCanonicalName())) {
        String value = ((org.postgresql.util.PGobject) obj).getValue();
        return value == null ? -1 : utf8String(dbLength, value);
      } else {
        return -1;
      }
    }

    private static int utf8String(int dbLength, String s) {
      int stringLength = s.length();
      if (stringLength > dbLength) {
        return stringLength;
      } else if (stringLength * 4 <= dbLength) {
        return -1;
      } else {
        return s.getBytes(StandardCharsets.UTF_8).length;
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
      } else if ("org.postgresql.util.PGobject".equals(obj.getClass().getCanonicalName())) {
        String value = ((org.postgresql.util.PGobject) obj).getValue();
        return value == null ? -1 : value.length();
      } else {
        return -1;
      }
    }
  }
}
