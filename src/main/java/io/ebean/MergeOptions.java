package io.ebean;

import java.util.Set;

/**
 * Options used to control a merge. Use MergeOptionsBuilder to create an instance.
 * <p>
 * Instances of MergeOptions are thread safe and safe to share across threads.
 */
public interface MergeOptions {

  /**
   * Returns true if Id values are supplied by the client.
   * <p>
   * This would be the case when for example a mobile creates data in it's own local database
   * and then sync's. In this case often the id values are UUID.
   */
  boolean isClientGeneratedIds();

  /**
   * Return true if delete permanent should be used and false for 'normal' delete that allows soft deletes.
   */
  boolean isDeletePermanent();

  /**
   * Return the paths included in the merge.
   */
  Set<String> paths();
}
