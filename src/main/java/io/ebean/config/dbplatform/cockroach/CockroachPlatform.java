package io.ebean.config.dbplatform.cockroach;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;

/**
 * CockroachDB based platform.
 */
public class CockroachPlatform extends PostgresPlatform {

  public CockroachPlatform() {
    super();
    this.platform = Platform.COCKROACH;
    // no like escape clause supported
    this.likeSpecialCharacters = new char[]{'%', '_'};
    this.likeClauseRaw = "like ?";
    this.likeClauseEscaped = "like ?";
  }

  /**
   * Needs a commit after create index such that alter table add foreign key ... succeeds.
   */
  @Override
  public boolean isDdlCommitOnCreateIndex() {
    return true;
  }

}
