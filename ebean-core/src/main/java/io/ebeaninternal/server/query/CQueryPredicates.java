package io.ebeaninternal.server.query;

import io.ebean.OrderBy;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiExpressionList;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.DeployParser;
import io.ebeaninternal.server.expression.DefaultExpressionRequest;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;
import io.ebeaninternal.server.querydefn.OrmUpdateProperties;
import io.ebeaninternal.server.rawsql.SpiRawSql;
import io.ebeaninternal.server.type.DataBind;
import io.ebeaninternal.server.util.BindParamsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

  private static final Logger logger = LoggerFactory.getLogger(CQueryPredicates.class);

  private final Binder binder;

  private final OrmQueryRequest<?> request;

  private final SpiQuery<?> query;

  private final Object idValue;

  /**
   * Named bind parameters.
   */
  private final BindParams bindParams;

  /**
   * Bind values from the where expressions.
   */
  private DefaultExpressionRequest filterMany;

  /**
   * SQL generated from the where expressions.
   */
  private String filterManyExprSql;

  /**
   * Bind values from the where expressions.
   */
  private DefaultExpressionRequest where;

  /**
   * SQL generated from the where expressions.
   */
  private String whereExprSql;

  /**
   * Bind values for having expression.
   */
  private DefaultExpressionRequest having;

  /**
   * SQL generated from the having expression.
   */
  private String havingExprSql;

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

  private String dbUpdateClause;

  /**
   * Includes from where and order by clauses.
   */
  private Set<String> predicateIncludes;

  private Set<String> orderByIncludes;

  CQueryPredicates(Binder binder, OrmQueryRequest<?> request) {
    this.binder = binder;
    this.request = request;
    this.query = request.getQuery();
    this.bindParams = query.getBindParams();
    this.idValue = query.getId();
  }

  public String bind(PreparedStatement stmt, Connection connection) throws SQLException {
    return bind(binder.dataBind(stmt, connection));
  }

  public String bind(DataBind dataBind) throws SQLException {

    OrmUpdateProperties updateProperties = query.getUpdateProperties();
    if (updateProperties != null) {
      // bind the update set clause
      updateProperties.bind(binder, dataBind);
    }

    if (query.isVersionsBetween() && binder.isAsOfStandardsBased()) {
      // sql2011 based versions between timestamp syntax
      Timestamp start = query.getVersionStart();
      Timestamp end = query.getVersionEnd();
      dataBind.append("between ").append(start).append(" and ").append(end);
      binder.bindObject(dataBind, start);
      binder.bindObject(dataBind, end);
      dataBind.append(", ");
    }

    CQueryPlan queryPlan = request.getQueryPlan();
    if (queryPlan != null) {
      int asOfTableCount = queryPlan.getAsOfTableCount();
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

    if (idValue != null) {
      // this is a find by id type query...
      request.getBeanDescriptor().bindId(dataBind, idValue);
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

    if (filterMany != null) {
      filterMany.bind(dataBind);
    }

    if (having != null) {
      having.bind(dataBind);
    }

    return dataBind.log().toString();
  }

  private void buildUpdateClause(boolean buildSql, DeployParser deployParser) {
    if (buildSql) {
      OrmUpdateProperties updateProperties = query.getUpdateProperties();
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
        String sql = query.getNativeSql();
        BindParamsParser.parse(bindParams, sql);

      } else if (query.isRawSql()) {
        // RawSql query hit cached query plan. Need to convert
        // named parameters into positioned parameters so that
        // the named parameters are bound
        SpiRawSql.Sql sql = query.getRawSql().getSql();
        String s = sql.isParsed() ? sql.getPreWhere() : sql.getUnparsedSql();
        BindParamsParser.parse(bindParams, s);
      }
    }
  }

  public void prepare(boolean buildSql) {

    DeployParser deployParser = request.createDeployParser();
    buildUpdateClause(buildSql, deployParser);
    buildBindWhereRawSql(buildSql);

    SpiExpressionList<?> whereExp = query.getWhereExpressions();
    if (whereExp != null) {
      this.where = new DefaultExpressionRequest(request, deployParser, binder, whereExp);
      if (buildSql) {
        whereExprSql = where.buildSql();
      }
    }

    BeanPropertyAssocMany<?> manyProperty = request.determineMany();
    if (manyProperty != null) {
      OrmQueryProperties chunk = query.getDetail().getChunk(manyProperty.getName(), false);
      SpiExpressionList<?> filterManyExpr = chunk.getFilterMany();
      if (filterManyExpr != null) {
        this.filterMany = new DefaultExpressionRequest(request, deployParser, binder, filterManyExpr);
        if (buildSql) {
          filterManyExprSql = filterMany.buildSql();
        }
      }
    }

    // having expression
    SpiExpressionList<?> havingExpr = query.getHavingExpressions();
    if (havingExpr != null) {
      this.having = new DefaultExpressionRequest(request, deployParser, binder, havingExpr);
      if (buildSql) {
        havingExprSql = having.buildSql();
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

    // order by is dependent on the manyProperty (if there is one)
    String logicalOrderBy = deriveOrderByWithMany(request.getManyProperty());
    if (logicalOrderBy != null) {
      dbOrderBy = deployParser.parse(logicalOrderBy);
    }

    // create a copy of the includes required to support the orderBy
    orderByIncludes = new HashSet<>(deployParser.getIncludes());

    dbWhere = deriveWhere(deployParser);
    dbFilterMany = deriveFilterMany(deployParser);
    dbHaving = deriveHaving(deployParser);

    // all includes including ones for manyWhere clause
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
    return parse(whereExprSql, deployParser);
  }

  /**
   * Replace the table alias place holders.
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
  }

  private boolean isEmpty(String s) {
    return s == null || s.isEmpty();
  }

  private String parse(String expr, DeployParser deployParser) {
    if (expr == null) return "";
    if (expr.isEmpty()) return expr;
    return deployParser.parse(expr);
  }

  private String deriveHaving(DeployParser deployParser) {
    return parse(havingExprSql, deployParser);
  }

  private String parseOrderBy() {

    OrderBy<?> orderBy = query.getOrderBy();
    if (orderBy == null) {
      return null;
    }
    return CQueryOrderBy.parse(request.getBeanDescriptor(), orderBy);
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

    if (idPos <= -1 || idPos >= manyPos) {
      if (idPos > manyPos) {
        // there was an error with the order by...
        String msg = "A Query on [" + desc + "] includes a join to a 'many' association [" + manyProp.getName();
        msg += "] with an incorrect orderBy [" + orderBy + "]. The id property [" + orderById + "]";
        msg += " must come before the many property [" + manyProp.getName() + "] in the orderBy.";
        msg += " Ebean has automatically modified the orderBy clause to do this.";

        logger.warn(msg);
      }

      // the id needs to come before the manyPropName
      orderBy = orderBy.substring(0, manyPos) + orderById + ", " + orderBy.substring(manyPos);
    }

    return orderBy;
  }

  /**
   * Return the bind values for the where expression.
   */
  public List<Object> getWhereExprBindValues() {
    return where.getBindValues();
  }

  /**
   * Return the db update set clause for an UpdateQuery.
   */
  String getDbUpdateClause() {
    return dbUpdateClause;
  }

  /**
   * Return the db column version of the combined where clause.
   */
  String getDbHaving() {
    return dbHaving;
  }

  /**
   * Return the db column version of the combined where clause.
   */
  String getDbWhere() {
    return dbWhere;
  }

  /**
   * Return a db filter for filtering many fetch joins.
   */
  String getDbFilterMany() {
    return dbFilterMany;
  }

  /**
   * Return the db column version of the order by clause.
   */
  String getDbOrderBy() {
    return dbOrderBy;
  }

  /**
   * Return the includes required for the where and order by clause.
   */
  Set<String> getPredicateIncludes() {
    return predicateIncludes;
  }

  /**
   * Return the orderBy includes.
   */
  Set<String> getOrderByIncludes() {
    return orderByIncludes;
  }

  String getLogWhereSql() {

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
