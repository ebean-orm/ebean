package org.tests.query.cancel;

import io.ebean.*;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.ForPlatform;
import io.ebean.annotation.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests, if all kind of queries are cancelable. There are two ways how to
 * cancel a query: <br/>
 * <b>At begin:</b>
 *
 * <pre>
 * query = DB.find(...)
 * query.cancel();
 * query.findList();
 * </pre>
 * <p>
 * The query was caneled before executing. In this case we do hit the DB driver
 * <br/>
 * <br/>
 * <b>During run:</b>
 *
 * <pre>
 * // Thread 1:              Thread 2
 * query = DB.find(...)
 * query.findList();
 *    ...finding
 *     ...finding            query.cancel();
 *      ...JDBC-Exception
 * </pre>
 * <p>
 * The test tries to simulate a slow query by installing the
 * {@link SlowDownEBasic} 'SELECT' trigger. The trigger can be configured to
 * wait 3 * <code>timing</code> ms and a second thread will cancel the query in
 * <code>timing</code> ms.
 * <p>
 * in this case, we expect a JDBC exception from the driver. <br/>
 * <br/>
 * NOTE:<br/>
 * H2 checks the cancel flag in org.h2.command.Prepared::setCurrentRowNumber
 * only every 128th row. So we need at least 128 models and we cannot check
 * queries like findCount or findOne, because they only return one row.
 *
 * @author Roland Praml, FOCONIS AG
 */
class SqlQueryCancelTest extends BaseTestCase {

  private final int timing = 20;

  @BeforeAll
  public static void setupTestData() throws SQLException {
    for (int i = 0; i < 128; i++) {
      EBasic model = new EBasic("Basic " + i);
      DB.save(model);
    }
    if (Platform.H2.equals(DB.getDefault().platform())) {
      SlowDownEBasic.setSelectWaitMillis(0);
    }
  }

  @Test
  public void cancelSqlQueryAtBegin() throws SQLException {
    doCancelSqlAtBegin(SqlQuery::findList);
    doCancelSqlAtBegin(SqlQuery::findOne);
    doCancelSqlAtBegin(q -> q.findEach(e -> {
    }));
    doCancelSqlAtBegin(q -> q.findEachWhile(e -> true));
  }

  @ForPlatform(Platform.H2)
  @Test
  public void cancelSqlDuringRun() throws SQLException {

    doCancelSqlDuringRun(SqlQuery::findList);
    // doCancelSqlDuringRun(q -> q.setMaxRows(1).findOne());
    // findOne cannot be tested, as H2 does the cancel check every 128 rows only
    doCancelSqlDuringRun(q -> q.findEach(e -> {
    }));
    doCancelSqlDuringRun(q -> q.findEachWhile(e -> true));
  }


  @Test
  public void cancelOrmQueryAtBegin() throws SQLException {
    doCancelOrmAtBegin(Query::findCount);
    doCancelOrmAtBegin(Query::findFutureCount);
    // We cannot test 'findCount' due H2 restrictions
    doCancelOrmAtBegin(Query::findFutureIds);
    doCancelOrmAtBegin(Query::findFutureList);
    doCancelOrmAtBegin(Query::findIds);
    doCancelOrmAtBegin(Query::findIterate);
    doCancelOrmAtBegin(Query::findList);
    doCancelOrmAtBegin(Query::findMap);
    doCancelOrmAtBegin(Query::findOne);
    doCancelOrmAtBegin(q -> q.setMaxRows(1000).findPagedList().getList()); // untested
    doCancelOrmAtBegin(Query::findSet);
    doCancelOrmAtBegin(q -> q.select("name").findSingleAttribute());
    doCancelOrmAtBegin(q -> q.select("name").findSingleAttributeList());
    doCancelOrmAtBegin(Query::findStream);
    //  testDuringRun(Query::findVersions);
    // EBasic has no history support, but it should work if @History is added
    doCancelOrmAtBegin(q -> q.findEach(e -> {
    }));
    doCancelOrmAtBegin(q -> q.findEachWhile(e -> true));
  }

  @ForPlatform(Platform.H2)
  @Test
  public void cancelOrmDuringRun() throws Throwable {
    // doCancelOrmDuringRun(Query::findCount);
    // testDuringRunFuture(Query::findFutureCount);
    // We cannot test 'findCount' due H2 restrictions
    doCancelOrmFutureDuringRun(Query::findFutureIds);
    doCancelOrmFutureDuringRun(Query::findFutureList);
    doCancelOrmDuringRun(Query::findIds);
    doCancelOrmDuringRun(Query::findIterate);
    doCancelOrmDuringRun(Query::findList);
    doCancelOrmDuringRun(Query::findMap);
    // doCancelOrmDuringRun(q -> q.setMaxRows(1).findOne());
    // findOne cannot be tested, as H2 does the cancel check every 128 rows only
    doCancelOrmDuringRun(q -> q.setMaxRows(1000).findPagedList().getList()); // untested
    doCancelOrmDuringRun(Query::findSet);
    doCancelOrmDuringRun(q -> q.select("name").findSingleAttribute());
    doCancelOrmDuringRun(q -> q.select("name").findSingleAttributeList());
    doCancelOrmDuringRun(Query::findStream);
    //  testDuringRun(Query::findVersions);
    // EBasic has no history support, but it should work if @History is added
    doCancelOrmDuringRun(q -> q.findEach(e -> {
    }));
    doCancelOrmDuringRun(q -> q.findEachWhile(e -> true));
  }

