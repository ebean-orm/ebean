import io.avaje.classpath.scanner.ClassPathScannerFactory;
import io.ebeaninternal.xmapping.api.XmapService;
import io.ebeaninternal.xmlmapping.JaxbXmapService;

/**
 * Provider of XmapService.
 */
module io.ebean.xmapping.xml {

  provides XmapService with JaxbXmapService;

  requires transitive io.ebean.xmapping.api;
  requires transitive java.xml;
  requires transitive java.xml.bind;
  requires io.avaje.classpath.scanner.api;
  requires io.avaje.classpath.scanner;
  requires static java.sql; // for testing

  uses ClassPathScannerFactory;

}
