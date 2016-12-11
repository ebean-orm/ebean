package io.ebeaninternal.server.type;

import io.ebeaninternal.server.core.timezone.DataTimeZone;

import java.sql.ResultSet;

/**
 * A DataReader with indexed positions for the properties to read specifically for RawSql use.
 */
public class RsetDataReaderIndexed extends RsetDataReader {

  private final int[] rsetIndexPositions;

  public RsetDataReaderIndexed(DataTimeZone dataTimeZone, ResultSet rset, int[] rsetIndexPositions, boolean rowNumberIncluded) {
    super(dataTimeZone, rset);
    if (!rowNumberIncluded) {
      this.rsetIndexPositions = rsetIndexPositions;
    } else {
      this.rsetIndexPositions = new int[rsetIndexPositions.length + 1];
      for (int i = 0; i < rsetIndexPositions.length; i++) {
        // increment all the column indexes by 1
        this.rsetIndexPositions[i + 1] = rsetIndexPositions[i] + 1;
      }
    }
  }

  @Override
  protected int pos() {
    int i = pos++;
    return rsetIndexPositions[i];
  }

}
