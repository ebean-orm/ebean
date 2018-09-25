package io.ebeaninternal.server.persist;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FlagsTest {


  @Test
  public void test() {

    int state = 0;

    state = Flags.setPublish(state);
    assertThat(Flags.isSet(state, Flags.PUBLISH)).isTrue();

    state = Flags.setMerge(state);
    state = Flags.setInsert(state);
    assertThat(Flags.isSet(state, Flags.PUBLISH)).isTrue();
    assertThat(Flags.isSet(state, Flags.MERGE)).isTrue();
    assertThat(Flags.isSet(state, Flags.INSERT)).isTrue();

    state = Flags.unsetPublish(state);
    assertThat(Flags.isSet(state, Flags.PUBLISH)).isFalse();
    assertThat(Flags.isSet(state, Flags.MERGE)).isTrue();
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

    assertThat(Flags.isPublishMergeOrNormal(0)).isFalse();
    assertThat(Flags.isPublishMergeOrNormal(Flags.INSERT)).isFalse();
    assertThat(Flags.isPublishMergeOrNormal(Flags.RECURSE)).isFalse();

    assertThat(Flags.isPublishMergeOrNormal(Flags.PUBLISH)).isTrue();
    assertThat(Flags.isPublishMergeOrNormal(Flags.MERGE)).isTrue();
    assertThat(Flags.isPublishMergeOrNormal(Flags.NORMAL)).isTrue();

    int mergePublish = Flags.setMerge(Flags.setPublish(0));
    assertThat(Flags.isPublishMergeOrNormal(mergePublish)).isTrue();
  }
}
