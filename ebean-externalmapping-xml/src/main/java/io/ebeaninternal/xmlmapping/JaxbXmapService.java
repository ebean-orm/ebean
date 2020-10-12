package io.ebeaninternal.xmlmapping;

import io.ebeaninternal.xmapping.api.XmapEbean;
import io.ebeaninternal.xmapping.api.XmapService;

import java.util.List;

public class JaxbXmapService implements XmapService {

  @Override
  public List<XmapEbean> read(ClassLoader classLoader, List<String> mappingLocations) {
    return new InternalConfigXmlRead(classLoader, mappingLocations).read();
  }
}
