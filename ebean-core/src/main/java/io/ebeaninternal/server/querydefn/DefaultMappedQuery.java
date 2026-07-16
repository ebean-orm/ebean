package io.ebeaninternal.server.querydefn;

import io.ebean.DtoMapContext;
import io.ebean.DtoMapper;
import io.ebean.MappedQuery;
import io.ebean.PagedList;
import io.ebean.Transaction;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Default implementation of {@link MappedQuery} backing {@code query.mapTo(dtoType)}.
 * <p>
 * Resolves the generated {@link DtoMapper} for the query's (entity, dto) pair, applies its
 * {@link DtoMapper#fetchGroup()} (derived from the target DTO's declared shape) - unless the
 * caller has already specified their own {@code select()}/{@code fetch()} spec, in which case
 * that manual spec is left untouched - and forces {@code setUnmodifiable(true)} on the
 * underlying query before it is executed, then maps the resulting entity graph into the
 * target DTO graph.
 */
public final class DefaultMappedQuery<T, D> implements MappedQuery<D> {

  private final SpiEbeanServer server;
  private final SpiQuery<T> query;
  private final Class<D> dtoType;
  private DtoMapper<T, D> mapper;
  private boolean applied;

  public DefaultMappedQuery(SpiEbeanServer server, SpiQuery<T> query, Class<D> dtoType) {
    this.server = server;
    this.query = query;
    this.dtoType = dtoType;
  }

  /**
   * Use an already-resolved mapper instance directly - e.g. a named variant accessor on a
   * generated mapper, such as {@code query.mapTo(User.class, userMapper.noFleets())} - bypassing
   * {@code server.dtoMapper(...)} lookup entirely.
   */
  public DefaultMappedQuery(SpiEbeanServer server, SpiQuery<T> query, Class<D> dtoType, DtoMapper<T, D> mapper) {
    this.server = server;
    this.query = query;
    this.dtoType = dtoType;
    this.mapper = mapper;
  }

  /**
   * Apply the mapper's fetch spec + unmodifiable to the underlying query (once) - must happen
   * before the query is executed. Uses an explicit {@code applied} flag rather than a
   * {@code mapper == null} check since a pre-supplied mapper (see the constructor above) is
   * already non-null before the fetch spec has been applied.
   */
  private DtoMapper<T, D> mapper() {
    if (!applied) {
      if (mapper == null) {
        mapper = server.dtoMapper(query.getBeanType(), dtoType);
      }
      if (query.detail().isEmpty()) {
        // only apply the mapper's derived fetch spec if the caller hasn't already specified
        // their own select()/fetch() - allowing manual query tuning/optimisation to take
        // precedence over the DTO shape's default fetch spec when needed
        query.select(mapper.fetchGroup());
      }
      query.setUnmodifiable(true);
      applied = true;
    }
    return mapper;
  }


  @Override
  public List<D> findList() {
    DtoMapper<T, D> m = mapper();
    return m.mapList(query.findList());
  }

  @Override
  public D findOne() {
    DtoMapper<T, D> m = mapper();
    return m.map(query.findOne());
  }

  @Override
  public Optional<D> findOneOrEmpty() {
    return Optional.ofNullable(findOne());
  }

  @Override
  public PagedList<D> findPagedList() {
    DtoMapper<T, D> m = mapper();
    return new MappedPagedList<>(query.findPagedList(), m);
  }

  @Override
  public Stream<D> findStream() {
    DtoMapper<T, D> m = mapper();
    DtoMapContext context = new DtoMapContext();
    return query.findStream().map(source -> m.map(source, context));
  }

  @Override
  public MappedQuery<D> usingMaster(boolean useMaster) {
    query.usingMaster(useMaster);
    return this;
  }

  @Override
  public MappedQuery<D> usingTransaction(Transaction transaction) {
    query.usingTransaction(transaction);
    return this;
  }

  @Override
  public MappedQuery<D> usingConnection(Connection connection) {
    query.usingConnection(connection);
    return this;
  }
}
