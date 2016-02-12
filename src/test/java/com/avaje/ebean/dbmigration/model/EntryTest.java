package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.ChangeSet;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class EntryTest {


  @Test
  public void test_when_empty() throws Exception {

    PendingDrops.Entry entry = createEntry();
    assertThat(entry.hasPendingDrops()).isFalse();
  }


  @Test
  public void test_when_normal() throws Exception {

    PendingDrops.Entry entry = createEntry();
    entry.add(new ChangeSet());

    assertThat(entry.hasPendingDrops()).isTrue();
  }

  @Test
  public void test_when_suppressOnly() throws Exception {

    PendingDrops.Entry entry = createEntry();

    ChangeSet cs = new ChangeSet();
    cs.setSuppressDropsForever(Boolean.TRUE);
    entry.add(cs);

    assertThat(entry.hasPendingDrops()).isFalse();
  }

  @Test
  public void test_when_both() throws Exception {

    PendingDrops.Entry entry = createEntry();

    ChangeSet cs = new ChangeSet();
    cs.setSuppressDropsForever(Boolean.TRUE);
    entry.add(cs);
    entry.add(new ChangeSet());

    assertThat(entry.hasPendingDrops()).isTrue();
  }

  @Test
  public void test_containsSuppressForever_when_empty() {

    PendingDrops.Entry entry = createEntry();

    assertThat(entry.containsSuppressForever()).isFalse();
  }

  @Test
  public void test_containsSuppressForever_when_not() {

    PendingDrops.Entry entry = createEntry();
    entry.add(new ChangeSet());

    assertThat(entry.containsSuppressForever()).isFalse();
  }

  @Test
  public void test_containsSuppressForever_when_does() {

    PendingDrops.Entry entry = createEntry();
    ChangeSet cs = new ChangeSet();
    cs.setSuppressDropsForever(Boolean.TRUE);
    entry.add(cs);

    assertThat(entry.containsSuppressForever()).isTrue();
  }

  @Test
  public void test_containsSuppressForever_when_mixed() {

    PendingDrops.Entry entry = createEntry();
    ChangeSet cs = new ChangeSet();
    cs.setSuppressDropsForever(Boolean.TRUE);
    entry.add(cs);
    entry.add(new ChangeSet());

    assertThat(entry.containsSuppressForever()).isTrue();
  }

  @Test
  public void test_removeDrops_when_empty() {

    PendingDrops.Entry entry = createEntry();
    assertThat(entry.removeDrops()).isTrue();
  }

  @Test
  public void test_removeDrops_when_notSuppressed() {

    PendingDrops.Entry entry = createEntry();
    entry.add(new ChangeSet());
    assertThat(entry.removeDrops()).isTrue();
  }

  @Test
  public void test_removeDrops_when_containsSuppressed() {

    PendingDrops.Entry entry = createEntry();
    entry.add(new ChangeSet());
    ChangeSet cs = new ChangeSet();
    cs.setSuppressDropsForever(Boolean.TRUE);
    entry.add(cs);

    assertThat(entry.removeDrops()).isFalse();
  }

  @NotNull
  private PendingDrops.Entry createEntry() {
    MigrationVersion version = MigrationVersion.parse("1.1");
    return new PendingDrops.Entry(version);
  }
}