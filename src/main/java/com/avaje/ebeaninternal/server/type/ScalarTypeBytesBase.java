package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;

import com.avaje.ebean.text.TextException;

/**
 * Base type for binary types.
 */
public abstract class ScalarTypeBytesBase extends ScalarTypeBase<byte[]> {
	
	protected ScalarTypeBytesBase(boolean jdbcNative, int jdbcType) {
		super(byte[].class, jdbcNative, jdbcType);
	}

    public Object convertFromBytes(byte[] bytes) {
        return bytes;
    }

    public byte[] convertToBytes(Object value) {
        return (byte[]) value;
    }
    
	public void bind(DataBind b, byte[] value) throws SQLException {
		if (value == null) {
			b.setNull(jdbcType);
		} else {
			b.setBytes(value);
		}
	}

	public Object toJdbcType(Object value) {
		return value;
	}

	public byte[] toBeanType(Object value) {
		return (byte[])value;
	}

	
	public String formatValue(byte[] t) {
        throw new TextException("Not supported");
    }

    public byte[] parse(String value) {
		throw new TextException("Not supported");
	}
	
	public byte[] parseDateTime(long systemTimeMillis) {
		throw new TextException("Not supported");
	}

	public boolean isDateTimeCapable() {
		return false;
	}

    public Object readData(DataInput dataInput) throws IOException {
        if (!dataInput.readBoolean()) {
            return null;
        } else {
            int len = dataInput.readInt();
            byte[] buf = new byte[len];
            dataInput.readFully(buf, 0, buf.length);
            return buf;
        }
    }

    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        if (v ==  null){
            dataOutput.writeBoolean(false);    
        } else {
            byte[] bytes = convertToBytes(v);
            dataOutput.writeInt(bytes.length);
            dataOutput.write(bytes);
        }
    }
	
	
	
}
