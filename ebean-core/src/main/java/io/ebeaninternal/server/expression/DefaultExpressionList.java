package io.ebeaninternal.server.expression;

import io.ebean.CacheMode;
import io.ebean.CountDistinctOrder;
import io.ebean.DtoQuery;
import io.ebean.Expression;
import io.ebean.ExpressionFactory;
import io.ebean.ExpressionList;
import io.ebean.FetchGroup;
import io.ebean.FetchPath;
import io.ebean.FutureIds;
import io.ebean.FutureList;
import io.ebean.FutureRowCount;
import io.ebean.Junction;
import io.ebean.OrderBy;
import io.ebean.PagedList;
import io.ebean.Pairs;
import io.ebean.Query;
import io.ebean.QueryIterator;
import io.ebean.Transaction;
import io.ebean.UpdateQuery;
import io.ebean.Version;
import io.ebean.event.BeanQueryRequest;
import io.ebean.search.Match;
import io.ebean.search.MultiMatch;
import io.ebean.search.TextCommonTerms;
import io.ebean.search.TextQueryString;
import io.ebean.search.TextSimple;
import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.NaturalKeyQueryData;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionList;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.api.SpiExpressionValidation;
import io.ebeaninternal.api.SpiJunction;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Default implementation of ExpressionList.
 */
public class DefaultExpressionList<T> implements SpiExpressionList<T> {

  private static final String AND = " and ";

  protected List<SpiExpression> list;
  protected final Query<T> query;
  private final ExpressionList<T> parentExprList;
  protected final ExpressionFactory expr;
  String allDocNestedPath;
  /**
   * Set to true for the "Text" root expression list.
   */
  private final boolean textRoot;

  /**
   * Construct for Text root expression list - this handles implicit Bool Should, Must etc.
   */
  public DefaultExpressionList(Query<T> query) {
    this(query, query.getExpressionFactory(), null, new ArrayList<>(), true);
  }

  public DefaultExpressionList(Query<T> query, ExpressionList<T> parentExprList) {
    this(query, query.getExpressionFactory(), parentExprList);
  }

  DefaultExpressionList(Query<T> query, ExpressionFactory expr, ExpressionList<T> parentExprList) {
    this(query, expr, parentExprList, new ArrayList<>());
  }

  DefaultExpressionList(Query<T> query, ExpressionFactory expr, ExpressionList<T> parentExprList, List<SpiExpression> list) {
    this(query, expr, parentExprList, list, false);
  }

  private DefaultExpressionList(Query<T> query, ExpressionFactory expr, ExpressionList<T> parentExprList, List<SpiExpression> list, boolean textRoot) {
    this.textRoot = textRoot;
    this.list = list;
    this.query = query;
    this.expr = expr;
    this.parentExprList = parentExprList;
  }

  private DefaultExpressionList() {
    this(null, null, null, new ArrayList<>());
  }

  /**
   * Wrap the expression list as a Junction or top level DefaultExpressionList.
   *
   * @param list       The list of expressions grouped by nested path
   * @param nestedPath The doc store nested path
   * @param type       The junction type (or null for top level expression list).
   * @return A single SpiExpression that has the nestedPath set
   */
  SpiExpression wrap(List<SpiExpression> list, String nestedPath, Junction.Type type) {
    DefaultExpressionList<T> wrapper = new DefaultExpressionList<>(query, expr, null, list, false);
    wrapper.setAllDocNested(nestedPath);
    if (type != null) {
      return new JunctionExpression<>(type, wrapper);
    } else {
      return wrapper;
    }
  }

  void simplifyEntries() {
    for (SpiExpression expr : list) {
      expr.simplify();
    }
  }

  @Override
  public void prefixProperty(String path) {
    for (SpiExpression expr : list) {
      expr.prefixProperty(path);
    }
  }

  @Override
  public Junction<T> toJunction() {
    return new JunctionExpression<>(Junction.Type.FILTER, this);
  }

  @Override
  public boolean naturalKey(NaturalKeyQueryData<?> data) {
    // can't use naturalKey cache
    return false;
  }

  @Override
  public void simplify() {
    simplifyEntries();
  }

