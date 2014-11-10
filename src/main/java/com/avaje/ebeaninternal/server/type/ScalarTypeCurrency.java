package com.avaje.ebeaninternal.server.type;

import java.io.IOException;
import java.util.Currency;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * ScalarType for java.util.Currency which converts to and from a VARCHAR database column.
 */
public class ScalarTypeCurrency extends ScalarTypeBaseVarchar<Currency> {

	public ScalarTypeCurrency() {
		super(Currency.class);
	}
	
	@Override
    public int getLength() {
        return 3;
    }

    @Override
    public Currency convertFromDbString(String dbValue) {
	    return Currency.getInstance(dbValue);
    }

    @Override
    public String convertToDbString(Currency beanValue) {
        return ((Currency)beanValue).getCurrencyCode();
    }

	public String formatValue(Currency v) {
        return v.toString();
    }

    public Currency parse(String value) {
		return Currency.getInstance(value);
	}
    
  @Override
  public Object jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    return parse(ctx.getValueAsString());
  }

  public void jsonWrite(JsonGenerator ctx, String name, Object value) throws IOException {
    ctx.writeStringField(name, formatValue((Currency)value));
  }
}
