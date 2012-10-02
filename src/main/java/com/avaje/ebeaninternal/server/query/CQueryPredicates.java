package com.avaje.ebeaninternal.server.query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.BindParams.OrderedList;
import com.avaje.ebeaninternal.api.SpiExpressionList;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DeployParser;
import com.avaje.ebeaninternal.server.persist.Binder;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryProperties;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.util.BindParamsParser;
import com.avaje.ebeaninternal.util.DefaultExpressionRequest;

/**
 * Compile Query Predicates.
 * <p>
 * This includes the where and having expressions which can be made up of
 * Strings with named parameters or Expression objects.
 * </p>
 * <p>
 * This builds the appropriate bits of where and having clauses and binds the
 * named parameters and expression values into the prepared statement.
 * </p>
 */
public class CQueryPredicates {

  private static final Logger logger = Logger.getLogger(CQueryPredicates.class.getName());

  private final Binder binder;

  private final OrmQueryRequest<?> request;

  private final SpiQuery<?> query;

  private final Object idValue;

  /**
   * Flag set if this is a SqlSelect type query.
   */
  private boolean rawSql;

  /**
   * Named bind parameters.
   */
  private final BindParams bindParams;

  /**
   * Named bind parameters for the having clause.
   */
  private OrderedList havingNamedParams;

  /**
   * Bind values from the where expressions.
   */
  private ArrayList<Object> filterManyExprBindValues;

  /**
   * SQL generated from the where expressions.
   */
  private String filterManyExprSql;

  /**
   * Bind values from the where expressions.
   */
  private ArrayList<Object> whereExprBindValues;

  /**
   * SQL generated from the where expressions.
   */
  private String whereExprSql;

  /**
   * SQL generated from where with named parameters.
   */
  private String whereRawSql;

  /**
   * Bind values for having expression.
   */
  private ArrayList<Object> havingExprBindValues;

  /**
   * SQL generated from the having expression.
   */
  private String havingExprSql;

  /**
   * SQL generated from having with named parameters.
   */
  private String havingRawSql;

  private String dbHaving;

  /**
   * logicalWhere with property names converted to db columns.
   */
  private String dbWhere;

  /**
   * Filter than can apply to a many fetch join.
   */
  private String dbFilterMany;

  /**
   * The order by clause.
   */
  private String logicalOrderBy;

  private String dbOrderBy;

  /**
   * Includes from where and order by clauses.
   */
  private Set<String> predicateIncludes;

  public CQueryPredicates(Binder binder, OrmQueryRequest<?> request) {
    this.binder = binder;
    this.request = request;
    this.query = request.getQuery();
    this.bindParams = query.getBindParams();
    this.idValue = query.getId();
  }

  public String bind(DataBind dataBind) throws SQLException {

    StringBuilder bindLog = new StringBuilder();

    if (idValue != null) {
      // this is a find by id type query...
      request.getBeanDescriptor().bindId(dataBind, idValue);
      bindLog.append(idValue);
    }

    if (bindParams != null) {
      // bind named and positioned parameters...
      binder.bind(bindParams, dataBind, bindLog);
    }

    if (whereExprBindValues != null) {

      for (int i = 0; i < whereExprBindValues.size(); i++) {
        Object bindValue = whereExprBindValues.get(i);
        binder.bindObject(dataBind, bindValue);
        if (i > 0 || idValue != null) {
          bindLog.append(", ");
        }
        bindLog.append(bindValue);
      }
    }

    if (filterManyExprBindValues != null) {

      for (int i = 0; i < filterManyExprBindValues.size(); i++) {
        Object bindValue = filterManyExprBindValues.get(i);
        binder.bindObject(dataBind, bindValue);
        if (i > 0 || idValue != null) {
          bindLog.append(", ");
        }
        bindLog.append(bindValue);
      }
    }

    if (havingNamedParams != null) {
      // bind named parameters in having...
      bindLog.append(" havingNamed ");
      binder.bind(havingNamedParams.list(), dataBind, bindLog);
    }

    if (havingExprBindValues != null) {
      // bind having expression...
      bindLog.append(" having ");
      for (int i = 0; i < havingExprBindValues.size(); i++) {
        Object bindValue = havingExprBindValues.get(i);
        binder.bindObject(dataBind, bindValue);
        if (i > 0) {
          bindLog.append(", ");
        }
        bindLog.append(bindValue);
      }
    }

    return bindLog.toString();
  }

  private void buildBindHavingRawSql(boolean buildSql, boolean parseRaw, DeployParser deployParser) {
    if (buildSql || bindParams != null) {
      // having clause with named parameters...
      havingRawSql = query.getAdditionalHaving();
      if (parseRaw) {
        havingRawSql = deployParser.parse(havingRawSql);
      }
      if (havingRawSql != null && bindParams != null) {
        // convert and order named parameters if required
        havingNamedParams = BindParamsParser.parseNamedParams(bindParams, havingRawSql);
        havingRawSql = havingNamedParams.getPreparedSql();
      }
    }
  }

