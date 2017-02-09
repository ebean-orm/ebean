// Generated from /home/rob/github/ebean/src/test/resources/EQL.g4 by ANTLR 4.6
package io.ebeaninternal.server.grammer.antlr;

import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class EQLParser extends Parser {
  static {
    RuntimeMetaData.checkVersion("4.6", RuntimeMetaData.VERSION);
  }

  protected static final DFA[] _decisionToDFA;
  protected static final PredictionContextCache _sharedContextCache =
    new PredictionContextCache();
  public static final int
    T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, T__6 = 7, T__7 = 8, T__8 = 9,
    T__9 = 10, T__10 = 11, T__11 = 12, T__12 = 13, T__13 = 14, T__14 = 15, T__15 = 16, T__16 = 17,
    T__17 = 18, T__18 = 19, T__19 = 20, T__20 = 21, T__21 = 22, T__22 = 23, T__23 = 24,
    T__24 = 25, T__25 = 26, T__26 = 27, T__27 = 28, T__28 = 29, T__29 = 30, T__30 = 31,
    T__31 = 32, T__32 = 33, T__33 = 34, T__34 = 35, T__35 = 36, T__36 = 37, T__37 = 38,
    T__38 = 39, T__39 = 40, T__40 = 41, T__41 = 42, T__42 = 43, T__43 = 44, T__44 = 45,
    T__45 = 46, T__46 = 47, T__47 = 48, T__48 = 49, T__49 = 50, T__50 = 51, T__51 = 52,
    T__52 = 53, T__53 = 54, T__54 = 55, T__55 = 56, T__56 = 57, INPUT_VARIABLE = 58, PATH_VARIABLE = 59,
    BOOLEAN_LITERAL = 60, NUMBER_LITERAL = 61, DOUBLE = 62, INT = 63, ZERO = 64, STRING_LITERAL = 65,
    WS = 66;
  public static final int
    RULE_select_statement = 0, RULE_select_clause = 1, RULE_distinct = 2,
    RULE_fetch_clause = 3, RULE_where_clause = 4, RULE_orderby_clause = 5,
    RULE_orderby_property = 6, RULE_nulls_firstlast = 7, RULE_asc_desc = 8,
    RULE_limit_clause = 9, RULE_offset_clause = 10, RULE_fetch_path = 11,
    RULE_fetch_property_set = 12, RULE_fetch_property_group = 13, RULE_fetch_property = 14,
    RULE_fetch_query_hint = 15, RULE_fetch_lazy_hint = 16, RULE_fetch_option = 17,
    RULE_fetch_query_option = 18, RULE_fetch_lazy_option = 19, RULE_fetch_batch_size = 20,
    RULE_conditional_expression = 21, RULE_conditional_term = 22, RULE_conditional_factor = 23,
    RULE_conditional_primary = 24, RULE_any_expression = 25, RULE_in_expression = 26,
    RULE_in_value = 27, RULE_between_expression = 28, RULE_propertyBetween_expression = 29,
    RULE_isNull_expression = 30, RULE_isNotNull_expression = 31, RULE_isEmpty_expression = 32,
    RULE_isNotEmpty_expression = 33, RULE_like_expression = 34, RULE_like_op = 35,
    RULE_comparison_expression = 36, RULE_comparison_operator = 37, RULE_value_expression = 38,
    RULE_literal = 39;
  public static final String[] ruleNames = {
    "select_statement", "select_clause", "distinct", "fetch_clause", "where_clause",
    "orderby_clause", "orderby_property", "nulls_firstlast", "asc_desc", "limit_clause",
    "offset_clause", "fetch_path", "fetch_property_set", "fetch_property_group",
    "fetch_property", "fetch_query_hint", "fetch_lazy_hint", "fetch_option",
    "fetch_query_option", "fetch_lazy_option", "fetch_batch_size", "conditional_expression",
    "conditional_term", "conditional_factor", "conditional_primary", "any_expression",
    "in_expression", "in_value", "between_expression", "propertyBetween_expression",
    "isNull_expression", "isNotNull_expression", "isEmpty_expression", "isNotEmpty_expression",
    "like_expression", "like_op", "comparison_expression", "comparison_operator",
    "value_expression", "literal"
  };

  private static final String[] _LITERAL_NAMES = {
    null, "'select'", "'('", "')'", "'distinct'", "'where'", "'order'", "'by'",
    "','", "'nulls'", "'first'", "'last'", "'asc'", "'desc'", "'limit'", "'offset'",
    "'fetch'", "'+'", "'query'", "'lazy'", "'or'", "'and'", "'not'", "'in'",
    "'between'", "'is'", "'null'", "'isNull'", "'isNotNull'", "'notNull'",
    "'empty'", "'isEmpty'", "'isNotEmpty'", "'notEmpty'", "'like'", "'ilike'",
    "'contains'", "'icontains'", "'startsWith'", "'istartsWith'", "'endsWith'",
    "'iendsWith'", "'='", "'eq'", "'>'", "'gt'", "'>='", "'ge'", "'gte'",
    "'<'", "'lt'", "'<='", "'le'", "'lte'", "'<>'", "'!='", "'ne'", "'ieq'",
    null, null, null, null, null, null, "'0'"
  };
  private static final String[] _SYMBOLIC_NAMES = {
    null, null, null, null, null, null, null, null, null, null, null, null,
    null, null, null, null, null, null, null, null, null, null, null, null,
    null, null, null, null, null, null, null, null, null, null, null, null,
    null, null, null, null, null, null, null, null, null, null, null, null,
    null, null, null, null, null, null, null, null, null, null, "INPUT_VARIABLE",
    "PATH_VARIABLE", "BOOLEAN_LITERAL", "NUMBER_LITERAL", "DOUBLE", "INT",
    "ZERO", "STRING_LITERAL", "WS"
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
  public String getGrammarFileName() {
    return "EQL.g4";
  }

  @Override
  public String[] getRuleNames() {
    return ruleNames;
  }

  @Override
  public String getSerializedATN() {
    return _serializedATN;
  }

  @Override
  public ATN getATN() {
    return _ATN;
  }


//  @Override
//  public void reportError(RecognitionException e) {
//    throw new RuntimeException("Error in grammer - " + e.getMessage());
//  }

  public EQLParser(TokenStream input) {
    super(input);
    _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
  }

  public static class Select_statementContext extends ParserRuleContext {
    public Select_clauseContext select_clause() {
      return getRuleContext(Select_clauseContext.class, 0);
    }

    public List<Fetch_clauseContext> fetch_clause() {
      return getRuleContexts(Fetch_clauseContext.class);
    }

    public Fetch_clauseContext fetch_clause(int i) {
      return getRuleContext(Fetch_clauseContext.class, i);
    }

    public Where_clauseContext where_clause() {
      return getRuleContext(Where_clauseContext.class, 0);
    }

    public Orderby_clauseContext orderby_clause() {
      return getRuleContext(Orderby_clauseContext.class, 0);
    }

    public Limit_clauseContext limit_clause() {
      return getRuleContext(Limit_clauseContext.class, 0);
    }

    public Select_statementContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_select_statement;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterSelect_statement(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitSelect_statement(this);
    }
  }

  public final Select_statementContext select_statement() throws RecognitionException {
    Select_statementContext _localctx = new Select_statementContext(_ctx, getState());
    enterRule(_localctx, 0, RULE_select_statement);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(81);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__0) {
          {
            setState(80);
            select_clause();
          }
        }

        setState(86);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == T__15) {
          {
            {
              setState(83);
              fetch_clause();
            }
          }
          setState(88);
          _errHandler.sync(this);
          _la = _input.LA(1);
        }
        setState(90);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__4) {
          {
            setState(89);
            where_clause();
          }
        }

        setState(93);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__5) {
          {
            setState(92);
            orderby_clause();
          }
        }

        setState(96);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__13) {
          {
            setState(95);
            limit_clause();
          }
        }

      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Select_clauseContext extends ParserRuleContext {
    public Fetch_property_groupContext fetch_property_group() {
      return getRuleContext(Fetch_property_groupContext.class, 0);
    }

    public DistinctContext distinct() {
      return getRuleContext(DistinctContext.class, 0);
    }

    public Select_clauseContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_select_clause;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterSelect_clause(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitSelect_clause(this);
    }
  }

  public final Select_clauseContext select_clause() throws RecognitionException {
    Select_clauseContext _localctx = new Select_clauseContext(_ctx, getState());
    enterRule(_localctx, 2, RULE_select_clause);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(98);
        match(T__0);
        setState(100);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__3) {
          {
            setState(99);
            distinct();
          }
        }

        setState(102);
        match(T__1);
        setState(103);
        fetch_property_group();
        setState(104);
        match(T__2);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class DistinctContext extends ParserRuleContext {
    public DistinctContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_distinct;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterDistinct(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitDistinct(this);
    }
  }

  public final DistinctContext distinct() throws RecognitionException {
    DistinctContext _localctx = new DistinctContext(_ctx, getState());
    enterRule(_localctx, 4, RULE_distinct);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(106);
        match(T__3);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Fetch_clauseContext extends ParserRuleContext {
    public Fetch_pathContext fetch_path() {
      return getRuleContext(Fetch_pathContext.class, 0);
    }

    public Fetch_clauseContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fetch_clause;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterFetch_clause(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitFetch_clause(this);
    }
  }

  public final Fetch_clauseContext fetch_clause() throws RecognitionException {
    Fetch_clauseContext _localctx = new Fetch_clauseContext(_ctx, getState());
    enterRule(_localctx, 6, RULE_fetch_clause);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(108);
        fetch_path();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Where_clauseContext extends ParserRuleContext {
    public Conditional_expressionContext conditional_expression() {
      return getRuleContext(Conditional_expressionContext.class, 0);
    }

    public Where_clauseContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_where_clause;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterWhere_clause(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitWhere_clause(this);
    }
  }

  public final Where_clauseContext where_clause() throws RecognitionException {
    Where_clauseContext _localctx = new Where_clauseContext(_ctx, getState());
    enterRule(_localctx, 8, RULE_where_clause);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(110);
        match(T__4);
        setState(111);
        conditional_expression();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Orderby_clauseContext extends ParserRuleContext {
    public List<Orderby_propertyContext> orderby_property() {
      return getRuleContexts(Orderby_propertyContext.class);
    }

    public Orderby_propertyContext orderby_property(int i) {
      return getRuleContext(Orderby_propertyContext.class, i);
    }

    public Orderby_clauseContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_orderby_clause;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterOrderby_clause(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitOrderby_clause(this);
    }
  }

  public final Orderby_clauseContext orderby_clause() throws RecognitionException {
    Orderby_clauseContext _localctx = new Orderby_clauseContext(_ctx, getState());
    enterRule(_localctx, 10, RULE_orderby_clause);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(113);
        match(T__5);
        setState(114);
        match(T__6);
        setState(115);
        orderby_property();
        setState(120);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == T__7) {
          {
            {
              setState(116);
              match(T__7);
              setState(117);
              orderby_property();
            }
          }
          setState(122);
          _errHandler.sync(this);
          _la = _input.LA(1);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Orderby_propertyContext extends ParserRuleContext {
    public TerminalNode PATH_VARIABLE() {
      return getToken(EQLParser.PATH_VARIABLE, 0);
    }

    public Asc_descContext asc_desc() {
      return getRuleContext(Asc_descContext.class, 0);
    }

    public Nulls_firstlastContext nulls_firstlast() {
      return getRuleContext(Nulls_firstlastContext.class, 0);
    }

    public Orderby_propertyContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_orderby_property;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterOrderby_property(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitOrderby_property(this);
    }
  }

  public final Orderby_propertyContext orderby_property() throws RecognitionException {
    Orderby_propertyContext _localctx = new Orderby_propertyContext(_ctx, getState());
    enterRule(_localctx, 12, RULE_orderby_property);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(123);
        match(PATH_VARIABLE);
        setState(125);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__11 || _la == T__12) {
          {
            setState(124);
            asc_desc();
          }
        }

        setState(128);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__8) {
          {
            setState(127);
            nulls_firstlast();
          }
        }

      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Nulls_firstlastContext extends ParserRuleContext {
    public Nulls_firstlastContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_nulls_firstlast;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterNulls_firstlast(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitNulls_firstlast(this);
    }
  }

  public final Nulls_firstlastContext nulls_firstlast() throws RecognitionException {
    Nulls_firstlastContext _localctx = new Nulls_firstlastContext(_ctx, getState());
    enterRule(_localctx, 14, RULE_nulls_firstlast);
    try {
      setState(134);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 9, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1);
        {
          setState(130);
          match(T__8);
          setState(131);
          match(T__9);
        }
        break;
        case 2:
          enterOuterAlt(_localctx, 2);
        {
          setState(132);
          match(T__8);
          setState(133);
          match(T__10);
        }
        break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Asc_descContext extends ParserRuleContext {
    public Asc_descContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_asc_desc;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterAsc_desc(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitAsc_desc(this);
    }
  }

  public final Asc_descContext asc_desc() throws RecognitionException {
    Asc_descContext _localctx = new Asc_descContext(_ctx, getState());
    enterRule(_localctx, 16, RULE_asc_desc);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(136);
        _la = _input.LA(1);
        if (!(_la == T__11 || _la == T__12)) {
          _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF) matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Limit_clauseContext extends ParserRuleContext {
    public TerminalNode NUMBER_LITERAL() {
      return getToken(EQLParser.NUMBER_LITERAL, 0);
    }

    public Offset_clauseContext offset_clause() {
      return getRuleContext(Offset_clauseContext.class, 0);
    }

    public Limit_clauseContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_limit_clause;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterLimit_clause(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitLimit_clause(this);
    }
  }

  public final Limit_clauseContext limit_clause() throws RecognitionException {
    Limit_clauseContext _localctx = new Limit_clauseContext(_ctx, getState());
    enterRule(_localctx, 18, RULE_limit_clause);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(138);
        match(T__13);
        setState(139);
        match(NUMBER_LITERAL);
        setState(141);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__14) {
          {
            setState(140);
            offset_clause();
          }
        }

      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Offset_clauseContext extends ParserRuleContext {
    public TerminalNode NUMBER_LITERAL() {
      return getToken(EQLParser.NUMBER_LITERAL, 0);
    }

    public Offset_clauseContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_offset_clause;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterOffset_clause(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitOffset_clause(this);
    }
  }

  public final Offset_clauseContext offset_clause() throws RecognitionException {
    Offset_clauseContext _localctx = new Offset_clauseContext(_ctx, getState());
    enterRule(_localctx, 20, RULE_offset_clause);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(143);
        match(T__14);
        setState(144);
        match(NUMBER_LITERAL);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Fetch_pathContext extends ParserRuleContext {
    public TerminalNode PATH_VARIABLE() {
      return getToken(EQLParser.PATH_VARIABLE, 0);
    }

    public Fetch_optionContext fetch_option() {
      return getRuleContext(Fetch_optionContext.class, 0);
    }

    public Fetch_property_setContext fetch_property_set() {
      return getRuleContext(Fetch_property_setContext.class, 0);
    }

    public Fetch_pathContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fetch_path;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterFetch_path(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitFetch_path(this);
    }
  }

  public final Fetch_pathContext fetch_path() throws RecognitionException {
    Fetch_pathContext _localctx = new Fetch_pathContext(_ctx, getState());
    enterRule(_localctx, 22, RULE_fetch_path);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(146);
        match(T__15);
        setState(148);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__17 || _la == T__18) {
          {
            setState(147);
            fetch_option();
          }
        }

        setState(150);
        match(PATH_VARIABLE);
        setState(152);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__1) {
          {
            setState(151);
            fetch_property_set();
          }
        }

      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Fetch_property_setContext extends ParserRuleContext {
    public Fetch_property_groupContext fetch_property_group() {
      return getRuleContext(Fetch_property_groupContext.class, 0);
    }

    public Fetch_property_setContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fetch_property_set;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterFetch_property_set(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitFetch_property_set(this);
    }
  }

  public final Fetch_property_setContext fetch_property_set() throws RecognitionException {
    Fetch_property_setContext _localctx = new Fetch_property_setContext(_ctx, getState());
    enterRule(_localctx, 24, RULE_fetch_property_set);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(154);
        match(T__1);
        setState(155);
        fetch_property_group();
        setState(156);
        match(T__2);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Fetch_property_groupContext extends ParserRuleContext {
    public List<Fetch_propertyContext> fetch_property() {
      return getRuleContexts(Fetch_propertyContext.class);
    }

    public Fetch_propertyContext fetch_property(int i) {
      return getRuleContext(Fetch_propertyContext.class, i);
    }

    public Fetch_property_groupContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fetch_property_group;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterFetch_property_group(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitFetch_property_group(this);
    }
  }

  public final Fetch_property_groupContext fetch_property_group() throws RecognitionException {
    Fetch_property_groupContext _localctx = new Fetch_property_groupContext(_ctx, getState());
    enterRule(_localctx, 26, RULE_fetch_property_group);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(158);
        fetch_property();
        setState(163);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == T__7) {
          {
            {
              setState(159);
              match(T__7);
              setState(160);
              fetch_property();
            }
          }
          setState(165);
          _errHandler.sync(this);
          _la = _input.LA(1);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Fetch_propertyContext extends ParserRuleContext {
    public TerminalNode PATH_VARIABLE() {
      return getToken(EQLParser.PATH_VARIABLE, 0);
    }

    public Fetch_query_hintContext fetch_query_hint() {
      return getRuleContext(Fetch_query_hintContext.class, 0);
    }

    public Fetch_lazy_hintContext fetch_lazy_hint() {
      return getRuleContext(Fetch_lazy_hintContext.class, 0);
    }

    public Fetch_propertyContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fetch_property;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterFetch_property(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitFetch_property(this);
    }
  }

  public final Fetch_propertyContext fetch_property() throws RecognitionException {
    Fetch_propertyContext _localctx = new Fetch_propertyContext(_ctx, getState());
    enterRule(_localctx, 28, RULE_fetch_property);
    try {
      setState(169);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 14, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1);
        {
          setState(166);
          match(PATH_VARIABLE);
        }
        break;
        case 2:
          enterOuterAlt(_localctx, 2);
        {
          setState(167);
          fetch_query_hint();
        }
        break;
        case 3:
          enterOuterAlt(_localctx, 3);
        {
          setState(168);
          fetch_lazy_hint();
        }
        break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Fetch_query_hintContext extends ParserRuleContext {
    public Fetch_query_optionContext fetch_query_option() {
      return getRuleContext(Fetch_query_optionContext.class, 0);
    }

    public Fetch_query_hintContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fetch_query_hint;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterFetch_query_hint(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitFetch_query_hint(this);
    }
  }

  public final Fetch_query_hintContext fetch_query_hint() throws RecognitionException {
    Fetch_query_hintContext _localctx = new Fetch_query_hintContext(_ctx, getState());
    enterRule(_localctx, 30, RULE_fetch_query_hint);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(171);
        match(T__16);
        setState(172);
        fetch_query_option();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Fetch_lazy_hintContext extends ParserRuleContext {
    public Fetch_lazy_optionContext fetch_lazy_option() {
      return getRuleContext(Fetch_lazy_optionContext.class, 0);
    }

    public Fetch_lazy_hintContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fetch_lazy_hint;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterFetch_lazy_hint(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitFetch_lazy_hint(this);
    }
  }

  public final Fetch_lazy_hintContext fetch_lazy_hint() throws RecognitionException {
    Fetch_lazy_hintContext _localctx = new Fetch_lazy_hintContext(_ctx, getState());
    enterRule(_localctx, 32, RULE_fetch_lazy_hint);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(174);
        match(T__16);
        setState(175);
        fetch_lazy_option();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Fetch_optionContext extends ParserRuleContext {
    public Fetch_query_optionContext fetch_query_option() {
      return getRuleContext(Fetch_query_optionContext.class, 0);
    }

    public Fetch_lazy_optionContext fetch_lazy_option() {
      return getRuleContext(Fetch_lazy_optionContext.class, 0);
    }

    public Fetch_optionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fetch_option;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterFetch_option(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitFetch_option(this);
    }
  }

  public final Fetch_optionContext fetch_option() throws RecognitionException {
    Fetch_optionContext _localctx = new Fetch_optionContext(_ctx, getState());
    enterRule(_localctx, 34, RULE_fetch_option);
    try {
      setState(179);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case T__17:
          enterOuterAlt(_localctx, 1);
        {
          setState(177);
          fetch_query_option();
        }
        break;
        case T__18:
          enterOuterAlt(_localctx, 2);
        {
          setState(178);
          fetch_lazy_option();
        }
        break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Fetch_query_optionContext extends ParserRuleContext {
    public Fetch_batch_sizeContext fetch_batch_size() {
      return getRuleContext(Fetch_batch_sizeContext.class, 0);
    }

    public Fetch_query_optionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fetch_query_option;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterFetch_query_option(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitFetch_query_option(this);
    }
  }

  public final Fetch_query_optionContext fetch_query_option() throws RecognitionException {
    Fetch_query_optionContext _localctx = new Fetch_query_optionContext(_ctx, getState());
    enterRule(_localctx, 36, RULE_fetch_query_option);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(181);
        match(T__17);
        setState(183);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__1) {
          {
            setState(182);
            fetch_batch_size();
          }
        }

      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Fetch_lazy_optionContext extends ParserRuleContext {
    public Fetch_batch_sizeContext fetch_batch_size() {
      return getRuleContext(Fetch_batch_sizeContext.class, 0);
    }

    public Fetch_lazy_optionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fetch_lazy_option;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterFetch_lazy_option(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitFetch_lazy_option(this);
    }
  }

  public final Fetch_lazy_optionContext fetch_lazy_option() throws RecognitionException {
    Fetch_lazy_optionContext _localctx = new Fetch_lazy_optionContext(_ctx, getState());
    enterRule(_localctx, 38, RULE_fetch_lazy_option);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(185);
        match(T__18);
        setState(187);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__1) {
          {
            setState(186);
            fetch_batch_size();
          }
        }

      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Fetch_batch_sizeContext extends ParserRuleContext {
    public TerminalNode NUMBER_LITERAL() {
      return getToken(EQLParser.NUMBER_LITERAL, 0);
    }

    public Fetch_batch_sizeContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_fetch_batch_size;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterFetch_batch_size(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitFetch_batch_size(this);
    }
  }

  public final Fetch_batch_sizeContext fetch_batch_size() throws RecognitionException {
    Fetch_batch_sizeContext _localctx = new Fetch_batch_sizeContext(_ctx, getState());
    enterRule(_localctx, 40, RULE_fetch_batch_size);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(189);
        match(T__1);
        setState(190);
        match(NUMBER_LITERAL);
        setState(191);
        match(T__2);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Conditional_expressionContext extends ParserRuleContext {
    public List<Conditional_termContext> conditional_term() {
      return getRuleContexts(Conditional_termContext.class);
    }

    public Conditional_termContext conditional_term(int i) {
      return getRuleContext(Conditional_termContext.class, i);
    }

    public Conditional_expressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_conditional_expression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterConditional_expression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitConditional_expression(this);
    }
  }

  public final Conditional_expressionContext conditional_expression() throws RecognitionException {
    Conditional_expressionContext _localctx = new Conditional_expressionContext(_ctx, getState());
    enterRule(_localctx, 42, RULE_conditional_expression);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(193);
        conditional_term();
        setState(198);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == T__19) {
          {
            {
              setState(194);
              match(T__19);
              setState(195);
              conditional_term();
            }
          }
          setState(200);
          _errHandler.sync(this);
          _la = _input.LA(1);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Conditional_termContext extends ParserRuleContext {
    public List<Conditional_factorContext> conditional_factor() {
      return getRuleContexts(Conditional_factorContext.class);
    }

    public Conditional_factorContext conditional_factor(int i) {
      return getRuleContext(Conditional_factorContext.class, i);
    }

    public Conditional_termContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_conditional_term;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterConditional_term(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitConditional_term(this);
    }
  }

  public final Conditional_termContext conditional_term() throws RecognitionException {
    Conditional_termContext _localctx = new Conditional_termContext(_ctx, getState());
    enterRule(_localctx, 44, RULE_conditional_term);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(201);
        conditional_factor();
        setState(206);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == T__20) {
          {
            {
              setState(202);
              match(T__20);
              setState(203);
              conditional_factor();
            }
          }
          setState(208);
          _errHandler.sync(this);
          _la = _input.LA(1);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Conditional_factorContext extends ParserRuleContext {
    public Conditional_primaryContext conditional_primary() {
      return getRuleContext(Conditional_primaryContext.class, 0);
    }

    public Conditional_factorContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_conditional_factor;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterConditional_factor(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitConditional_factor(this);
    }
  }

  public final Conditional_factorContext conditional_factor() throws RecognitionException {
    Conditional_factorContext _localctx = new Conditional_factorContext(_ctx, getState());
    enterRule(_localctx, 46, RULE_conditional_factor);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(210);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__21) {
          {
            setState(209);
            match(T__21);
          }
        }

        setState(212);
        conditional_primary();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Conditional_primaryContext extends ParserRuleContext {
    public Any_expressionContext any_expression() {
      return getRuleContext(Any_expressionContext.class, 0);
    }

    public Conditional_expressionContext conditional_expression() {
      return getRuleContext(Conditional_expressionContext.class, 0);
    }

    public Conditional_primaryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_conditional_primary;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterConditional_primary(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitConditional_primary(this);
    }
  }

  public final Conditional_primaryContext conditional_primary() throws RecognitionException {
    Conditional_primaryContext _localctx = new Conditional_primaryContext(_ctx, getState());
    enterRule(_localctx, 48, RULE_conditional_primary);
    try {
      setState(219);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 21, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1);
        {
          setState(214);
          any_expression();
        }
        break;
        case 2:
          enterOuterAlt(_localctx, 2);
        {
          setState(215);
          match(T__1);
          setState(216);
          conditional_expression();
          setState(217);
          match(T__2);
        }
        break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Any_expressionContext extends ParserRuleContext {
    public Comparison_expressionContext comparison_expression() {
      return getRuleContext(Comparison_expressionContext.class, 0);
    }

    public Like_expressionContext like_expression() {
      return getRuleContext(Like_expressionContext.class, 0);
    }

    public Between_expressionContext between_expression() {
      return getRuleContext(Between_expressionContext.class, 0);
    }

    public PropertyBetween_expressionContext propertyBetween_expression() {
      return getRuleContext(PropertyBetween_expressionContext.class, 0);
    }

    public In_expressionContext in_expression() {
      return getRuleContext(In_expressionContext.class, 0);
    }

    public IsNull_expressionContext isNull_expression() {
      return getRuleContext(IsNull_expressionContext.class, 0);
    }

    public IsNotNull_expressionContext isNotNull_expression() {
      return getRuleContext(IsNotNull_expressionContext.class, 0);
    }

    public IsEmpty_expressionContext isEmpty_expression() {
      return getRuleContext(IsEmpty_expressionContext.class, 0);
    }

    public IsNotEmpty_expressionContext isNotEmpty_expression() {
      return getRuleContext(IsNotEmpty_expressionContext.class, 0);
    }

    public Any_expressionContext any_expression() {
      return getRuleContext(Any_expressionContext.class, 0);
    }

    public Any_expressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_any_expression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterAny_expression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitAny_expression(this);
    }
  }

  public final Any_expressionContext any_expression() throws RecognitionException {
    Any_expressionContext _localctx = new Any_expressionContext(_ctx, getState());
    enterRule(_localctx, 50, RULE_any_expression);
    try {
      setState(234);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 22, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1);
        {
          setState(221);
          comparison_expression();
        }
        break;
        case 2:
          enterOuterAlt(_localctx, 2);
        {
          setState(222);
          like_expression();
        }
        break;
        case 3:
          enterOuterAlt(_localctx, 3);
        {
          setState(223);
          between_expression();
        }
        break;
        case 4:
          enterOuterAlt(_localctx, 4);
        {
          setState(224);
          propertyBetween_expression();
        }
        break;
        case 5:
          enterOuterAlt(_localctx, 5);
        {
          setState(225);
          in_expression();
        }
        break;
        case 6:
          enterOuterAlt(_localctx, 6);
        {
          setState(226);
          isNull_expression();
        }
        break;
        case 7:
          enterOuterAlt(_localctx, 7);
        {
          setState(227);
          isNotNull_expression();
        }
        break;
        case 8:
          enterOuterAlt(_localctx, 8);
        {
          setState(228);
          isEmpty_expression();
        }
        break;
        case 9:
          enterOuterAlt(_localctx, 9);
        {
          setState(229);
          isNotEmpty_expression();
        }
        break;
        case 10:
          enterOuterAlt(_localctx, 10);
        {
          setState(230);
          match(T__1);
          setState(231);
          any_expression();
          setState(232);
          match(T__2);
        }
        break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class In_expressionContext extends ParserRuleContext {
    public TerminalNode PATH_VARIABLE() {
      return getToken(EQLParser.PATH_VARIABLE, 0);
    }

    public In_valueContext in_value() {
      return getRuleContext(In_valueContext.class, 0);
    }

    public In_expressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_in_expression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterIn_expression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitIn_expression(this);
    }
  }

  public final In_expressionContext in_expression() throws RecognitionException {
    In_expressionContext _localctx = new In_expressionContext(_ctx, getState());
    enterRule(_localctx, 52, RULE_in_expression);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(236);
        match(PATH_VARIABLE);
        setState(237);
        match(T__22);
        setState(238);
        in_value();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class In_valueContext extends ParserRuleContext {
    public TerminalNode INPUT_VARIABLE() {
      return getToken(EQLParser.INPUT_VARIABLE, 0);
    }

    public List<Value_expressionContext> value_expression() {
      return getRuleContexts(Value_expressionContext.class);
    }

    public Value_expressionContext value_expression(int i) {
      return getRuleContext(Value_expressionContext.class, i);
    }

    public In_valueContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_in_value;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterIn_value(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitIn_value(this);
    }
  }

  public final In_valueContext in_value() throws RecognitionException {
    In_valueContext _localctx = new In_valueContext(_ctx, getState());
    enterRule(_localctx, 54, RULE_in_value);
    int _la;
    try {
      setState(252);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case INPUT_VARIABLE:
          enterOuterAlt(_localctx, 1);
        {
          setState(240);
          match(INPUT_VARIABLE);
        }
        break;
        case T__1:
          enterOuterAlt(_localctx, 2);
        {
          setState(241);
          match(T__1);
          setState(242);
          value_expression();
          setState(247);
          _errHandler.sync(this);
          _la = _input.LA(1);
          while (_la == T__7) {
            {
              {
                setState(243);
                match(T__7);
                setState(244);
                value_expression();
              }
            }
            setState(249);
            _errHandler.sync(this);
            _la = _input.LA(1);
          }
          setState(250);
          match(T__2);
        }
        break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Between_expressionContext extends ParserRuleContext {
    public TerminalNode PATH_VARIABLE() {
      return getToken(EQLParser.PATH_VARIABLE, 0);
    }

    public List<Value_expressionContext> value_expression() {
      return getRuleContexts(Value_expressionContext.class);
    }

    public Value_expressionContext value_expression(int i) {
      return getRuleContext(Value_expressionContext.class, i);
    }

    public Between_expressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_between_expression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterBetween_expression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitBetween_expression(this);
    }
  }

  public final Between_expressionContext between_expression() throws RecognitionException {
    Between_expressionContext _localctx = new Between_expressionContext(_ctx, getState());
    enterRule(_localctx, 56, RULE_between_expression);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(254);
        match(PATH_VARIABLE);
        setState(255);
        match(T__23);
        setState(256);
        value_expression();
        setState(257);
        match(T__20);
        setState(258);
        value_expression();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class PropertyBetween_expressionContext extends ParserRuleContext {
    public Value_expressionContext value_expression() {
      return getRuleContext(Value_expressionContext.class, 0);
    }

    public List<TerminalNode> PATH_VARIABLE() {
      return getTokens(EQLParser.PATH_VARIABLE);
    }

    public TerminalNode PATH_VARIABLE(int i) {
      return getToken(EQLParser.PATH_VARIABLE, i);
    }

    public PropertyBetween_expressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_propertyBetween_expression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterPropertyBetween_expression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitPropertyBetween_expression(this);
    }
  }

  public final PropertyBetween_expressionContext propertyBetween_expression() throws RecognitionException {
    PropertyBetween_expressionContext _localctx = new PropertyBetween_expressionContext(_ctx, getState());
    enterRule(_localctx, 58, RULE_propertyBetween_expression);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(260);
        value_expression();
        setState(261);
        match(T__23);
        setState(262);
        match(PATH_VARIABLE);
        setState(263);
        match(T__20);
        setState(264);
        match(PATH_VARIABLE);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class IsNull_expressionContext extends ParserRuleContext {
    public TerminalNode PATH_VARIABLE() {
      return getToken(EQLParser.PATH_VARIABLE, 0);
    }

    public IsNull_expressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_isNull_expression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterIsNull_expression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitIsNull_expression(this);
    }
  }

  public final IsNull_expressionContext isNull_expression() throws RecognitionException {
    IsNull_expressionContext _localctx = new IsNull_expressionContext(_ctx, getState());
    enterRule(_localctx, 60, RULE_isNull_expression);
    try {
      setState(271);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 25, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1);
        {
          setState(266);
          match(PATH_VARIABLE);
          setState(267);
          match(T__24);
          setState(268);
          match(T__25);
        }
        break;
        case 2:
          enterOuterAlt(_localctx, 2);
        {
          setState(269);
          match(PATH_VARIABLE);
          setState(270);
          match(T__26);
        }
        break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class IsNotNull_expressionContext extends ParserRuleContext {
    public TerminalNode PATH_VARIABLE() {
      return getToken(EQLParser.PATH_VARIABLE, 0);
    }

    public IsNotNull_expressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_isNotNull_expression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterIsNotNull_expression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitIsNotNull_expression(this);
    }
  }

  public final IsNotNull_expressionContext isNotNull_expression() throws RecognitionException {
    IsNotNull_expressionContext _localctx = new IsNotNull_expressionContext(_ctx, getState());
    enterRule(_localctx, 62, RULE_isNotNull_expression);
    try {
      setState(281);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 26, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1);
        {
          setState(273);
          match(PATH_VARIABLE);
          setState(274);
          match(T__24);
          setState(275);
          match(T__21);
          setState(276);
          match(T__25);
        }
        break;
        case 2:
          enterOuterAlt(_localctx, 2);
        {
          setState(277);
          match(PATH_VARIABLE);
          setState(278);
          match(T__27);
        }
        break;
        case 3:
          enterOuterAlt(_localctx, 3);
        {
          setState(279);
          match(PATH_VARIABLE);
          setState(280);
          match(T__28);
        }
        break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class IsEmpty_expressionContext extends ParserRuleContext {
    public TerminalNode PATH_VARIABLE() {
      return getToken(EQLParser.PATH_VARIABLE, 0);
    }

    public IsEmpty_expressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_isEmpty_expression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterIsEmpty_expression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitIsEmpty_expression(this);
    }
  }

  public final IsEmpty_expressionContext isEmpty_expression() throws RecognitionException {
    IsEmpty_expressionContext _localctx = new IsEmpty_expressionContext(_ctx, getState());
    enterRule(_localctx, 64, RULE_isEmpty_expression);
    try {
      setState(288);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 27, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1);
        {
          setState(283);
          match(PATH_VARIABLE);
          setState(284);
          match(T__24);
          setState(285);
          match(T__29);
        }
        break;
        case 2:
          enterOuterAlt(_localctx, 2);
        {
          setState(286);
          match(PATH_VARIABLE);
          setState(287);
          match(T__30);
        }
        break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class IsNotEmpty_expressionContext extends ParserRuleContext {
    public TerminalNode PATH_VARIABLE() {
      return getToken(EQLParser.PATH_VARIABLE, 0);
    }

    public IsNotEmpty_expressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_isNotEmpty_expression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterIsNotEmpty_expression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitIsNotEmpty_expression(this);
    }
  }

  public final IsNotEmpty_expressionContext isNotEmpty_expression() throws RecognitionException {
    IsNotEmpty_expressionContext _localctx = new IsNotEmpty_expressionContext(_ctx, getState());
    enterRule(_localctx, 66, RULE_isNotEmpty_expression);
    try {
      setState(298);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 28, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1);
        {
          setState(290);
          match(PATH_VARIABLE);
          setState(291);
          match(T__24);
          setState(292);
          match(T__21);
          setState(293);
          match(T__29);
        }
        break;
        case 2:
          enterOuterAlt(_localctx, 2);
        {
          setState(294);
          match(PATH_VARIABLE);
          setState(295);
          match(T__31);
        }
        break;
        case 3:
          enterOuterAlt(_localctx, 3);
        {
          setState(296);
          match(PATH_VARIABLE);
          setState(297);
          match(T__32);
        }
        break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Like_expressionContext extends ParserRuleContext {
    public TerminalNode PATH_VARIABLE() {
      return getToken(EQLParser.PATH_VARIABLE, 0);
    }

    public Like_opContext like_op() {
      return getRuleContext(Like_opContext.class, 0);
    }

    public Value_expressionContext value_expression() {
      return getRuleContext(Value_expressionContext.class, 0);
    }

    public Like_expressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_like_expression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterLike_expression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitLike_expression(this);
    }
  }

  public final Like_expressionContext like_expression() throws RecognitionException {
    Like_expressionContext _localctx = new Like_expressionContext(_ctx, getState());
    enterRule(_localctx, 68, RULE_like_expression);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(300);
        match(PATH_VARIABLE);
        setState(301);
        like_op();
        setState(302);
        value_expression();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Like_opContext extends ParserRuleContext {
    public Like_opContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_like_op;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterLike_op(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitLike_op(this);
    }
  }

  public final Like_opContext like_op() throws RecognitionException {
    Like_opContext _localctx = new Like_opContext(_ctx, getState());
    enterRule(_localctx, 70, RULE_like_op);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(304);
        _la = _input.LA(1);
        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__33) | (1L << T__34) | (1L << T__35) | (1L << T__36) | (1L << T__37) | (1L << T__38) | (1L << T__39) | (1L << T__40))) != 0))) {
          _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF) matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Comparison_expressionContext extends ParserRuleContext {
    public TerminalNode PATH_VARIABLE() {
      return getToken(EQLParser.PATH_VARIABLE, 0);
    }

    public Comparison_operatorContext comparison_operator() {
      return getRuleContext(Comparison_operatorContext.class, 0);
    }

    public Value_expressionContext value_expression() {
      return getRuleContext(Value_expressionContext.class, 0);
    }

    public Comparison_expressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_comparison_expression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterComparison_expression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitComparison_expression(this);
    }
  }

  public final Comparison_expressionContext comparison_expression() throws RecognitionException {
    Comparison_expressionContext _localctx = new Comparison_expressionContext(_ctx, getState());
    enterRule(_localctx, 72, RULE_comparison_expression);
    try {
      setState(314);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case PATH_VARIABLE:
          enterOuterAlt(_localctx, 1);
        {
          setState(306);
          match(PATH_VARIABLE);
          setState(307);
          comparison_operator();
          setState(308);
          value_expression();
        }
        break;
        case INPUT_VARIABLE:
        case BOOLEAN_LITERAL:
        case NUMBER_LITERAL:
        case STRING_LITERAL:
          enterOuterAlt(_localctx, 2);
        {
          setState(310);
          value_expression();
          setState(311);
          comparison_operator();
          setState(312);
          match(PATH_VARIABLE);
        }
        break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Comparison_operatorContext extends ParserRuleContext {
    public Comparison_operatorContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_comparison_operator;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterComparison_operator(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitComparison_operator(this);
    }
  }

  public final Comparison_operatorContext comparison_operator() throws RecognitionException {
    Comparison_operatorContext _localctx = new Comparison_operatorContext(_ctx, getState());
    enterRule(_localctx, 74, RULE_comparison_operator);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(316);
        _la = _input.LA(1);
        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__41) | (1L << T__42) | (1L << T__43) | (1L << T__44) | (1L << T__45) | (1L << T__46) | (1L << T__47) | (1L << T__48) | (1L << T__49) | (1L << T__50) | (1L << T__51) | (1L << T__52) | (1L << T__53) | (1L << T__54) | (1L << T__55) | (1L << T__56))) != 0))) {
          _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF) matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class Value_expressionContext extends ParserRuleContext {
    public LiteralContext literal() {
      return getRuleContext(LiteralContext.class, 0);
    }

    public TerminalNode INPUT_VARIABLE() {
      return getToken(EQLParser.INPUT_VARIABLE, 0);
    }

    public Value_expressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_value_expression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterValue_expression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitValue_expression(this);
    }
  }

  public final Value_expressionContext value_expression() throws RecognitionException {
    Value_expressionContext _localctx = new Value_expressionContext(_ctx, getState());
    enterRule(_localctx, 76, RULE_value_expression);
    try {
      setState(320);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case BOOLEAN_LITERAL:
        case NUMBER_LITERAL:
        case STRING_LITERAL:
          enterOuterAlt(_localctx, 1);
        {
          setState(318);
          literal();
        }
        break;
        case INPUT_VARIABLE:
          enterOuterAlt(_localctx, 2);
        {
          setState(319);
          match(INPUT_VARIABLE);
        }
        break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class LiteralContext extends ParserRuleContext {
    public TerminalNode STRING_LITERAL() {
      return getToken(EQLParser.STRING_LITERAL, 0);
    }

    public TerminalNode BOOLEAN_LITERAL() {
      return getToken(EQLParser.BOOLEAN_LITERAL, 0);
    }

    public TerminalNode NUMBER_LITERAL() {
      return getToken(EQLParser.NUMBER_LITERAL, 0);
    }

    public LiteralContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_literal;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).enterLiteral(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof EQLListener) ((EQLListener) listener).exitLiteral(this);
    }
  }

  public final LiteralContext literal() throws RecognitionException {
    LiteralContext _localctx = new LiteralContext(_ctx, getState());
    enterRule(_localctx, 78, RULE_literal);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(322);
        _la = _input.LA(1);
        if (!(((((_la - 60)) & ~0x3f) == 0 && ((1L << (_la - 60)) & ((1L << (BOOLEAN_LITERAL - 60)) | (1L << (NUMBER_LITERAL - 60)) | (1L << (STRING_LITERAL - 60)))) != 0))) {
          _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF) matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static final String _serializedATN =
    "\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3D\u0147\4\2\t\2\4" +
      "\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t" +
      "\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22" +
      "\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31" +
      "\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!" +
      "\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\3\2\5\2T\n\2\3" +
      "\2\7\2W\n\2\f\2\16\2Z\13\2\3\2\5\2]\n\2\3\2\5\2`\n\2\3\2\5\2c\n\2\3\3" +
      "\3\3\5\3g\n\3\3\3\3\3\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7" +
      "\3\7\3\7\7\7y\n\7\f\7\16\7|\13\7\3\b\3\b\5\b\u0080\n\b\3\b\5\b\u0083\n" +
      "\b\3\t\3\t\3\t\3\t\5\t\u0089\n\t\3\n\3\n\3\13\3\13\3\13\5\13\u0090\n\13" +
      "\3\f\3\f\3\f\3\r\3\r\5\r\u0097\n\r\3\r\3\r\5\r\u009b\n\r\3\16\3\16\3\16" +
      "\3\16\3\17\3\17\3\17\7\17\u00a4\n\17\f\17\16\17\u00a7\13\17\3\20\3\20" +
      "\3\20\5\20\u00ac\n\20\3\21\3\21\3\21\3\22\3\22\3\22\3\23\3\23\5\23\u00b6" +
      "\n\23\3\24\3\24\5\24\u00ba\n\24\3\25\3\25\5\25\u00be\n\25\3\26\3\26\3" +
      "\26\3\26\3\27\3\27\3\27\7\27\u00c7\n\27\f\27\16\27\u00ca\13\27\3\30\3" +
      "\30\3\30\7\30\u00cf\n\30\f\30\16\30\u00d2\13\30\3\31\5\31\u00d5\n\31\3" +
      "\31\3\31\3\32\3\32\3\32\3\32\3\32\5\32\u00de\n\32\3\33\3\33\3\33\3\33" +
      "\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\5\33\u00ed\n\33\3\34\3\34" +
      "\3\34\3\34\3\35\3\35\3\35\3\35\3\35\7\35\u00f8\n\35\f\35\16\35\u00fb\13" +
      "\35\3\35\3\35\5\35\u00ff\n\35\3\36\3\36\3\36\3\36\3\36\3\36\3\37\3\37" +
      "\3\37\3\37\3\37\3\37\3 \3 \3 \3 \3 \5 \u0112\n \3!\3!\3!\3!\3!\3!\3!\3" +
      "!\5!\u011c\n!\3\"\3\"\3\"\3\"\3\"\5\"\u0123\n\"\3#\3#\3#\3#\3#\3#\3#\3" +
      "#\5#\u012d\n#\3$\3$\3$\3$\3%\3%\3&\3&\3&\3&\3&\3&\3&\3&\5&\u013d\n&\3" +
      "\'\3\'\3(\3(\5(\u0143\n(\3)\3)\3)\2\2*\2\4\6\b\n\f\16\20\22\24\26\30\32" +
      "\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNP\2\6\3\2\16\17\3\2$+\3\2,;\4\2" +
      ">?CC\u0148\2S\3\2\2\2\4d\3\2\2\2\6l\3\2\2\2\bn\3\2\2\2\np\3\2\2\2\fs\3" +
      "\2\2\2\16}\3\2\2\2\20\u0088\3\2\2\2\22\u008a\3\2\2\2\24\u008c\3\2\2\2" +
      "\26\u0091\3\2\2\2\30\u0094\3\2\2\2\32\u009c\3\2\2\2\34\u00a0\3\2\2\2\36" +
      "\u00ab\3\2\2\2 \u00ad\3\2\2\2\"\u00b0\3\2\2\2$\u00b5\3\2\2\2&\u00b7\3" +
      "\2\2\2(\u00bb\3\2\2\2*\u00bf\3\2\2\2,\u00c3\3\2\2\2.\u00cb\3\2\2\2\60" +
      "\u00d4\3\2\2\2\62\u00dd\3\2\2\2\64\u00ec\3\2\2\2\66\u00ee\3\2\2\28\u00fe" +
      "\3\2\2\2:\u0100\3\2\2\2<\u0106\3\2\2\2>\u0111\3\2\2\2@\u011b\3\2\2\2B" +
      "\u0122\3\2\2\2D\u012c\3\2\2\2F\u012e\3\2\2\2H\u0132\3\2\2\2J\u013c\3\2" +
      "\2\2L\u013e\3\2\2\2N\u0142\3\2\2\2P\u0144\3\2\2\2RT\5\4\3\2SR\3\2\2\2" +
      "ST\3\2\2\2TX\3\2\2\2UW\5\b\5\2VU\3\2\2\2WZ\3\2\2\2XV\3\2\2\2XY\3\2\2\2" +
      "Y\\\3\2\2\2ZX\3\2\2\2[]\5\n\6\2\\[\3\2\2\2\\]\3\2\2\2]_\3\2\2\2^`\5\f" +
      "\7\2_^\3\2\2\2_`\3\2\2\2`b\3\2\2\2ac\5\24\13\2ba\3\2\2\2bc\3\2\2\2c\3" +
      "\3\2\2\2df\7\3\2\2eg\5\6\4\2fe\3\2\2\2fg\3\2\2\2gh\3\2\2\2hi\7\4\2\2i" +
      "j\5\34\17\2jk\7\5\2\2k\5\3\2\2\2lm\7\6\2\2m\7\3\2\2\2no\5\30\r\2o\t\3" +
      "\2\2\2pq\7\7\2\2qr\5,\27\2r\13\3\2\2\2st\7\b\2\2tu\7\t\2\2uz\5\16\b\2" +
      "vw\7\n\2\2wy\5\16\b\2xv\3\2\2\2y|\3\2\2\2zx\3\2\2\2z{\3\2\2\2{\r\3\2\2" +
      "\2|z\3\2\2\2}\177\7=\2\2~\u0080\5\22\n\2\177~\3\2\2\2\177\u0080\3\2\2" +
      "\2\u0080\u0082\3\2\2\2\u0081\u0083\5\20\t\2\u0082\u0081\3\2\2\2\u0082" +
      "\u0083\3\2\2\2\u0083\17\3\2\2\2\u0084\u0085\7\13\2\2\u0085\u0089\7\f\2" +
      "\2\u0086\u0087\7\13\2\2\u0087\u0089\7\r\2\2\u0088\u0084\3\2\2\2\u0088" +
      "\u0086\3\2\2\2\u0089\21\3\2\2\2\u008a\u008b\t\2\2\2\u008b\23\3\2\2\2\u008c" +
      "\u008d\7\20\2\2\u008d\u008f\7?\2\2\u008e\u0090\5\26\f\2\u008f\u008e\3" +
      "\2\2\2\u008f\u0090\3\2\2\2\u0090\25\3\2\2\2\u0091\u0092\7\21\2\2\u0092" +
      "\u0093\7?\2\2\u0093\27\3\2\2\2\u0094\u0096\7\22\2\2\u0095\u0097\5$\23" +
      "\2\u0096\u0095\3\2\2\2\u0096\u0097\3\2\2\2\u0097\u0098\3\2\2\2\u0098\u009a" +
      "\7=\2\2\u0099\u009b\5\32\16\2\u009a\u0099\3\2\2\2\u009a\u009b\3\2\2\2" +
      "\u009b\31\3\2\2\2\u009c\u009d\7\4\2\2\u009d\u009e\5\34\17\2\u009e\u009f" +
      "\7\5\2\2\u009f\33\3\2\2\2\u00a0\u00a5\5\36\20\2\u00a1\u00a2\7\n\2\2\u00a2" +
      "\u00a4\5\36\20\2\u00a3\u00a1\3\2\2\2\u00a4\u00a7\3\2\2\2\u00a5\u00a3\3" +
      "\2\2\2\u00a5\u00a6\3\2\2\2\u00a6\35\3\2\2\2\u00a7\u00a5\3\2\2\2\u00a8" +
      "\u00ac\7=\2\2\u00a9\u00ac\5 \21\2\u00aa\u00ac\5\"\22\2\u00ab\u00a8\3\2" +
      "\2\2\u00ab\u00a9\3\2\2\2\u00ab\u00aa\3\2\2\2\u00ac\37\3\2\2\2\u00ad\u00ae" +
      "\7\23\2\2\u00ae\u00af\5&\24\2\u00af!\3\2\2\2\u00b0\u00b1\7\23\2\2\u00b1" +
      "\u00b2\5(\25\2\u00b2#\3\2\2\2\u00b3\u00b6\5&\24\2\u00b4\u00b6\5(\25\2" +
      "\u00b5\u00b3\3\2\2\2\u00b5\u00b4\3\2\2\2\u00b6%\3\2\2\2\u00b7\u00b9\7" +
      "\24\2\2\u00b8\u00ba\5*\26\2\u00b9\u00b8\3\2\2\2\u00b9\u00ba\3\2\2\2\u00ba" +
      "\'\3\2\2\2\u00bb\u00bd\7\25\2\2\u00bc\u00be\5*\26\2\u00bd\u00bc\3\2\2" +
      "\2\u00bd\u00be\3\2\2\2\u00be)\3\2\2\2\u00bf\u00c0\7\4\2\2\u00c0\u00c1" +
      "\7?\2\2\u00c1\u00c2\7\5\2\2\u00c2+\3\2\2\2\u00c3\u00c8\5.\30\2\u00c4\u00c5" +
      "\7\26\2\2\u00c5\u00c7\5.\30\2\u00c6\u00c4\3\2\2\2\u00c7\u00ca\3\2\2\2" +
      "\u00c8\u00c6\3\2\2\2\u00c8\u00c9\3\2\2\2\u00c9-\3\2\2\2\u00ca\u00c8\3" +
      "\2\2\2\u00cb\u00d0\5\60\31\2\u00cc\u00cd\7\27\2\2\u00cd\u00cf\5\60\31" +
      "\2\u00ce\u00cc\3\2\2\2\u00cf\u00d2\3\2\2\2\u00d0\u00ce\3\2\2\2\u00d0\u00d1" +
      "\3\2\2\2\u00d1/\3\2\2\2\u00d2\u00d0\3\2\2\2\u00d3\u00d5\7\30\2\2\u00d4" +
      "\u00d3\3\2\2\2\u00d4\u00d5\3\2\2\2\u00d5\u00d6\3\2\2\2\u00d6\u00d7\5\62" +
      "\32\2\u00d7\61\3\2\2\2\u00d8\u00de\5\64\33\2\u00d9\u00da\7\4\2\2\u00da" +
      "\u00db\5,\27\2\u00db\u00dc\7\5\2\2\u00dc\u00de\3\2\2\2\u00dd\u00d8\3\2" +
      "\2\2\u00dd\u00d9\3\2\2\2\u00de\63\3\2\2\2\u00df\u00ed\5J&\2\u00e0\u00ed" +
      "\5F$\2\u00e1\u00ed\5:\36\2\u00e2\u00ed\5<\37\2\u00e3\u00ed\5\66\34\2\u00e4" +
      "\u00ed\5> \2\u00e5\u00ed\5@!\2\u00e6\u00ed\5B\"\2\u00e7\u00ed\5D#\2\u00e8" +
      "\u00e9\7\4\2\2\u00e9\u00ea\5\64\33\2\u00ea\u00eb\7\5\2\2\u00eb\u00ed\3" +
      "\2\2\2\u00ec\u00df\3\2\2\2\u00ec\u00e0\3\2\2\2\u00ec\u00e1\3\2\2\2\u00ec" +
      "\u00e2\3\2\2\2\u00ec\u00e3\3\2\2\2\u00ec\u00e4\3\2\2\2\u00ec\u00e5\3\2" +
      "\2\2\u00ec\u00e6\3\2\2\2\u00ec\u00e7\3\2\2\2\u00ec\u00e8\3\2\2\2\u00ed" +
      "\65\3\2\2\2\u00ee\u00ef\7=\2\2\u00ef\u00f0\7\31\2\2\u00f0\u00f1\58\35" +
      "\2\u00f1\67\3\2\2\2\u00f2\u00ff\7<\2\2\u00f3\u00f4\7\4\2\2\u00f4\u00f9" +
      "\5N(\2\u00f5\u00f6\7\n\2\2\u00f6\u00f8\5N(\2\u00f7\u00f5\3\2\2\2\u00f8" +
      "\u00fb\3\2\2\2\u00f9\u00f7\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa\u00fc\3\2" +
      "\2\2\u00fb\u00f9\3\2\2\2\u00fc\u00fd\7\5\2\2\u00fd\u00ff\3\2\2\2\u00fe" +
      "\u00f2\3\2\2\2\u00fe\u00f3\3\2\2\2\u00ff9\3\2\2\2\u0100\u0101\7=\2\2\u0101" +
      "\u0102\7\32\2\2\u0102\u0103\5N(\2\u0103\u0104\7\27\2\2\u0104\u0105\5N" +
      "(\2\u0105;\3\2\2\2\u0106\u0107\5N(\2\u0107\u0108\7\32\2\2\u0108\u0109" +
      "\7=\2\2\u0109\u010a\7\27\2\2\u010a\u010b\7=\2\2\u010b=\3\2\2\2\u010c\u010d" +
      "\7=\2\2\u010d\u010e\7\33\2\2\u010e\u0112\7\34\2\2\u010f\u0110\7=\2\2\u0110" +
      "\u0112\7\35\2\2\u0111\u010c\3\2\2\2\u0111\u010f\3\2\2\2\u0112?\3\2\2\2" +
      "\u0113\u0114\7=\2\2\u0114\u0115\7\33\2\2\u0115\u0116\7\30\2\2\u0116\u011c" +
      "\7\34\2\2\u0117\u0118\7=\2\2\u0118\u011c\7\36\2\2\u0119\u011a\7=\2\2\u011a" +
      "\u011c\7\37\2\2\u011b\u0113\3\2\2\2\u011b\u0117\3\2\2\2\u011b\u0119\3" +
      "\2\2\2\u011cA\3\2\2\2\u011d\u011e\7=\2\2\u011e\u011f\7\33\2\2\u011f\u0123" +
      "\7 \2\2\u0120\u0121\7=\2\2\u0121\u0123\7!\2\2\u0122\u011d\3\2\2\2\u0122" +
      "\u0120\3\2\2\2\u0123C\3\2\2\2\u0124\u0125\7=\2\2\u0125\u0126\7\33\2\2" +
      "\u0126\u0127\7\30\2\2\u0127\u012d\7 \2\2\u0128\u0129\7=\2\2\u0129\u012d" +
      "\7\"\2\2\u012a\u012b\7=\2\2\u012b\u012d\7#\2\2\u012c\u0124\3\2\2\2\u012c" +
      "\u0128\3\2\2\2\u012c\u012a\3\2\2\2\u012dE\3\2\2\2\u012e\u012f\7=\2\2\u012f" +
      "\u0130\5H%\2\u0130\u0131\5N(\2\u0131G\3\2\2\2\u0132\u0133\t\3\2\2\u0133" +
      "I\3\2\2\2\u0134\u0135\7=\2\2\u0135\u0136\5L\'\2\u0136\u0137\5N(\2\u0137" +
      "\u013d\3\2\2\2\u0138\u0139\5N(\2\u0139\u013a\5L\'\2\u013a\u013b\7=\2\2" +
      "\u013b\u013d\3\2\2\2\u013c\u0134\3\2\2\2\u013c\u0138\3\2\2\2\u013dK\3" +
      "\2\2\2\u013e\u013f\t\4\2\2\u013fM\3\2\2\2\u0140\u0143\5P)\2\u0141\u0143" +
      "\7<\2\2\u0142\u0140\3\2\2\2\u0142\u0141\3\2\2\2\u0143O\3\2\2\2\u0144\u0145" +
      "\t\5\2\2\u0145Q\3\2\2\2!SX\\_bfz\177\u0082\u0088\u008f\u0096\u009a\u00a5" +
      "\u00ab\u00b5\u00b9\u00bd\u00c8\u00d0\u00d4\u00dd\u00ec\u00f9\u00fe\u0111" +
      "\u011b\u0122\u012c\u013c\u0142";
  public static final ATN _ATN =
    new ATNDeserializer().deserialize(_serializedATN.toCharArray());

  static {
    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
      _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
    }
  }
}
