package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.Junction;
import com.avaje.ebean.LikeType;
import com.avaje.ebean.search.Match;
import com.avaje.ebean.search.MultiMatch;
import com.avaje.ebean.search.TextCommonTerms;
import com.avaje.ebean.search.TextQueryString;
import com.avaje.ebean.search.TextSimple;

import java.io.IOException;
import java.util.List;
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
  void writeIds(List<?> idList) throws IOException;

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

}
