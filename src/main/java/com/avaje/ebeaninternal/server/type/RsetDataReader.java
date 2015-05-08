package com.avaje.ebeaninternal.server.type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import com.avaje.ebeaninternal.server.core.Message;

public class RsetDataReader implements DataReader {

    private static final int bufferSize = 512;

    static final int clobBufferSize = 512;
    
    static final int stringInitialSize = 512;

    private final ResultSet rset;
    
    protected int pos;
    
    public RsetDataReader(ResultSet rset) {
        this.rset = rset;
    }

    public void close() throws SQLException {
        rset.close();
    }

    public boolean next() throws SQLException {
        return rset.next();
    }
    
    public void resetColumnPosition() {
        pos = 0;
    }

    public void incrementPos(int increment){
        pos += increment;
    }
    
    protected int pos() {
        return ++pos;
    }

    public Array getArray() throws SQLException {
        return rset.getArray(pos());
    }


    public InputStream getAsciiStream() throws SQLException {
        return rset.getAsciiStream(pos());
    }

    public Object getObject() throws SQLException {
      return rset.getObject(pos());
    }

    public BigDecimal getBigDecimal() throws SQLException {
        return rset.getBigDecimal(pos());
    }


    public InputStream getBinaryStream() throws SQLException {
        return rset.getBinaryStream(pos());
    }

    public Boolean getBoolean() throws SQLException {
        boolean v = rset.getBoolean(pos());
        if (rset.wasNull()){
            return null;
        }
        return Boolean.valueOf(v);
    }

    public Byte getByte() throws SQLException {
        byte v = rset.getByte(pos());
        if (rset.wasNull()){
            return null;
        }
        return Byte.valueOf(v);
    }

    public byte[] getBytes() throws SQLException {
        return rset.getBytes(pos());
    }

    public Date getDate() throws SQLException {
        return rset.getDate(pos());
    }

    public Double getDouble() throws SQLException {
        double v = rset.getDouble(pos());
        if (rset.wasNull()){
            return null;
        }
        return Double.valueOf(v);
    }

    public Float getFloat() throws SQLException {
        float v = rset.getFloat(pos());
        if (rset.wasNull()){
            return null;
        }
        return Float.valueOf(v);
    }

    public Integer getInt() throws SQLException {
        int v = rset.getInt(pos());
        if (rset.wasNull()){
            return null;
        }
        return Integer.valueOf(v);
    }


    public Long getLong() throws SQLException {
        long v = rset.getLong(pos());
        if (rset.wasNull()){
            return null;
        }
        return Long.valueOf(v);
    }


    public Ref getRef() throws SQLException {
        return rset.getRef(pos());
    }


    public Short getShort() throws SQLException {
        short s = rset.getShort(pos());
        if (rset.wasNull()){
            return null;
        }
        return Short.valueOf(s);
    }


    public String getString() throws SQLException {
        return rset.getString(pos());
    }


    public Time getTime() throws SQLException {
        return rset.getTime(pos());
    }


    public Timestamp getTimestamp() throws SQLException {   
        return rset.getTimestamp(pos());
    }
    
    public String getStringFromStream() throws SQLException {
        Reader reader = rset.getCharacterStream(pos());
        if (reader == null) {
            return null;
        }
        return readStringLob(reader);
    }
    
    public String getStringClob() throws SQLException {

        Clob clob = rset.getClob(pos());
        if (clob == null) {
            return null;
        }
        Reader reader = clob.getCharacterStream();
        if (reader == null) {
            return null;
        }
        return readStringLob(reader);
    }
    
    protected String readStringLob(Reader reader) throws SQLException {

        char[] buffer = new char[clobBufferSize];
        int readLength = 0;
        StringBuilder out = new StringBuilder(stringInitialSize);
        try {
            while ((readLength = reader.read(buffer)) != -1) {
                out.append(buffer, 0, readLength);
            }
            reader.close();
        } catch (IOException e) {
            throw new SQLException(Message.msg("persist.clob.io", e.getMessage()));
        }

        return out.toString();
    }

    public byte[] getBinaryBytes() throws SQLException {
        InputStream in = rset.getBinaryStream(pos());   
        return getBinaryLob(in);
    }
    
    public byte[] getBlobBytes() throws SQLException {
        Blob blob = rset.getBlob(pos());
        if (blob == null) {
            return null;
        }
        InputStream in = blob.getBinaryStream();
        return getBinaryLob(in);
    }
    
    protected byte[] getBinaryLob(InputStream in) throws SQLException {

        try {
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
        
            byte[] buf = new byte[bufferSize];
            int len;
            while ((len = in.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, len);
            }
            byte[] data = out.toByteArray();
        
            if (data.length == 0) {
                data = null;
            }
            in.close();
            out.close();
            return data;
        
        } catch (IOException e) {
            throw new SQLException(e.getClass().getName() + ":" + e.getMessage());
        }
    }

}
