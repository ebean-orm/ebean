package io.ebeaninternal.server.querydefn;

import org.jspecify.annotations.NullMarked;
import io.ebean.DtoQuery;
import io.ebean.ProfileLocation;
import io.ebean.QueryIterator;
import io.ebean.Transaction;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.dto.DtoBeanDescriptor;
import io.ebeaninternal.server.dto.DtoMappingRequest;
import io.ebeaninternal.server.dto.DtoQueryPlan;
import io.ebeaninternal.server.transaction.ExternalJdbcTransaction;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Default implementation of DtoQuery.
 */
@NullMarked
public final class DefaultDtoQuery<T> extends AbstractQuery implements SpiDtoQuery<T> {

  private final SpiEbeanServer server;
  private final DtoBeanDescriptor<T> descriptor;
  private final SpiQuery<?> ormQuery;
  private String sql;
  private int firstRow;
  private int maxRows;
  private int timeout;
  private int bufferFetchSizeHint;
  private boolean relaxedMode;
  private String label;
  private ProfileLocation profileLocation;
  private final BindParams bindParams = new BindParams();
  private SpiTransaction transaction;

  /**
   * Create given an underlying ORM query.
   */
  public DefaultDtoQuery(SpiEbeanServer server, DtoBeanDescriptor<T> descriptor, SpiQuery<?> ormQuery) {
    this.server = server;
    this.descriptor = descriptor;
    this.ormQuery = ormQuery;
    this.useMaster = ormQuery.isUseMaster();
    this.label = ormQuery.label();
    this.profileLocation = ormQuery.profileLocation();
  }

  /**
   * Create given a native SQL query.
   */
  public DefaultDtoQuery(SpiEbeanServer server, DtoBeanDescriptor<T> descriptor, String sql) {
    this.server = server;
    this.descriptor = descriptor;
    this.ormQuery = null;
    this.sql = sql;
  }

  @Override
  public String planKey() {
    return sql + ":first" + firstRow + ":max" + maxRows;
  }

  @Override
  public DtoQueryPlan queryPlan(Object planKey) {
    return descriptor.queryPlan(planKey);
  }

  @Override
  public DtoQueryPlan buildPlan(DtoMappingRequest request) {
    return descriptor.buildPlan(request);
  }

  @Override
  public void putQueryPlan(Object planKey, DtoQueryPlan plan) {
    descriptor.putQueryPlan(planKey, plan);
  }

  @Override
  public DtoQuery<T> usingTransaction(Transaction transaction) {
    this.transaction = (SpiTransaction) transaction;
    return this;
  }

  @Override
  public DtoQuery<T> usingConnection(Connection connection) {
    this.transaction = new ExternalJdbcTransaction(connection);
    return this;
  }

  @Override
  public DtoQuery<T> usingMaster(boolean useMaster) {
    this.useMaster = useMaster;
    return this;
  }

  @Override
  public boolean isUseMaster() {
    return useMaster;
  }

  @Override
  public void findEach(Consumer<T> consumer) {
    server.findDtoEach(this, consumer);
  }

  @Override
  public void findEach(int batch, Consumer<List<T>> consumer) {
    server.findDtoEach(this, batch, consumer);
  }

  @Override
  public void findEachWhile(Predicate<T> consumer) {
    server.findDtoEachWhile(this, consumer);
  }

  @Override
  public QueryIterator<T> findIterate() {
    return server.findDtoIterate(this);
  }

  @Override
  public Stream<T> findStream() {
    return server.findDtoStream(this);
  }

  @Override
  public List<T> findList() {
    return server.findDtoList(this);
  }

  @Nullable
  @Override
  public T findOne() {
    return server.findDtoOne(this);
  }

  @Override
  public Optional<T> findOneOrEmpty() {
    return Optional.ofNullable(findOne());
  }

  @Override
  public DtoQuery<T> setNullParameter(String name, int jdbcType) {
    if (ormQuery != null) {
      ormQuery.initBindParams().setNullParameter(name, jdbcType);
    } else {
      bindParams.setNullParameter(name, jdbcType);
    }
    return this;
  }

  @Override
  public DtoQuery<T> setNullParameter(int position, int jdbcType) {
    if (ormQuery != null) {
      ormQuery.initBindParams().setNullParameter(position, jdbcType);
    } else {
      bindParams.setNullParameter(position, jdbcType);
    }
    return this;
  }

  @Override
  public DtoQuery<T> setParameter(int position, Object value) {
    if (ormQuery != null) {
      ormQuery.setParameter(position, value);
    } else {
      bindParams.setParameter(position, value);
    }
    return this;
  }

  @Override
  public DtoQuery<T> setParameter(String paramName, Object value) {
    if (ormQuery != null) {
      ormQuery.setParameter(paramName, value);
    } else {
      bindParams.setParameter(paramName, value);
    }
    return this;
  }

  @Override
  public DtoQuery<T> setArrayParameter(String paramName, Collection<?> values) {
    if (ormQuery != null) {
      ormQuery.setArrayParameter(paramName, values);
    } else {
      bindParams.setArrayParameter(paramName, values);
    }
    return this;
  }

  @Override
  public DtoQuery<T> setParameters(Object... values) {
    if (ormQuery != null) {
      ormQuery.setParameters(values);
    } else {
      bindParams.setNextParameters(values);
    }
    return this;
  }

  @Override
  public DtoQuery<T> setParameter(Object value) {
    if (ormQuery != null) {
      ormQuery.setParameter(value);
    } else {
      bindParams.setNextParameter(value);
    }
    return this;
  }

  @Override
  public String toString() {
    return "DtoQuery " + sql;
  }

  @Override
  public Class<T> type() {
    return descriptor.type();
  }

  @Override
  public SpiQuery<?> ormQuery() {
    return ormQuery;
  }

  @Override
  public SpiTransaction transaction() {
    return transaction;
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
    if (ormQuery != null) {
      ormQuery.setLabel(label);
    }
    return this;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Nullable
  @Override
  public String planLabel() {
    if (label != null) {
      return label;
    }
    if (profileLocation != null) {
      return profileLocation.label();
    }
    return null;
  }

  @Override
  public void obtainLocation() {
    if (profileLocation != null) {
      profileLocation.obtain();
    }
  }

  @Override
  public DtoQuery<T> setProfileLocation(ProfileLocation profileLocation) {
    this.profileLocation = profileLocation;
    return this;
  }

  @Override
  public ProfileLocation profileLocation() {
    return profileLocation;
  }

  @Override
  public int getFirstRow() {
    return firstRow;
  }

  @Override
  public DtoQuery<T> setFirstRow(int firstRow) {
    this.firstRow = firstRow;
    if (ormQuery != null) {
      ormQuery.setFirstRow(firstRow);
    }
    return this;
  }

  @Override
  public int getMaxRows() {
    return maxRows;
  }

  @Override
  public DtoQuery<T> setMaxRows(int maxRows) {
    this.maxRows = maxRows;
    if (ormQuery != null) {
      ormQuery.setMaxRows(maxRows);
    }
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
  public void setDefaultFetchBuffer(int fetchSize) {
    if (bufferFetchSizeHint == 0) {
      bufferFetchSizeHint = fetchSize;
    }
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
