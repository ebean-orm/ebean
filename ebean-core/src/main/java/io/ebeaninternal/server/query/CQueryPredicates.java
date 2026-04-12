package io.ebeaninternal.server.query;

import io.ebean.OrderBy;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.bind.DataBind;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.DeployParser;
import io.ebeaninternal.server.expression.DefaultExpressionRequest;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;
import io.ebeaninternal.server.querydefn.OrmUpdateProperties;
import io.ebeaninternal.server.rawsql.SpiRawSql;
import io.ebeaninternal.server.util.BindParamsParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static java.lang.System.Logger.Level.WARNING;

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
public final class CQueryPredicates {

  private final Binder binder;
  private final OrmQueryRequest<?> request;
  private final SpiQuery<?> query;
  private final Object idValue;
  private final BindParams bindParams;
  private DefaultExpressionRequest filterMany;
  private boolean filterManyJoin;
  /**
   * Bind values from the where expressions.
   */
  private DefaultExpressionRequest where;
  /**
   * Bind values for having expression.
   */
  private DefaultExpressionRequest having;
  private String dbHaving;
  /**
   * logicalWhere with property names converted to db columns.
   */
  private String dbWhere;
  /**
   * Filter than can apply to a many fetch join.
   */
  private String dbFilterMany;
  private String dbOrderBy;
  private String dbDistinctOn;
  private String dbUpdateClause;
  /**
   * Includes from where and order by clauses.
   */
  private Set<String> predicateIncludes;
  private Set<String> orderByIncludes;

  CQueryPredicates(Binder binder, OrmQueryRequest<?> request) {
    this.binder = binder;
    this.request = request;
    this.query = request.query();
    this.bindParams = query.bindParams();
    this.idValue = query.getId();
  }

  public String bind(PreparedStatement stmt, Connection connection) throws SQLException {
    return bind(binder.dataBind(stmt, connection));
  }

  public String bind(DataBind dataBind) throws SQLException {
    OrmUpdateProperties updateProperties = query.updateProperties();
    if (updateProperties != null) {
      // bind the update set clause
      updateProperties.bind(binder, dataBind);
    }
    if (query.isVersionsBetween() && binder.isAsOfStandardsBased()) {
      // sql2011 based versions between timestamp syntax
      Timestamp start = query.versionStart();
      Timestamp end = query.versionEnd();
      dataBind.append("between ").append(start).append(" and ").append(end);
      binder.bindObject(dataBind, start);
      binder.bindObject(dataBind, end);
      dataBind.append(", ");
    }
    CQueryPlan queryPlan = request.queryPlan();
    if (queryPlan != null) {
      int asOfTableCount = queryPlan.asOfTableCount();
      if (asOfTableCount > 0) {
        // bind the asOf value for each table alias as part of the from/join clauses
        // there is one effective date predicate per table alias
        Timestamp asOf = query.getAsOf();
        dataBind.append("asOf ").append(asOf);
        for (int i = 0; i < asOfTableCount * binder.getAsOfBindCount(); i++) {
          binder.bindObject(dataBind, asOf);
        }
        dataBind.append(", ");
      }
    }
    if (filterManyJoin) {
      filterMany.bind(dataBind);
    }
    if (idValue != null) {
      // this is a find by id type query...
      request.descriptor().bindId(dataBind, idValue);
      dataBind.append(idValue);
      dataBind.append(", ");
    }
    if (bindParams != null) {
      // bind named and positioned parameters...
      binder.bind(bindParams, dataBind, dataBind.log());
    }
    if (where != null) {
      where.bind(dataBind);
    }
    if (!filterManyJoin && filterMany != null) {
      filterMany.bind(dataBind);
    }
    if (having != null) {
      having.bind(dataBind);
    }
    return dataBind.log().toString();
  }

  private void buildUpdateClause(boolean buildSql, DeployParser deployParser) {
    if (buildSql) {
      OrmUpdateProperties updateProperties = query.updateProperties();
      if (updateProperties != null) {
        dbUpdateClause = updateProperties.buildSetClause(deployParser);
      }
    }
  }

  public String parseBindParams(String sql) {
    if (bindParams != null && bindParams.requiresNamedParamsPrepare()) {
      return BindParamsParser.parse(bindParams, sql);
    } else {
      return sql;
    }
  }

  /**
   * Convert named parameters into an OrderedList.
   */
  private void buildBindWhereRawSql(boolean buildSql) {
    if (!buildSql && bindParams != null && bindParams.requiresNamedParamsPrepare()) {
      if (query.isNativeSql()) {
        // convert named params into positioned params
        String sql = query.nativeSql();
        BindParamsParser.parse(bindParams, sql);

      } else if (query.isRawSql()) {
        // RawSql query hit cached query plan. Need to convert
        // named parameters into positioned parameters so that
        // the named parameters are bound
        SpiRawSql.Sql sql = query.rawSql().getSql();
        String s = sql.isParsed() ? sql.getPreWhere() : sql.getUnparsedSql();
        BindParamsParser.parse(bindParams, s);
      }
    }
  }