  /**
   * Write being aware if it is the Top level "text" expressions.
   * <p>
   * If this is the Top level "text" expressions then it detects if explicit or implicit Bool Should, Must etc is required
   * to wrap the expressions.
   * </p>
   * <p>
   * If implicit Bool is required SHOULD is used.
   * </p>
   */
  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    if (!textRoot) {
      writeDocQuery(context, null);

    } else {
      // this is a Top level "text" expressions so we may need to wrap in Bool SHOULD etc.
      if (list.isEmpty()) {
        throw new IllegalStateException("empty expression list?");
      }

      if (allDocNestedPath != null) {
        context.startNested(allDocNestedPath);
      }
      int size = list.size();
      SpiExpression first = list.get(0);
      boolean explicitBool = first instanceof SpiJunction<?>;
      boolean implicitBool = !explicitBool && size > 1;

      if (implicitBool || explicitBool) {
        context.startBoolGroup();
      }
      if (implicitBool) {
        context.startBoolGroupList(Junction.Type.SHOULD);
      }
      for (SpiExpression expr : list) {
        if (explicitBool) {
          try {
            ((SpiJunction<?>) expr).writeDocQueryJunction(context);
          } catch (ClassCastException e) {
            throw new IllegalStateException("The top level text() expressions should be all be 'Must', 'Should' or 'Must Not' or none of them should be.", e);
          }
        } else {
          expr.writeDocQuery(context);
        }
      }
      if (implicitBool) {
        context.endBoolGroupList();
      }
      if (implicitBool || explicitBool) {
        context.endBoolGroup();
      }
      if (allDocNestedPath != null) {
        context.endNested();
      }
    }
  }

  @Override
  public void writeDocQuery(DocQueryContext context, SpiExpression idEquals) throws IOException {
    if (allDocNestedPath != null) {
      context.startNested(allDocNestedPath);
    }
    int size = list.size();
    if (size == 1 && idEquals == null) {
      // only 1 expression - skip bool
      list.get(0).writeDocQuery(context);
    } else if (size == 0 && idEquals != null) {
      // only idEquals - skip bool
      idEquals.writeDocQuery(context);
    } else {
      // bool must wrap all the children
      context.startBoolMust();
      if (idEquals != null) {
        idEquals.writeDocQuery(context);
      }
      for (SpiExpression expr : list) {
        expr.writeDocQuery(context);
      }
      context.endBool();
    }
    if (allDocNestedPath != null) {
      context.endNested();
    }
  }

  @Override
  public SpiExpressionList<?> trimPath(int prefixTrim) {
    throw new IllegalStateException("Only allowed on FilterExpressionList");
  }

  public List<SpiExpression> internalList() {
    return list;
  }

  /**
   * Return a copy of the expression list.
   * <p>
   * Each of the expressions are expected to be immutable and safe to reference.
   * </p>
   */
  public DefaultExpressionList<T> copy(Query<T> query) {
    DefaultExpressionList<T> copy = new DefaultExpressionList<>(query, expr, null);
    copy.list.addAll(list);
    return copy;
  }

  @Override
  public DefaultExpressionList<T> copyForPlanKey() {
    DefaultExpressionList<T> copy = new DefaultExpressionList<>();
    for (int i = 0; i < list.size(); i++) {
      copy.list.add(list.get(i).copyForPlanKey());
    }
    return copy;
  }

  @Override
  public Object getIdEqualTo(String idName) {
    // always return null for this expression
    return null;
  }

  /**
   * Return true if one of the expressions is related to a Many property.
   */
  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins whereManyJoins) {
    for (SpiExpression expr : list) {
      expr.containsMany(desc, whereManyJoins);
    }
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    for (SpiExpression expr : list) {
      expr.validate(validation);
    }
  }

  @Override
  public Query<T> query() {
    return query;
  }

  @Override
  public Query<T> asOf(Timestamp asOf) {
    return query.asOf(asOf);
  }

  @Override
  public Query<T> asDraft() {
    return query.asDraft();
  }

  @Override
  public <D> DtoQuery<D> asDto(Class<D> dtoClass) {
    return query.asDto(dtoClass);
  }

  @Override
  public UpdateQuery<T> asUpdate() {
    return query.asUpdate();
  }

  @Override
  public Query<T> setIncludeSoftDeletes() {
    return query.setIncludeSoftDeletes();
  }

  @Override
  public List<Version<T>> findVersions() {
    return query.findVersions();
  }

  @Override
  public List<Version<T>> findVersionsBetween(Timestamp start, Timestamp end) {
    return query.findVersionsBetween(start, end);
  }

  @Override
  public ExpressionList<T> where() {
    return query.where();
  }

  @Override
  public OrderBy<T> order() {
    return query.order();
  }

  @Override
  public OrderBy<T> orderBy() {
    return query.order();
  }

  @Override
  public ExpressionList<T> order(String orderByClause) {
    query.order(orderByClause);
    return this;
  }

  @Override
  public ExpressionList<T> orderBy(String orderBy) {
    query.order(orderBy);
    return this;
  }

  @Override
  public Query<T> setOrderBy(String orderBy) {
    return query.order(orderBy);
  }

  @Override
  public Query<T> orderById(boolean orderById) {
    return query.orderById(orderById);
  }

  @Override
  public Query<T> apply(FetchPath fetchPath) {
    return query.apply(fetchPath);
  }

  @Override
  public Query<T> usingTransaction(Transaction transaction) {
    return query.usingTransaction(transaction);
  }

  @Override
  public Query<T> usingConnection(Connection connection) {
    return query.usingConnection(connection);
  }

  @Override
  public int delete() {
    return query.delete();
  }

  @Override
  public int delete(Transaction transaction) {
    return query.delete(transaction);
  }

  @Override
  public int update() {
    return query.update();
  }

  @Override
  public int update(Transaction transaction) {
    return query.update(transaction);
  }

  @Override
  public FutureIds<T> findFutureIds() {
    return query.findFutureIds();
  }

  @Override
  public FutureRowCount<T> findFutureCount() {
    return query.findFutureCount();
  }

  @Override
  public FutureList<T> findFutureList() {
    return query.findFutureList();
  }

  @Override
  public PagedList<T> findPagedList() {
    return query.findPagedList();
  }

  @Override
  public int findCount() {
    return query.findCount();
  }

  @Override
  public <A> List<A> findIds() {
    return query.findIds();
  }

  @Override
  public QueryIterator<T> findIterate() {
    return query.findIterate();
  }

  @Override
  public void findEach(Consumer<T> consumer) {
    query.findEach(consumer);
  }

  @Override
  public void findEach(int batch, Consumer<List<T>> consumer) {
    query.findEach(batch, consumer);
  }

  @Override
  public void findEachWhile(Predicate<T> consumer) {
    query.findEachWhile(consumer);
  }

  @Override
  public List<T> findList() {
    return query.findList();
  }

  @Override
  public Set<T> findSet() {
    return query.findSet();
  }

  @Override
  public <K> Map<K, T> findMap() {
    return query.findMap();
  }

  @Override
  public <A> List<A> findSingleAttributeList() {
    return query.findSingleAttributeList();
  }

  @Override
  public boolean exists() {
    return query.exists();
  }

  @Override
  public T findOne() {
    return query.findOne();
  }

  @Override
  public Optional<T> findOneOrEmpty() {
    return query.findOneOrEmpty();
  }

  @Override
  public ExpressionList<T> filterMany(String manyProperty) {
    return query.filterMany(manyProperty);
  }

  @Override
  public ExpressionList<T> filterMany(String manyProperty, String expressions, Object... params) {
    return query.filterMany(manyProperty).where(expressions, params);
  }

  @Override
  public Query<T> withLock(Query.LockType lockType) {
    return query.withLock(lockType);
  }

  @Override
  public Query<T> withLock(Query.LockType lockType, Query.LockWait lockWait) {
    return query.withLock(lockType, lockWait);
  }

  @Override
  public Query<T> forUpdate() {
    return query.forUpdate();
  }

  @Override
  public Query<T> forUpdateNoWait() {
    return query.forUpdateNoWait();
  }

  @Override
  public Query<T> forUpdateSkipLocked() {
    return query.forUpdateSkipLocked();
  }

  @Override
  public Query<T> select(String fetchProperties) {
    return query.select(fetchProperties);
  }

  @Override
  public Query<T> select(FetchGroup<T> fetchGroup) {
    return query.select(fetchGroup);
  }

  @Override
  public Query<T> setDistinct(boolean distinct) {
    return query.setDistinct(distinct);
  }

  @Override
  public Query<T> setDocIndexName(String indexName) {
    return query.setDocIndexName(indexName);
  }

  @Override
  public ExpressionList<T> setFirstRow(int firstRow) {
    query.setFirstRow(firstRow);
    return this;
  }

  @Override
  public ExpressionList<T> setMaxRows(int maxRows) {
    query.setMaxRows(maxRows);
    return this;
  }

  @Override
  public Query<T> setMapKey(String mapKey) {
    return query.setMapKey(mapKey);
  }

  @Override
  public Query<T> setUseCache(boolean useCache) {
    return query.setUseCache(useCache);
  }

  @Override
  public Query<T> setBeanCacheMode(CacheMode useCache) {
    return query.setBeanCacheMode(useCache);
  }

  @Override
  public Query<T> setUseQueryCache(CacheMode useCache) {
    return query.setUseQueryCache(useCache);
  }

  @Override
  public Query<T> setCountDistinct(CountDistinctOrder orderBy) {
    return query.setCountDistinct(orderBy);
  }

  @Override
  public Query<T> setUseDocStore(boolean useDocsStore) {
    return query.setUseDocStore(useDocsStore);
  }

  @Override
  public Query<T> setDisableLazyLoading(boolean disableLazyLoading) {
    return query.setDisableLazyLoading(disableLazyLoading);
  }

  @Override
  public Query<T> setDisableReadAuditing() {
    return query.setDisableReadAuditing();
  }

  @Override
  public Query<T> setLabel(String label) {
    return query.setLabel(label);
  }

  @Override
  public ExpressionList<T> having() {
    return query.having();
  }

  @Override
  public ExpressionList<T> add(Expression expr) {
    list.add((SpiExpression) expr);
    return this;
  }

  @Override
  public ExpressionList<T> addAll(ExpressionList<T> exprList) {
    SpiExpressionList<T> spiList = (SpiExpressionList<T>) exprList;
    list.addAll(spiList.getUnderlyingList());
    return this;
  }

  @Override
  public List<SpiExpression> getUnderlyingList() {
    return list;
  }

  @Override
  public boolean isEmpty() {
    return list.isEmpty();
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    for (int i = 0, size = list.size(); i < size; i++) {
      SpiExpression expression = list.get(i);
      if (i > 0) {
        request.append(AND);
      }
      expression.addSql(request);
    }
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    for (SpiExpression expr : list) {
      expr.addBindValues(request);
    }
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    for (SpiExpression expr : list) {
      expr.prepareExpression(request);
    }
  }

  /**
   * Calculate a hash based on the expressions but excluding the actual bind
   * values.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("List[");
    if (textRoot) {
      builder.append("textRoot:true ");
    }
    if (allDocNestedPath != null) {
      builder.append("path:").append(allDocNestedPath).append(" ");
    }
    for (SpiExpression expr : list) {
      expr.queryPlanHash(builder);
      builder.append(",");
    }
    builder.append("]");
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    key.add(list.size());
    for (SpiExpression expr : list) {
      expr.queryBindKey(key);
    }
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    DefaultExpressionList<?> that = (DefaultExpressionList<?>) other;
    if (list.size() != that.list.size()) {
      return false;
    }
    for (int i = 0, size = list.size(); i < size; i++) {
      if (!list.get(i).isSameByBind(that.list.get(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Path exists - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonExists(String propertyName, String path) {
    return add(expr.jsonExists(propertyName, path));
  }

  /**
   * Path does not exist - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonNotExists(String propertyName, String path) {
    return add(expr.jsonNotExists(propertyName, path));
  }

  /**
   * Equal to expression for the value at the given path in the JSON document.
   */
  @Override
  public ExpressionList<T> jsonEqualTo(String propertyName, String path, Object value) {
    return add(expr.jsonEqualTo(propertyName, path, value));
  }

  /**
   * Not Equal to - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonNotEqualTo(String propertyName, String path, Object val) {
    return add(expr.jsonNotEqualTo(propertyName, path, val));
  }

  /**
   * Greater than - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonGreaterThan(String propertyName, String path, Object val) {
    return add(expr.jsonGreaterThan(propertyName, path, val));
  }

  /**
   * Greater than or equal to - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonGreaterOrEqual(String propertyName, String path, Object val) {
    return add(expr.jsonGreaterOrEqual(propertyName, path, val));
  }

  /**
   * Less than - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonLessThan(String propertyName, String path, Object val) {
    return add(expr.jsonLessThan(propertyName, path, val));
  }

  /**
   * Less than or equal to - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonLessOrEqualTo(String propertyName, String path, Object val) {
    return add(expr.jsonLessOrEqualTo(propertyName, path, val));
  }

  /**
   * Between - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonBetween(String propertyName, String path, Object lowerValue, Object upperValue) {
    return add(expr.jsonBetween(propertyName, path, lowerValue, upperValue));
  }

  @Override
  public ExpressionList<T> where(String expressions, Object... params) {
    expr.where(this, expressions, params);
    return this;
  }

  @Override
  public ExpressionList<T> bitwiseAny(String propertyName, long flags) {
    return add(expr.bitwiseAny(propertyName, flags));
  }

  @Override
  public ExpressionList<T> bitwiseNot(String propertyName, long flags) {
    return add(expr.bitwiseAnd(propertyName, flags, 0));
  }

  @Override
  public ExpressionList<T> bitwiseAll(String propertyName, long flags) {
    return add(expr.bitwiseAll(propertyName, flags));
  }

  @Override
  public ExpressionList<T> bitwiseAnd(String propertyName, long flags, long match) {
    return add(expr.bitwiseAnd(propertyName, flags, match));
  }

  @Override
  public ExpressionList<T> eq(String propertyName, Object value) {
    return add(expr.eq(propertyName, value));
  }

  @Override
  public ExpressionList<T> eqOrNull(String propertyName, Object value) {
    return add(expr.eqOrNull(propertyName, value));
  }

  @Override
  public ExpressionList<T> ieq(String propertyName, String value) {
    return add(expr.ieq(propertyName, value));
  }

  @Override
  public ExpressionList<T> ine(String propertyName, String value) {
    return add(expr.ine(propertyName, value));
  }

  @Override
  public ExpressionList<T> ne(String propertyName, Object value) {
    return add(expr.ne(propertyName, value));
  }

  @Override
  public ExpressionList<T> allEq(Map<String, Object> propertyMap) {
    return add(expr.allEq(propertyMap));
  }

  @Override
  public ExpressionList<T> and(Expression expOne, Expression expTwo) {
    return add(expr.and(expOne, expTwo));
  }

  @Override
  public ExpressionList<T> inRangeWith(String lowProperty, String highProperty, Object value) {
    return add(expr.inRangeWith(lowProperty, highProperty, value));
  }

  @Override
  public ExpressionList<T> inRange(String propertyName, Object value1, Object value2) {
    return add(expr.inRange(propertyName, value1, value2));
  }

  @Override
  public ExpressionList<T> between(String propertyName, Object value1, Object value2) {
    return add(expr.between(propertyName, value1, value2));
  }

  @Override
  public ExpressionList<T> betweenProperties(String lowProperty, String highProperty, Object value) {
    return add(expr.betweenProperties(lowProperty, highProperty, value));
  }

  @Override
  public ExpressionList<T> contains(String propertyName, String value) {
    return add(expr.contains(propertyName, value));
  }

  @Override
  public ExpressionList<T> endsWith(String propertyName, String value) {
    return add(expr.endsWith(propertyName, value));
  }

  @Override
  public ExpressionList<T> ge(String propertyName, Object value) {
    add(expr.ge(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> gt(String propertyName, Object value) {
    return add(expr.gt(propertyName, value));
  }

  @Override
  public ExpressionList<T> gtOrNull(String propertyName, Object value) {
    add(expr.gtOrNull(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> geOrNull(String propertyName, Object value) {
    add(expr.geOrNull(propertyName, value));
    return this;
  }

  @Override
  public ExpressionList<T> icontains(String propertyName, String value) {
    return add(expr.icontains(propertyName, value));
  }

  @Override
  public ExpressionList<T> idIn(Object... idValues) {
    return add(expr.idIn(idValues));
  }

  @Override
  public ExpressionList<T> idIn(Collection<?> idCollection) {
    return add(expr.idIn(idCollection));
  }

  @Override
  public ExpressionList<T> idEq(Object value) {
    if (query != null && parentExprList == null) {
      query.setId(value);
    } else {
      add(expr.idEq(value));
    }
    return this;
  }

  @Override
  public ExpressionList<T> iendsWith(String propertyName, String value) {
    return add(expr.iendsWith(propertyName, value));
  }

  @Override
  public ExpressionList<T> ilike(String propertyName, String value) {
    return add(expr.ilike(propertyName, value));
  }

  @Override
  public ExpressionList<T> inPairs(Pairs pairs) {
    return add(expr.inPairs(pairs));
  }

  @Override
  public ExpressionList<T> in(String propertyName, Query<?> subQuery) {
    return add(expr.in(propertyName, subQuery));
  }

  @Override
  public ExpressionList<T> in(String propertyName, Collection<?> values) {
    return add(expr.in(propertyName, values));
  }

  @Override
  public ExpressionList<T> inOrEmpty(String propertyName, Collection<?> values) {
    if (notEmpty(values)) {
      add(expr.in(propertyName, values));
    }
    return this;
  }

  @Override
  public ExpressionList<T> in(String propertyName, Object... values) {
    return add(expr.in(propertyName, values));
  }

  @Override
  public ExpressionList<T> notIn(String propertyName, Object... values) {
    return add(expr.notIn(propertyName, values));
  }

  @Override
  public ExpressionList<T> notIn(String propertyName, Collection<?> values) {
    return add(expr.notIn(propertyName, values));
  }

  @Override
  public ExpressionList<T> notIn(String propertyName, Query<?> subQuery) {
    return add(expr.notIn(propertyName, subQuery));
  }

  @Override
  public ExpressionList<T> isEmpty(String propertyName) {
    return add(expr.isEmpty(propertyName));
  }

  @Override
  public ExpressionList<T> isNotEmpty(String propertyName) {
    return add(expr.isNotEmpty(propertyName));
  }

  @Override
  public ExpressionList<T> exists(Query<?> subQuery) {
    return add(expr.exists(subQuery));
  }

  @Override
  public ExpressionList<T> notExists(Query<?> subQuery) {
    return add(expr.notExists(subQuery));
  }

  @Override
  public ExpressionList<T> isNotNull(String propertyName) {
    return add(expr.isNotNull(propertyName));
  }

  @Override
  public ExpressionList<T> isNull(String propertyName) {
    return add(expr.isNull(propertyName));
  }

  @Override
  public ExpressionList<T> istartsWith(String propertyName, String value) {
    return add(expr.istartsWith(propertyName, value));
  }

  @Override
  public ExpressionList<T> le(String propertyName, Object value) {
    return add(expr.le(propertyName, value));
  }

  @Override
  public ExpressionList<T> exampleLike(Object example) {
    return add(expr.exampleLike(example));
  }

  @Override
  public ExpressionList<T> iexampleLike(Object example) {
    return add(expr.iexampleLike(example));
  }

  @Override
  public ExpressionList<T> like(String propertyName, String value) {
    return add(expr.like(propertyName, value));
  }

  @Override
  public ExpressionList<T> lt(String propertyName, Object value) {
    return add(expr.lt(propertyName, value));
  }

  @Override
  public ExpressionList<T> ltOrNull(String propertyName, Object value) {
    return add(expr.ltOrNull(propertyName, value));
  }

  @Override
  public ExpressionList<T> leOrNull(String propertyName, Object value) {
    return add(expr.leOrNull(propertyName, value));
  }

  @Override
  public ExpressionList<T> not(Expression exp) {
    return add(expr.not(exp));
  }

  @Override
  public ExpressionList<T> or(Expression expOne, Expression expTwo) {
    return add(expr.or(expOne, expTwo));
  }

  @Override
  public ExpressionList<T> arrayContains(String propertyName, Object... elementValue) {
    return add(expr.arrayContains(propertyName, elementValue));
  }

  @Override
  public ExpressionList<T> arrayNotContains(String propertyName, Object... values) {
    return add(expr.arrayNotContains(propertyName, values));
  }

  @Override
  public ExpressionList<T> arrayIsEmpty(String propertyName) {
    return add(expr.arrayIsEmpty(propertyName));
  }

  @Override
  public ExpressionList<T> arrayIsNotEmpty(String propertyName) {
    return add(expr.arrayIsNotEmpty(propertyName));
  }

  @Override
  public ExpressionList<T> raw(String raw, Object value) {
    return add(expr.raw(raw, value));
  }

  @Override
  public ExpressionList<T> raw(String raw, Object... values) {
    return add(expr.raw(raw, values));
  }

  @Override
  public ExpressionList<T> raw(String raw) {
    return add(expr.raw(raw));
  }

  @Override
  public ExpressionList<T> rawOrEmpty(String raw, Collection<?> values) {
    if (notEmpty(values)) {
      add(expr.raw(raw, values));
    }
    return this;
  }

  private boolean notEmpty(Collection<?> values) {
    return values != null && !values.isEmpty();
  }

  @Override
  public ExpressionList<T> startsWith(String propertyName, String value) {
    return add(expr.startsWith(propertyName, value));
  }

  @Override
  public ExpressionList<T> match(String propertyName, String search) {
    return match(propertyName, search, null);
  }

  @Override
  public ExpressionList<T> match(String propertyName, String search, Match options) {
    setUseDocStore(true);
    return add(expr.textMatch(propertyName, search, options));
  }

  @Override
  public ExpressionList<T> multiMatch(String query, String... fields) {
    return multiMatch(query, MultiMatch.fields(fields));
  }

  @Override
  public ExpressionList<T> multiMatch(String query, MultiMatch options) {
    setUseDocStore(true);
    return add(expr.textMultiMatch(query, options));
  }

  @Override
  public ExpressionList<T> textSimple(String search, TextSimple options) {
    setUseDocStore(true);
    return add(expr.textSimple(search, options));
  }

  @Override
  public ExpressionList<T> textQueryString(String search, TextQueryString options) {
    setUseDocStore(true);
    return add(expr.textQueryString(search, options));
  }

  @Override
  public ExpressionList<T> textCommonTerms(String search, TextCommonTerms options) {
    setUseDocStore(true);
    return add(expr.textCommonTerms(search, options));
  }

  protected Junction<T> junction(Junction.Type type) {
    Junction<T> junction = expr.junction(type, query, this);
    add(junction);
    return junction;
  }

  @Override
  public ExpressionList<T> endJunction() {
    return parentExprList == null ? this : parentExprList;
  }

  @Override
  public ExpressionList<T> endAnd() {
    return endJunction();
  }

  @Override
  public ExpressionList<T> endOr() {
    return endJunction();
  }

  @Override
  public ExpressionList<T> endNot() {
    return endJunction();
  }

  @Override
  public Junction<T> and() {
    return conjunction();
  }

  @Override
  public Junction<T> or() {
    return disjunction();
  }

  @Override
  public Junction<T> not() {
    return junction(Junction.Type.NOT);
  }

  @Override
  public Junction<T> conjunction() {
    return junction(Junction.Type.AND);
  }

  @Override
  public Junction<T> disjunction() {
    return junction(Junction.Type.OR);
  }

  @Override
  public Junction<T> must() {
    setUseDocStore(true);
    return junction(Junction.Type.MUST);
  }

  @Override
  public Junction<T> should() {
    setUseDocStore(true);
    return junction(Junction.Type.SHOULD);
  }

  @Override
  public Junction<T> mustNot() {
    setUseDocStore(true);
    return junction(Junction.Type.MUST_NOT);
  }

  @Override
  public String nestedPath(BeanDescriptor<?> desc) {
    // effectively handled by JunctionExpression
    return null;
  }

  /**
   * Set the nested path that all contained expressions share.
   */
  public void setAllDocNested(String allDocNestedPath) {
    this.allDocNestedPath = allDocNestedPath;
  }

  /**
   * Replace the underlying expression list with one organised by nested path.
   */
  public void setUnderlying(List<SpiExpression> groupedByNesting) {
    this.list = groupedByNesting;
  }

  /**
   * Prepare expressions for document store nested path handling.
   */
  public void prepareDocNested(BeanDescriptor<T> beanDescriptor) {
    PrepareDocNested.prepare(this, beanDescriptor);
  }

  public Object idEqualTo(String idName) {
    if (idName == null) {
      return null;
    }
    if (list.size() == 1) {
      return list.get(0).getIdEqualTo(idName);
    }
    return null;
  }
}
