package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.text.TextException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for streaming between a File and the database.
 */
public class ScalarTypeFile extends ScalarTypeBase<File> {

  private static Logger logger = LoggerFactory.getLogger(ScalarTypeFile.class);

  private final String prefix;

  private final String suffix;

  private final File directory;

  private final int bufferSize;

  /**
   * Construct with reasonable defaults of Blob and 8096 buffer size.
   */
  public ScalarTypeFile() {
    this(Types.BLOB, "db-", null, null, 8096);
  }

  /**
   * Create the ScalarTypeFile.
   */
  public ScalarTypeFile(int jdbcType, String prefix, String suffix, File directory, int bufferSize) {
    super(File.class, false, jdbcType);
    this.prefix = prefix;
    this.suffix = suffix;
    this.directory = directory;
    this.bufferSize = bufferSize;
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
  public File read(DataReader dataReader) throws SQLException {

    InputStream is = dataReader.getBinaryStream();
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
  public void bind(DataBind b, File value) throws SQLException {
    if (value == null) {
      b.setNull(jdbcType);
    } else {
      try {
        // stream from our file to the db
        InputStream fi = getInputStream(value);
        b.setBlob(fi, value.length());
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
  public void jsonWrite(JsonGenerator ctx, String name, File value) throws IOException {
    ctx.writeFieldName(name);
    InputStream is = getInputStream(value);
    ctx.writeBinary(is, (int) value.length());
  }

  @Override
  public File jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    File tempFile = File.createTempFile(prefix, suffix, directory);
    OutputStream os = getOutputStream(tempFile);
    ctx.readBinaryValue(os);
    os.flush();
    os.close();
    return tempFile;
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
  public File convertFromMillis(long systemTimeMillis) {
    throw new TextException("Not supported");
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public File readData(DataInput dataInput) throws IOException {
    // skip reading large file
    return null;
  }

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
          logger.error("Error when closing outputstream", e);
        }
      }
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          logger.error("Error when closing inputstream ", e);
        }
      }
    }
  }

}
