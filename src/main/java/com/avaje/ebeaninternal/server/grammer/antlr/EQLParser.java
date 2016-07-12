// Generated from /home/rob/github/avaje-ebeanorm/src/test/resources/EQL.g4 by ANTLR 4.5.3
package com.avaje.ebeaninternal.server.grammer.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class EQLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, T__34=35, T__35=36, T__36=37, T__37=38, 
		T__38=39, T__39=40, T__40=41, T__41=42, T__42=43, T__43=44, INPUT_VARIABLE=45, 
		PATH_VARIABLE=46, BOOLEAN_LITERAL=47, NUMBER_LITERAL=48, STRING_LITERAL=49, 
		WS=50;
	public static final int
		RULE_select_statement = 0, RULE_select_clause = 1, RULE_fetch_clause = 2, 
		RULE_where_clause = 3, RULE_fetch_path = 4, RULE_fetch_property_set = 5, 
		RULE_fetch_property_group = 6, RULE_fetch_property = 7, RULE_conditional_expression = 8, 
		RULE_conditional_term = 9, RULE_conditional_factor = 10, RULE_conditional_primary = 11, 
		RULE_any_expression = 12, RULE_in_expression = 13, RULE_in_value = 14, 
		RULE_between_expression = 15, RULE_propertyBetween_expression = 16, RULE_isNull_expression = 17, 
		RULE_isNotNull_expression = 18, RULE_isEmpty_expression = 19, RULE_isNotEmpty_expression = 20, 
		RULE_like_expression = 21, RULE_like_op = 22, RULE_comparison_expression = 23, 
		RULE_comparison_operator = 24, RULE_value_expression = 25, RULE_literal = 26;
	public static final String[] ruleNames = {
		"select_statement", "select_clause", "fetch_clause", "where_clause", "fetch_path", 
		"fetch_property_set", "fetch_property_group", "fetch_property", "conditional_expression", 
		"conditional_term", "conditional_factor", "conditional_primary", "any_expression", 
		"in_expression", "in_value", "between_expression", "propertyBetween_expression", 
		"isNull_expression", "isNotNull_expression", "isEmpty_expression", "isNotEmpty_expression", 
		"like_expression", "like_op", "comparison_expression", "comparison_operator", 
		"value_expression", "literal"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'select'", "'('", "')'", "'where'", "'fetch'", "','", "'or'", "'and'", 
		"'not'", "'in'", "'between'", "'is'", "'null'", "'isNull'", "'isNotNull'", 
		"'notNull'", "'empty'", "'isEmpty'", "'isNotEmpty'", "'notEmpty'", "'like'", 
		"'ilike'", "'contains'", "'icontains'", "'startsWith'", "'istartsWith'", 
		"'endsWith'", "'iendsWith'", "'='", "'eq'", "'>'", "'gt'", "'>='", "'ge'", 
		"'gte'", "'<'", "'lt'", "'<='", "'le'", "'lte'", "'<>'", "'!='", "'ne'", 
		"'ieq'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, "INPUT_VARIABLE", 
		"PATH_VARIABLE", "BOOLEAN_LITERAL", "NUMBER_LITERAL", "STRING_LITERAL", 
		"WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "EQL.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public EQLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class Select_statementContext extends ParserRuleContext {
		public Select_clauseContext select_clause() {
			return getRuleContext(Select_clauseContext.class,0);
		}
		public List<Fetch_clauseContext> fetch_clause() {
			return getRuleContexts(Fetch_clauseContext.class);
		}
		public Fetch_clauseContext fetch_clause(int i) {
			return getRuleContext(Fetch_clauseContext.class,i);
		}
		public Where_clauseContext where_clause() {
			return getRuleContext(Where_clauseContext.class,0);
		}
		public Select_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterSelect_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitSelect_statement(this);
		}
	}

	public final Select_statementContext select_statement() throws RecognitionException {
		Select_statementContext _localctx = new Select_statementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_select_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(55);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(54);
				select_clause();
				}
			}

			setState(60);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__4) {
				{
				{
				setState(57);
				fetch_clause();
				}
				}
				setState(62);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(64);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(63);
				where_clause();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Select_clauseContext extends ParserRuleContext {
		public Fetch_property_groupContext fetch_property_group() {
			return getRuleContext(Fetch_property_groupContext.class,0);
		}
		public Select_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterSelect_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitSelect_clause(this);
		}
	}

	public final Select_clauseContext select_clause() throws RecognitionException {
		Select_clauseContext _localctx = new Select_clauseContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_select_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			match(T__0);
			setState(67);
			match(T__1);
			setState(68);
			fetch_property_group();
			setState(69);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Fetch_clauseContext extends ParserRuleContext {
		public Fetch_pathContext fetch_path() {
			return getRuleContext(Fetch_pathContext.class,0);
		}
		public Fetch_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fetch_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterFetch_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitFetch_clause(this);
		}
	}

	public final Fetch_clauseContext fetch_clause() throws RecognitionException {
		Fetch_clauseContext _localctx = new Fetch_clauseContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_fetch_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			fetch_path();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Where_clauseContext extends ParserRuleContext {
		public Conditional_expressionContext conditional_expression() {
			return getRuleContext(Conditional_expressionContext.class,0);
		}
		public Where_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_where_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterWhere_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitWhere_clause(this);
		}
	}

	public final Where_clauseContext where_clause() throws RecognitionException {
		Where_clauseContext _localctx = new Where_clauseContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_where_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73);
			match(T__3);
			setState(74);
			conditional_expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Fetch_pathContext extends ParserRuleContext {
		public TerminalNode PATH_VARIABLE() { return getToken(EQLParser.PATH_VARIABLE, 0); }
		public Fetch_property_setContext fetch_property_set() {
			return getRuleContext(Fetch_property_setContext.class,0);
		}
		public Fetch_pathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fetch_path; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterFetch_path(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitFetch_path(this);
		}
	}

	public final Fetch_pathContext fetch_path() throws RecognitionException {
		Fetch_pathContext _localctx = new Fetch_pathContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_fetch_path);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			match(T__4);
			setState(77);
			match(PATH_VARIABLE);
			setState(79);
			_la = _input.LA(1);
			if (_la==T__1) {
				{
				setState(78);
				fetch_property_set();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Fetch_property_setContext extends ParserRuleContext {
		public Fetch_property_groupContext fetch_property_group() {
			return getRuleContext(Fetch_property_groupContext.class,0);
		}
		public Fetch_property_setContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fetch_property_set; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterFetch_property_set(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitFetch_property_set(this);
		}
	}

	public final Fetch_property_setContext fetch_property_set() throws RecognitionException {
		Fetch_property_setContext _localctx = new Fetch_property_setContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_fetch_property_set);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(81);
			match(T__1);
			setState(82);
			fetch_property_group();
			setState(83);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Fetch_property_groupContext extends ParserRuleContext {
		public List<Fetch_propertyContext> fetch_property() {
			return getRuleContexts(Fetch_propertyContext.class);
		}
		public Fetch_propertyContext fetch_property(int i) {
			return getRuleContext(Fetch_propertyContext.class,i);
		}
		public Fetch_property_groupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fetch_property_group; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterFetch_property_group(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitFetch_property_group(this);
		}
	}

	public final Fetch_property_groupContext fetch_property_group() throws RecognitionException {
		Fetch_property_groupContext _localctx = new Fetch_property_groupContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_fetch_property_group);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			fetch_property();
			setState(90);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__5) {
				{
				{
				setState(86);
				match(T__5);
				setState(87);
				fetch_property();
				}
				}
				setState(92);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Fetch_propertyContext extends ParserRuleContext {
		public TerminalNode PATH_VARIABLE() { return getToken(EQLParser.PATH_VARIABLE, 0); }
		public Fetch_propertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fetch_property; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterFetch_property(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitFetch_property(this);
		}
	}

	public final Fetch_propertyContext fetch_property() throws RecognitionException {
		Fetch_propertyContext _localctx = new Fetch_propertyContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_fetch_property);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
			match(PATH_VARIABLE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Conditional_expressionContext extends ParserRuleContext {
		public List<Conditional_termContext> conditional_term() {
			return getRuleContexts(Conditional_termContext.class);
		}
		public Conditional_termContext conditional_term(int i) {
			return getRuleContext(Conditional_termContext.class,i);
		}
		public Conditional_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditional_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterConditional_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitConditional_expression(this);
		}
	}

	public final Conditional_expressionContext conditional_expression() throws RecognitionException {
		Conditional_expressionContext _localctx = new Conditional_expressionContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_conditional_expression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(95);
			conditional_term();
			}
			setState(100);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__6) {
				{
				{
				setState(96);
				match(T__6);
				setState(97);
				conditional_term();
				}
				}
				setState(102);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Conditional_termContext extends ParserRuleContext {
		public List<Conditional_factorContext> conditional_factor() {
			return getRuleContexts(Conditional_factorContext.class);
		}
		public Conditional_factorContext conditional_factor(int i) {
			return getRuleContext(Conditional_factorContext.class,i);
		}
		public Conditional_termContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditional_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterConditional_term(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitConditional_term(this);
		}
	}

	public final Conditional_termContext conditional_term() throws RecognitionException {
		Conditional_termContext _localctx = new Conditional_termContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_conditional_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(103);
			conditional_factor();
			}
			setState(108);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__7) {
				{
				{
				setState(104);
				match(T__7);
				setState(105);
				conditional_factor();
				}
				}
				setState(110);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Conditional_factorContext extends ParserRuleContext {
		public Conditional_primaryContext conditional_primary() {
			return getRuleContext(Conditional_primaryContext.class,0);
		}
		public Conditional_factorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditional_factor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterConditional_factor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitConditional_factor(this);
		}
	}

	public final Conditional_factorContext conditional_factor() throws RecognitionException {
		Conditional_factorContext _localctx = new Conditional_factorContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_conditional_factor);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(111);
				match(T__8);
				}
			}

			setState(114);
			conditional_primary();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Conditional_primaryContext extends ParserRuleContext {
		public Any_expressionContext any_expression() {
			return getRuleContext(Any_expressionContext.class,0);
		}
		public Conditional_expressionContext conditional_expression() {
			return getRuleContext(Conditional_expressionContext.class,0);
		}
		public Conditional_primaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditional_primary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterConditional_primary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitConditional_primary(this);
		}
	}

	public final Conditional_primaryContext conditional_primary() throws RecognitionException {
		Conditional_primaryContext _localctx = new Conditional_primaryContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_conditional_primary);
		try {
			setState(121);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(116);
				any_expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(117);
				match(T__1);
				setState(118);
				conditional_expression();
				setState(119);
				match(T__2);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Any_expressionContext extends ParserRuleContext {
		public Comparison_expressionContext comparison_expression() {
			return getRuleContext(Comparison_expressionContext.class,0);
		}
		public Like_expressionContext like_expression() {
			return getRuleContext(Like_expressionContext.class,0);
		}
		public Between_expressionContext between_expression() {
			return getRuleContext(Between_expressionContext.class,0);
		}
		public PropertyBetween_expressionContext propertyBetween_expression() {
			return getRuleContext(PropertyBetween_expressionContext.class,0);
		}
		public In_expressionContext in_expression() {
			return getRuleContext(In_expressionContext.class,0);
		}
		public IsNull_expressionContext isNull_expression() {
			return getRuleContext(IsNull_expressionContext.class,0);
		}
		public IsNotNull_expressionContext isNotNull_expression() {
			return getRuleContext(IsNotNull_expressionContext.class,0);
		}
		public IsEmpty_expressionContext isEmpty_expression() {
			return getRuleContext(IsEmpty_expressionContext.class,0);
		}
		public IsNotEmpty_expressionContext isNotEmpty_expression() {
			return getRuleContext(IsNotEmpty_expressionContext.class,0);
		}
		public Any_expressionContext any_expression() {
			return getRuleContext(Any_expressionContext.class,0);
		}
		public Any_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_any_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterAny_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitAny_expression(this);
		}
	}

	public final Any_expressionContext any_expression() throws RecognitionException {
		Any_expressionContext _localctx = new Any_expressionContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_any_expression);
		try {
			setState(136);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(123);
				comparison_expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(124);
				like_expression();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(125);
				between_expression();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(126);
				propertyBetween_expression();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(127);
				in_expression();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(128);
				isNull_expression();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(129);
				isNotNull_expression();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(130);
				isEmpty_expression();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(131);
				isNotEmpty_expression();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(132);
				match(T__1);
				setState(133);
				any_expression();
				setState(134);
				match(T__2);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class In_expressionContext extends ParserRuleContext {
		public TerminalNode PATH_VARIABLE() { return getToken(EQLParser.PATH_VARIABLE, 0); }
		public In_valueContext in_value() {
			return getRuleContext(In_valueContext.class,0);
		}
		public In_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_in_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterIn_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitIn_expression(this);
		}
	}

	public final In_expressionContext in_expression() throws RecognitionException {
		In_expressionContext _localctx = new In_expressionContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_in_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138);
			match(PATH_VARIABLE);
			setState(139);
			match(T__9);
			setState(140);
			in_value();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class In_valueContext extends ParserRuleContext {
		public TerminalNode INPUT_VARIABLE() { return getToken(EQLParser.INPUT_VARIABLE, 0); }
		public List<Value_expressionContext> value_expression() {
			return getRuleContexts(Value_expressionContext.class);
		}
		public Value_expressionContext value_expression(int i) {
			return getRuleContext(Value_expressionContext.class,i);
		}
		public In_valueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_in_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterIn_value(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitIn_value(this);
		}
	}

	public final In_valueContext in_value() throws RecognitionException {
		In_valueContext _localctx = new In_valueContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_in_value);
		int _la;
		try {
			setState(154);
			switch (_input.LA(1)) {
			case INPUT_VARIABLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(142);
				match(INPUT_VARIABLE);
				}
				break;
			case T__1:
				enterOuterAlt(_localctx, 2);
				{
				setState(143);
				match(T__1);
				setState(144);
				value_expression();
				setState(149);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__5) {
					{
					{
					setState(145);
					match(T__5);
					setState(146);
					value_expression();
					}
					}
					setState(151);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(152);
				match(T__2);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Between_expressionContext extends ParserRuleContext {
		public TerminalNode PATH_VARIABLE() { return getToken(EQLParser.PATH_VARIABLE, 0); }
		public List<Value_expressionContext> value_expression() {
			return getRuleContexts(Value_expressionContext.class);
		}
		public Value_expressionContext value_expression(int i) {
			return getRuleContext(Value_expressionContext.class,i);
		}
		public Between_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_between_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterBetween_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitBetween_expression(this);
		}
	}

	public final Between_expressionContext between_expression() throws RecognitionException {
		Between_expressionContext _localctx = new Between_expressionContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_between_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(156);
			match(PATH_VARIABLE);
			setState(157);
			match(T__10);
			setState(158);
			value_expression();
			setState(159);
			match(T__7);
			setState(160);
			value_expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyBetween_expressionContext extends ParserRuleContext {
		public Value_expressionContext value_expression() {
			return getRuleContext(Value_expressionContext.class,0);
		}
		public List<TerminalNode> PATH_VARIABLE() { return getTokens(EQLParser.PATH_VARIABLE); }
		public TerminalNode PATH_VARIABLE(int i) {
			return getToken(EQLParser.PATH_VARIABLE, i);
		}
		public PropertyBetween_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyBetween_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterPropertyBetween_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitPropertyBetween_expression(this);
		}
	}

	public final PropertyBetween_expressionContext propertyBetween_expression() throws RecognitionException {
		PropertyBetween_expressionContext _localctx = new PropertyBetween_expressionContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_propertyBetween_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			value_expression();
			setState(163);
			match(T__10);
			setState(164);
			match(PATH_VARIABLE);
			setState(165);
			match(T__7);
			setState(166);
			match(PATH_VARIABLE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IsNull_expressionContext extends ParserRuleContext {
		public TerminalNode PATH_VARIABLE() { return getToken(EQLParser.PATH_VARIABLE, 0); }
		public IsNull_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_isNull_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterIsNull_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitIsNull_expression(this);
		}
	}

	public final IsNull_expressionContext isNull_expression() throws RecognitionException {
		IsNull_expressionContext _localctx = new IsNull_expressionContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_isNull_expression);
		try {
			setState(173);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(168);
				match(PATH_VARIABLE);
				setState(169);
				match(T__11);
				setState(170);
				match(T__12);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(171);
				match(PATH_VARIABLE);
				setState(172);
				match(T__13);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IsNotNull_expressionContext extends ParserRuleContext {
		public TerminalNode PATH_VARIABLE() { return getToken(EQLParser.PATH_VARIABLE, 0); }
		public IsNotNull_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_isNotNull_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterIsNotNull_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitIsNotNull_expression(this);
		}
	}

	public final IsNotNull_expressionContext isNotNull_expression() throws RecognitionException {
		IsNotNull_expressionContext _localctx = new IsNotNull_expressionContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_isNotNull_expression);
		try {
			setState(183);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(175);
				match(PATH_VARIABLE);
				setState(176);
				match(T__11);
				setState(177);
				match(T__8);
				setState(178);
				match(T__12);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(179);
				match(PATH_VARIABLE);
				setState(180);
				match(T__14);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(181);
				match(PATH_VARIABLE);
				setState(182);
				match(T__15);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IsEmpty_expressionContext extends ParserRuleContext {
		public TerminalNode PATH_VARIABLE() { return getToken(EQLParser.PATH_VARIABLE, 0); }
		public IsEmpty_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_isEmpty_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterIsEmpty_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitIsEmpty_expression(this);
		}
	}

	public final IsEmpty_expressionContext isEmpty_expression() throws RecognitionException {
		IsEmpty_expressionContext _localctx = new IsEmpty_expressionContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_isEmpty_expression);
		try {
			setState(190);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(185);
				match(PATH_VARIABLE);
				setState(186);
				match(T__11);
				setState(187);
				match(T__16);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(188);
				match(PATH_VARIABLE);
				setState(189);
				match(T__17);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IsNotEmpty_expressionContext extends ParserRuleContext {
		public TerminalNode PATH_VARIABLE() { return getToken(EQLParser.PATH_VARIABLE, 0); }
		public IsNotEmpty_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_isNotEmpty_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterIsNotEmpty_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitIsNotEmpty_expression(this);
		}
	}

	public final IsNotEmpty_expressionContext isNotEmpty_expression() throws RecognitionException {
		IsNotEmpty_expressionContext _localctx = new IsNotEmpty_expressionContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_isNotEmpty_expression);
		try {
			setState(200);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(192);
				match(PATH_VARIABLE);
				setState(193);
				match(T__11);
				setState(194);
				match(T__8);
				setState(195);
				match(T__16);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(196);
				match(PATH_VARIABLE);
				setState(197);
				match(T__18);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(198);
				match(PATH_VARIABLE);
				setState(199);
				match(T__19);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Like_expressionContext extends ParserRuleContext {
		public TerminalNode PATH_VARIABLE() { return getToken(EQLParser.PATH_VARIABLE, 0); }
		public Like_opContext like_op() {
			return getRuleContext(Like_opContext.class,0);
		}
		public Value_expressionContext value_expression() {
			return getRuleContext(Value_expressionContext.class,0);
		}
		public Like_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_like_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterLike_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitLike_expression(this);
		}
	}

	public final Like_expressionContext like_expression() throws RecognitionException {
		Like_expressionContext _localctx = new Like_expressionContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_like_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(202);
			match(PATH_VARIABLE);
			setState(203);
			like_op();
			setState(204);
			value_expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Like_opContext extends ParserRuleContext {
		public Like_opContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_like_op; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterLike_op(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitLike_op(this);
		}
	}

	public final Like_opContext like_op() throws RecognitionException {
		Like_opContext _localctx = new Like_opContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_like_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(206);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__20) | (1L << T__21) | (1L << T__22) | (1L << T__23) | (1L << T__24) | (1L << T__25) | (1L << T__26) | (1L << T__27))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Comparison_expressionContext extends ParserRuleContext {
		public TerminalNode PATH_VARIABLE() { return getToken(EQLParser.PATH_VARIABLE, 0); }
		public Comparison_operatorContext comparison_operator() {
			return getRuleContext(Comparison_operatorContext.class,0);
		}
		public Value_expressionContext value_expression() {
			return getRuleContext(Value_expressionContext.class,0);
		}
		public Comparison_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparison_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterComparison_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitComparison_expression(this);
		}
	}

	public final Comparison_expressionContext comparison_expression() throws RecognitionException {
		Comparison_expressionContext _localctx = new Comparison_expressionContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_comparison_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(208);
			match(PATH_VARIABLE);
			setState(209);
			comparison_operator();
			setState(210);
			value_expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Comparison_operatorContext extends ParserRuleContext {
		public Comparison_operatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparison_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterComparison_operator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitComparison_operator(this);
		}
	}

	public final Comparison_operatorContext comparison_operator() throws RecognitionException {
		Comparison_operatorContext _localctx = new Comparison_operatorContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_comparison_operator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(212);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__28) | (1L << T__29) | (1L << T__30) | (1L << T__31) | (1L << T__32) | (1L << T__33) | (1L << T__34) | (1L << T__35) | (1L << T__36) | (1L << T__37) | (1L << T__38) | (1L << T__39) | (1L << T__40) | (1L << T__41) | (1L << T__42) | (1L << T__43))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Value_expressionContext extends ParserRuleContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public TerminalNode INPUT_VARIABLE() { return getToken(EQLParser.INPUT_VARIABLE, 0); }
		public Value_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterValue_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitValue_expression(this);
		}
	}

	public final Value_expressionContext value_expression() throws RecognitionException {
		Value_expressionContext _localctx = new Value_expressionContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_value_expression);
		try {
			setState(216);
			switch (_input.LA(1)) {
			case BOOLEAN_LITERAL:
			case NUMBER_LITERAL:
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(214);
				literal();
				}
				break;
			case INPUT_VARIABLE:
				enterOuterAlt(_localctx, 2);
				{
				setState(215);
				match(INPUT_VARIABLE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(EQLParser.STRING_LITERAL, 0); }
		public TerminalNode BOOLEAN_LITERAL() { return getToken(EQLParser.BOOLEAN_LITERAL, 0); }
		public TerminalNode NUMBER_LITERAL() { return getToken(EQLParser.NUMBER_LITERAL, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EQLListener ) ((EQLListener)listener).exitLiteral(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_literal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(218);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BOOLEAN_LITERAL) | (1L << NUMBER_LITERAL) | (1L << STRING_LITERAL))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\64\u00df\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\3\2\5\2:\n\2\3\2\7\2=\n\2\f\2\16\2@\13"+
		"\2\3\2\5\2C\n\2\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\6\5"+
		"\6R\n\6\3\7\3\7\3\7\3\7\3\b\3\b\3\b\7\b[\n\b\f\b\16\b^\13\b\3\t\3\t\3"+
		"\n\3\n\3\n\7\ne\n\n\f\n\16\nh\13\n\3\13\3\13\3\13\7\13m\n\13\f\13\16\13"+
		"p\13\13\3\f\5\fs\n\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\5\r|\n\r\3\16\3\16\3"+
		"\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u008b\n\16"+
		"\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\7\20\u0096\n\20\f\20\16"+
		"\20\u0099\13\20\3\20\3\20\5\20\u009d\n\20\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\22\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\5\23\u00b0\n\23"+
		"\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\5\24\u00ba\n\24\3\25\3\25\3\25"+
		"\3\25\3\25\5\25\u00c1\n\25\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\5\26"+
		"\u00cb\n\26\3\27\3\27\3\27\3\27\3\30\3\30\3\31\3\31\3\31\3\31\3\32\3\32"+
		"\3\33\3\33\5\33\u00db\n\33\3\34\3\34\3\34\2\2\35\2\4\6\b\n\f\16\20\22"+
		"\24\26\30\32\34\36 \"$&(*,.\60\62\64\66\2\5\3\2\27\36\3\2\37.\3\2\61\63"+
		"\u00de\29\3\2\2\2\4D\3\2\2\2\6I\3\2\2\2\bK\3\2\2\2\nN\3\2\2\2\fS\3\2\2"+
		"\2\16W\3\2\2\2\20_\3\2\2\2\22a\3\2\2\2\24i\3\2\2\2\26r\3\2\2\2\30{\3\2"+
		"\2\2\32\u008a\3\2\2\2\34\u008c\3\2\2\2\36\u009c\3\2\2\2 \u009e\3\2\2\2"+
		"\"\u00a4\3\2\2\2$\u00af\3\2\2\2&\u00b9\3\2\2\2(\u00c0\3\2\2\2*\u00ca\3"+
		"\2\2\2,\u00cc\3\2\2\2.\u00d0\3\2\2\2\60\u00d2\3\2\2\2\62\u00d6\3\2\2\2"+
		"\64\u00da\3\2\2\2\66\u00dc\3\2\2\28:\5\4\3\298\3\2\2\29:\3\2\2\2:>\3\2"+
		"\2\2;=\5\6\4\2<;\3\2\2\2=@\3\2\2\2><\3\2\2\2>?\3\2\2\2?B\3\2\2\2@>\3\2"+
		"\2\2AC\5\b\5\2BA\3\2\2\2BC\3\2\2\2C\3\3\2\2\2DE\7\3\2\2EF\7\4\2\2FG\5"+
		"\16\b\2GH\7\5\2\2H\5\3\2\2\2IJ\5\n\6\2J\7\3\2\2\2KL\7\6\2\2LM\5\22\n\2"+
		"M\t\3\2\2\2NO\7\7\2\2OQ\7\60\2\2PR\5\f\7\2QP\3\2\2\2QR\3\2\2\2R\13\3\2"+
		"\2\2ST\7\4\2\2TU\5\16\b\2UV\7\5\2\2V\r\3\2\2\2W\\\5\20\t\2XY\7\b\2\2Y"+
		"[\5\20\t\2ZX\3\2\2\2[^\3\2\2\2\\Z\3\2\2\2\\]\3\2\2\2]\17\3\2\2\2^\\\3"+
		"\2\2\2_`\7\60\2\2`\21\3\2\2\2af\5\24\13\2bc\7\t\2\2ce\5\24\13\2db\3\2"+
		"\2\2eh\3\2\2\2fd\3\2\2\2fg\3\2\2\2g\23\3\2\2\2hf\3\2\2\2in\5\26\f\2jk"+
		"\7\n\2\2km\5\26\f\2lj\3\2\2\2mp\3\2\2\2nl\3\2\2\2no\3\2\2\2o\25\3\2\2"+
		"\2pn\3\2\2\2qs\7\13\2\2rq\3\2\2\2rs\3\2\2\2st\3\2\2\2tu\5\30\r\2u\27\3"+
		"\2\2\2v|\5\32\16\2wx\7\4\2\2xy\5\22\n\2yz\7\5\2\2z|\3\2\2\2{v\3\2\2\2"+
		"{w\3\2\2\2|\31\3\2\2\2}\u008b\5\60\31\2~\u008b\5,\27\2\177\u008b\5 \21"+
		"\2\u0080\u008b\5\"\22\2\u0081\u008b\5\34\17\2\u0082\u008b\5$\23\2\u0083"+
		"\u008b\5&\24\2\u0084\u008b\5(\25\2\u0085\u008b\5*\26\2\u0086\u0087\7\4"+
		"\2\2\u0087\u0088\5\32\16\2\u0088\u0089\7\5\2\2\u0089\u008b\3\2\2\2\u008a"+
		"}\3\2\2\2\u008a~\3\2\2\2\u008a\177\3\2\2\2\u008a\u0080\3\2\2\2\u008a\u0081"+
		"\3\2\2\2\u008a\u0082\3\2\2\2\u008a\u0083\3\2\2\2\u008a\u0084\3\2\2\2\u008a"+
		"\u0085\3\2\2\2\u008a\u0086\3\2\2\2\u008b\33\3\2\2\2\u008c\u008d\7\60\2"+
		"\2\u008d\u008e\7\f\2\2\u008e\u008f\5\36\20\2\u008f\35\3\2\2\2\u0090\u009d"+
		"\7/\2\2\u0091\u0092\7\4\2\2\u0092\u0097\5\64\33\2\u0093\u0094\7\b\2\2"+
		"\u0094\u0096\5\64\33\2\u0095\u0093\3\2\2\2\u0096\u0099\3\2\2\2\u0097\u0095"+
		"\3\2\2\2\u0097\u0098\3\2\2\2\u0098\u009a\3\2\2\2\u0099\u0097\3\2\2\2\u009a"+
		"\u009b\7\5\2\2\u009b\u009d\3\2\2\2\u009c\u0090\3\2\2\2\u009c\u0091\3\2"+
		"\2\2\u009d\37\3\2\2\2\u009e\u009f\7\60\2\2\u009f\u00a0\7\r\2\2\u00a0\u00a1"+
		"\5\64\33\2\u00a1\u00a2\7\n\2\2\u00a2\u00a3\5\64\33\2\u00a3!\3\2\2\2\u00a4"+
		"\u00a5\5\64\33\2\u00a5\u00a6\7\r\2\2\u00a6\u00a7\7\60\2\2\u00a7\u00a8"+
		"\7\n\2\2\u00a8\u00a9\7\60\2\2\u00a9#\3\2\2\2\u00aa\u00ab\7\60\2\2\u00ab"+
		"\u00ac\7\16\2\2\u00ac\u00b0\7\17\2\2\u00ad\u00ae\7\60\2\2\u00ae\u00b0"+
		"\7\20\2\2\u00af\u00aa\3\2\2\2\u00af\u00ad\3\2\2\2\u00b0%\3\2\2\2\u00b1"+
		"\u00b2\7\60\2\2\u00b2\u00b3\7\16\2\2\u00b3\u00b4\7\13\2\2\u00b4\u00ba"+
		"\7\17\2\2\u00b5\u00b6\7\60\2\2\u00b6\u00ba\7\21\2\2\u00b7\u00b8\7\60\2"+
		"\2\u00b8\u00ba\7\22\2\2\u00b9\u00b1\3\2\2\2\u00b9\u00b5\3\2\2\2\u00b9"+
		"\u00b7\3\2\2\2\u00ba\'\3\2\2\2\u00bb\u00bc\7\60\2\2\u00bc\u00bd\7\16\2"+
		"\2\u00bd\u00c1\7\23\2\2\u00be\u00bf\7\60\2\2\u00bf\u00c1\7\24\2\2\u00c0"+
		"\u00bb\3\2\2\2\u00c0\u00be\3\2\2\2\u00c1)\3\2\2\2\u00c2\u00c3\7\60\2\2"+
		"\u00c3\u00c4\7\16\2\2\u00c4\u00c5\7\13\2\2\u00c5\u00cb\7\23\2\2\u00c6"+
		"\u00c7\7\60\2\2\u00c7\u00cb\7\25\2\2\u00c8\u00c9\7\60\2\2\u00c9\u00cb"+
		"\7\26\2\2\u00ca\u00c2\3\2\2\2\u00ca\u00c6\3\2\2\2\u00ca\u00c8\3\2\2\2"+
		"\u00cb+\3\2\2\2\u00cc\u00cd\7\60\2\2\u00cd\u00ce\5.\30\2\u00ce\u00cf\5"+
		"\64\33\2\u00cf-\3\2\2\2\u00d0\u00d1\t\2\2\2\u00d1/\3\2\2\2\u00d2\u00d3"+
		"\7\60\2\2\u00d3\u00d4\5\62\32\2\u00d4\u00d5\5\64\33\2\u00d5\61\3\2\2\2"+
		"\u00d6\u00d7\t\3\2\2\u00d7\63\3\2\2\2\u00d8\u00db\5\66\34\2\u00d9\u00db"+
		"\7/\2\2\u00da\u00d8\3\2\2\2\u00da\u00d9\3\2\2\2\u00db\65\3\2\2\2\u00dc"+
		"\u00dd\t\4\2\2\u00dd\67\3\2\2\2\239>BQ\\fnr{\u008a\u0097\u009c\u00af\u00b9"+
		"\u00c0\u00ca\u00da";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}