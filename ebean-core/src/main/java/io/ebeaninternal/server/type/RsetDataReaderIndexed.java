package io.ebeaninternal.server.type;

import io.ebeaninternal.server.core.timezone.DataTimeZone;

import java.sql.ResultSet;

/**
 * A DataReader with indexed positions for the properties to read specifically for RawSql use.
 */
public class RsetDataReaderIndexed extends RsetDataReader {

  private final int[] rsetIndexPositions;

  public RsetDataReaderIndexed(DataTimeZone dataTimeZone, ResultSet rset, int[] rsetIndexPositions) {
    super(dataTimeZone, rset);
    this.rsetIndexPositions = rsetIndexPositions;
  }

  @Override
  protected int pos() {
    int i = pos++;
    return rsetIndexPositions[i];
  }

}
