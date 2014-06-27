package com.avaje.ebean.json;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

public class EJsonReader {

  @SuppressWarnings("unchecked")
  public static Map<String, Object> parseObject(String json) {
    return (Map<String, Object>) parse(json);
  }

  @SuppressWarnings("unchecked")
  public static List<Object> parseList(String json) {
    return (List<Object>) parse(json);
  }

  public static Object parse(String json) {
    return parse(new StringReader(json));
  }

  public static Object parse(Reader reader) {
    return parse(Json.createParser(reader));
  }

  public static Object parse(JsonParser parser) {
    return new EJsonReader(parser).parseJson();
  }

  private final JsonParser parser;
  
  private Stack stack;

  private Context currentContext;


  EJsonReader(JsonParser parser) {
    this.parser = parser;
  }

  private void startArray() {
    stack.push(currentContext);
    currentContext = new ArrayContext();
  }

  private void startObject() {
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
    if (!stack.isEmpty()) {
      currentContext = stack.pop();
    }
  }

  private void setValue(Object value) {
    currentContext.setValue(value);
  }

  private void setValueNull() {
    currentContext.setValueNull();
  }

  private Object parseJson() {

    if (!parser.hasNext()) {
      return null;
    }
    
    Event event = parser.next();
    if (Event.VALUE_NULL == event) {
      // it is just a null value
      return null;
    }
    Object simpleValue = getSimpleValue(event);
    if (simpleValue != null) {
      // it is a simple string, number or boolean
      return simpleValue;
    }
    
    stack = new Stack();
    // it is a object or array, process the first event
    processEvent(event);
    
    // process the rest of the object or array
    while (parser.hasNext()) {
      processEvent(parser.next());
    }

    return currentContext.getValue();
  }
  
  /**
   * See if the event is a value rather than object or array.
   * <p>
   * If just a value then return that value else return null.
   */
  private Object getSimpleValue(Event event) {
    
    switch (event) {
    case VALUE_STRING:
      return parser.getString();

    case VALUE_NUMBER:
      if (parser.isIntegralNumber()) {
        return parser.getLong();
      } else {
        return parser.getBigDecimal();
      }

    case VALUE_TRUE:
      return Boolean.TRUE;

    case VALUE_FALSE:
      return Boolean.FALSE;

    default:
      return false;
    }
  }

  /**
   * Process the event for objects and arrays.
   */
  private void processEvent(Event event) {
    switch (event) {

    case START_ARRAY:
      startArray();
      break;

    case START_OBJECT:
      startObject();
      break;

    case KEY_NAME:
      currentContext.setKey(parser.getString());
      break;

    case VALUE_STRING:
      setValue(parser.getString());
      break;

    case VALUE_NUMBER:
      if (parser.isIntegralNumber()) {
        setValue(parser.getLong());
      } else {
        setValue(parser.getBigDecimal());
      }
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

    private Context pop() {
      if (head == null) {
        throw new NoSuchElementException();
      }
      Context temp = head;
      head = head.next;
      return temp;
    }

    private boolean isEmpty() {
      return head == null;
    }
  }

  private static abstract class Context {
    Context next;
    abstract Object getValue();
    abstract void setKey(String key);
    abstract void setValue(Object value);
    abstract void setValueNull();
  }
  
  private static class ObjectContext extends Context {
        
    private final Map<String, Object> map = new LinkedHashMap<String, Object>();

    private String key;

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
