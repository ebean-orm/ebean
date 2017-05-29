package io.ebeaninternal.server.expression;

import io.ebean.Junction;
import io.ebean.LikeType;
import io.ebean.plugin.ExpressionPath;
import io.ebean.search.Match;
import io.ebean.search.MultiMatch;
import io.ebean.search.TextCommonTerms;
import io.ebean.search.TextQueryString;
import io.ebean.search.TextSimple;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Context for writing a doc store query.
 */
public interface DocQueryContext {

  /**
   * Start a junction.
   */
  void startBool(Junction.Type type) throws IOException;

  /**
   * Start a conjunction.
   */
  void startBoolMust() throws IOException;

  /**
   * Start a boolean NOT.
   */
  void startBoolMustNot() throws IOException;

  /**
   * End a bool expression/group.
   */
  void endBool() throws IOException;

  /**
   * Write a equalTo expression.
   */
  void writeEqualTo(String propertyName, Object value) throws IOException;

  /**
   * Write a case insensitive equalTo expression.
   */
  void writeIEqualTo(String propName, String value) throws IOException;

  /**
   * Write a range operation with one value.
   */
  void writeRange(String propertyName, String rangeType, Object value) throws IOException;

  /**
   * Write a range operation with a lower and upper values.
   */
  void writeRange(String propertyName, Op lowOp, Object valueLow, Op highOp, Object valueHigh) throws IOException;

  /**
   * Write an In expression.
   */
  void writeIn(String propertyName, Object[] values, boolean not) throws IOException;

  /**
   * Write an Id in expression.
   */
  void writeIds(Collection<?> idCollection) throws IOException;

  /**
   * Write an Id equals expression.
   */
  void writeId(Object value) throws IOException;

  /**
   * Write a raw expression with bind values (might not be supported).
   */
  void writeRaw(String raw, Object[] values) throws IOException;

  /**
   * Write an exists expression.
   */
  void writeExists(boolean notNull, String propertyName) throws IOException;

  /**
   * Write one of the base expressions.
   */
  void writeSimple(Op type, String propertyName, Object value) throws IOException;

  /**
   * Write an all equals expression.
   */
  void writeAllEquals(Map<String, Object> propMap) throws IOException;

  /**
   * Write a Like expression.
   */
  void writeLike(String propName, String val, LikeType type, boolean caseInsensitive) throws IOException;

  /**
   * Write a Match expression.
   */
  void writeMatch(String propName, String search, Match options) throws IOException;

  /**
   * Write a Multi-match expression.
   */
  void writeMultiMatch(String search, MultiMatch options) throws IOException;

  /**
   * Write a simple expression.
   */
  void writeTextSimple(String search, TextSimple options) throws IOException;

  /**
   * Write a common terms expression.
   */
  void writeTextCommonTerms(String search, TextCommonTerms options) throws IOException;

  /**
   * Write a query string expression.
   */
  void writeTextQueryString(String search, TextQueryString options) throws IOException;

  /**
   * Start a Bool which may contain some of Must, Must Not, Should.
   */
  void startBoolGroup() throws IOException;

  /**
   * Start a Must, Must Not or Should list.
   */
  void startBoolGroupList(Junction.Type type) throws IOException;

  /**
   * End a Must, Must Not or Should list.
   */
  void endBoolGroupList() throws IOException;

  /**
   * End the Bool group.
   */
  void endBoolGroup() throws IOException;

  /**
   * Return the expression path for the given property path.
   */
  ExpressionPath getExpressionPath(String propName);

  /**
   * Start nested path expressions.
   */
  void startNested(String nestedPath) throws IOException;

  /**
   * End nested path expressions.
   */
  void endNested() throws IOException;

  /**
   * Start a not wrapping an expression.
   */
  void startNot() throws IOException;

  /**
   * End a not wrapper.
   */
  void endNot() throws IOException;
}
