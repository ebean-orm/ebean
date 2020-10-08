package io.ebeaninternal.server.type;

import io.ebeaninternal.server.core.timezone.DataTimeZone;
import io.ebeaninternal.server.type.bindcapture.BindCapture;
import io.ebeaninternal.server.type.bindcapture.BindCaptureStatement;

/**
 * Special DataBind used to capture bind values for obtaining explain plans.
 */
public class DataBindCapture extends DataBind {

  private final BindCaptureStatement captureStatement;

  /**
   * Create given the dataTimeZone in use.
   */
  public static DataBindCapture of(DataTimeZone dataTimeZone) {
    return new DataBindCapture(dataTimeZone, new BindCaptureStatement());
  }

  private DataBindCapture(DataTimeZone dataTimeZone, BindCaptureStatement pstmt) {
    super(dataTimeZone, pstmt, null);
    this.captureStatement = pstmt;
  }

  /**
   * Return the bind values capture used to obtain explain plans.
   */
  public BindCapture bindCapture() {
    return captureStatement.bindCapture();
  }

  @Override
  public void setArray(String arrayType, Object[] elements) {
    captureStatement.setArray(++pos, arrayType, elements);
  }

}
