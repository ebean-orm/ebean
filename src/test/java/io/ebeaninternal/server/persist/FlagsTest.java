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
  public void isPublishOrMerge() {

    assertThat(Flags.isPublishOrMerge(0)).isFalse();
    assertThat(Flags.isPublishOrMerge(Flags.INSERT)).isFalse();

    assertThat(Flags.isPublishOrMerge(Flags.PUBLISH)).isTrue();
    assertThat(Flags.isPublishOrMerge(Flags.MERGE)).isTrue();

    int mergePublish = Flags.setMerge(Flags.setPublish(0));
    assertThat(Flags.isPublishOrMerge(mergePublish)).isTrue();
  }
}
