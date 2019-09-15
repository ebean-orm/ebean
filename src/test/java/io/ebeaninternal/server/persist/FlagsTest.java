package io.ebeaninternal.server.persist;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FlagsTest {


  @Test
  public void test() {

    int state = 0;

    state = Flags.setMerge(state);
    state = Flags.setInsert(state);
    assertThat(Flags.isSet(state, Flags.MERGE)).isTrue();
    assertThat(Flags.isSet(state, Flags.INSERT)).isTrue();

    state = Flags.unsetMerge(state);
    assertThat(Flags.isSet(state, Flags.MERGE)).isFalse();
    assertThat(Flags.isSet(state, Flags.INSERT)).isTrue();
  }

  @Test
  public void insert() {

    int state = 0;

    state = Flags.setInsert(state);
    assertThat(Flags.isSet(state, Flags.INSERT)).isTrue();
    assertThat(Flags.isSet(state, Flags.NORMAL)).isFalse();
  }

  @Test
  public void insertNormal() {

    int state = 0;

    state = Flags.setInsertNormal(state);
    assertThat(Flags.isSet(state, Flags.INSERT)).isTrue();
    assertThat(Flags.isSet(state, Flags.NORMAL)).isTrue();
  }

  @Test
  public void update() {

    int state = 0;

    state = Flags.setUpdate(state);
    assertThat(Flags.isSet(state, Flags.INSERT)).isFalse();
    assertThat(Flags.isSet(state, Flags.NORMAL)).isFalse();
  }

  @Test
  public void updateNormal() {

    int state = 0;

    state = Flags.setUpdateNormal(state);
    assertThat(Flags.isSet(state, Flags.INSERT)).isFalse();
    assertThat(Flags.isSet(state, Flags.NORMAL)).isTrue();
  }

  @Test
  public void isPublishOrMerge() {

    assertThat(Flags.isMergeOrNormal(0)).isFalse();
    assertThat(Flags.isMergeOrNormal(Flags.INSERT)).isFalse();
    assertThat(Flags.isMergeOrNormal(Flags.RECURSE)).isFalse();

    assertThat(Flags.isMergeOrNormal(Flags.MERGE)).isTrue();
    assertThat(Flags.isMergeOrNormal(Flags.NORMAL)).isTrue();

    assertThat(Flags.isMergeOrNormal(Flags.setMerge(0))).isTrue();
  }
}
