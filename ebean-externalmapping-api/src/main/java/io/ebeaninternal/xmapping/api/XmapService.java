package io.ebeaninternal.xmapping.api;

import java.util.List;

/**
 * Read and return external mapping for named queries etc.
 */
public interface XmapService {

  /**
   * Read and return external named queries etc.
   */
  List<XmapEbean> read(ClassLoader classLoader, List<String> mappingLocations);

}
