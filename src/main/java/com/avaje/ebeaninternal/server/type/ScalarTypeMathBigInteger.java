package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Types;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for java.math.BigInteger.
 */
public class ScalarTypeMathBigInteger extends ScalarTypeBase<BigInteger> {

	public ScalarTypeMathBigInteger() {
		super(BigInteger.class, false, Types.BIGINT);
	}
	
	public void bind(DataBind b, BigInteger value) throws SQLException {
		if (value == null){
			b.setNull(Types.BIGINT);
		} else {
			b.setLong(value.longValue());
		}
	}

	public BigInteger read(DataReader dataReader) throws SQLException {
		
		Long l = dataReader.getLong();
		if (l == null){
			return null;
		}
		return new BigInteger(String.valueOf(l));
	}
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toLong(value);
	}

	public BigInteger toBeanType(Object value) {
		return BasicTypeConverter.toMathBigInteger(value);
	}

	
	public String formatValue(BigInteger v) {
        return v.toString();
    }

    public BigInteger parse(String value) {
		return new BigInteger(value);
	}

	public BigInteger parseDateTime(long systemTimeMillis) {
		return BigInteger.valueOf(systemTimeMillis);
	}

	public boolean isDateTimeCapable() {
		return true;
	}
    
    public Object readData(DataInput dataInput) throws IOException {
        if (!dataInput.readBoolean()) {
            return null;
        } else {
            long val = dataInput.readLong();
            return Long.valueOf(val);
        }
    }

    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        
        Long value = (Long)v;
        if (value == null){
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            dataOutput.writeLong(value.longValue());            
        }
    }

}