  /**
   * Convert named parameters into an OrderedList.
   */
  private void buildBindWhereRawSql(boolean buildSql, boolean parseRaw, DeployParser parser) {
    if (buildSql || bindParams != null) {
      whereRawSql = buildWhereRawSql();
      boolean hasRaw = !"".equals(whereRawSql);
      if (hasRaw && parseRaw) {
        // parse with encrypted property awareness. This means that if we have
        // an
        // encrypted property we will insert special named parameter place
        // holders
        // for binding the encryption key values
        parser.setEncrypted(true);
        whereRawSql = parser.parse(whereRawSql);
        parser.setEncrypted(false);
      }

      if (bindParams != null) {
        if (hasRaw) {
          whereRawSql = BindParamsParser.parse(bindParams, whereRawSql, request.getBeanDescriptor());

        } else if (query.isRawSql() && !buildSql) {
          // RawSql query hit cached query plan. Need to convert
          // named parameters into positioned parameters so that
          // the named parameters are bound
          String s = query.getRawSql().getSql().getPreWhere();
          if (bindParams.requiresNamedParamsPrepare()) {
            BindParamsParser.parse(bindParams, s);
          }
        }
      }
    }
  }

  private String buildWhereRawSql() {
    // this is the where part of a OQL query which
    // may contain bind parameters...
    String whereRaw = query.getRawWhereClause();
    if (whereRaw == null) {
      whereRaw = "";
    }
    // add any additional stuff to the where clause
    String additionalWhere = query.getAdditionalWhere();
    if (additionalWhere != null) {
      whereRaw += additionalWhere;
    }
    return whereRaw;
  }

  public void prepare(boolean buildSql) {

    DeployParser deployParser = request.createDeployParser();

    prepare(buildSql, true, deployParser);
  }

  public void prepareRawSql(DeployParser deployParser) {
    prepare(true, false, deployParser);
  }

  /**
   * This combines the sql from named/positioned parameters and expressions.
   */
  private void prepare(boolean buildSql, boolean parseRaw, DeployParser deployParser) {

    buildBindWhereRawSql(buildSql, parseRaw, deployParser);
    buildBindHavingRawSql(buildSql, parseRaw, deployParser);

    SpiExpressionList<?> whereExp = query.getWhereExpressions();
    if (whereExp != null) {
      DefaultExpressionRequest whereReq = new DefaultExpressionRequest(request, deployParser);
      whereExprBindValues = whereExp.buildBindValues(whereReq);
      if (buildSql) {
        whereExprSql = whereExp.buildSql(whereReq);
      }
    }

    BeanPropertyAssocMany<?> manyProperty = request.getManyProperty();
    if (manyProperty != null) {
      OrmQueryProperties chunk = query.getDetail().getChunk(manyProperty.getName(), false);
      SpiExpressionList<?> filterMany = chunk.getFilterMany();
      if (filterMany != null) {
        DefaultExpressionRequest filterReq = new DefaultExpressionRequest(request, deployParser);
        filterManyExprBindValues = filterMany.buildBindValues(filterReq);
        if (buildSql) {
          filterManyExprSql = filterMany.buildSql(filterReq);
        }
      }
    }

    // having expression
    SpiExpressionList<?> havingExpr = query.getHavingExpressions();
    if (havingExpr != null) {
      DefaultExpressionRequest havingReq = new DefaultExpressionRequest(request, deployParser);
      havingExprBindValues = havingExpr.buildBindValues(havingReq);
      if (buildSql) {
        havingExprSql = havingExpr.buildSql(havingReq);
      }
    }

    if (buildSql) {
      parsePropertiesToDbColumns(deployParser);
    }

  }

  /**
   * Parse/Convert property names to database columns in the where and order by
   * clauses etc.
   */
  private void parsePropertiesToDbColumns(DeployParser deployParser) {

    dbWhere = deriveWhere(deployParser);
    dbFilterMany = deriveFilterMany(deployParser);
    dbHaving = deriveHaving(deployParser);

    // order by is dependent on the manyProperty (if there is one)
    logicalOrderBy = deriveOrderByWithMany(request.getManyProperty());
    if (logicalOrderBy != null) {
      dbOrderBy = deployParser.parse(logicalOrderBy);
    }

    predicateIncludes = deployParser.getIncludes();
  }

  private String deriveFilterMany(DeployParser deployParser) {
    if (isEmpty(filterManyExprSql)) {
      return null;
    } else {
      return deployParser.parse(filterManyExprSql);
    }
  }

  private String deriveWhere(DeployParser deployParser) {
    return parse(whereRawSql, whereExprSql, deployParser);
  }

  /**
   * Replace the table alias place holders.
   */
  public void parseTableAlias(SqlTreeAlias alias) {
    if (dbWhere != null) {
      // use the where table alias
      dbWhere = alias.parseWhere(dbWhere);
    }
    if (dbFilterMany != null) {
      // use the select table alias
      dbFilterMany = alias.parse(dbFilterMany);
    }
    if (dbHaving != null) {
      dbHaving = alias.parseWhere(dbHaving);
    }
    if (dbOrderBy != null) {
      dbOrderBy = alias.parse(dbOrderBy);
    }
  }

