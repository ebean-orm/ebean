package io.ebeaninternal.server.bind.capture;

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
public final class BindCaptureStatement extends BindCaptureStatementBase implements PreparedStatement {

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
  public void setBoolean(int parameterIndex, boolean value) {
    capture.add(new BindCaptureTypes.Boolean(parameterIndex, value));
  }

  @Override
  public void setByte(int parameterIndex, byte value) {
    capture.add(new BindCaptureTypes.Byte(parameterIndex, value));
  }

  @Override
  public void setShort(int parameterIndex, short value) {
    capture.add(new BindCaptureTypes.TShort(parameterIndex, value));
  }

  @Override
  public void setInt(int parameterIndex, int value) {
    capture.add(new BindCaptureTypes.TInt(parameterIndex, value));
  }

  @Override
  public void setLong(int parameterIndex, long value) {
    capture.add(new BindCaptureTypes.TLong(parameterIndex, value));
  }

  @Override
  public void setFloat(int parameterIndex, float value) {
    capture.add(new BindCaptureTypes.TFloat(parameterIndex, value));
  }

  @Override
  public void setDouble(int parameterIndex, double value) {
    capture.add(new BindCaptureTypes.TDouble(parameterIndex, value));
  }

  @Override
  public void setBigDecimal(int parameterIndex, BigDecimal value) {
    capture.add(new BindCaptureTypes.TBigDecimal(parameterIndex, value));
  }

  @Override
  public void setString(int parameterIndex, String value) {
    capture.add(new BindCaptureTypes.TString(parameterIndex, value));
  }

  @Override
  public void setBytes(int parameterIndex, byte[] value) {
    capture.add(new BindCaptureTypes.Bytes(parameterIndex, value));
  }

  @Override
  public void setDate(int parameterIndex, Date value) {
    capture.add(new BindCaptureTypes.TDate(parameterIndex, value));
  }

  @Override
  public void setTime(int parameterIndex, Time value) {
    capture.add(new BindCaptureTypes.TTime(parameterIndex, value));
  }

  @Override
  public void setTimestamp(int parameterIndex, Timestamp value) {
    capture.add(new BindCaptureTypes.TTimestamp(parameterIndex, value, null));
  }

  @Override
  public void setTimestamp(int parameterIndex, Timestamp value, Calendar cal) {
    capture.add(new BindCaptureTypes.TTimestamp(parameterIndex, value, cal));
  }

  @Override
  public void setObject(int parameterIndex, Object value) {
    capture.add(new BindCaptureTypes.TObject(parameterIndex, value));
  }

  @Override
  public void setBinaryStream(int parameterIndex, InputStream value, int length) {
    capture.add(new BindCaptureTypes.BinaryStream(parameterIndex));
  }

  @Override
  public void setCharacterStream(int parameterIndex, Reader value, int length) {
    capture.add(new BindCaptureTypes.CharacterStream(parameterIndex));
  }

}
