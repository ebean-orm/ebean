package com.avaje.ebeaninternal.server.type;

import java.util.Currency;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

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
  public Object jsonRead(JsonParser ctx, Event event) {
    return parse(ctx.getString());
  }

  public void jsonWrite(JsonGenerator ctx, String name, Object value) {
    ctx.write(name, formatValue((Currency)value));
  }
}
