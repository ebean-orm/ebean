package io.ebeaninternal.server.changelog;

import io.ebean.ValuePair;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeSet;
import io.ebean.event.changelog.ChangeType;
import io.ebean.event.changelog.TxnState;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.HashMap;
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

  @NotNull
  private BeanChange createInsert(long startId) {
    Map<String, ValuePair> values = new LinkedHashMap<>();
    values.put("name", new ValuePair("rob", null));
    values.put("modified", new ValuePair(new Timestamp(System.currentTimeMillis()), null));

    BeanChange bean = new BeanChange("mytable", startId + 1, ChangeType.INSERT, null);
    bean.setValues(values);
    return bean;
  }

  @NotNull
  private BeanChange createUpdate(long startId) {
    Map<String, ValuePair> values = new LinkedHashMap<>();
    values.put("name", new ValuePair("jim", "steve"));
    values.put("nowHasVal", new ValuePair("wasNull", null));
    values.put("nowNull", new ValuePair(null, "hadVal"));

    values.put("modified", new ValuePair(new Timestamp(System.currentTimeMillis()), null));

    BeanChange bean = new BeanChange("mytable", startId + 2, ChangeType.UPDATE, null);
    bean.setValues(values);
    return bean;
  }

  @NotNull
  private BeanChange createDelete(long startId) {
    return new BeanChange("mytable", startId + 3, ChangeType.DELETE, new HashMap<>());
  }

}
