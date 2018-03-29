package io.ebean;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Builds a MergeOptions which is immutable and thread safe.
 */
public class MergeOptionsBuilder {

  private Set<String> paths = new LinkedHashSet<>();

  private boolean clientGeneratedIds;

  private boolean deletePermanent = true;

  /**
   * Add a path that will included in the merge.
   *
   * @param path The path relative to the root type.
   * @return The builder to chain another addPath() or build().
   */
  public MergeOptionsBuilder addPath(String path) {
    paths.add(path);
    return this;
  }

  /**
   * Set to true if Id values are supplied by the client.
   * <p>
   * This would be the case when for example a mobile creates data in it's own local database
   * and then sync's. In this case often the id values are UUID.
   */
  public MergeOptionsBuilder setClientGeneratedIds() {
    this.clientGeneratedIds = true;
    return this;
  }

  /**
   * Set that deletions should use delete permanent (rather than default which allows soft deletes).
   */
  public MergeOptionsBuilder setDeletePermanent() {
    this.deletePermanent = true;
    return this;
  }

  /**
   * Build and return the MergeOptions instance.
   */
  public MOptions build() {
    return new MOptions(paths, clientGeneratedIds, deletePermanent);
  }

  private static class MOptions implements MergeOptions {

    private final boolean clientGeneratedIds;
    private final boolean deletePermanent;
    private final Set<String> paths;

    private MOptions(Set<String> paths, boolean clientGeneratedIds, boolean deletePermanent) {
      this.paths = paths;
      this.clientGeneratedIds = clientGeneratedIds;
      this.deletePermanent = deletePermanent;
    }

    public Set<String> paths() {
      return paths;
    }

    @Override
    public boolean isClientGeneratedIds() {
      return clientGeneratedIds;
    }

    @Override
    public boolean isDeletePermanent() {
      return deletePermanent;
    }
  }
}
