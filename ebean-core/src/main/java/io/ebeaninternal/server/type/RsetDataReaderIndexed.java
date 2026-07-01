package io.ebeaninternal.server.type;

import io.ebeaninternal.server.core.timezone.DataTimeZone;

import java.sql.ResultSet;

/**
 * A DataReader with indexed positions for the properties to read specifically for RawSql use.
 */
public final class RsetDataReaderIndexed extends RsetDataReader {

  private final int[] rsetIndexPositions;

  public RsetDataReaderIndexed(boolean unmodifiable, DataTimeZone dataTimeZone, ResultSet rset, int[] rsetIndexPositions) {
    super(unmodifiable, dataTimeZone, rset);
    this.rsetIndexPositions = rsetIndexPositions;
  }

  @Override
  protected int pos() {
    int i = pos++;
    return rsetIndexPositions[i];
  }

}
