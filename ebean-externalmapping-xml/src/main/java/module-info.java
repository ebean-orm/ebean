import io.avaje.classpath.scanner.ClassPathScannerFactory;
import io.ebeaninternal.xmapping.api.XmapService;
import io.ebeaninternal.xmlmapping.JaxbXmapService;

/**
 * Provider of XmapService.
 */
module io.ebean.xmapping.xml {

  provides XmapService with JaxbXmapService;

  requires transitive io.ebean.xmapping.api;
  requires transitive jakarta.xml.bind;
  requires io.avaje.classpath.scanner.api;
  requires io.avaje.classpath.scanner;
  requires static org.slf4j;
  requires static java.sql; // for testing

  uses ClassPathScannerFactory;

}
