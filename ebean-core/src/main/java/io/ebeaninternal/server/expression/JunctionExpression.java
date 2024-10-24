package io.ebeaninternal.server.expression;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import io.ebean.*;
import io.ebean.event.BeanQueryRequest;
import io.ebean.search.*;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Junction implementation.
 */
@NullMarked
final class JunctionExpression<T> implements SpiJunction<T>, SpiExpression, ExpressionList<T> {

  DefaultExpressionList<T> exprList;
  Junction.Type type;

  JunctionExpression(Junction.Type type, Query<T> query, ExpressionList<T> parent) {
    this.type = type;
    this.exprList = new DefaultExpressionList<>(query, parent);
  }

  /**
   * Construct for copyForPlanKey.
   */
  JunctionExpression(Junction.Type type, DefaultExpressionList<T> exprList) {
    this.type = type;
    this.exprList = exprList;
  }

  @Override
  public void prefixProperty(String path) {
    exprList.prefixProperty(path);
  }

  @Override
  public boolean naturalKey(NaturalKeyQueryData<?> data) {
    // can't use naturalKey cache
    return false;
  }

  /**
   * Simplify nested expressions where possible.
   * <p>
   * This is expected to only used after expressions are built via query language parsing.
   * </p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public void simplify() {
    exprList.simplifyEntries();

    List<SpiExpression> list = exprList.list;
    if (list.size() == 1 && list.get(0) instanceof JunctionExpression) {
      @SuppressWarnings("rawtypes")
      JunctionExpression nested = (JunctionExpression) list.get(0);
      if (type == Type.AND && !nested.type.isText()) {
        // and (and (a, b, c)) -> and (a, b, c)
        // and (not (a, b, c)) -> not (a, b, c)
        // and (or  (a, b, c)) -> or  (a, b, c)
        this.exprList = nested.exprList;
        this.type = nested.type;
      } else if (type == Type.NOT && nested.type == Type.AND) {
        // not (and (a, b, c)) -> not (a, b, c)
        this.exprList = nested.exprList;
      }
    }
  }

  @Override
  public SpiExpression copyForPlanKey() {
    return new JunctionExpression<>(type, exprList.copyForPlanKey());
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.startBool(type);
    for (SpiExpression expr : exprList.internalList()) {
      expr.writeDocQuery(context);
    }
    context.endBool();
  }

  @Override
  public void writeDocQueryJunction(DocQueryContext context) throws IOException {
    context.startBoolGroupList(type);
    for (SpiExpression expr : exprList.internalList()) {
      expr.writeDocQuery(context);
    }
    context.endBoolGroupList();
  }

  @Override
  public Object getIdEqualTo(String idName) {
    // always null for this expression
    return null;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {
    List<SpiExpression> list = exprList.internalList();
    // get the current state for 'require outer joins'
    boolean parentOuterJoins = manyWhereJoin.isRequireOuterJoins();
    if (type == Type.OR) {
      // turn on outer joins required for disjunction expressions
      manyWhereJoin.setRequireOuterJoins(true);
    }
    for (SpiExpression expr : list) {
      expr.containsMany(desc, manyWhereJoin);
    }
    if (type == Type.OR && !parentOuterJoins) {
      // restore state to not forcing outer joins
      manyWhereJoin.setRequireOuterJoins(false);
    }
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    exprList.validate(validation);
  }

  @Override
  public Junction<T> add(Expression item) {
    exprList.add(item);
    return this;
  }

  @Override
  public Junction<T> addAll(ExpressionList<T> addList) {
    exprList.addAll(addList);
    return this;
  }

  @Override
  public void addBindValues(SpiExpressionBind request) {
    for (SpiExpression expr : exprList.internalList()) {
      expr.addBindValues(request);
    }
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    List<SpiExpression> list = exprList.internalList();
    if (!list.isEmpty()) {
      request.append(type.prefix());
      request.append('(');
      for (int i = 0; i < list.size(); i++) {
        SpiExpression item = list.get(i);
        if (i > 0) {
          request.append(type.literal());
        }
        item.addSql(request);
      }
      request.append(')');
    }
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    for (SpiExpression expr : exprList.internalList()) {
      expr.prepareExpression(request);
    }
  }

  /**
   * Based on Junction type and all the expression contained.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append(type).append('[');
    for (SpiExpression expr : exprList.internalList()) {
      expr.queryPlanHash(builder);
      builder.append(',');
    }
    builder.append(']');
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    for (SpiExpression expr : exprList.internalList()) {
      expr.queryBindKey(key);
    }
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    JunctionExpression<?> that = (JunctionExpression<?>) other;
    return type == that.type && exprList.isSameByBind(that.exprList);
  }

  @Override
  public ExpressionList<T> match(String propertyName, String search) {
    return match(propertyName, search, null);
  }

  @Override
  public ExpressionList<T> match(String propertyName, String search, Match options) {
    return exprList.match(propertyName, search, options);
  }

  @Override
  public ExpressionList<T> multiMatch(String query, String... properties) {
    return exprList.multiMatch(query, properties);
  }

  @Override
  public ExpressionList<T> multiMatch(String query, MultiMatch options) {
    return exprList.multiMatch(query, options);
  }

  @Override
  public ExpressionList<T> textSimple(String search, TextSimple options) {
    return exprList.textSimple(search, options);
  }

  @Override
  public ExpressionList<T> textQueryString(String search, TextQueryString options) {
    return exprList.textQueryString(search, options);
  }

  @Override
  public ExpressionList<T> textCommonTerms(String search, TextCommonTerms options) {
    return exprList.textCommonTerms(search, options);
  }

  @Override
  public ExpressionList<T> allEq(Map<String, Object> propertyMap) {
    return exprList.allEq(propertyMap);
  }

  @Override
  public ExpressionList<T> and(Expression expOne, Expression expTwo) {
    return exprList.and(expOne, expTwo);
  }

  @Override
  public ExpressionList<T> inRangeWith(String lowProperty, String highProperty, Object value) {
    return exprList.inRangeWith(lowProperty, highProperty, value);
  }

  @Override
  public ExpressionList<T> inRangeWithProperties(String propertyName, String lowProperty, String highProperty) {
    return exprList.inRangeWithProperties(propertyName, lowProperty, highProperty);
  }

  @Override
  public ExpressionList<T> inRange(String propertyName, Object value1, Object value2) {
    return exprList.inRange(propertyName, value1, value2);
  }

  @Override
  public ExpressionList<T> between(String propertyName, Object value1, Object value2) {
    return exprList.between(propertyName, value1, value2);
  }

  @Override
  public ExpressionList<T> betweenProperties(String lowProperty, String highProperty, Object value) {
    return exprList.betweenProperties(lowProperty, highProperty, value);
  }

  @Override
  public ExpressionList<T> contains(String propertyName, String value) {
    return exprList.contains(propertyName, value);
  }

  @Override
  public ExpressionList<T> endsWith(String propertyName, String value) {
    return exprList.endsWith(propertyName, value);
  }

  @Override
  public ExpressionList<T> eq(String propertyName, Query<?> subQuery) {
    return exprList.eq(propertyName, subQuery);
  }

  @Override
  public ExpressionList<T> eq(String propertyName, Object value) {
    return exprList.eq(propertyName, value);
  }

  @Override
  public ExpressionList<T> eqIfPresent(String propertyName, @Nullable Object value) {
    return value == null ? this : exprList.eq(propertyName, value);
  }

  @Override
  public ExpressionList<T> eqOrNull(String propertyName, Object value) {
    return exprList.eqOrNull(propertyName, value);
  }

  @Override
  public ExpressionList<T> exampleLike(Object example) {
    return exprList.exampleLike(example);
  }

  @Override
  public ExpressionList<T> where(String expressions, Object... params) {
    throw new IllegalStateException("where not allowed on Junction expression list");
  }

  @Override
  public ExpressionList<T> filterMany(String prop) {
    throw new IllegalStateException("filterMany not allowed on Junction expression list");
  }

  @Override
  @SuppressWarnings("removal") // old API
  public ExpressionList<T> filterMany(String manyProperty, String expressions, Object... params) {
    throw new IllegalStateException("filterMany not allowed on Junction expression list");
  }

  @Override
  public ExpressionList<T> filterManyRaw(String manyProperty, String rawExpression, Object... params) {
    throw new IllegalStateException("filterMany not allowed on Junction expression list");
  }

  @Override
  public Query<T> usingTransaction(Transaction transaction) {
    return exprList.usingTransaction(transaction);
  }

  @Override
  public Query<T> usingConnection(Connection connection) {
    return exprList.usingConnection(connection);
  }

  @Override
  public int delete() {
    return exprList.delete();
  }

  @Override
  @SuppressWarnings("removal") // old API
  public int delete(Transaction transaction) {
    return exprList.delete(transaction);
  }

  @Override
  public int update() {
    return exprList.update();
  }

  @Override
  @SuppressWarnings("removal") // old API
  public int update(Transaction transaction) {
    return exprList.update(transaction);
  }

  @Override
  public Query<T> asOf(Timestamp asOf) {
    return exprList.asOf(asOf);
  }

  @Override
  public Query<T> asDraft() {
    return exprList.asDraft();
  }

  @Override
  public <D> DtoQuery<D> asDto(Class<D> dtoClass) {
    return exprList.asDto(dtoClass);
  }

  @Override
  public UpdateQuery<T> asUpdate() {
    return exprList.asUpdate();
  }

  @Override
  public Query<T> setIncludeSoftDeletes() {
    return exprList.setIncludeSoftDeletes();
  }

  @Override
  public List<Version<T>> findVersions() {
    return exprList.findVersions();
  }

  @Override
  public List<Version<T>> findVersionsBetween(Timestamp start, Timestamp end) {
    return exprList.findVersionsBetween(start, end);
  }

  @Override
  public Query<T> apply(FetchPath fetchPath) {
    return exprList.apply(fetchPath);
  }

  @Override
  public boolean exists() {
    return exprList.exists();
  }

  @Override
  public FutureIds<T> findFutureIds() {
    return exprList.findFutureIds();
  }

  @Override
  public FutureList<T> findFutureList() {
    return exprList.findFutureList();
  }

  @Override
  public FutureRowCount<T> findFutureCount() {
    return exprList.findFutureCount();
  }

  @Override
  public <A> List<A> findIds() {
    return exprList.findIds();
  }

  @Override
  public QueryIterator<T> findIterate() {
    return exprList.findIterate();
  }

  @Override
  public void findEach(Consumer<T> consumer) {
    exprList.findEach(consumer);
  }

  @Override
  public void findEach(int batch, Consumer<List<T>> consumer) {
    exprList.findEach(batch, consumer);
  }

  @Override
  public void findEachWhile(Predicate<T> consumer) {
    exprList.findEachWhile(consumer);
  }

  @Override
  public List<T> findList() {
    return exprList.findList();
  }

  @Override
  public <K> Map<K, T> findMap() {
    return exprList.findMap();
  }

  @Override
  public <A> List<A> findSingleAttributeList() {
    return exprList.findSingleAttributeList();
  }

  @Override
  public <A> Set<A> findSingleAttributeSet() {
    return exprList.findSingleAttributeSet();
  }

  @Override
  public PagedList<T> findPagedList() {
    return exprList.findPagedList();
  }

  @Override
  public int findCount() {
    return exprList.findCount();
  }

  @Override
  public Set<T> findSet() {
    return exprList.findSet();
  }

  @Nullable
  @Override
  public T findOne() {
    return exprList.findOne();
  }

  @Override
  public Optional<T> findOneOrEmpty() {
    return exprList.findOneOrEmpty();
  }

  @Override
  public Query<T> withLock(Query.LockType lockType) {
    return exprList.withLock(lockType);
  }

  @Override
  public Query<T> withLock(Query.LockType lockType, Query.LockWait lockWait) {
    return exprList.withLock(lockType, lockWait);
  }

  @Override
  public Query<T> forUpdate() {
    return exprList.forUpdate();
  }

  @Override
  public Query<T> forUpdateNoWait() {
    return exprList.forUpdateNoWait();
  }

  @Override
  public Query<T> forUpdateSkipLocked() {
    return exprList.forUpdateSkipLocked();
  }

  /**
   * Path exists - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonExists(String propertyName, String path) {
    return exprList.jsonExists(propertyName, path);
  }

  /**
   * Path does not exist - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonNotExists(String propertyName, String path) {
    return exprList.jsonNotExists(propertyName, path);
  }

  /**
   * Equal to - for the value at the given path in the JSON document.
   */
  @Override
  public ExpressionList<T> jsonEqualTo(String propertyName, String path, Object value) {
    return exprList.jsonEqualTo(propertyName, path, value);
  }

