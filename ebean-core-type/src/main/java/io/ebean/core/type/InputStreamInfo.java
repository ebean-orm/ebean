package io.ebean.core.type;

import java.io.InputStream;

/**
 * Helper to transports length info of DataBind.setBinaryStream(stream, length) to BindValidation
 *
 * @author Roland Praml, FOCONIS AG
 */
public class InputStreamInfo {
  private final InputStream stream;

  private final long length;

  public InputStreamInfo(InputStream stream, long length) {
    this.stream = stream;
    this.length = length;
  }

  public InputStream stream() {
    return stream;
  }

  public long length() {
    return length;
  }
}
