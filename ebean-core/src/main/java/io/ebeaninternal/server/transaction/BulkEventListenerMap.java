package io.ebeaninternal.server.transaction;

import io.ebean.event.BulkTableEvent;
import io.ebean.event.BulkTableEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class BulkEventListenerMap {

  private final HashMap<String, Entry> map = new HashMap<>();

  public BulkEventListenerMap(List<BulkTableEventListener> listeners) {

    if (listeners != null) {
      for (BulkTableEventListener l : listeners) {
        Set<String> tables = l.registeredTables();
        for (String tableName : tables) {
          register(tableName, l);
        }
      }
    }
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public void process(BulkTableEvent event) {

    Entry entry = map.get(event.getTableName());
    if (entry != null) {
      entry.process(event);
    }
  }

  private void register(String tableName, BulkTableEventListener l) {
    String upperTableName = tableName.trim().toUpperCase();
    Entry entry = map.computeIfAbsent(upperTableName, k -> new Entry());
    entry.add(l);
  }

  private static class Entry {

    final List<BulkTableEventListener> listeners = new ArrayList<>();

    private void add(BulkTableEventListener l) {
      listeners.add(l);
    }

    private void process(BulkTableEvent event) {
      for (BulkTableEventListener listener : listeners) {
        listener.process(event);
      }
    }
  }
}