  @Test
  public void cancelOrmDuringIterate() throws SQLException {

    Query<EBasic> query = DB.find(EBasic.class);

    QueryIterator<EBasic> iter = query.findIterate();
    assertThat(iter.hasNext()).isTrue();
    query.cancel();
    assertThat(iter.next()).isNotNull();

    // We might have 100 entities in a buffer. So we must iterate through all.
    assertThatThrownBy(() -> {
      while (iter.hasNext()) iter.next();
    })
      .isInstanceOf(PersistenceException.class)
      .hasMessageContaining("Query was cancelled");
  }

  @Test
  public void cancelOrmDtoQueryAtBegin() throws SQLException {

    doCancelOrmDtoAtBegin(DtoQuery::findIterate);
    doCancelOrmDtoAtBegin(DtoQuery::findList);
    doCancelOrmDtoAtBegin(DtoQuery::findOne);
    doCancelOrmDtoAtBegin(DtoQuery::findStream);
    doCancelOrmDtoAtBegin(q -> q.findEach(e -> {
    }));
    doCancelOrmDtoAtBegin(q -> q.findEachWhile(e -> true));
  }

  @ForPlatform(Platform.H2)
  @Test
  public void cancelOrmDtoDuringRun() throws SQLException {

    doCancelOrmDtoDuringRun(DtoQuery::findIterate);
    doCancelOrmDtoDuringRun(DtoQuery::findList);
    // doCancelOrmDtoDuringRun(q -> q.setMaxRows(1).findOne());
    // findOne cannot be tested, as H2 does the cancel check every 128 rows only
    doCancelOrmDtoDuringRun(DtoQuery::findStream);
    doCancelOrmDtoDuringRun(q -> q.findEach(e -> {
    }));
    doCancelOrmDtoDuringRun(q -> q.findEachWhile(e -> true));
  }

  @Test
  void cancelOrmDtoDuringIterate() {
    DtoQuery<EBasicDto> query = DB.find(EBasic.class).select("id,status").asDto(EBasicDto.class);

    QueryIterator<EBasicDto> iter = query.findIterate();
    assertThat(iter.hasNext()).isTrue();
    query.cancel();
    assertThat(iter.next()).isNotNull();

    // We might have 100 entities in a buffer. So we must iterate through all.
    assertThatThrownBy(() -> {
      while (iter.hasNext()) iter.next();
    })
      .isInstanceOf(PersistenceException.class)
      .hasMessageContaining("Query was cancelled");
  }

  @Test
  public void cancelSqlDtoQueryAtBegin() throws SQLException {

    doCancelSqlDtoAtBegin(DtoQuery::findIterate);
    doCancelSqlDtoAtBegin(DtoQuery::findList);
    doCancelSqlDtoAtBegin(DtoQuery::findOne);
    doCancelSqlDtoAtBegin(DtoQuery::findStream);
    doCancelSqlDtoAtBegin(q -> q.findEach(e -> {
    }));
    doCancelSqlDtoAtBegin(q -> q.findEachWhile(e -> true));
  }

  @ForPlatform(Platform.H2)
  @Test
  public void cancelSqlDtoDuringRun() throws SQLException {

    //doCancelSqlDtoDuringRun(DtoQuery::findIterate);
    doCancelSqlDtoDuringRun(DtoQuery::findList);
    // doCancelSqlDtoDuringRun(q -> q.setMaxRows(1).findOne());
    // findOne cannot be tested, as H2 does the cancel check every 128 rows only
    doCancelSqlDtoDuringRun(DtoQuery::findStream);
    doCancelSqlDtoDuringRun(q -> q.findEach(e -> {
    }));
    doCancelSqlDtoDuringRun(q -> q.findEachWhile(e -> true));
  }

  @Test
  public void cancelSqlDtoDuringIterate() throws SQLException {

    DtoQuery<EBasicDto> query = DB.findDto(EBasicDto.class, "select id, status from e_basic");

    QueryIterator<EBasicDto> iter = query.findIterate();
    assertThat(iter.hasNext()).isTrue();
    query.cancel();
    assertThat(iter.next()).isNotNull();

    // We might have 100 entities in a buffer. So we must iterate through all.
    assertThatThrownBy(() -> {
      while (iter.hasNext()) iter.next();
    })
      .isInstanceOf(PersistenceException.class)
      .hasMessageContaining("Query was cancelled");
  }

