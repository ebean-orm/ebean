package com.avaje.ebeaninternal.server.query;

import com.avaje.ebean.RawSql;
import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.BindParams.OrderedList;
import com.avaje.ebeaninternal.api.SpiExpressionList;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DeployParser;
import com.avaje.ebeaninternal.server.expression.DefaultExpressionRequest;
import com.avaje.ebeaninternal.server.persist.Binder;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryProperties;
import com.avaje.ebeaninternal.server.querydefn.OrmUpdateProperties;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.util.BindParamsParser;
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
   * Named bind parameters for the having clause.
   */
  private OrderedList havingNamedParams;

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
   * SQL generated from where with named parameters.
   */
  private String whereRawSql;

  /**
   * Bind values for having expression.
   */
  private DefaultExpressionRequest having;

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

  private String dbOrderBy;

  private String dbUpdateClause;

  /**
   * Includes from where and order by clauses.
   */
  private Set<String> predicateIncludes;

  private Set<String> orderByIncludes;

  public CQueryPredicates(Binder binder, OrmQueryRequest<?> request) {
    this.binder = binder;
    this.request = request;
    this.query = request.getQuery();
    this.bindParams = query.getBindParams();
    this.idValue = query.getId();
  }

  public String bind(PreparedStatement stmt,  Connection connection) throws SQLException {
    return bind(binder.dataBind(stmt, connection));
  }

  public String bind(DataBind dataBind) throws SQLException {

    OrmUpdateProperties updateProperties = query.getUpdateProperties();
    if (updateProperties != null) {
      // bind the update set clause
      updateProperties.bind(binder, dataBind);
    }

    if (query.isVersionsBetween() && binder.isBindAsOfWithFromClause()) {
      // sql2011 based versions between timestamp syntax
      Timestamp start = query.getVersionStart();
      Timestamp end = query.getVersionEnd();
      dataBind.append("between ").append(start).append(" and ").append(end);
      binder.bindObject(dataBind, start);
      binder.bindObject(dataBind, end);
      dataBind.append(", ");
    }

    List<String> historyTableAlias = query.getAsOfTableAlias();
    if (historyTableAlias != null && binder.isBindAsOfWithFromClause()) {
      // bind the asOf value for each table alias as part of the from/join clauses
      // there is one effective date predicate per table alias
      Timestamp asOf = query.getAsOf();
      dataBind.append("asOf ").append(asOf);
      for (int i = 0; i < historyTableAlias.size() * binder.getAsOfBindCount(); i++) {
        binder.bindObject(dataBind, asOf);
      }
      dataBind.append(", ");
    }

    if (idValue != null) {
      // this is a find by id type query...
      request.getBeanDescriptor().bindId(dataBind, idValue);
      dataBind.append(idValue);
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

    if (historyTableAlias != null && !binder.isBindAsOfWithFromClause()) {
      // bind the asOf value for each table alias after all the normal predicates
      // there is one effective date predicate per table alias
      Timestamp asOf = query.getAsOf();
      dataBind.append(" asOf ").append(asOf);
      for (int i = 0; i < historyTableAlias.size() * binder.getAsOfBindCount(); i++) {
        binder.bindObject(dataBind, asOf);
      }
    }

    if (havingNamedParams != null) {
      // bind named parameters in having...
      binder.bind(havingNamedParams.list(), dataBind, dataBind.log());
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
        // an encrypted property we will insert special named parameter place
        // holders for binding the encryption key values
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
          RawSql.Sql sql = query.getRawSql().getSql();
          String s = sql.isParsed() ? sql.getPreWhere() : sql.getUnparsedSql();
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

  /**
   * This combines the sql from named/positioned parameters and expressions.
   */
  private void prepare(boolean buildSql, boolean parseRaw, DeployParser deployParser) {

    buildUpdateClause(buildSql, deployParser);
    buildBindWhereRawSql(buildSql, parseRaw, deployParser);
    buildBindHavingRawSql(buildSql, parseRaw, deployParser);

    SpiExpressionList<?> whereExp = query.getWhereExpressions();
    if (whereExp != null) {
      this.where = new DefaultExpressionRequest(request, deployParser, binder, whereExp);
      if (buildSql) {
        whereExprSql = where.buildSql();
      }
    }

    BeanPropertyAssocMany<?> manyProperty = request.getManyProperty();
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
    orderByIncludes = new HashSet<String>(deployParser.getIncludes());

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
  public String getDbUpdateClause() {
    return dbUpdateClause;
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
   * Return the orderBy includes.
   */
  public Set<String> getOrderByIncludes() {
    return orderByIncludes;
  }

  public String getLogWhereSql() {

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
