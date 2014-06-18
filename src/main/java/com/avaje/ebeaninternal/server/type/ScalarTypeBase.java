package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.text.json.WriteJsonBuffer;



/**
 * Base ScalarType object.
 */
public abstract class ScalarTypeBase<T> implements ScalarType<T> {

	protected final Class<T> type;
	protected final boolean jdbcNative;
	protected final int jdbcType;
	
	public ScalarTypeBase(Class<T> type, boolean jdbcNative, int jdbcType) {
		this.type = type;
		this.jdbcNative = jdbcNative;
		this.jdbcType = jdbcType;
	}
	
	/**
	 * Default implementation of mutable false.
	 */
	@Override
  public boolean isMutable() {
    return false;
  }

	/**
	 * Default to true.
	 */
  @Override
  public boolean isDirty(Object value) {
    return true;
  }

  /**
	 * Just return 0.
	 */
	public int getLength() {
		return 0;
	}

	public boolean isJdbcNative() {
		return jdbcNative;
	}
	
	public int getJdbcType() {
		return jdbcType;
	}
	
	public Class<T> getType() {
		return type;
	}

	@SuppressWarnings("unchecked")
    public String format(Object v) {
        return formatValue((T)v);
    }

    /**
	 * Return true if the value is null.
	 */
	public boolean isDbNull(Object value) {
		return value == null;
	}

	/**
	 * Returns the value that was passed in.
	 */
	public Object getDbNullValue(Object value) {
		return value;
	}

    public void loadIgnore(DataReader dataReader) {  
        dataReader.incrementPos(1);
    }
	
    public void accumulateScalarTypes(String propName, CtCompoundTypeScalarList list) {
        list.addScalarType(propName, this);
    }

    public void jsonWrite(WriteJsonBuffer buffer, T value, JsonValueAdapter ctx) {
    	String v = jsonToString(value, ctx);
    	buffer.append(v);
    }
    
    public String jsonToString(T value, JsonValueAdapter ctx) {
        return formatValue(value);
    }
    
    public T jsonFromString(String value, JsonValueAdapter ctx) {
        return parse(value);
    }
    
}
