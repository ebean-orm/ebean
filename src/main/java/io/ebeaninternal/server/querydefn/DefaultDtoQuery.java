package io.ebeaninternal.server.querydefn;

import io.ebean.DtoQuery;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiDtoQuery;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.dto.DtoBeanDescriptor;
import io.ebeaninternal.server.dto.DtoMappingRequest;
import io.ebeaninternal.server.dto.DtoQueryPlan;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Default implementation of DtoQuery.
 */
public class DefaultDtoQuery<T> implements SpiDtoQuery<T> {

  private final SpiEbeanServer server;

  private final DtoBeanDescriptor<T> descriptor;

  private String sql;

  private int firstRow;

  private int maxRows;

  private int timeout;

  private int bufferFetchSizeHint;

  private boolean relaxedMode;

  private String label;

  /**
   * Bind parameters when using the query language.
   */
  private final BindParams bindParams = new BindParams();

  /**
   * Additional supply a query detail object.
   */
  public DefaultDtoQuery(SpiEbeanServer server, DtoBeanDescriptor<T> descriptor, String sql) {
    this.server = server;
    this.descriptor = descriptor;
    this.sql = sql;
  }

  @Override
  public String planKey() {
    return sql+":first"+firstRow+":max"+maxRows;
  }

  @Override
  public DtoQueryPlan getQueryPlan(String planKey) {
    return descriptor.getQueryPlan(planKey);
  }

  @Override
  public DtoQueryPlan buildPlan(DtoMappingRequest request) {
    return descriptor.buildPlan(request);
  }

  @Override
  public void putQueryPlan(String planKey, DtoQueryPlan plan) {
    descriptor.putQueryPlan(planKey, plan);
  }

  @Override
  public void findEach(Consumer<T> consumer) {
    server.findDtoEach(this, consumer);
  }

  @Override
  public void findEachWhile(Predicate<T> consumer) {
    server.findDtoEachWhile(this, consumer);
  }

  @Override
  public List<T> findList() {
    return server.findDtoList(this);
  }

  @Override
  public T findOne() {
    return server.findDtoOne(this);
  }

  @Override
  public Optional<T> findOneOrEmpty() {
    return Optional.ofNullable(findOne());
  }

  @Override
  public DtoQuery<T> setParameter(int position, Object value) {
    bindParams.setParameter(position, value);
    return this;
  }

  @Override
  public DtoQuery<T> setParameter(String paramName, Object value) {
    bindParams.setParameter(paramName, value);
    return this;
  }

  @Override
  public String toString() {
    return "DtoQuery [" + sql + "]";
  }

  @Override
  public Class<T> getType() {
    return descriptor.getType();
  }

  @Override
  public DtoQuery<T> setRelaxedMode() {
    this.relaxedMode = true;
    return this;
  }

  @Override
  public boolean isRelaxedMode() {
    return relaxedMode;
  }

  @Override
  public DtoQuery<T> setLabel(String label) {
    this.label = label;
    return this;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public int getFirstRow() {
    return firstRow;
  }

  @Override
  public DtoQuery<T> setFirstRow(int firstRow) {
    this.firstRow = firstRow;
    return this;
  }

  @Override
  public int getMaxRows() {
    return maxRows;
  }

  @Override
  public DtoQuery<T> setMaxRows(int maxRows) {
    this.maxRows = maxRows;
    return this;
  }

  @Override
  public int getTimeout() {
    return timeout;
  }

  @Override
  public DtoQuery<T> setTimeout(int secs) {
    this.timeout = secs;
    return this;
  }

  @Override
  public BindParams getBindParams() {
    return bindParams;
  }

  @Override
  public DtoQuery<T> setBufferFetchSizeHint(int bufferFetchSizeHint) {
    this.bufferFetchSizeHint = bufferFetchSizeHint;
    return this;
  }

  @Override
  public int getBufferFetchSizeHint() {
    return bufferFetchSizeHint;
  }

  @Override
  public String getQuery() {
    return sql;
  }

}
