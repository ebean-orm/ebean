package io.ebeaninternal.api;

import io.ebean.meta.MetaQueryPlan;

import java.time.Instant;

/**
 * Internal database query plan being capture.
 */
public interface SpiDbQueryPlan extends MetaQueryPlan {

  /**
   * Extend with queryTimeMicros, captureCount, captureMicros and when the bind values were captured.
   */
  SpiDbQueryPlan with(long queryTimeMicros, long captureCount, long captureMicros, Instant whenCaptured, Object tenantId);

}
