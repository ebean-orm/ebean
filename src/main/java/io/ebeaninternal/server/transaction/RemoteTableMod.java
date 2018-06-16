package io.ebeaninternal.server.transaction;

import io.ebeaninternal.server.cluster.binarymessage.BinaryMessage;
import io.ebeaninternal.server.cluster.binarymessage.BinaryMessageList;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class RemoteTableMod {

  private final long timestamp;

  private final Set<String> tables;

  public RemoteTableMod(long timestamp, Set<String> tables) {
    this.timestamp = timestamp;
    this.tables = tables;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public Set<String> getTables() {
    return tables;
  }

  public static RemoteTableMod readBinaryMessage(DataInput dataInput) throws IOException {

    long timestamp = dataInput.readLong();
    int count = dataInput.readInt();

    Set<String> tables = new LinkedHashSet<>();
    for (int i = 0; i < count; i++) {
      tables.add(dataInput.readUTF());
    }
    return new RemoteTableMod(timestamp, tables);
  }

  public void writeBinary(BinaryMessageList msgList) throws IOException {
    BinaryMessage msg = new BinaryMessage(tables.size() * 20  + 30);
    DataOutputStream os = msg.getOs();
    os.writeInt(BinaryMessage.TYPE_TABLEMOD);
    os.writeLong(timestamp);
    os.writeInt(tables.size());
    for (String table : tables) {
      os.writeUTF(table);
    }
    os.close();
    msgList.add(msg);
  }
}
