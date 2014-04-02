package com.avaje.ebeaninternal.server.text.json;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.text.TextException;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonElement;
import com.avaje.ebean.text.json.JsonReadOptions;
import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.type.EscapeJson;
import com.avaje.ebeaninternal.util.ParamTypeHelper;
import com.avaje.ebeaninternal.util.ParamTypeHelper.ManyType;
import com.avaje.ebeaninternal.util.ParamTypeHelper.TypeInfo;

/**
 * Default implementation of JsonContext.
 * 
 * @author rbygrave
 */
public class DJsonContext implements JsonContext {

    private final SpiEbeanServer server;
    
    private final JsonValueAdapter dfltValueAdapter;
        
    private final boolean dfltPretty;
    
    public DJsonContext(SpiEbeanServer server, JsonValueAdapter dfltValueAdapter, boolean dfltPretty){
        this.server = server;
        this.dfltValueAdapter = dfltValueAdapter;
        this.dfltPretty = dfltPretty;
    }

    public boolean isSupportedType(Type genericType) {
        return server.isSupportedType(genericType);
    }

    private ReadJsonSource createReader(Reader jsonReader) {
        return new ReadJsonSourceReader(jsonReader, 256, 512);
    }
    
    public <T> T toBean(Class<T> cls, String json){
        return toBean(cls, new ReadJsonSourceString(json), null);
    }
    
    public <T> T toBean(Class<T> cls, Reader jsonReader) {
        return toBean(cls, createReader(jsonReader), null);
    }
    
    public <T> T toBean(Class<T> cls, String json, JsonReadOptions options){
        return toBean(cls, new ReadJsonSourceString(json), options);
    }    

    public <T> T toBean(Class<T> cls, Reader jsonReader, JsonReadOptions options) {
        return toBean(cls, createReader(jsonReader), options);
    }

    private <T> T toBean(Class<T> cls, ReadJsonSource src, JsonReadOptions options){

        BeanDescriptor<T> d = getDecriptor(cls);
        ReadJsonContext ctx = new ReadJsonContext(src, dfltValueAdapter, options);
        return d.jsonReadBean(ctx, null);
    }

    public <T> List<T> toList(Class<T> cls, String json){
        return toList(cls, new ReadJsonSourceString(json), null);        
    }

    public <T> List<T> toList(Class<T> cls, String json, JsonReadOptions options){
        return toList(cls, new ReadJsonSourceString(json), options);        
    }
    
    public <T> List<T> toList(Class<T> cls, Reader jsonReader){
        return toList(cls, createReader(jsonReader), null);        
    }

    public <T> List<T> toList(Class<T> cls, Reader jsonReader, JsonReadOptions options){
        return toList(cls, createReader(jsonReader), options);        
    }
    
    private <T> List<T> toList(Class<T> cls, ReadJsonSource src, JsonReadOptions options){

        try {
            BeanDescriptor<T> d = getDecriptor(cls);
    
            List<T> list = new ArrayList<T>();
            
            ReadJsonContext ctx = new ReadJsonContext(src, dfltValueAdapter, options);
            ctx.readArrayBegin();
            do {
                T bean = d.jsonReadBean(ctx, null);
                if (bean != null){
                    list.add(bean);
                }
                if (!ctx.readArrayNext()){
                    break;
                }
            } while(true);
            
            return list;
        } catch (RuntimeException e){
            throw new TextException("Error parsing "+src, e);
        }
    }
    
    
    public Object toObject(Type genericType, String json, JsonReadOptions options) {
        
        TypeInfo info = ParamTypeHelper.getTypeInfo(genericType);
        Class<?> beanType = info.getBeanType();
        if (JsonElement.class.isAssignableFrom(beanType)){
        	return InternalJsonParser.parse(json);
        }

        ManyType manyType = info.getManyType();
        switch (manyType) {
        case NONE:
            return toBean(info.getBeanType(), json, options);            

        case LIST:
            return toList(info.getBeanType(), json, options);
            
        default:
            String msg = "ManyType "+manyType+" not supported yet";
            throw new TextException(msg);
        }
    }
    
    public Object toObject(Type genericType, Reader json, JsonReadOptions options) {
            	
        TypeInfo info = ParamTypeHelper.getTypeInfo(genericType);
        Class<?> beanType = info.getBeanType();
        if (JsonElement.class.isAssignableFrom(beanType)){
        	return InternalJsonParser.parse(json);
        }
        
        ManyType manyType = info.getManyType();
        switch (manyType) {
        case NONE:
            return toBean(info.getBeanType(), json, options);            

        case LIST:
            return toList(info.getBeanType(), json, options);
            
        default:
            String msg = "ManyType "+manyType+" not supported yet";
            throw new TextException(msg);
        }
    }


    public void toJsonWriter(Object o, Writer writer) {
        toJsonWriter(o, writer, dfltPretty, null, null);
    }

    public void toJsonWriter(Object o, Writer writer, boolean pretty) {
        toJsonWriter(o, writer, pretty, null, null);
    }    

