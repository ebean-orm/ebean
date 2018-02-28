package io.ebeaninternal.server.type;

import io.ebeaninternal.server.core.BasicTypeConverter;

import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

public class ScalarTypeUUIDBinary extends ScalarTypeUUIDBase {

  private final boolean optimized;


  protected ScalarTypeUUIDBinary(boolean optimized) {
    super(false, Types.BINARY);
    this.optimized = optimized;
  }

  @Override
  public int getLength() {
    return 16;
  }

  @Override
  public Object toJdbcType(Object value) {
    return convertToBytes(value, optimized);
  }

  @Override
  public UUID toBeanType(Object value) {
    if (value instanceof byte[]) {
      return convertFromBytes((byte[]) value, optimized);
    } else {
      return BasicTypeConverter.toUUID(value, optimized);
    }
  }

  @Override
  public void bind(DataBind b, UUID value) throws SQLException {
    if (value == null) {
      b.setNull(Types.BINARY);
    } else {
      b.setBytes(convertToBytes(value, optimized));
    }
  }

  @Override
  public UUID read(DataReader dataReader) throws SQLException {
    byte[] bytes = dataReader.getBytes();
    if (bytes == null) {
      return null;
    } else {
      return convertFromBytes(bytes, optimized);
    }
  }

  /**
   * Convert from byte[] to UUID.
   */
  public static UUID convertFromBytes(byte[] bytes, boolean optimized) {

    int usableBytes = Math.min(bytes.length, 16);

    // Need exactly 16 bytes - pad the input if not enough bytes are provided
    // Use provided bytes in the least significant position; if more than 16 bytes are given,
    // then use the first 16 bytes from the array;
    byte[] barr = new byte[16];
    for (int i = 15, j = usableBytes - 1; j >= 0; i--, j--) {
      barr[i] = bytes[j];
    }

    long msb;
    if (optimized) {
      msb = ((long)barr[4] << 56) +          // XXXXXXXX-____-____-...
            ((long)(barr[5] & 255) << 48) +  // -> put at end 4..7 of buf
            ((long)(barr[6] & 255) << 40) +
            ((long)(barr[7] & 255) << 32) +
            ((long)(barr[2] & 255) << 24) +  // ________-XXXX-____-...
            ((barr[3] & 255) << 16) +        // put at 2..3 in buf
            ((barr[0] & 255) <<  8) +        // ________-____-XXXX-...
            ((barr[1] & 255) <<  0);         // put at 0..1 in buf
    } else {
      msb = ((long)barr[0] << 56) +         // XXXXXXXX-____-____-...
            ((long)(barr[1] & 255) << 48) +
            ((long)(barr[2] & 255) << 40) +
            ((long)(barr[3] & 255) << 32) +
            ((long)(barr[4] & 255) << 24) + // ________-XXXX-____-...
            ((barr[5] & 255) << 16) +
            ((barr[6] & 255) <<  8) +       // ________-____-XXXX-...
            ((barr[7] & 255) <<  0);
    }
    long lsb = ((long)barr[8] << 56) +
        ((long)(barr[9] & 255) << 48) +
        ((long)(barr[10] & 255) << 40) +
        ((long)(barr[11] & 255) << 32) +
        ((long)(barr[12] & 255) << 24) +
        ((barr[13] & 255) << 16) +
        ((barr[14] & 255) <<  8) +
        ((barr[15] & 255) <<  0);

    return new UUID(msb, lsb);
  }

  /**
   * Convert from UUID to byte[].
   */
  public static byte[] convertToBytes(Object value, boolean optimized) {


    UUID uuid = (UUID) value;
    byte[] ret = new byte[16];
    long l = uuid.getMostSignificantBits();

    if (optimized) {
      ret[0] = (byte) (l >>> 8); // was 6/7
      ret[1] = (byte) (l >>> 0);

      ret[2] = (byte) (l >>> 24); // was 4/5
      ret[3] = (byte) (l >>> 16);

      ret[4] = (byte) (l >>> 56); // was 0..3
      ret[5] = (byte) (l >>> 48);
      ret[6] = (byte) (l >>> 40);
      ret[7] = (byte) (l >>> 32);
    } else {
      ret[0] = (byte) (l >>> 56);
      ret[1] = (byte) (l >>> 48);
      ret[2] = (byte) (l >>> 40);
      ret[3] = (byte) (l >>> 32);
      ret[4] = (byte) (l >>> 24);
      ret[5] = (byte) (l >>> 16);
      ret[6] = (byte) (l >>> 8);
      ret[7] = (byte) (l >>> 0);
    }
    l = uuid.getLeastSignificantBits();
    ret[8] = (byte) (l >>> 56);
    ret[9] = (byte) (l >>> 48);
    ret[10] = (byte) (l >>> 40);
    ret[11] = (byte) (l >>> 32);
    ret[12] = (byte) (l >>> 24);
    ret[13] = (byte) (l >>> 16);
    ret[14] = (byte) (l >>> 8);
    ret[15] = (byte) (l >>> 0);

    return ret;
  }

}
