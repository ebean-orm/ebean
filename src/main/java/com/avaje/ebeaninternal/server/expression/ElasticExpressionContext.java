package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.OrderBy;
import com.avaje.ebean.plugin.BeanType;
import com.avaje.ebean.plugin.ExpressionPath;
import com.avaje.ebeaninternal.server.query.SplitName;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Context for writing elastic search expressions.
 */
public class ElasticExpressionContext {

  public static final String MUST = "must";
  public static final String SHOULD = "should";
  public static final String MUST_NOT = "must_not";
  public static final String BOOL = "bool";
  public static final String TERM = "term";
  public static final String RANGE = "range";
  public static final String TERMS = "terms";
  public static final String IDS = "ids";
  public static final String VALUES = "values";
  public static final String PREFIX = "prefix";
  public static final String MATCH = "match";
  public static final String WILDCARD = "wildcard";
  public static final String EXISTS = "exists";
  public static final String FIELD = "field";

  private final JsonGenerator json;

  private final BeanType<?> desc;

  private String currentNestedPath;

  public ElasticExpressionContext(JsonGenerator json, BeanType<?> desc) {
    this.json = json;
    this.desc = desc;
  }

  /**
   * Return the JsonGenerator.
   */
  public JsonGenerator json() {
    return json;
  }

  /**
   * Flush the JsonGenerator buffer.
   */
  public void flush() throws IOException {
    endNested();
    json.flush();
  }

  /**
   * Return true if the path contains a many.
   */
  public boolean containsMany(String path) {
    ExpressionPath elPath = desc.getExpressionPath(path);
    return elPath == null || elPath.containsMany();
  }

  /**
   * Return an associated 'raw' property given the property name.
   */
  private String rawProperty(String propertyName) {
    return desc.docStore().rawProperty(propertyName);
  }

  public void writeBoolStart(boolean conjunction) throws IOException {
    writeBoolStart((conjunction) ? MUST : SHOULD);
  }

  public void writeBoolMustStart() throws IOException {
    writeBoolStart(MUST);
  }

  public void writeBoolMustNotStart() throws IOException {
    writeBoolStart(MUST_NOT);
  }

  private void writeBoolStart(String type) throws IOException {
    endNested();
    json.writeStartObject();
    json.writeObjectFieldStart(BOOL);
    json.writeArrayFieldStart(type);
  }

  public void writeBoolEnd() throws IOException {
    json.writeEndArray();
    json.writeEndObject();
    json.writeEndObject();
  }

  public void writeTerm(String propertyName, Object value) throws IOException {

    writeRawType(TERM, rawProperty(propertyName), value);
  }

  public void writeRange(String propertyName, String rangeType, Object value) throws IOException {

    prepareNestedPath(propertyName);
    json.writeStartObject();
    json.writeObjectFieldStart(RANGE);
    json.writeObjectFieldStart(rawProperty(propertyName));
    json.writeFieldName(rangeType);
    json.writeObject(value);
    json.writeEndObject();
    json.writeEndObject();
    json.writeEndObject();
  }

  public void writeRange(String propertyName, Op lowOp, Object valueLow, Op highOp, Object valueHigh) throws IOException {

    prepareNestedPath(propertyName);
    json.writeStartObject();
    json.writeObjectFieldStart(RANGE);
    json.writeObjectFieldStart(rawProperty(propertyName));
    json.writeFieldName(lowOp.docExp());
    json.writeObject(valueLow);
    json.writeFieldName(highOp.docExp());
    json.writeObject(valueHigh);
    json.writeEndObject();
    json.writeEndObject();
    json.writeEndObject();
  }

  public void writeTerms(String propertyName, Object[] values) throws IOException {

    prepareNestedPath(propertyName);
    json.writeStartObject();
    json.writeObjectFieldStart(TERMS);
    json.writeArrayFieldStart(rawProperty(propertyName));
    for (Object value : values) {
      json.writeObject(value);
    }
    json.writeEndArray();
    json.writeEndObject();
    json.writeEndObject();
  }