  private boolean isEmpty(String s) {
    return s == null || s.length() == 0;
  }

  private String parse(String raw, String expr, DeployParser deployParser) {

    StringBuilder sb = new StringBuilder();
    if (!isEmpty(raw)) {
      sb.append(raw);
    }
    if (!isEmpty(expr)) {
      if (sb.length() > 0) {
        sb.append(" and ");
      }
      sb.append(deployParser.parse(expr));
    }
    return sb.toString();
  }

  private String deriveHaving(DeployParser deployParser) {
    return parse(havingRawSql, havingExprSql, deployParser);
  }

  private String parseOrderBy() {

    return CQueryOrderBy.parse(request.getBeanDescriptor(), query);
  }

  /**
   * There is a many property so we need to make sure the ordering is
   * appropriate.
   */
  private String deriveOrderByWithMany(BeanPropertyAssocMany<?> manyProp) {

    if (manyProp == null) {
      return parseOrderBy();
    }

    String orderBy = parseOrderBy();

    BeanDescriptor<?> desc = request.getBeanDescriptor();
    String orderById = desc.getDefaultOrderBy();

    if (orderBy == null) {
      orderBy = orderById;
    }

    // check for default ordering on the many property...
    String manyOrderBy = manyProp.getFetchOrderBy();
    if (manyOrderBy != null) {
      orderBy = orderBy + ", " + CQueryBuilder.prefixOrderByFields(manyProp.getName(), manyOrderBy);
    }

    if (request.isFindById()) {
      // only one master bean so should be fine...
      return orderBy;
    }

    if (orderBy.startsWith(orderById)) {
      return orderBy;
    }

    // more than one top level row may be returned so
    // we need to make sure their is an order by on the
    // top level first (to ensure master/detail construction).

    int manyPos = orderBy.indexOf(manyProp.getName());
    int idPos = orderBy.indexOf(" " + orderById);

    if (manyPos == -1) {
      // no ordering of the many
      if (idPos == -1) {
        // append the orderById so that master level objects are ordered
        // even if the orderBy is not unique for the master object
        return orderBy + ", " + orderById;
      }
      // orderById is already in the order by clause
      return orderBy;
    }

    if (idPos > -1 && idPos < manyPos) {
      // its all ok, id property appears before a many property

    } else {
      if (idPos > manyPos) {
        // there was an error with the order by...
        String msg = "A Query on [" + desc + "] includes a join to a 'many' association [" + manyProp.getName();
        msg += "] with an incorrect orderBy [" + orderBy + "]. The id property [" + orderById + "]";
        msg += " must come before the many property [" + manyProp.getName() + "] in the orderBy.";
        msg += " Ebean has automatically modified the orderBy clause to do this.";

        logger.log(Level.WARNING, msg);
      }

      // the id needs to come before the manyPropName
      orderBy = orderBy.substring(0, manyPos) + orderById + ", " + orderBy.substring(manyPos);
    }

    return orderBy;
  }

  /**
   * Return the bind values for the where expression.
   */
  public ArrayList<Object> getWhereExprBindValues() {
    return whereExprBindValues;
  }

  /**
   * Return the db column version of the combined where clause.
   */
  public String getDbHaving() {
    return dbHaving;
  }

  /**
   * Return the db column version of the combined where clause.
   */
  public String getDbWhere() {
    return dbWhere;
  }

  /**
   * Return a db filter for filtering many fetch joins.
   */
  public String getDbFilterMany() {
    return dbFilterMany;
  }

  /**
   * Return the db column version of the order by clause.
   */
  public String getDbOrderBy() {
    return dbOrderBy;
  }

  /**
   * Return the includes required for the where and order by clause.
   */
  public Set<String> getPredicateIncludes() {
    return predicateIncludes;
  }

  /**
   * The where sql with named bind parameters converted to ?.
   */
  public String getWhereRawSql() {
    return whereRawSql;
  }

  /**
   * The where sql from the expression objects.
   */
  public String getWhereExpressionSql() {
    return whereExprSql;
  }

  /**
   * The having sql with named bind parameters converted to ?.
   */
  public String getHavingRawSql() {
    return havingRawSql;
  }

  /**
   * The having sql from the expression objects.
   */
  public String getHavingExpressionSql() {
    return havingExprSql;
  }

  public String getLogWhereSql() {
    if (rawSql) {
      return "";
    }
    if (dbWhere == null && dbFilterMany == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    if (dbWhere != null) {
      sb.append(dbWhere);
    }
    if (dbFilterMany != null) {
      if (sb.length() > 0) {
        sb.append(" and ");
      }
      sb.append(dbFilterMany);
    }
    String logPred = sb.toString();
    if (logPred.length() > 400) {
      logPred = logPred.substring(0, 400) + " ...";
    }
    return logPred;
  }
}
