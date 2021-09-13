package io.ebeaninternal.server.changelog;

import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeSet;
import io.ebean.event.changelog.ChangeType;
import io.ebean.event.changelog.TxnState;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Helper {

  public ChangeSet createChangeSet(String txnId, long startId) {

    ChangeSet cs = new ChangeSet();
    cs.setTxnId(txnId);
    cs.setTxnState(TxnState.COMMITTED);
    cs.setTxnBatch(0);
    cs.setSource("myApp");
    cs.setUserId("user234");
    cs.setUserIpAddress("123.4.5.6");
    cs.getUserContext().put("someKey", "user defined input");

    List<BeanChange> changes = cs.getChanges();

    changes.add(createInsert(startId));
    changes.add(createUpdate(startId));
    changes.add(createDelete(startId));

    return cs;
  }

  private BeanChange createInsert(long startId) {

    Map<String, Object> values = new LinkedHashMap<>();
    values.put("name", "rob");
    values.put("modified", new Timestamp(System.currentTimeMillis()));

    Map<String, Object> oldValues = new LinkedHashMap<>();
    oldValues.put("name", "rob");
    oldValues.put("modified", new Timestamp(System.currentTimeMillis() - 100000));

    return new BeanChange("mytable", null, startId + 1, ChangeType.INSERT, "");//values, oldValues);
  }

  private BeanChange createUpdate(long startId) {
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("name", "jim");
    values.put("nowHasVal", "wasNull");
    values.put("nowNull", null);
    values.put("modified", new Timestamp(System.currentTimeMillis()));


    Map<String, Object> oldValues = new LinkedHashMap<>();
    oldValues.put("name", "steve");
    oldValues.put("nowHasVal", null);
    oldValues.put("nowNull", "hadVal");
    oldValues.put("modified", new Timestamp(System.currentTimeMillis() - 100000));


    return new BeanChange("mytable", null, startId + 2, ChangeType.UPDATE, "");//values, oldValues);
  }

  private BeanChange createDelete(long startId) {
    return new BeanChange("mytable", null, startId + 3, ChangeType.DELETE, null);
  }

}
