// Generated from /home/rob/github/ebean/src/test/resources/EQL.g4 by ANTLR 4.7
package io.ebeaninternal.server.grammer.antlr;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link EQLParser}.
 */
public interface EQLListener extends ParseTreeListener {
  /**
   * Enter a parse tree produced by {@link EQLParser#select_statement}.
   *
   * @param ctx the parse tree
   */
  void enterSelect_statement(EQLParser.Select_statementContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#select_statement}.
   *
   * @param ctx the parse tree
   */
  void exitSelect_statement(EQLParser.Select_statementContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#select_clause}.
   *
   * @param ctx the parse tree
   */
  void enterSelect_clause(EQLParser.Select_clauseContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#select_clause}.
   *
   * @param ctx the parse tree
   */
  void exitSelect_clause(EQLParser.Select_clauseContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#distinct}.
   *
   * @param ctx the parse tree
   */
  void enterDistinct(EQLParser.DistinctContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#distinct}.
   *
   * @param ctx the parse tree
   */
  void exitDistinct(EQLParser.DistinctContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#fetch_clause}.
   *
   * @param ctx the parse tree
   */
  void enterFetch_clause(EQLParser.Fetch_clauseContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#fetch_clause}.
   *
   * @param ctx the parse tree
   */
  void exitFetch_clause(EQLParser.Fetch_clauseContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#where_clause}.
   *
   * @param ctx the parse tree
   */
  void enterWhere_clause(EQLParser.Where_clauseContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#where_clause}.
   *
   * @param ctx the parse tree
   */
  void exitWhere_clause(EQLParser.Where_clauseContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#orderby_clause}.
   *
   * @param ctx the parse tree
   */
  void enterOrderby_clause(EQLParser.Orderby_clauseContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#orderby_clause}.
   *
   * @param ctx the parse tree
   */
  void exitOrderby_clause(EQLParser.Orderby_clauseContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#orderby_property}.
   *
   * @param ctx the parse tree
   */
  void enterOrderby_property(EQLParser.Orderby_propertyContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#orderby_property}.
   *
   * @param ctx the parse tree
   */
  void exitOrderby_property(EQLParser.Orderby_propertyContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#nulls_firstlast}.
   *
   * @param ctx the parse tree
   */
  void enterNulls_firstlast(EQLParser.Nulls_firstlastContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#nulls_firstlast}.
   *
   * @param ctx the parse tree
   */
  void exitNulls_firstlast(EQLParser.Nulls_firstlastContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#asc_desc}.
   *
   * @param ctx the parse tree
   */
  void enterAsc_desc(EQLParser.Asc_descContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#asc_desc}.
   *
   * @param ctx the parse tree
   */
  void exitAsc_desc(EQLParser.Asc_descContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#limit_clause}.
   *
   * @param ctx the parse tree
   */
  void enterLimit_clause(EQLParser.Limit_clauseContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#limit_clause}.
   *
   * @param ctx the parse tree
   */
  void exitLimit_clause(EQLParser.Limit_clauseContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#offset_clause}.
   *
   * @param ctx the parse tree
   */
  void enterOffset_clause(EQLParser.Offset_clauseContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#offset_clause}.
   *
   * @param ctx the parse tree
   */
  void exitOffset_clause(EQLParser.Offset_clauseContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#fetch_path}.
   *
   * @param ctx the parse tree
   */
  void enterFetch_path(EQLParser.Fetch_pathContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#fetch_path}.
   *
   * @param ctx the parse tree
   */
  void exitFetch_path(EQLParser.Fetch_pathContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#fetch_property_set}.
   *
   * @param ctx the parse tree
   */
  void enterFetch_property_set(EQLParser.Fetch_property_setContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#fetch_property_set}.
   *
   * @param ctx the parse tree
   */
  void exitFetch_property_set(EQLParser.Fetch_property_setContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#fetch_property_group}.
   *
   * @param ctx the parse tree
   */
  void enterFetch_property_group(EQLParser.Fetch_property_groupContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#fetch_property_group}.
   *
   * @param ctx the parse tree
   */
  void exitFetch_property_group(EQLParser.Fetch_property_groupContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#fetch_property}.
   *
   * @param ctx the parse tree
   */
  void enterFetch_property(EQLParser.Fetch_propertyContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#fetch_property}.
   *
   * @param ctx the parse tree
   */
  void exitFetch_property(EQLParser.Fetch_propertyContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#fetch_query_hint}.
   *
   * @param ctx the parse tree
   */
  void enterFetch_query_hint(EQLParser.Fetch_query_hintContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#fetch_query_hint}.
   *
   * @param ctx the parse tree
   */
  void exitFetch_query_hint(EQLParser.Fetch_query_hintContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#fetch_lazy_hint}.
   *
   * @param ctx the parse tree
   */
  void enterFetch_lazy_hint(EQLParser.Fetch_lazy_hintContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#fetch_lazy_hint}.
   *
   * @param ctx the parse tree
   */
  void exitFetch_lazy_hint(EQLParser.Fetch_lazy_hintContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#fetch_option}.
   *
   * @param ctx the parse tree
   */
  void enterFetch_option(EQLParser.Fetch_optionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#fetch_option}.
   *
   * @param ctx the parse tree
   */
  void exitFetch_option(EQLParser.Fetch_optionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#fetch_query_option}.
   *
   * @param ctx the parse tree
   */
  void enterFetch_query_option(EQLParser.Fetch_query_optionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#fetch_query_option}.
   *
   * @param ctx the parse tree
   */
  void exitFetch_query_option(EQLParser.Fetch_query_optionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#fetch_lazy_option}.
   *
   * @param ctx the parse tree
   */
  void enterFetch_lazy_option(EQLParser.Fetch_lazy_optionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#fetch_lazy_option}.
   *
   * @param ctx the parse tree
   */
  void exitFetch_lazy_option(EQLParser.Fetch_lazy_optionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#fetch_batch_size}.
   *
   * @param ctx the parse tree
   */
  void enterFetch_batch_size(EQLParser.Fetch_batch_sizeContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#fetch_batch_size}.
   *
   * @param ctx the parse tree
   */
  void exitFetch_batch_size(EQLParser.Fetch_batch_sizeContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#conditional_expression}.
   *
   * @param ctx the parse tree
   */
  void enterConditional_expression(EQLParser.Conditional_expressionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#conditional_expression}.
   *
   * @param ctx the parse tree
   */
  void exitConditional_expression(EQLParser.Conditional_expressionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#conditional_term}.
   *
   * @param ctx the parse tree
   */
  void enterConditional_term(EQLParser.Conditional_termContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#conditional_term}.
   *
   * @param ctx the parse tree
   */
  void exitConditional_term(EQLParser.Conditional_termContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#conditional_factor}.
   *
   * @param ctx the parse tree
   */
  void enterConditional_factor(EQLParser.Conditional_factorContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#conditional_factor}.
   *
   * @param ctx the parse tree
   */
  void exitConditional_factor(EQLParser.Conditional_factorContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#conditional_primary}.
   *
   * @param ctx the parse tree
   */
  void enterConditional_primary(EQLParser.Conditional_primaryContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#conditional_primary}.
   *
   * @param ctx the parse tree
   */
  void exitConditional_primary(EQLParser.Conditional_primaryContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#any_expression}.
   *
   * @param ctx the parse tree
   */
  void enterAny_expression(EQLParser.Any_expressionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#any_expression}.
   *
   * @param ctx the parse tree
   */
  void exitAny_expression(EQLParser.Any_expressionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#in_expression}.
   *
   * @param ctx the parse tree
   */
  void enterIn_expression(EQLParser.In_expressionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#in_expression}.
   *
   * @param ctx the parse tree
   */
  void exitIn_expression(EQLParser.In_expressionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#in_value}.
   *
   * @param ctx the parse tree
   */
  void enterIn_value(EQLParser.In_valueContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#in_value}.
   *
   * @param ctx the parse tree
   */
  void exitIn_value(EQLParser.In_valueContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#between_expression}.
   *
   * @param ctx the parse tree
   */
  void enterBetween_expression(EQLParser.Between_expressionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#between_expression}.
   *
   * @param ctx the parse tree
   */
  void exitBetween_expression(EQLParser.Between_expressionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#propertyBetween_expression}.
   *
   * @param ctx the parse tree
   */
  void enterPropertyBetween_expression(EQLParser.PropertyBetween_expressionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#propertyBetween_expression}.
   *
   * @param ctx the parse tree
   */
  void exitPropertyBetween_expression(EQLParser.PropertyBetween_expressionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#isNull_expression}.
   *
   * @param ctx the parse tree
   */
  void enterIsNull_expression(EQLParser.IsNull_expressionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#isNull_expression}.
   *
   * @param ctx the parse tree
   */
  void exitIsNull_expression(EQLParser.IsNull_expressionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#isNotNull_expression}.
   *
   * @param ctx the parse tree
   */
  void enterIsNotNull_expression(EQLParser.IsNotNull_expressionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#isNotNull_expression}.
   *
   * @param ctx the parse tree
   */
  void exitIsNotNull_expression(EQLParser.IsNotNull_expressionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#isEmpty_expression}.
   *
   * @param ctx the parse tree
   */
  void enterIsEmpty_expression(EQLParser.IsEmpty_expressionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#isEmpty_expression}.
   *
   * @param ctx the parse tree
   */
  void exitIsEmpty_expression(EQLParser.IsEmpty_expressionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#isNotEmpty_expression}.
   *
   * @param ctx the parse tree
   */
  void enterIsNotEmpty_expression(EQLParser.IsNotEmpty_expressionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#isNotEmpty_expression}.
   *
   * @param ctx the parse tree
   */
  void exitIsNotEmpty_expression(EQLParser.IsNotEmpty_expressionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#like_expression}.
   *
   * @param ctx the parse tree
   */
  void enterLike_expression(EQLParser.Like_expressionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#like_expression}.
   *
   * @param ctx the parse tree
   */
  void exitLike_expression(EQLParser.Like_expressionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#like_op}.
   *
   * @param ctx the parse tree
   */
  void enterLike_op(EQLParser.Like_opContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#like_op}.
   *
   * @param ctx the parse tree
   */
  void exitLike_op(EQLParser.Like_opContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#comparison_expression}.
   *
   * @param ctx the parse tree
   */
  void enterComparison_expression(EQLParser.Comparison_expressionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#comparison_expression}.
   *
   * @param ctx the parse tree
   */
  void exitComparison_expression(EQLParser.Comparison_expressionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#comparison_operator}.
   *
   * @param ctx the parse tree
   */
  void enterComparison_operator(EQLParser.Comparison_operatorContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#comparison_operator}.
   *
   * @param ctx the parse tree
   */
  void exitComparison_operator(EQLParser.Comparison_operatorContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#value_expression}.
   *
   * @param ctx the parse tree
   */
  void enterValue_expression(EQLParser.Value_expressionContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#value_expression}.
   *
   * @param ctx the parse tree
   */
  void exitValue_expression(EQLParser.Value_expressionContext ctx);

  /**
   * Enter a parse tree produced by {@link EQLParser#literal}.
   *
   * @param ctx the parse tree
   */
  void enterLiteral(EQLParser.LiteralContext ctx);

  /**
   * Exit a parse tree produced by {@link EQLParser#literal}.
   *
   * @param ctx the parse tree
   */
  void exitLiteral(EQLParser.LiteralContext ctx);
}
