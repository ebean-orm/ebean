package com.avaje.ebeaninternal.server.type;

import java.net.MalformedURLException;
import java.net.URL;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.avaje.ebean.text.TextException;

/**
 * ScalarType for java.net.URL which converts to and from a VARCHAR database column.
 */
public class ScalarTypeURL extends ScalarTypeBaseVarchar<URL> {

	public ScalarTypeURL() {
		super(URL.class);
	}
	
	
	@Override
    public URL convertFromDbString(String dbValue) {
	    try {
            return new URL(dbValue);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error with URL ["+dbValue+"] "+e);
        }
    }

    @Override
    public String convertToDbString(URL beanValue) {
        return formatValue(beanValue);
    }

	public String formatValue(URL v) {
        return v.toString();
    }

    public URL parse(String value) {
		try {
			return new URL(value);
		} catch (MalformedURLException e) {
			throw new TextException(e);
		}
	}
    
    @Override
    public Object jsonRead(JsonParser ctx, Event event) {
      return parse(ctx.getString());
    }

}
