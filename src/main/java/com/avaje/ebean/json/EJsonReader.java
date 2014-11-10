package com.avaje.ebean.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

class EJsonReader {

  static JsonFactory json = new JsonFactory();
  
  @SuppressWarnings("unchecked")
  static Map<String, Object> parseObject(String json) throws IOException  {
    return (Map<String, Object>) parse(json);
  }
  
  @SuppressWarnings("unchecked")
  static Map<String, Object> parseObject(Reader reader) throws IOException {
    return (Map<String, Object>) parse(reader, false);
  }
  
  @SuppressWarnings("unchecked")
  static Map<String, Object> parseObject(JsonParser parser) throws IOException {
    return (Map<String, Object>) parse(parser, false);
  }

  @SuppressWarnings("unchecked")
  static List<Object> parseList(String json) throws IOException {
    return (List<Object>) parse(json);
  }
  
  @SuppressWarnings("unchecked")
  static List<Object> parseList(Reader reader) throws IOException {
    return (List<Object>) parse(reader, false);
  }
  
  @SuppressWarnings("unchecked")
  static List<Object> parseList(JsonParser parser) throws IOException {
    return (List<Object>) parse(parser, false);
  }

  static Object parse(String json) throws IOException {
    return parse(new StringReader(json), false);
  }

  static Object parse(Reader reader, boolean partial) throws IOException {
    return parse(json.createParser(reader), partial);
  }

  static Object parse(JsonParser parser, boolean partial) throws IOException {
    return new EJsonReader(parser, partial).parseJson();
  }

  private final JsonParser parser;
  
  //private final boolean partial;
  
  private int depth;
  
  private Stack stack;

  private Context currentContext;


  EJsonReader(JsonParser parser, boolean partial) {
    this.parser = parser;
    //this.partial = partial;
  }

  private void startArray() {
    depth++;
    stack.push(currentContext);
    currentContext = new ArrayContext();
  }

  private void startObject() {
    depth++;
    stack.push(currentContext);
    currentContext = new ObjectContext();
  }

  private void endArray() {
    end();
  }

  private void endObject() {
    end();
  }

  private void end() {
    depth--;
    if (!stack.isEmpty()) {
      
      //if (currentContext != null) {
      //  Object value = currentContext.getValue();
      //}
      currentContext = stack.pop(currentContext);
    }
  }

  private void setValue(Object value) {
    currentContext.setValue(value);
  }

  private void setValueNull() {
    currentContext.setValueNull();
  }

  private Object parseJson() {

    try {
      JsonToken token = parser.nextToken();
      if (JsonToken.VALUE_NULL == token) {
        return null;
      }
    
////    if (jp.nextToken() != JsonToken.START_OBJECT) {
////      throw new IOException("Expected data to start with an Object");
////    }
////    TwitterEntry result = new TwitterEntry();
////    // Iterate over object fields:
////    while (jp.nextToken() != JsonToken.END_OBJECT) {
////     String fieldName = jp.getCurrentName();
////     // Let's move to value
////     jp.nextToken();
////     
//    if (!parser.hasNext()) {
//      return null;
//    }
//    
//   
//    Object simpleValue = getSimpleValue(JsonToken);
//    if (simpleValue != null) {
//      // it is a simple string, number or boolean
//      return simpleValue;
//    }

      stack = new Stack();
      // it is a object or array, process the first JsonToken
      processJsonToken(token);

      // process the rest of the object or array
      while (depth > 0) {
        token = parser.nextToken();
        processJsonToken(token);
      }

      return currentContext.getValue();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
//  /**
//   * See if the JsonToken is a value rather than object or array.
//   * <p>
//   * If just a value then return that value else return null.
//   * @throws IOException 
//   */
//  private Object getSimpleValue(JsonToken JsonToken) throws IOException {
//    
//    switch (JsonToken) {
//    case VALUE_STRING:
//      return parser.getValueAsString();
//
//    case VALUE_NUMBER_INT:
//      return parser.getLongValue();
//
//    case VALUE_NUMBER_FLOAT:
//      return parser.getDecimalValue();
//
//    case VALUE_TRUE:
//      return Boolean.TRUE;
//
//    case VALUE_FALSE:
//      return Boolean.FALSE;
//
//    default:
//      return null;
//    }
//  }

  /**
   * Process the JsonToken for objects and arrays.
   */
  private void processJsonToken(JsonToken token) throws IOException {
    switch (token) {

    case START_ARRAY:
      startArray();
      break;

    case START_OBJECT:
      startObject();
      break;

    case FIELD_NAME:
      currentContext.setKey(parser.getCurrentName());
      break;

    case VALUE_STRING:
      setValue(parser.getValueAsString());
      break;

    case VALUE_NUMBER_INT:
      setValue(parser.getLongValue());
      break;
      
    case VALUE_NUMBER_FLOAT:
      setValue(parser.getDecimalValue());
      break;

    case VALUE_TRUE:
      setValue(Boolean.TRUE);
      break;

    case VALUE_FALSE:
      setValue(Boolean.FALSE);
      break;

    case VALUE_NULL:
      setValueNull();
      break;

    case END_OBJECT:
      endObject();
      break;

    case END_ARRAY:
      endArray();
      break;

    default:
      break;
    }
  }
  
  private static final class Stack {
    
    private Context head;
    
    private void push(Context context) {
      if (context != null) {
        context.next = head;
        head = context;
      }
    }

    private Context pop(Context endingContext) {
      if (head == null) {
        throw new NoSuchElementException();
      }
      Context temp = head;
      head = head.next;
      temp.popContext(endingContext);
      return temp;
    }

    private boolean isEmpty() {
      return head == null;
    }
  }

  private static abstract class Context {
    Context next;
    abstract void popContext(Context temp);
    abstract Object getValue();
    abstract void setKey(String key);
    abstract void setValue(Object value);
    abstract void setValueNull();
  }
  
  private static class ObjectContext extends Context {
        
    private final Map<String, Object> map = new LinkedHashMap<String, Object>();

    private String key;

    public void popContext(Context temp) {
      setValue(temp.getValue());
    }

    Object getValue() {
      return map;
    }

    void setKey(String key) {
      this.key = key;
    }

    void setValue(Object value) {
      map.put(key, value);
    }

    void setValueNull() {
      map.put(key, null);
    }
  }

  private static class ArrayContext extends Context {
    
    private final List<Object> values = new ArrayList<Object>();

    public void popContext(Context temp) {
      values.add(temp.getValue());
    }

    Object getValue() {
      return values;
    }
    
    void setValue(Object value) {
      values.add(value);
    }

    void setValueNull() {
      // ignore
    }
    void setKey(String key) {  
      // not expected
    }
  }

}
