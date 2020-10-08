// Generated from /home/rob/github/ebean-dir/ebean/src/test/resources/EQL.g4 by ANTLR 4.8
package io.ebeaninternal.server.grammer.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link EQLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface EQLVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link EQLParser#select_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_statement(EQLParser.Select_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#select_properties}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_properties(EQLParser.Select_propertiesContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#select_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_clause(EQLParser.Select_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#distinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinct(EQLParser.DistinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#fetch_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_clause(EQLParser.Fetch_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_clause(EQLParser.Where_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#orderby_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderby_clause(EQLParser.Orderby_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#orderby_property}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderby_property(EQLParser.Orderby_propertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#nulls_firstlast}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNulls_firstlast(EQLParser.Nulls_firstlastContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#asc_desc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsc_desc(EQLParser.Asc_descContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#limit_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimit_clause(EQLParser.Limit_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#offset_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOffset_clause(EQLParser.Offset_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#fetch_path}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_path(EQLParser.Fetch_pathContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#fetch_property_set}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_property_set(EQLParser.Fetch_property_setContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#fetch_property_group}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_property_group(EQLParser.Fetch_property_groupContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#fetch_path_path}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_path_path(EQLParser.Fetch_path_pathContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#fetch_property}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_property(EQLParser.Fetch_propertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#fetch_query_hint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_query_hint(EQLParser.Fetch_query_hintContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#fetch_lazy_hint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_lazy_hint(EQLParser.Fetch_lazy_hintContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#fetch_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_option(EQLParser.Fetch_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#fetch_query_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_query_option(EQLParser.Fetch_query_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#fetch_lazy_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_lazy_option(EQLParser.Fetch_lazy_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#fetch_batch_size}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_batch_size(EQLParser.Fetch_batch_sizeContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#conditional_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditional_expression(EQLParser.Conditional_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#conditional_term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditional_term(EQLParser.Conditional_termContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#conditional_factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditional_factor(EQLParser.Conditional_factorContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#conditional_primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditional_primary(EQLParser.Conditional_primaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#any_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAny_expression(EQLParser.Any_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#inOrEmpty_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInOrEmpty_expression(EQLParser.InOrEmpty_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#in_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIn_expression(EQLParser.In_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#in_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIn_value(EQLParser.In_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#between_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBetween_expression(EQLParser.Between_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#inrange_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInrange_expression(EQLParser.Inrange_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#inrange_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInrange_op(EQLParser.Inrange_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#propertyBetween_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertyBetween_expression(EQLParser.PropertyBetween_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#isNull_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsNull_expression(EQLParser.IsNull_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#isNotNull_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsNotNull_expression(EQLParser.IsNotNull_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#isEmpty_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsEmpty_expression(EQLParser.IsEmpty_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#isNotEmpty_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsNotEmpty_expression(EQLParser.IsNotEmpty_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#like_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLike_expression(EQLParser.Like_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#like_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLike_op(EQLParser.Like_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#comparison_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison_expression(EQLParser.Comparison_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#comparison_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison_operator(EQLParser.Comparison_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#value_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_expression(EQLParser.Value_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(EQLParser.LiteralContext ctx);
}