  public void writeIds(List<?> idList) throws IOException {

    endNested();
    json.writeStartObject();
    json.writeObjectFieldStart(IDS);
    json.writeArrayFieldStart(VALUES);
    for (Object id : idList) {
      json.writeObject(id);
    }
    json.writeEndArray();
    json.writeEndObject();
    json.writeEndObject();
  }

  public void writeId(Object value) throws IOException {

    List<Object> ids = new ArrayList<Object>(1);
    ids.add(value);
    writeIds(ids);
  }

  public void writeSuffix(String propertyName, String value) {
    throw new IllegalArgumentException("Not implemented yet. Could search for a mapped 'reversed' property and do prefix query");
  }

  public void writePrefix(String propertyName, String value) throws IOException {
    // use analysed field
    prepareNestedPath(propertyName);
    writeRawType(PREFIX, propertyName, value);
  }

  public void writeMatch(String propertyName, String value) throws IOException {
    // use analysed field
    prepareNestedPath(propertyName);
    writeRawType(MATCH, propertyName, value);
  }

  public void writeWildcard(String propertyName, String value) throws IOException {
    prepareNestedPath(propertyName);
    writeRawType(WILDCARD, propertyName, value);
  }

  public void writeRaw(String jsonExpression) throws IOException {
    json.writeRaw(jsonExpression);
  }

  public void writeExists(boolean notNull, String propertyName) throws IOException {

    prepareNestedPath(propertyName);
    if (!notNull) {
      writeBoolMustNotStart();
    }
    writeExists(propertyName);
    if (!notNull) {
      writeBoolEnd();
    }
  }

  private void writeExists(String propertyName) throws IOException {
    writeRawType(EXISTS, FIELD, propertyName);
  }

  private void writeRawType(String type, String propertyName, Object value) throws IOException {

    prepareNestedPath(propertyName);
    json.writeStartObject();
    json.writeObjectFieldStart(type);
    json.writeFieldName(propertyName);
    json.writeObject(value);
    json.writeEndObject();
    json.writeEndObject();
  }



  public void writeSimple(Op type, String propertyName, Object value) throws IOException {

    prepareNestedPath(propertyName);
    switch (type) {
      case EQ:
        writeTerm(propertyName, value);
        break;
      case NOT_EQ:
        writeBoolMustNotStart();
        writeTerm(propertyName, value);
        writeBoolEnd();
        break;
      case EXISTS:
        writeExists(true, propertyName);
        break;
      case NOT_EXISTS:
        writeExists(false, propertyName);
        break;
      case BETWEEN:
        throw new IllegalStateException("BETWEEN Not expected in SimpleExpression?");

      default:
        writeRange(propertyName, type.docExp(), value);
    }

  }

  /**
   * Write the query sort.
   */
  public <T> void writeOrderBy(OrderBy<T> orderBy) throws IOException {

    if (orderBy != null && !orderBy.isEmpty()) {
      json.writeArrayFieldStart("sort");
      for (OrderBy.Property property : orderBy.getProperties()) {
        json.writeStartObject();
        json.writeObjectFieldStart(rawProperty(property.getProperty()));
        json.writeStringField("order", property.isAscending() ? "asc" : "desc");
        json.writeEndObject();
        json.writeEndObject();
      }
      json.writeEndArray();
    }
  }

  private void prepareNestedPath(String propName) throws IOException {
    ExpressionPath exprPath = desc.getExpressionPath(propName);
    if (exprPath != null && exprPath.containsMany()) {
      String[] manyPath = SplitName.splitBegin(propName);
      startNested(manyPath[0]);
    } else {
      endNested();
    }
  }

  private void startNested(String nestedPath) throws IOException {

    if (currentNestedPath != null) {
      if (currentNestedPath.equals(nestedPath)) {
        // just add to currentNestedPath
        return;
      } else {
        endNested();
      }
    }
    currentNestedPath = nestedPath;

    json.writeStartObject();
    json.writeObjectFieldStart("nested");
    json.writeStringField("path", nestedPath);
    json.writeFieldName("filter");
  }

  private void endNested() throws IOException {
    if (currentNestedPath != null) {
      currentNestedPath = null;
      //json.writeEndObject();
      json.writeEndObject();
      json.writeEndObject();
    }
  }
}
