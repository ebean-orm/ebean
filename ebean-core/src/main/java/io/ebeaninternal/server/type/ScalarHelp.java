package io.ebeaninternal.server.type;

import java.io.DataOutput;
import java.io.IOException;

/**
 * Utility methods for ScalarTypes.
 */
public class ScalarHelp {

  /**
   * Write the string content as UTF with the proceeding boolean true indicating the non-null.
   */
  public static void writeUTF(DataOutput dataOutput, String content) throws IOException {
    dataOutput.writeBoolean(true);
    dataOutput.writeUTF(content);
  }
}
