package com.avaje.ebeaninternal.server.type;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.SQLException;

public interface DataReader {

    public void close() throws SQLException;

    public boolean next() throws SQLException;

    public void resetColumnPosition();
    
    public void incrementPos(int increment);

    public byte[] getBinaryBytes() throws SQLException;

    public byte[] getBlobBytes() throws SQLException;

    public String getStringFromStream() throws SQLException;

    public String getStringClob() throws SQLException;
    
    public String getString() throws SQLException;

    public Boolean getBoolean() throws SQLException;

    public Byte getByte() throws SQLException;

    public Short getShort() throws SQLException;

    public Integer getInt() throws SQLException;

    public Long getLong() throws SQLException;

    public Float getFloat() throws SQLException;

    public Double getDouble() throws SQLException;

    public byte[] getBytes() throws SQLException;

    public java.sql.Date getDate() throws SQLException;

    public java.sql.Time getTime() throws SQLException;

    public java.sql.Timestamp getTimestamp() throws SQLException;

    public BigDecimal getBigDecimal() throws SQLException;

    public Array getArray() throws SQLException;
    
    public Object getObject() throws SQLException;

    public InputStream getBinaryStream() throws SQLException;
}