  /**
   * Not Equal to - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonNotEqualTo(String propertyName, String path, Object val) {
    return exprList.jsonNotEqualTo(propertyName, path, val);
  }

  /**
   * Greater than - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonGreaterThan(String propertyName, String path, Object val) {
    return exprList.jsonGreaterThan(propertyName, path, val);
  }

  /**
   * Greater than or equal to - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonGreaterOrEqual(String propertyName, String path, Object val) {
    return exprList.jsonGreaterOrEqual(propertyName, path, val);
  }

  /**
   * Less than - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonLessThan(String propertyName, String path, Object val) {
    return exprList.jsonLessThan(propertyName, path, val);
  }

  /**
   * Less than or equal to - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonLessOrEqualTo(String propertyName, String path, Object val) {
    return exprList.jsonLessOrEqualTo(propertyName, path, val);
  }

  /**
   * Between - for the given path in a JSON document.
   */
  @Override
  public ExpressionList<T> jsonBetween(String propertyName, String path, Object lowerValue, Object upperValue) {
    return exprList.jsonBetween(propertyName, path, lowerValue, upperValue);
  }

  @Override
  public ExpressionList<T> arrayContains(String propertyName, Object... values) {
    return exprList.arrayContains(propertyName, values);
  }

  @Override
  public ExpressionList<T> arrayNotContains(String propertyName, Object... values) {
    return exprList.arrayNotContains(propertyName, values);
  }

