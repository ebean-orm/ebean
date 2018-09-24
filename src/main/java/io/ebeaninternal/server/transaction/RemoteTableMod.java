package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.BinaryReadContext;
import io.ebeaninternal.api.BinaryWritable;
import io.ebeaninternal.api.BinaryWriteContext;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class RemoteTableMod implements BinaryWritable {

  private final long timestamp;

  private final Set<String> tables;

  public RemoteTableMod(long timestamp, Set<String> tables) {
    this.timestamp = timestamp;
    this.tables = tables;
  }

  @Override
  public String toString() {
    return "TableMod[" + timestamp + "; " + tables + "]";
  }

  public long getTimestamp() {
    return timestamp;
  }

  public Set<String> getTables() {
    return tables;
  }

  public static RemoteTableMod readBinaryMessage(BinaryReadContext dataInput) throws IOException {

    long timestamp = dataInput.readLong();
    int count = dataInput.readInt();

    Set<String> tables = new LinkedHashSet<>();
    for (int i = 0; i < count; i++) {
      tables.add(dataInput.readUTF());
    }
    return new RemoteTableMod(timestamp, tables);
  }

  @Override
  public void writeBinary(BinaryWriteContext out) throws IOException {
    DataOutputStream os = out.start(TYPE_TABLEMOD);
    os.writeLong(timestamp);
    os.writeInt(tables.size());
    for (String table : tables) {
      os.writeUTF(table);
    }
  }

}
