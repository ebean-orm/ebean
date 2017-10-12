package org.tests.changelog;

import io.ebean.BaseTestCase;
import io.ebean.EbeanServerFactory;
import io.ebean.annotation.ChangeLog;
import io.ebean.config.ServerConfig;
import io.ebean.event.BeanPersistRequest;
import io.ebean.event.changelog.ChangeLogFilter;
import io.ebean.event.changelog.ChangeLogListener;
import io.ebean.event.changelog.ChangeLogPrepare;
import io.ebean.event.changelog.ChangeLogRegister;
import io.ebean.event.changelog.ChangeSet;
import io.ebeaninternal.api.SpiEbeanServer;
import org.tests.model.basic.EBasicChangeLog;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

  @Test
  public void test() {

    EBasicChangeLog bean = new EBasicChangeLog();
    bean.setName("logBean");
    bean.setShortDescription("hello");
    server.save(bean);

    bean.setName("ChangedName");
    server.save(bean);

    server.delete(bean);

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
