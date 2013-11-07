package com.avaje.tests.unitinternal;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebeaninternal.server.core.XmlConfigLoader;
import com.avaje.ebeaninternal.server.lib.util.Dnode;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Tests basic xml loading functionality across multiple jars and inside our own test structure.
 *
 * @author Rob Bygrave
 * @author Richard Vowles - http://plus.google.com/RichardVowles
 */
public class TestXmlConfigLoader extends BaseTestCase {

  @Test
  public void test() {

    XmlConfigLoader xmlConfigLoader = new XmlConfigLoader(null);

    List<Dnode> ebeanOrmXml = xmlConfigLoader.search(META_INF_EBEAN_ORM_XML);

    Assert.assertNotNull(ebeanOrmXml);
    Assert.assertTrue("Found ebean-orm.xml", ebeanOrmXml.size() > 0);

  }

	private static final String META_INF_EBEAN_ORM_XML = "META-INF/ebean-orm.xml";
	private static final String META_INF_ORM_XML = "META-INF/orm.xml";
	private static final String WEB_INF_CLASSES = "WEB-INF/classes/";

	@Test
	public void ensureEbeanOrmStillLoads() throws IOException {
		URL jar = setupJar();
		URL jarBang = setupJarBang();

		URLClassLoader cl = new URLClassLoader(new URL[] { jar, jarBang}, getClass().getClassLoader());

		XmlConfigLoader loader = new XmlConfigLoader(cl);

		Assert.assertEquals(3, loader.search(META_INF_EBEAN_ORM_XML).size()); // 1 in parent, 2 in ours
		Assert.assertEquals(2, loader.search(META_INF_ORM_XML).size()); // 2 in ours

	}

	private URL createJar(File jarFile, String offset) throws IOException {
		FileOutputStream stream = new FileOutputStream(jarFile);
		JarOutputStream jarOutputStream = new JarOutputStream(stream);

		JarEntry entry = new JarEntry(offset + META_INF_EBEAN_ORM_XML);
		jarOutputStream.putNextEntry(entry);
		InputStream classStream = getClass().getResourceAsStream("/" + META_INF_EBEAN_ORM_XML);
		IOUtils.copy(classStream, jarOutputStream);

		entry = new JarEntry(offset + META_INF_ORM_XML);
		jarOutputStream.putNextEntry(entry);
		classStream = getClass().getResourceAsStream("/" + META_INF_EBEAN_ORM_XML);
		IOUtils.copy(classStream, jarOutputStream);

		jarOutputStream.close();
		stream.close();

		if (offset.length() > 0) {
			return new URL("jar:" + jarFile.toURI().toString() + "!/" + offset);
		} else {
			return jarFile.toURI().toURL();
		}
	}

	private URL setupJarBang() throws IOException {
		File jarFile = File.createTempFile("bang", ".war");
		return createJar(jarFile, WEB_INF_CLASSES);
	}

	private URL setupJar() throws IOException {
		File jarFile = File.createTempFile("nobang", ".jar");
		return createJar(jarFile, "");
	}
}
