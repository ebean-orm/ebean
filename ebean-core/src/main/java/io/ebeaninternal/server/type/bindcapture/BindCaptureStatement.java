package io.ebeaninternal.server.type.bindcapture;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Special PreparedStatement used to capture bind values used to obtain explain plans.
 */
public class BindCaptureStatement extends BindCaptureStatementBase implements PreparedStatement {

  private final BindCapture capture = new BindCapture();

  /**
   * Return the captured bind values.
   */
  public BindCapture bindCapture() {
    return capture;
  }

  public void setArray(int parameterIndex, String arrayType, Object[] elements) {
    capture.add(new BindCaptureTypes.TArray(parameterIndex, arrayType, elements));
  }

  @Override
  public void setNull(int parameterIndex, int sqlType) {
    capture.add(new BindCaptureTypes.Null(parameterIndex, sqlType));
  }

  @Override
  public void setBoolean(int parameterIndex, boolean x) {
    capture.add(new BindCaptureTypes.Boolean(parameterIndex, x));
  }

  @Override
  public void setByte(int parameterIndex, byte x) {
    capture.add(new BindCaptureTypes.Byte(parameterIndex, x));
  }

  @Override
  public void setShort(int parameterIndex, short x) {
    capture.add(new BindCaptureTypes.TShort(parameterIndex, x));
  }

  @Override
  public void setInt(int parameterIndex, int x) {
    capture.add(new BindCaptureTypes.TInt(parameterIndex, x));
  }

  @Override
  public void setLong(int parameterIndex, long x) {
    capture.add(new BindCaptureTypes.TLong(parameterIndex, x));
  }

  @Override
  public void setFloat(int parameterIndex, float x) {
    capture.add(new BindCaptureTypes.TFloat(parameterIndex, x));
  }

  @Override
  public void setDouble(int parameterIndex, double x) {
    capture.add(new BindCaptureTypes.TDouble(parameterIndex, x));
  }

  @Override
  public void setBigDecimal(int parameterIndex, BigDecimal x) {
    capture.add(new BindCaptureTypes.TBigDecimal(parameterIndex, x));
  }

  @Override
  public void setString(int parameterIndex, String x) {
    capture.add(new BindCaptureTypes.TString(parameterIndex, x));
  }

  @Override
  public void setBytes(int parameterIndex, byte[] x) {
    capture.add(new BindCaptureTypes.Bytes(parameterIndex, x));
  }

  @Override
  public void setDate(int parameterIndex, Date x) {
    capture.add(new BindCaptureTypes.TDate(parameterIndex, x));
  }

  @Override
  public void setTime(int parameterIndex, Time x) {
    capture.add(new BindCaptureTypes.TTime(parameterIndex, x));
  }

  @Override
  public void setTimestamp(int parameterIndex, Timestamp x) {
    capture.add(new BindCaptureTypes.TTimestamp(parameterIndex, x, null));
  }

  @Override
  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) {
    capture.add(new BindCaptureTypes.TTimestamp(parameterIndex, x, cal));
  }

  @Override
  public void setObject(int parameterIndex, Object x) {
    capture.add(new BindCaptureTypes.TObject(parameterIndex, x));
  }

  @Override
  public void setBinaryStream(int parameterIndex, InputStream x, int length) {
    capture.add(new BindCaptureTypes.BinaryStream(parameterIndex));
  }

  @Override
  public void setCharacterStream(int parameterIndex, Reader reader, int length) {
    capture.add(new BindCaptureTypes.CharacterStream(parameterIndex));
  }

}