  public void prepare(boolean buildSql) {
    DeployParser deployParser = request.createDeployParser();
    buildUpdateClause(buildSql, deployParser);
    buildBindWhereRawSql(buildSql);

    if (buildSql) {
      dbOrderBy = deriveOrderByWithMany(deployParser);
      // create a copy of the includes required to support the orderBy
      orderByIncludes = new HashSet<>(deployParser.includes());
    }
    SpiExpressionList<?> whereExp = query.whereExpressions();
    if (whereExp != null) {
      this.where = new DefaultExpressionRequest(request, deployParser, binder, whereExp);
      if (buildSql) {
        dbWhere = where.buildSql();
      }
    }
    SpiQueryManyJoin manyProperty = request.manyJoin();
    if (manyProperty != null) {
      OrmQueryProperties chunk = query.detail().getChunk(manyProperty.path(), false);
      if (chunk != null) {
        SpiExpressionList<?> filterManyExpr = chunk.getFilterMany();
        if (filterManyExpr != null) {
          filterManyJoin = chunk.isFilterManyJoin();
          filterMany = new DefaultExpressionRequest(request, deployParser, binder, filterManyExpr);
          if (buildSql) {
            dbFilterMany = filterMany.buildSql();
          }
        }
      }
    }
    SpiExpressionList<?> havingExpr = query.havingExpressions();
    if (havingExpr != null) {
      this.having = new DefaultExpressionRequest(request, deployParser, binder, havingExpr);
      if (buildSql) {
        dbHaving = having.buildSql();
      }
    }
    if (buildSql) {
      final String distinctOn = query.distinctOn();
      if (distinctOn != null) {
        dbDistinctOn = deployParser.parse(distinctOn);
      }
      predicateIncludes = deployParser.includes();
    }
  }

  /**
   * Replace the table alias place-holders.
   */
  void parseTableAlias(SqlTreeAlias alias) {
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
    if (dbDistinctOn != null) {
      dbDistinctOn = alias.parse(dbDistinctOn);
    }
  }

  private String parseOrderBy(DeployParser parser) {
    OrderBy<?> orderBy = query.getOrderBy();
    if (orderBy == null) {
      return null;
    }
    return CQueryOrderBy.parse(parser, request.descriptor(), orderBy);
  }

  /**
   * There is a many property we need to make sure the ordering is appropriate.
   */
  private String deriveOrderByWithMany(DeployParser parser) {
    String orderBy = parseOrderBy(parser);
    if (!request.includeManyJoin()) {
      return orderBy;
    }
    String orderById = parser.parse(request.descriptor().defaultOrderBy());
    if (orderBy == null) {
      orderBy = orderById;
    }
    // check for default ordering on the many property...
    SpiQueryManyJoin manyProp = request.manyJoin();
    String manyOrderBy = manyProp.fetchOrderBy();
    if (manyOrderBy != null) {
      orderBy = orderBy + ", " + parser.parse(CQueryBuilder.prefixOrderByFields(manyProp.path(), manyOrderBy));
    }
    if (request.isFindById()) {
      // only one master bean so should be fine...
      return orderBy;
    }
    // more than one top level row may be returned so
    // we need to make sure their is an order by on the
    // top level first (to ensure master/detail construction).
    int idPos = orderBy.indexOf(orderById);
    if (idPos == 0) {
      return orderBy;
    }
    int manyPos = orderBy.indexOf("${" + manyProp.path() + "}");
    if (manyPos == -1) {
      // no ordering of the many
      if (idPos == -1) {
        // append the orderById so that master level objects are ordered
        return orderBy + ", " + orderById;
      }
      // orderById is already in the order by clause
      return orderBy;
    }
    if (idPos == -1 || idPos >= manyPos) {
      if (idPos > manyPos) {
        // there was an error with the order by...
        String msg = "A Query on [" + request.descriptor() + "] includes a join to a 'many' association [" + manyProp.path()
          + "] with an incorrect orderBy [" + orderBy + "]. The id property [" + orderById
          + "] must come before the many property [" + manyProp.path() + "] in the orderBy."
          + " Ebean has automatically modified the orderBy clause to do this.";
        CoreLog.log.log(WARNING, msg);
      }
      // the id needs to come before the manyPropName
      orderBy = orderBy.substring(0, manyPos) + orderById + ", " + orderBy.substring(manyPos);
    }
    return orderBy;
  }

  /**
   * Return the bind values for the where expression.
   */
  public List<Object> whereExprBindValues() {
    if (idValue == null && where == null) {
      return Collections.emptyList();
    }
    if (where == null) {
      return List.of(idValue);
    }
    if (idValue == null) {
      return where.bindValues();
    }

    List<Object> bindValues = new ArrayList<>();
    bindValues.add(idValue);
    bindValues.addAll(where.bindValues());
    return Collections.unmodifiableList(bindValues);
  }

  /**
   * Return the db update set clause for an UpdateQuery.
   */
  String dbUpdateClause() {
    return dbUpdateClause;
  }

  /**
   * Return the db column version of the combined where clause.
   */
  String dbHaving() {
    return dbHaving;
  }

  /**
   * Return the db column version of the combined where clause.
   */
  String dbWhere() {
    return dbWhere;
  }

  /**
   * Return a db filter to be included in the WHERE.
   */
  String dbFilterManyWhere() {
    return filterManyJoin ? null : dbFilterMany;
  }

  /**
   * Return a db filter to be included in the JOIN.
   */
  String dbFilterManyJoin() {
    return filterManyJoin ? dbFilterMany : null;
  }

  /**
   * Return the db column version of the order by clause.
   */
  String dbOrderBy() {
    return dbOrderBy;
  }

  /**
   * Return the db distinct on clause.
   */
  String dbDistinctOn() {
    return dbDistinctOn;
  }

  /**
   * Return the includes required for the where and order by clause.
   */
  Set<String> predicateIncludes() {
    return predicateIncludes;
  }

  /**
   * Return the orderBy includes.
   */
  Set<String> orderByIncludes() {
    return orderByIncludes;
  }

  String logWhereSql() {
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
