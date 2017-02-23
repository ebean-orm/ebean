package org.tests.idkeys;

import io.ebean.BaseTestCase;
import io.ebean.Transaction;
import io.ebean.bean.EntityBean;

import org.junit.Test;
import org.tests.idkeys.db.AuditLog;

import static org.junit.Assert.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test various aspects of the PropertyChangeSupport
 */
public class TestPropertyChangeSupport extends BaseTestCase implements PropertyChangeListener {
  private int nuofEvents = 0;
  private List<PropertyChangeEvent> pces = new ArrayList<>();
  private PropertyChangeEvent lastPce;

  public void propertyChange(PropertyChangeEvent evt) {
    nuofEvents++;
    lastPce = evt;
    pces.add(evt);
  }

  /**
   * Test the core property change functionality
   */
  @Test
  public void testPropertyChange() throws SQLException {
    resetEvent();

    AuditLog al = new AuditLog();

    assertTrue("is EntityBean check", al instanceof EntityBean);

    addListener(al, this);

    // test if we get property notification about simple property changes
    al.setDescription("ABC");

    assertNotNull(lastPce);
    assertEquals(1, nuofEvents);
    assertEquals("description", lastPce.getPropertyName());
    assertNull(lastPce.getOldValue());
    assertEquals("ABC", lastPce.getNewValue());

    al.setDescription("DEF");

    assertNotNull(lastPce);
    assertEquals(2, nuofEvents);
    assertEquals("description", lastPce.getPropertyName());
    assertEquals("ABC", lastPce.getOldValue());
    assertEquals("DEF", lastPce.getNewValue());

    resetEvent();

    // test if we get change notification if EBean assigns an id to the bean
    server().save(al);

    assertNotNull(lastPce);
    assertEquals("id", lastPce.getPropertyName());
    assertNull(lastPce.getOldValue());
    assertNotNull(lastPce.getNewValue());

    String prevLogDesc = al.getDescription();

    resetEvent();

    // simulate external change and test if we get change notification when we refresh the entity
    Transaction tx = server().beginTransaction();
    PreparedStatement pstm = tx.getConnection().prepareStatement("update audit_log set description = ? where id = ?");
    pstm.setString(1, "GHI");
    pstm.setLong(2, al.getId());
    int updated = pstm.executeUpdate();
    pstm.close();

    assertEquals(1, updated);

    tx.commit();

    assertNull(lastPce);
    assertEquals(0, nuofEvents);

    server().refresh(al);

    assertEquals("GHI", al.getDescription());

    assertNotNull(lastPce);
    assertEquals(1, nuofEvents);
    assertEquals("description", lastPce.getPropertyName());
    assertEquals(prevLogDesc, lastPce.getOldValue());
    assertNotNull("GHI", lastPce.getNewValue());

    // check if we fire with the real new value which might be a modified version of what we passed to the set method
    resetEvent();
    al.setModifiedDescription("MODIFIED_VALUE");

    assertNotNull(lastPce);
    assertEquals(1, nuofEvents);
    assertEquals("modifiedDescription", lastPce.getPropertyName());
    assertEquals(null, lastPce.getOldValue());
    assertNotNull("_MODIFIED_VALUE_", lastPce.getNewValue());
  }

  /**
   * check if
   * <ul>
   * <li>updating a lazy loaded property fires two events</li>
   * </ul>
   */
  @Test
  public void testPartialLoad() throws SQLException {
    AuditLog log = new AuditLog();
    log.setDescription("log");

    server().save(log);

    assertNotNull(log.getId());

    resetEvent();

    List<AuditLog> logs = server().find(AuditLog.class)
      .where().eq("id", log.getId())
      .select("id")
      .findList();

    assertNotNull(logs);
    assertEquals(1, logs.size());

    AuditLog logLazy = logs.get(0);

    addListener(logLazy, this);

    // this will lazy load and update the property
    logLazy.setDescription("updated log");

    // which should result in one PCE events
    assertEquals(1, pces.size());

    // test for the acutal update of the value
    PropertyChangeEvent propertyChangeEvent = pces.get(0);

    assertEquals("description", propertyChangeEvent.getPropertyName());
    assertEquals("log", propertyChangeEvent.getOldValue());
    assertEquals("updated log", propertyChangeEvent.getNewValue());

  }

  private void resetEvent() {
    lastPce = null;
    nuofEvents = 0;
    pces.clear();
  }

  private static void addListener(AuditLog al, PropertyChangeListener listener) {
    try {

      //Ebean.getBeanState(al).addPropertyChangeListener(listener);

      Method apcs = al.getClass().getMethod("addPropertyChangeListener", PropertyChangeListener.class);
      apcs.invoke(al, listener);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