  @Override
  public ExpressionList<T> arrayIsEmpty(String propertyName) {
    return exprList.arrayIsEmpty(propertyName);
  }

  @Override
  public ExpressionList<T> arrayIsNotEmpty(String propertyName) {
    return exprList.arrayIsNotEmpty(propertyName);
  }

  @Override
  public ExpressionList<T> bitwiseAny(String propertyName, long flags) {
    return exprList.bitwiseAny(propertyName, flags);
  }

  @Override
  public ExpressionList<T> bitwiseAll(String propertyName, long flags) {
    return exprList.bitwiseAll(propertyName, flags);
  }

  @Override
  public ExpressionList<T> bitwiseAnd(String propertyName, long flags, long match) {
    return exprList.bitwiseAnd(propertyName, flags, match);
  }

  @Override
  public ExpressionList<T> bitwiseNot(String propertyName, long flags) {
    return exprList.bitwiseNot(propertyName, flags);
  }

  @Override
  public ExpressionList<T> ge(String propertyName, Query<?> subQuery) {
    return exprList.ge(propertyName, subQuery);
  }

  @Override
  public ExpressionList<T> ge(String propertyName, Object value) {
    return exprList.ge(propertyName, value);
  }

  @Override
  public ExpressionList<T> gt(String propertyName, Query<?> subQuery) {
    return exprList.gt(propertyName, subQuery);
  }

