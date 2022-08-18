package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.text.TextException;
import io.ebeaninternal.api.CoreLog;

import java.io.*;
import java.lang.System.Logger.Level;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for streaming between a File and the database.
 */
final class ScalarTypeFile extends ScalarTypeBase<File> {

  private final String prefix;
  private final String suffix;
  private final File directory;
  private final int bufferSize;

  /**
   * Construct with reasonable defaults of Blob and 8096 buffer size.
   */
  ScalarTypeFile() {
    this(Types.LONGVARBINARY, "db-", null, null, 8096);
  }

  /**
   * Create the ScalarTypeFile.
   */
  ScalarTypeFile(int jdbcType, String prefix, String suffix, File directory, int bufferSize) {
    super(File.class, false, jdbcType);
    this.prefix = prefix;
    this.suffix = suffix;
    this.directory = directory;
    this.bufferSize = bufferSize;
  }

  @Override
  public boolean isBinaryType() {
    return true;
  }

  private InputStream getInputStream(File value) throws IOException {
    FileInputStream fi = new FileInputStream(value);
    return new BufferedInputStream(fi, bufferSize);
  }

  private OutputStream getOutputStream(File value) throws IOException {
    FileOutputStream fi = new FileOutputStream(value);
    return new BufferedOutputStream(fi, bufferSize);
  }

  @Override
  public File read(DataReader reader) throws SQLException {
    InputStream is = reader.getBinaryStream();
    if (is == null) {
      return null;
    }
    try {
      // stream from db into our temp file
      File tempFile = File.createTempFile(prefix, suffix, directory);
      OutputStream os = getOutputStream(tempFile);
      pump(is, os);
      return tempFile;
    } catch (IOException e) {
      throw new SQLException("Error reading db file inputStream", e);
    }
  }


  @Override
  public void bind(DataBinder binder, File value) throws SQLException {
    if (value == null) {
      binder.setNull(jdbcType);
    } else {
      try {
        // stream from our file to the db
        InputStream fi = getInputStream(value);
        binder.setBinaryStream(fi, value.length());
      } catch (IOException e) {
        throw new SQLException("Error trying to set file inputStream", e);
      }
    }
  }

  @Override
  public Object toJdbcType(Object value) {
    return value;
  }

  @Override
  public File toBeanType(Object value) {
    return (File) value;
  }

  @Override
  public void jsonWrite(JsonGenerator writer, File value) throws IOException {
    InputStream is = getInputStream(value);
    writer.writeBinary(is, (int) value.length());
  }

  @Override
  public File jsonRead(JsonParser parser) throws IOException {
    File tempFile = File.createTempFile(prefix, suffix, directory);
    try (OutputStream os = getOutputStream(tempFile)) {
      parser.readBinaryValue(os);
      os.flush();
    }
    return tempFile;
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.BINARY;
  }

  @Override
  public String formatValue(File file) {
    throw new TextException("Not supported");
  }

  @Override
  public File parse(String value) {
    throw new TextException("Not supported");
  }

  @Override
  public File readData(DataInput dataInput) throws IOException {
    // skip reading large file
    return null;
  }

  @Override
  public void writeData(DataOutput dataOutput, File file) throws IOException {
    // skip writing large file
  }

  /**
   * Helper method to pump bytes from input to output.
   */
  public long pump(InputStream is, OutputStream out) throws IOException {

    long totalBytes = 0;
    InputStream input = null;
    OutputStream output = null;

    try {
      input = new BufferedInputStream(is, bufferSize);
      output = new BufferedOutputStream(out, bufferSize);

      byte[] buffer = new byte[bufferSize];
      int length;
      while (((length = input.read(buffer)) > 0)) {
        output.write(buffer, 0, length);
        totalBytes += length;
      }

      output.flush();

      return totalBytes;

    } finally {
      if (output != null) {
        try {
          output.close();
        } catch (IOException e) {
          CoreLog.log.log(Level.ERROR, "Error when closing outputStream", e);
        }
      }
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          CoreLog.log.log(Level.ERROR, "Error when closing inputStream ", e);
        }
      }
    }
  }

}
