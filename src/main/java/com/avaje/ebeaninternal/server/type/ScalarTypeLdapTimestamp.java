package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.PersistenceException;

import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.text.json.WriteJsonBuffer;

/**
 * Wrapper type that wraps all java.sql.Timestamp types for LDAP.
 * 
 * @author rbygrave
 */
public class ScalarTypeLdapTimestamp<T> implements ScalarType<T> {

    private static final String timestampLDAPFormat = "yyyyMMddHHmmss'Z'";

    private final ScalarType<T> baseType;

    public ScalarTypeLdapTimestamp(ScalarType<T> baseType) {
        this.baseType = baseType;
    }
    
    public T toBeanType(Object value) {
        if (value == null){
            return null;
        }
        if (value instanceof String == false){
            String msg = "Expecting a String type but got "+value.getClass()+" value["+value+"]";
            throw new PersistenceException(msg);
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(timestampLDAPFormat);
            Date date = sdf.parse((String)value);
            
            return baseType.parseDateTime(date.getTime());
            
        } catch (Exception e) {
            String msg = "Error parsing LDAP timestamp "+value;
            throw new PersistenceException(msg, e);
        }
    }

    public Object toJdbcType(Object value) {
        
        if (value == null){
            return null;
        }
        
        Object ts = baseType.toJdbcType(value);
        if (ts instanceof java.sql.Timestamp == false){
            String msg = "Expecting a Timestamp type but got "+value.getClass()+" value["+value+"]";
            throw new PersistenceException(msg);
        }
        
        Timestamp t = (Timestamp)ts;
        SimpleDateFormat sdf = new SimpleDateFormat(timestampLDAPFormat);
        return sdf.format(t);
    }

    
    public void bind(DataBind b, T value) throws SQLException {
        baseType.bind(b, value);
    }

    public int getJdbcType() {
        return Types.VARCHAR;
    }

    public int getLength() {
        return baseType.getLength();
    }

    public Class<T> getType() {
        return baseType.getType();
    }

    public boolean isDateTimeCapable() {
        return baseType.isDateTimeCapable();
    }

    public boolean isJdbcNative() {
        return false;
    }

    public void loadIgnore(DataReader dataReader) {
        baseType.loadIgnore(dataReader);
    }

    public String format(Object v) {
        return baseType.format(v);
    }

    public String formatValue(T t) {
        return baseType.formatValue(t);
    }

    public T parse(String value) {
        return baseType.parse(value);
    }
    
    public T parseDateTime(long systemTimeMillis) {
        return baseType.parseDateTime(systemTimeMillis);
    }

    public T read(DataReader dataReader) throws SQLException {
        return baseType.read(dataReader);
    }

    public void accumulateScalarTypes(String propName, CtCompoundTypeScalarList list) {
        baseType.accumulateScalarTypes(propName, list);
    }

    public String jsonToString(T value, JsonValueAdapter ctx) {
        return baseType.jsonToString(value, ctx);
    }

    public void jsonWrite(WriteJsonBuffer buffer, T value, JsonValueAdapter ctx) {
	    baseType.jsonWrite(buffer, value, ctx);
    }

	public T jsonFromString(String value, JsonValueAdapter ctx) {
        return baseType.jsonFromString(value, ctx);
    }
    
    public Object readData(DataInput dataInput) throws IOException {
        return baseType.readData(dataInput);
    }

    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        baseType.writeData(dataOutput, v);
    }
}