  @Override
  public ExpressionList<T> gt(String propertyName, Object value) {
    return exprList.gt(propertyName, value);
  }

  @Override
  public ExpressionList<T> gtOrNull(String propertyName, Object value) {
    return exprList.gtOrNull(propertyName, value);
  }

  @Override
  public ExpressionList<T> geOrNull(String propertyName, Object value) {
    return exprList.geOrNull(propertyName, value);
  }

  @Override
  public ExpressionList<T> gtIfPresent(String propertyName, @Nullable Object value) {
    return value == null ? this : exprList.gt(propertyName, value);
  }

  @Override
  public ExpressionList<T> geIfPresent(String propertyName, @Nullable Object value) {
    return value == null ? this : exprList.ge(propertyName, value);
  }

  @Override
  public ExpressionList<T> having() {
    throw new IllegalStateException("having() not allowed on Junction expression list");
  }

  @Override
  public ExpressionList<T> icontains(String propertyName, String value) {
    return exprList.icontains(propertyName, value);
  }

  @Override
  public ExpressionList<T> idEq(Object value) {
    return exprList.idEq(value);
  }

  @Override
  public ExpressionList<T> idIn(Object... idValues) {
    return exprList.idIn(idValues);
  }

  @Override
  public ExpressionList<T> idIn(Collection<?> idValues) {
    return exprList.idIn(idValues);
  }