    public void toJsonWriter(Object o, Writer writer, boolean pretty, JsonWriteOptions options){
        toJsonWriter(o, writer, pretty, null, null);
    }
    
    public void toJsonWriter(Object o, Writer writer, boolean pretty, JsonWriteOptions options, String callback) {
        toJsonInternal(o, new WriteJsonBufferWriter(writer), pretty, options, callback);
    }

    public String toJsonString(Object o){
        return toJsonString(o, dfltPretty, null);
    }

    public String toJsonString(Object o, boolean pretty){
        return toJsonString(o, pretty, null);
    }

    public String toJsonString(Object o, boolean pretty, JsonWriteOptions options){
        return toJsonString(o, pretty, options, null);
    }
    
    public String toJsonString(Object o, boolean pretty, JsonWriteOptions options, String callback){
        WriteJsonBufferString b = new WriteJsonBufferString();
        toJsonInternal(o, b, pretty, options, callback);
        return b.getBufferOutput();
    }

    @SuppressWarnings("unchecked")
    private void toJsonInternal(Object o, WriteJsonBuffer buffer, boolean pretty, JsonWriteOptions options, String requestCallback){

    	if (o == null){
    		buffer.append("null");
    	} else if (o instanceof Number) {
    		buffer.append(o.toString());
    	} else if (o instanceof Boolean) {
    		buffer.append(o.toString());
    	} else if (o instanceof String) {
    		EscapeJson.escapeQuote(o.toString(), buffer);
    	} else if (o instanceof JsonElement) {
    	
    	} else if (o instanceof Map<?,?>){
            toJsonFromMap((Map<Object,Object>)o, buffer, pretty, options, requestCallback);
            
    	} else if (o instanceof Collection<?>){
            toJsonFromCollection((Collection<?>)o, buffer, pretty, options, requestCallback);
            
        } else {
            BeanDescriptor<?> d = getDecriptor(o.getClass());
            WriteJsonContext ctx = new WriteJsonContext(buffer, pretty, dfltValueAdapter, options, requestCallback, server);
            d.jsonWrite(ctx, (EntityBean)o);
            ctx.end();
        }
    }
   

    private <T> void toJsonFromCollection(Collection<T> c, WriteJsonBuffer buffer, boolean pretty, JsonWriteOptions options, String requestCallback){
        
        Iterator<T> it = c.iterator();
        if (!it.hasNext()){
        	buffer.append("[]");
          return;
        }
        
        WriteJsonContext ctx = new WriteJsonContext(buffer, pretty, dfltValueAdapter, options, requestCallback, server);

        Object o = it.next();
        BeanDescriptor<?> d = getDecriptor(o.getClass());

        ctx.appendArrayBegin();
        d.jsonWrite(ctx, (EntityBean)o);
        while (it.hasNext()) {
            ctx.appendComma();
            T t = it.next();        
            d.jsonWrite(ctx, (EntityBean)t);
        }
        ctx.appendArrayEnd();
        ctx.end();
    }

    private void toJsonFromMap(Map<Object,Object> map, WriteJsonBuffer buffer, boolean pretty, JsonWriteOptions options, String requestCallback){
        
        if (map.isEmpty()){
        	buffer.append("{}");
            return;
        }
        
        WriteJsonContext ctx = new WriteJsonContext(buffer, pretty, dfltValueAdapter, options, requestCallback, server);

        Set<Entry<Object,Object>> entrySet = map.entrySet();
        Iterator<Entry<Object, Object>> it = entrySet.iterator();
        
        Entry<Object, Object> entry = it.next();

        ctx.appendObjectBegin();
        toJsonMapKey(buffer, false, entry.getKey());
        toJsonMapValue(buffer, pretty, options, requestCallback, entry.getValue());
        
        while (it.hasNext()) {
            entry = it.next();
            ctx.appendComma();
            toJsonMapKey(buffer, pretty, entry.getKey());
            toJsonMapValue(buffer, pretty, options, requestCallback, entry.getValue());
        }
        ctx.appendObjectEnd();
        ctx.end();
    }

	private void toJsonMapKey(WriteJsonBuffer buffer, boolean pretty, Object key) {
		if (pretty){
			buffer.append("\n");
		}
        buffer.append("\"");
        buffer.append(key.toString());
        buffer.append("\":");
	}
	
	private void toJsonMapValue(WriteJsonBuffer buffer, boolean pretty, JsonWriteOptions options, String requestCallback,
            Object value) {
		
        if (value == null){
        	buffer.append("null");
        } else {
            toJsonInternal(value, buffer, pretty, options, requestCallback);        	
        }
    }
    
    private <T> BeanDescriptor<T> getDecriptor(Class<T> cls) {
        BeanDescriptor<T> d = server.getBeanDescriptor(cls);
        if (d == null){
            String msg = "No BeanDescriptor found for "+cls;
            throw new RuntimeException(msg);
        }
        return d;
    }
}
