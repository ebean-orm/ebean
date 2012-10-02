package com.avaje.ebeaninternal.server.text.json;

import com.avaje.ebean.text.json.JsonElement;
import com.avaje.ebean.text.json.JsonElementArray;
import com.avaje.ebean.text.json.JsonElementBoolean;
import com.avaje.ebean.text.json.JsonElementNull;
import com.avaje.ebean.text.json.JsonElementNumber;
import com.avaje.ebean.text.json.JsonElementObject;
import com.avaje.ebean.text.json.JsonElementString;



public class ReadJsonRawReader {

	public static JsonElement readJsonElement(ReadJsonInterface ctx) {
		return new ReadJsonRawReader(ctx).readJsonElement();
	}
	
    private final ReadJsonInterface ctx;
    
    private ReadJsonRawReader(ReadJsonInterface ctx){
        this.ctx = ctx;
    }

    private JsonElement readJsonElement() {
        return readValue();
    }
    
    private JsonElement readValue() {
        
        ctx.ignoreWhiteSpace();
        
        char c = ctx.nextChar();
        
        switch (c) {
        case '{':
            return readObject();
            
        case '[':
            return readArray();
            
        case '"':
            return readString();

        default:
            return readUnquoted(c);
        }
    }

    private JsonElement readArray() {
        
        JsonElementArray a = new JsonElementArray();
                
        do {
            JsonElement value = readValue();
            a.add(value);            
            if (!ctx.readArrayNext()){
                break;
            }
        } while(true);
        
        return a;
    }

    private JsonElement readObject() {
        
        JsonElementObject o = new JsonElementObject();
        
        do {
            if (!ctx.readKeyNext()){
                break;
            } else {
                // we read a property key ...
                String key = ctx.getTokenKey();
                JsonElement value = readValue();
                
                o.put(key, value);
                
                if (!ctx.readValueNext()){
                    break;
                }
            } 
        } while(true);
        
        return o;
    }
    
    private JsonElement readString() {
        String s = ctx.readQuotedValue();
        return new JsonElementString(s);
    }
    
    private JsonElement readUnquoted(char c) {
        String s = ctx.readUnquotedValue(c);
        if ("null".equals(s)){
            return JsonElementNull.NULL;
 
        } else if ("true".equals(s)){
            return JsonElementBoolean.TRUE;
            
        } else if ("false".equals(s)) {
            return JsonElementBoolean.FALSE;
            
        }
        return new JsonElementNumber(s);
    }
}