  @Override
  public ExpressionList<T> iendsWith(String propertyName, String value) {
    return exprList.iendsWith(propertyName, value);
  }

  @Override
  public ExpressionList<T> ieq(String propertyName, String value) {
    return exprList.ieq(propertyName, value);
  }

  @Override
  public ExpressionList<T> ine(String propertyName, String value) {
    return exprList.ine(propertyName, value);
  }

  @Override
  public ExpressionList<T> iexampleLike(Object example) {
    return exprList.iexampleLike(example);
  }

  @Override
  public ExpressionList<T> ilike(String propertyName, String value) {
    return exprList.ilike(propertyName, value);
  }

  @Override
  public ExpressionList<T> inPairs(Pairs pairs) {
    return exprList.inPairs(pairs);
  }

  @Override
  public ExpressionList<T> inTuples(InTuples pairs) {
    return exprList.inTuples(pairs);
  }

  @Override
  public ExpressionList<T> in(String propertyName, Collection<?> values) {
    return exprList.in(propertyName, values);
  }

  @Override
  public ExpressionList<T> inOrEmpty(String propertyName, Collection<?> values) {
    return exprList.inOrEmpty(propertyName, values);
  }

  @Override
  public ExpressionList<T> in(String propertyName, Object... values) {
    return exprList.in(propertyName, values);
  }

  @Override
  public ExpressionList<T> in(String propertyName, Query<?> subQuery) {
    return exprList.in(propertyName, subQuery);
  }

  @Override
  public ExpressionList<T> exists(String sqlSubQuery, Object... bindValues) {
    return exprList.exists(sqlSubQuery, bindValues);
  }

  @Override
  public ExpressionList<T> notExists(String sqlSubQuery, Object... bindValues) {
    return exprList.notExists(sqlSubQuery, bindValues);
  }

  @Override
  public ExpressionList<T> inSubQuery(String propertyName, String sqlSubQuery, Object... bindValues) {
    return exprList.inSubQuery(propertyName, sqlSubQuery, bindValues);
  }

  @Override
  public ExpressionList<T> notInSubQuery(String propertyName, String sqlSubQuery, Object... bindValues) {
    return exprList.notInSubQuery(propertyName, sqlSubQuery, bindValues);
  }

  @Override
  public ExpressionList<T> eqSubQuery(String propertyName, String sqlSubQuery, Object... bindValues) {
    return exprList.eqSubQuery(propertyName, sqlSubQuery, bindValues);
  }

  @Override
  public ExpressionList<T> neSubQuery(String propertyName, String sqlSubQuery, Object... bindValues) {
    return exprList.neSubQuery(propertyName, sqlSubQuery, bindValues);
  }

