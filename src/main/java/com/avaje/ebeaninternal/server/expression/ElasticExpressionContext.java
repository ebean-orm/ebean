package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.OrderBy;
import com.avaje.ebean.plugin.BeanType;
import com.avaje.ebean.plugin.ExpressionPath;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebeaninternal.server.query.SplitName;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.StringWriter;
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

  private final JsonContext jsonContext;

  private final JsonGenerator json;

  private final StringWriter writer;

  private final BeanType<?> desc;

  private String currentNestedPath;

  /**
   * Construct given the JSON generator and root bean type.
   */
  public ElasticExpressionContext(JsonContext jsonContext, BeanType<?> desc) {
    this.jsonContext = jsonContext;
    this.desc = desc;
    this.writer = new StringWriter(200);
    this.json = jsonContext.createGenerator(writer);
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
  public String flush() throws IOException {
    endNested();
    json.flush();
    return writer.toString();
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
   * This just returns the original propertyName if no 'raw' property is mapped.
   */
  private String rawProperty(String propertyName) {
    return desc.docStore().rawProperty(propertyName);
  }

  /**
   * Start Bool MUST or SHOULD.
   * <p>
   * If conjunction is true then MUST(and) and if false is SHOULD(or).
   */
  public void writeBoolStart(boolean conjunction) throws IOException {
    writeBoolStart((conjunction) ? MUST : SHOULD);
  }

  /**
   * Start Bool MUST.
   */
  public void writeBoolMustStart() throws IOException {
    writeBoolStart(MUST);
  }

  /**
   * Start Bool MUST_NOT.
   */
  public void writeBoolMustNotStart() throws IOException {
    writeBoolStart(MUST_NOT);
  }

  /**
   * Start a Bool expression list with the given type (MUST, MUST_NOT, SHOULD).
   */
  private void writeBoolStart(String type) throws IOException {
    endNested();
    json.writeStartObject();
    json.writeObjectFieldStart(BOOL);
    json.writeArrayFieldStart(type);
  }

  /**
   * Write the end of a Bool expression list.
   */
  public void writeBoolEnd() throws IOException {
    json.writeEndArray();
    json.writeEndObject();
    json.writeEndObject();
  }

  /**
   * Write a term expression.
   */
  public void writeTerm(String propertyName, Object value) throws IOException {

    // prepareNested on propertyName and expression uses raw
    prepareNestedPath(propertyName);
    writeRawExpression(TERM, rawProperty(propertyName), value);
  }

  /**
   * Write a range expression with a single value.
   */
  public void writeRange(String propertyName, String rangeType, Object value) throws IOException {

    prepareNestedPath(propertyName);
    json.writeStartObject();
    json.writeObjectFieldStart(RANGE);
    json.writeObjectFieldStart(rawProperty(propertyName));
    json.writeFieldName(rangeType);
    jsonContext.writeScalar(json, value);
    json.writeEndObject();
    json.writeEndObject();
    json.writeEndObject();
  }

  /**
   * Write a range expression with a low and high value.
   */
  public void writeRange(String propertyName, Op lowOp, Object valueLow, Op highOp, Object valueHigh) throws IOException {

    prepareNestedPath(propertyName);
    json.writeStartObject();
    json.writeObjectFieldStart(RANGE);
    json.writeObjectFieldStart(rawProperty(propertyName));
    json.writeFieldName(lowOp.docExp());
    jsonContext.writeScalar(json, valueLow);
    json.writeFieldName(highOp.docExp());
    jsonContext.writeScalar(json, valueHigh);
    json.writeEndObject();
    json.writeEndObject();
    json.writeEndObject();
  }

  /**
   * Write a terms expression.
   */
  public void writeTerms(String propertyName, Object[] values) throws IOException {

    prepareNestedPath(propertyName);
    json.writeStartObject();
    json.writeObjectFieldStart(TERMS);
    json.writeArrayFieldStart(rawProperty(propertyName));
    for (Object value : values) {
      jsonContext.writeScalar(json, value);
    }
    json.writeEndArray();
    json.writeEndObject();
    json.writeEndObject();
  }

  /**
   * Write an Ids expression.
   */
  public void writeIds(List<?> idList) throws IOException {

    endNested();
    json.writeStartObject();
    json.writeObjectFieldStart(IDS);
    json.writeArrayFieldStart(VALUES);
    for (Object id : idList) {
      jsonContext.writeScalar(json, id);
    }
    json.writeEndArray();
    json.writeEndObject();
    json.writeEndObject();
  }

  /**
   * Write an Id expression.
   */
  public void writeId(Object value) throws IOException {

    List<Object> ids = new ArrayList<Object>(1);
    ids.add(value);
    writeIds(ids);
  }

  /**
   * Write a prefix expression.
   */
  public void writeStartsWith(String propertyName, String value) throws IOException {
    // use analysed field
    writeRawWithPrepareNested(PREFIX, propertyName, value.toLowerCase());
  }

  /**
   * Suffix expression not supported yet.
   */
  public void writeEndsWith(String propertyName, String value) throws IOException {
    // use analysed field
    // this will likely be slow - best to avoid if you can
    writeWildcard(propertyName, "*" + value.toLowerCase());
  }

  /**
   * Write a match expression.
   */
  public void writeContains(String propertyName, String value) throws IOException {
    // use analysed field
    writeWildcard(propertyName, "*" + value.toLowerCase() + "*");
  }

  /**
   * Write a wildcard expression.
   */
  public void writeLike(String propertyName, String value) throws IOException {
    // use analysed field
    String val = value.toLowerCase();
    // replace SQL wildcard characters with ElasticSearch ones
    val = val.replace('_', '?');
    val = val.replace('%', '*');
    writeRawWithPrepareNested(WILDCARD, propertyName, val);
  }

  /**
   * Write case-insensitive equal to.
   */
  public void writeIEqual(String propName, String value) throws IOException {

    String[] values = value.toLowerCase().split(" ");
    if (values.length == 1) {
      writeMatch(propName, value);
    } else {
      // Boolean AND all the terms together
      writeBoolStart(true);
      for (String val : values) {
        writeMatch(propName, val);
      }
      writeBoolEnd();
    }
  }

  /**
   * Write a prefix expression.
   */
  public void writeMatch(String propertyName, String value) throws IOException {
    // use analysed field
    writeRawWithPrepareNested(MATCH, propertyName, value.toLowerCase());
  }

  /**
   * Write a wildcard expression.
   */
  public void writeWildcard(String propertyName, String value) throws IOException {
    writeRawWithPrepareNested(WILDCARD, propertyName, value);
  }

  /**
   * Write raw JSON to the query buffer.
   */
  public void writeRaw(String jsonExpression) throws IOException {
    json.writeRaw(jsonExpression);
  }

  /**
   * Write an exists expression.
   */
  public void writeExists(boolean notNull, String propertyName) throws IOException {

    // prepareNestedPath prior to BoolMustNotStart
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
    writeRawExpression(EXISTS, FIELD, propertyName);
  }

  /**
   * Write with prepareNestedPath() on the propertyName
   */
  private void writeRawWithPrepareNested(String type, String propertyName, Object value) throws IOException {

    prepareNestedPath(propertyName);
    writeRawExpression(type, propertyName, value);
  }

  /**
   * Write raw.  prepareNestedPath() should already be done.
   */
  private void writeRawExpression(String type, String propertyName, Object value) throws IOException {

    json.writeStartObject();
    json.writeObjectFieldStart(type);
    json.writeFieldName(propertyName);
    jsonContext.writeScalar(json, value);
    json.writeEndObject();
    json.writeEndObject();
  }

  /**
   * Write an expression for the core operations.
   */
  public void writeSimple(Op type, String propertyName, Object value) throws IOException {

    // prepareNested prior to boolMustNotStart
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

  /**
   * Check if we need to start a nested path filter and do so if required.
   */
  private void prepareNestedPath(String propName) throws IOException {
    ExpressionPath exprPath = desc.getExpressionPath(propName);
    if (exprPath != null && exprPath.containsMany()) {
      String[] manyPath = SplitName.splitBegin(propName);
      startNested(manyPath[0]);
    } else {
      endNested();
    }
  }

  /**
   * Start a nested path filter.
   */
  private void startNested(String nestedPath) throws IOException {

    if (currentNestedPath != null) {
      if (currentNestedPath.equals(nestedPath)) {
        // just add to currentNestedPath
        return;
      } else {
        // end the prior one as this is different
        endNested();
      }
    }
    currentNestedPath = nestedPath;

    json.writeStartObject();
    json.writeObjectFieldStart("nested");
    json.writeStringField("path", nestedPath);
    json.writeFieldName("filter");
  }

  /**
   * End a nested path filter if one is still open.
   */
  private void endNested() throws IOException {
    if (currentNestedPath != null) {
      currentNestedPath = null;
      json.writeEndObject();
      json.writeEndObject();
    }
  }

}
