package org.tests.changelog;

import io.ebean.BaseTestCase;
import io.ebean.EbeanServerFactory;
import io.ebean.annotation.ChangeLog;
import io.ebean.config.ServerConfig;
import io.ebean.event.BeanPersistRequest;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeLogFilter;
import io.ebean.event.changelog.ChangeLogListener;
import io.ebean.event.changelog.ChangeLogPrepare;
import io.ebean.event.changelog.ChangeLogRegister;
import io.ebean.event.changelog.ChangeSet;
import io.ebean.event.changelog.ChangeType;
import io.ebeaninternal.api.SpiEbeanServer;

import org.tests.model.basic.EBasicChangeLog;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;


public class TestChangeLog extends BaseTestCase {

  TDChangeLogPrepare changeLogPrepare = new TDChangeLogPrepare();

  TDChangeLogListener changeLogListener = new TDChangeLogListener();

  TDChangeLogRegister changeLogRegister = new TDChangeLogRegister();

  SpiEbeanServer server;

  @Before
  public void setup() {
    server = getServer();
  }

  @After
  public void shutdown() {
    server.shutdown(true, false);
  }
  /**
   * Returns the last changes or null. Will reset internal state.
   */
  private BeanChange getLastChanges() {
    if (changeLogListener.changes == null) {
      return null;
    }
    BeanChange ret = changeLogListener.changes.getChanges().get(0);
    changeLogListener.changes = null;
    return ret;
  }

  @Test
  public void test() {

    EBasicChangeLog bean = new EBasicChangeLog();
    bean.setName("logBean");
    bean.setShortDescription("hello\n\"'"); // some JSON special chars
    bean.getList().add("Item 1");
    bean.getList().add("Item 2");

    // insert
    server.save(bean);

    BeanChange change = getLastChanges();
    assertThat(change.getEvent()).isEqualTo(ChangeType.INSERT);
    assertThat(change.getData())
      .contains("\"name\":\"logBean\"")
      .contains("\"shortDescription\":\"hello\\n\\\"\'\"");

    // save again
    server.save(bean);
    assertThat(getLastChanges()).isNull();

    // fetch & save clean
    bean = server.find(EBasicChangeLog.class, bean.getId());
    server.save(bean);
    assertThat(getLastChanges()).isNull();

    // update list
    bean.getList().add("Item 3");
    server.save(bean);

    change = getLastChanges();
    assertThat(change.getEvent()).isEqualTo(ChangeType.UPDATE);
    assertThat(change.getData()).contains("\"list\":[\"Item 1\",\"Item 2\",\"Item 3\"]");
    assertThat(change.getOldData()).contains("\"list\":[\"Item 1\",\"Item 2\"]");

    // read list -> clean
    assertThat(bean.getList().get(0)).isEqualTo("Item 1");
    server.save(bean);
    assertThat(getLastChanges()).isNull();

    // modify no json property
    bean.setName("ChangedName");
    server.save(bean);
    change = getLastChanges();
    assertThat(change.getData()).contains("\"name\":\"ChangedName\"");
    assertThat(change.getOldData()).contains("\"name\":\"logBean\"");


    // delete
    server.delete(bean);

    change = changeLogListener.changes.getChanges().get(0);
    assertThat(change.getEvent()).isEqualTo(ChangeType.DELETE);

  }

  private SpiEbeanServer getServer() {

    System.setProperty("ebean.ignoreExtraDdl", "true");

    ServerConfig config = new ServerConfig();
    config.setName("h2other");
    config.loadFromProperties();

    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDefaultServer(false);
    config.setRegister(false);
    config.setChangeLogAsync(false);

    config.addClass(EBasicChangeLog.class);

    config.setChangeLogPrepare(changeLogPrepare);
    config.setChangeLogListener(changeLogListener);
    config.setChangeLogRegister(changeLogRegister);

    return (SpiEbeanServer) EbeanServerFactory.create(config);
  }

  class TDChangeLogPrepare implements ChangeLogPrepare {
    @Override
    public boolean prepare(ChangeSet changes) {
      changes.setUserId("appUser1");
      changes.setUserIpAddress("1.1.1.1");
      return true;
    }
  }

  class TDChangeLogListener implements ChangeLogListener {

    ObjectMapper objectMapper = new ObjectMapper();

    ChangeSet changes;

    /**
     */
    public TDChangeLogListener() {
      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }


    @Override
    public void log(ChangeSet changes) {
      this.changes = changes;
      try {
        String json = objectMapper.writeValueAsString(changes);
        ChangeSet changes1 = objectMapper.readValue(json, ChangeSet.class);

        assertEquals(changes.getTxnId(), changes1.getTxnId());
        assertEquals(changes.getUserId(), changes1.getUserId());
        assertEquals(changes.getUserIpAddress(), changes1.getUserIpAddress());

      } catch (Exception e) {
        throw new AssertionError(e);
      }
    }

  }

  class TDChangeLogRegister implements ChangeLogRegister {

    @Override
    public ChangeLogFilter getChangeFilter(Class<?> beanType) {

      ChangeLog changeLog = beanType.getAnnotation(ChangeLog.class);
      if (changeLog != null) {
        return new TDFilter();
      }

      return null;
    }
  }


  /**
   * Change log filter that includes all the inserts, updates and deletes.
   */
  class TDFilter implements ChangeLogFilter {

    /**
     * Returns true including all the inserts.
     */
    @Override
    public boolean includeInsert(BeanPersistRequest<?> insertRequest) {
      return true;
    }

    @Override
    public boolean includeUpdate(BeanPersistRequest<?> updateRequest) {
      return true;
    }

    @Override
    public boolean includeDelete(BeanPersistRequest<?> deleteRequest) {
      return true;
    }
  }
}