  @Override
  public ExpressionList<T> gtSubQuery(String propertyName, String sqlSubQuery, Object... bindValues) {
    return exprList.gtSubQuery(propertyName, sqlSubQuery, bindValues);
  }

  @Override
  public ExpressionList<T> geSubQuery(String propertyName, String sqlSubQuery, Object... bindValues) {
    return exprList.geSubQuery(propertyName, sqlSubQuery, bindValues);
  }

  @Override
  public ExpressionList<T> ltSubQuery(String propertyName, String sqlSubQuery, Object... bindValues) {
    return exprList.ltSubQuery(propertyName, sqlSubQuery, bindValues);
  }

  @Override
  public ExpressionList<T> leSubQuery(String propertyName, String sqlSubQuery, Object... bindValues) {
    return exprList.leSubQuery(propertyName, sqlSubQuery, bindValues);
  }

  @Override
  public ExpressionList<T> notIn(String propertyName, Collection<?> values) {
    return exprList.notIn(propertyName, values);
  }

  @Override
  public ExpressionList<T> notIn(String propertyName, Object... values) {
    return exprList.notIn(propertyName, values);
  }

  @Override
  public ExpressionList<T> notIn(String propertyName, Query<?> subQuery) {
    return exprList.notIn(propertyName, subQuery);
  }

  @Override
  public ExpressionList<T> isEmpty(String propertyName) {
    return exprList.isEmpty(propertyName);
  }

  @Override
  public ExpressionList<T> isNotEmpty(String propertyName) {
    return exprList.isNotEmpty(propertyName);
  }

  @Override
  public ExpressionList<T> exists(Query<?> subQuery) {
    return exprList.exists(subQuery);
  }

  @Override
  public ExpressionList<T> notExists(Query<?> subQuery) {
    return exprList.notExists(subQuery);
  }

  @Override
  public ExpressionList<T> isNotNull(String propertyName) {
    return exprList.isNotNull(propertyName);
  }

  @Override
  public ExpressionList<T> isNull(String propertyName) {
    return exprList.isNull(propertyName);
  }

  @Override
  public ExpressionList<T> istartsWith(String propertyName, String value) {
    return exprList.istartsWith(propertyName, value);
  }

  @Override
  public ExpressionList<T> le(String propertyName, Query<?> subQuery) {
    return exprList.le(propertyName, subQuery);
  }

  @Override
  public ExpressionList<T> le(String propertyName, Object value) {
    return exprList.le(propertyName, value);
  }

  @Override
  public ExpressionList<T> like(String propertyName, String value) {
    return exprList.like(propertyName, value);
  }

  @Override
  public ExpressionList<T> lt(String propertyName, Query<?> subQuery) {
    return exprList.lt(propertyName, subQuery);
  }

  @Override
  public ExpressionList<T> lt(String propertyName, Object value) {
    return exprList.lt(propertyName, value);
  }

  @Override
  public ExpressionList<T> ltOrNull(String propertyName, Object value) {
    return exprList.ltOrNull(propertyName, value);
  }

  @Override
  public ExpressionList<T> leOrNull(String propertyName, Object value) {
    return exprList.leOrNull(propertyName, value);
  }

  @Override
  public ExpressionList<T> ltIfPresent(String propertyName, @Nullable Object value) {
    return value == null ? this : exprList.lt(propertyName, value);
  }

  @Override
  public ExpressionList<T> leIfPresent(String propertyName, @Nullable Object value) {
    return value == null ? this : exprList.le(propertyName, value);
  }

  @Override
  public ExpressionList<T> ne(String propertyName, Query<?> subQuery) {
    return exprList.ne(propertyName, subQuery);
  }

  @Override
  public ExpressionList<T> ne(String propertyName, Object value) {
    return exprList.ne(propertyName, value);
  }

  @Override
  public ExpressionList<T> not(Expression exp) {
    return exprList.not(exp);
  }

  @Override
  public ExpressionList<T> or(Expression expOne, Expression expTwo) {
    return exprList.or(expOne, expTwo);
  }

