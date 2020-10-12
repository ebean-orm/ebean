package io.ebeaninternal.xmapping.api;

import java.util.ArrayList;
import java.util.List;

/**
 * External mapping for Entity and DTO beans.
 */
public class XmapEbean {

  protected final List<XmapEntity> entity = new ArrayList<>();
  protected final List<XmapDto> dto = new ArrayList<>();

  public List<XmapEntity> getEntity() {
    return entity;
  }

  public List<XmapDto> getDto() {
    return dto;
  }

}