  private void doCancelSqlAtBegin(Consumer<SqlQuery> test) throws SQLException {
    SqlQuery query = DB.sqlQuery("select * from e_basic");
    query.cancel();
    assertThatThrownBy(() -> test.accept(query))
      .isInstanceOf(PersistenceException.class)
      .hasMessageContaining("Query was cancelled");
  }

  private void doCancelSqlDuringRun(Consumer<SqlQuery> test) throws SQLException {
    SqlQuery warmup = DB.sqlQuery("select * from e_basic");
    test.accept(warmup);
    SqlQuery query = DB.sqlQuery("select * from e_basic");
    executeDelayed(query::cancel);
    assertThatThrownBy(() -> test.accept(query))
      .isInstanceOf(PersistenceException.class)
      .hasCauseInstanceOf(org.h2.jdbc.JdbcSQLTimeoutException.class);
  }

  private void doCancelOrmAtBegin(Consumer<Query<EBasic>> test) throws SQLException {
    Query<EBasic> query = DB.find(EBasic.class);
    query.cancel();
    assertThatThrownBy(() -> test.accept(query))
      .isInstanceOf(PersistenceException.class)
      .hasMessageContaining("Query was cancelled");
  }

  private void doCancelOrmDuringRun(Consumer<Query<EBasic>> test) throws SQLException {
    Query<EBasic> warmup = DB.find(EBasic.class);
    test.accept(warmup);
    Query<EBasic> warmup2 = DB.find(EBasic.class);
    test.accept(warmup2);
    Query<EBasic> query = DB.find(EBasic.class);
    executeDelayed(query::cancel);
    assertThatThrownBy(() -> test.accept(query))
      .isInstanceOf(PersistenceException.class)
      .hasCauseInstanceOf(org.h2.jdbc.JdbcSQLTimeoutException.class);
  }

  private void doCancelOrmFutureDuringRun(Function<Query<EBasic>, Future<?>> test) throws SQLException, InterruptedException, ExecutionException {
    Query<EBasic> warmup = DB.find(EBasic.class);
    test.apply(warmup).get();

    Query<EBasic> query = DB.find(EBasic.class);
    executeDelayed(query::cancel);
    assertThatThrownBy(() -> {
      try {
        test.apply(query).get();
      } catch (ExecutionException ee) {
        throw ee.getCause();
      }
    })
      .isInstanceOf(PersistenceException.class)
      .hasCauseInstanceOf(org.h2.jdbc.JdbcSQLTimeoutException.class);
  }

  private void doCancelOrmDtoAtBegin(Consumer<DtoQuery<EBasicDto>> test) throws SQLException {
    DtoQuery<EBasicDto> query = DB.find(EBasic.class).select("id,status").asDto(EBasicDto.class);
    query.cancel();
    assertThatThrownBy(() -> test.accept(query))
      .isInstanceOf(PersistenceException.class)
      .hasMessageContaining("Query was cancelled");
  }

  private void doCancelOrmDtoDuringRun(Consumer<DtoQuery<EBasicDto>> test) throws SQLException {
    DtoQuery<EBasicDto> warmup = DB.find(EBasic.class).select("id,status").asDto(EBasicDto.class);
    test.accept(warmup);

    DtoQuery<EBasicDto> query = DB.find(EBasic.class).select("id,status").asDto(EBasicDto.class);
    executeDelayed(query::cancel);
    assertThatThrownBy(() -> test.accept(query))
      .isInstanceOf(PersistenceException.class)
      .hasCauseInstanceOf(org.h2.jdbc.JdbcSQLTimeoutException.class);
  }

  private void doCancelSqlDtoAtBegin(Consumer<DtoQuery<EBasicDto>> test) throws SQLException {
    DtoQuery<EBasicDto> query = DB.findDto(EBasicDto.class, "select id, status from e_basic");
    query.cancel();
    assertThatThrownBy(() -> test.accept(query))
      .isInstanceOf(PersistenceException.class)
      .hasMessageContaining("Query was cancelled");
  }

  private void doCancelSqlDtoDuringRun(Consumer<DtoQuery<EBasicDto>> test) throws SQLException {
    DtoQuery<EBasicDto> warmup = DB.findDto(EBasicDto.class, "select id, status from e_basic");
    test.accept(warmup);

    DtoQuery<EBasicDto> query = DB.findDto(EBasicDto.class, "select id, status from e_basic");
    executeDelayed(query::cancel);
    assertThatThrownBy(() -> test.accept(query))
      .isInstanceOf(PersistenceException.class)
      .hasCauseInstanceOf(org.h2.jdbc.JdbcSQLTimeoutException.class);
  }

  private void executeDelayed(Runnable r) throws SQLException {
    // We modify the DB here. Otherwise we may hit an internal H2 cache, if the
    // same query is performed. Queries from the cache cannot be canceled.
    EBasic makeDbDirty = new EBasic("Basic " + UUID.randomUUID());
    DB.save(makeDbDirty);
    SlowDownEBasic.setSelectWaitMillis(timing * 3);
    new Thread(() -> {
      try {
        Thread.sleep(timing);
        r.run();
        SlowDownEBasic.setSelectWaitMillis(0);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
  }

}