  @Override
  public OrderBy<T> orderBy() {
    return exprList.orderBy();
  }

  @Override
  public ExpressionList<T> orderBy(String orderBy) {
    return exprList.orderBy(orderBy);
  }

  @Override
  public Query<T> orderById(boolean orderById) {
    return exprList.orderById(orderById);
  }

  @Override
  public Query<T> query() {
    return exprList.query();
  }

  @Override
  public ExpressionList<T> raw(String raw, Object value) {
    return exprList.raw(raw, value);
  }

  @Override
  public ExpressionList<T> raw(String raw, Object... values) {
    return exprList.raw(raw, values);
  }

  @Override
  public ExpressionList<T> rawOrEmpty(String raw, Collection<?> values) {
    return exprList.rawOrEmpty(raw, values);
  }

  @Override
  public ExpressionList<T> raw(String raw) {
    return exprList.raw(raw);
  }

  @Override
  public Query<T> select(String properties) {
    return exprList.select(properties);
  }

  @Override
  public Query<T> select(FetchGroup<T> fetchGroup) {
    return exprList.select(fetchGroup);
  }

  @Override
  public Query<T> setDistinct(boolean distinct) {
    return exprList.setDistinct(distinct);
  }

  @Override
  public Query<T> setDocIndexName(String indexName) {
    return exprList.setDocIndexName(indexName);
  }

  @Override
  public ExpressionList<T> setFirstRow(int firstRow) {
    return exprList.setFirstRow(firstRow);
  }

  @Override
  public Query<T> setMapKey(String mapKey) {
    return exprList.setMapKey(mapKey);
  }

  @Override
  public ExpressionList<T> setMaxRows(int maxRows) {
    return exprList.setMaxRows(maxRows);
  }

  @Override
  public Query<T> setUseCache(boolean useCache) {
    return exprList.setUseCache(useCache);
  }

  @Override
  public Query<T> setBeanCacheMode(CacheMode useCache) {
    return exprList.setBeanCacheMode(useCache);
  }

  @Override
  public Query<T> setUseQueryCache(CacheMode useCache) {
    return exprList.setUseQueryCache(useCache);
  }

  @Override
  public Query<T> setUseDocStore(boolean useDocsStore) {
    return exprList.setUseDocStore(useDocsStore);
  }

  @Override
  public Query<T> setDisableLazyLoading(boolean disableLazyLoading) {
    return exprList.setDisableLazyLoading(disableLazyLoading);
  }

  @Override
  public Query<T> setDisableReadAuditing() {
    return exprList.setDisableReadAuditing();
  }

  @Override
  public Query<T> setCountDistinct(CountDistinctOrder orderBy) {
    return exprList.setCountDistinct(orderBy);
  }

  @Override
  public Query<T> setLabel(String label) {
    return exprList.setLabel(label);
  }

  @Override
  public ExpressionList<T> startsWith(String propertyName, String value) {
    return exprList.startsWith(propertyName, value);
  }

  @Override
  public ExpressionList<T> where() {
    return exprList.where();
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
    return exprList.not();
  }

  @Override
  public Junction<T> conjunction() {
    return exprList.conjunction();
  }

  @Override
  public Junction<T> disjunction() {
    return exprList.disjunction();
  }

  @Override
  public Junction<T> must() {
    return exprList.must();
  }

  @Override
  public Junction<T> should() {
    return exprList.should();
  }

  @Override
  public Junction<T> mustNot() {
    return exprList.mustNot();
  }

  @Override
  public ExpressionList<T> endJunction() {
    return exprList.endJunction();
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
  public String nestedPath(BeanDescriptor<?> desc) {
    PrepareDocNested.prepare(exprList, desc, type);
    String nestedPath = exprList.allDocNestedPath;
    if (nestedPath != null) {
      // push the nestedPath up to parent
      exprList.setAllDocNested(null);
      return nestedPath;
    }
    return null;
  }

  @Override
  public ExpressionList<T> clear() {
    return exprList.clear();
  }
}
